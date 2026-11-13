package com.js.business.login;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequestLog;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.jn.business.email.suppression.JnBusinessLoadEmailSuppressions;
import com.jn.entities.JnEntityEmailSuppressed;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.js.entities.JsEntityLoginPassword;
import com.js.services.JsServiceLogin;

/**
 * Scheduled process (every hour) that asks the support team to resend the login token to whoever received a
 * token in the last 24 hours and still has no password. In order:
 * <ol>
 * <li>reads, in the request log of the production service of login, the {@code POST /login/{email}/token/language/{language}}
 * ({@code CreateLoginToken}) answered with 200 in the last 24 hours, keeping the last user agent and IP of each e-mail;</li>
 * <li>refreshes the suppression lists of the e-mail provider ({@code JnBusinessLoadEmailSuppressions}) and leaves out the
 * e-mails present in {@code jn_email_suppressed};</li>
 * <li>leaves out the e-mails present in {@code js_login_password};</li>
 * <li>runs {@code JsServiceLogin.ResendLoginToken} (the service of {@code POST /login/{email}/token/request/resending})
 * for each e-mail left. A refusal of the service (no token, already requested, already resent) only counts.</li>
 * </ol>
 * It reads the log and the database in batch, so it runs only in the scheduled process (OQL).
 * <p>
 * Input: none of its own. Output: the input plus the counters of {@link JsonFieldNames}.
 */
public class JsBusinessResendLoginTokenToUsersWithoutPassword implements CcpBusiness {

	/** The single instance: the business keeps no state. */
	public static final JsBusinessResendLoginTokenToUsersWithoutPassword INSTANCE = new JsBusinessResendLoginTokenToUsersWithoutPassword();

	private JsBusinessResendLoginTokenToUsersWithoutPassword() {}

	/** The texts of the search in the request log and the output counters. */
	enum JsonFieldNames implements CcpJsonFieldName {
		/** The Cloud Run service of the login API. */
		loginService {
			public String getValue() {
				return "jobsnow-js";
			}
		},
		/** The URL of {@code CreateLoginToken}; the first group is the e-mail. */
		createLoginTokenPath {
			public String getValue() {
				return "/login/([^/?]+)/token/language/[^/?]+$";
			}
		},
		/** The distinct e-mails that received a token in the period. */
		emailsWithToken,
		/** The e-mails left out because the provider suppressed them. */
		suppressedEmails,
		/** The e-mails left out because they already have a password. */
		emailsWithPassword,
		/** The e-mails whose resending was requested. */
		resendRequested,
		/** The e-mails refused by the service, by status. */
		resendRefused,
		/** The refusal of a request whose fields the service did not accept. */
		invalidRequest,
		/** The lines of the request log read in the period. */
		requestsRead,
		/** The e-mails left after the suppressed ones and the ones with password are taken out. */
		emailsLeft,
		/** The name of the step, in each line of the log. */
		step,
		/** Step A: reading of the request log. */
		readRequestLog,
		/** Step B: load of the suppression lists of the e-mail provider. */
		loadEmailSuppressions,
		/** Steps B and C: e-mails taken out because they are suppressed or have a password. */
		filterEmails,
		/** Step D: the resending of the token. */
		resendLoginToken,
	}

	/** The period read from the request log: the last 24 hours. */
	private static final long PERIOD_IN_MILLIS = 24L * 60 * 60 * 1000;

	/** How many e-mails go in each union all. */
	private static final int BATCH_SIZE = 500;

	/**
	 * Runs the four steps.
	 * @param json the input of the scheduled process
	 * @return the input plus the counters; the counters of each step also go, one JSON line per step, to the standard
	 * output (the log of the job), never with an e-mail
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		List<CcpJsonRepresentation> logRequests = this.getLogRequests();
		Map<String, CcpJsonRepresentation> requestsByEmail = this.getLastRequestByEmail(logRequests);
		int requestsRead = logRequests.size();
		int emailsWithToken = requestsByEmail.size();
		CcpJsonRepresentation readCounters = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.requestsRead, requestsRead)
				.put(JsonFieldNames.emailsWithToken, emailsWithToken);
		this.logStep(JsonFieldNames.readRequestLog, readCounters);

		CcpJsonRepresentation loadResult = JnBusinessLoadEmailSuppressions.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
		CcpJsonRepresentation loadCounters = loadResult.getJsonPiece(JnBusinessLoadEmailSuppressions.Fields.savedSuppressions,
				JnBusinessLoadEmailSuppressions.Fields.ignoredSuppressions);
		this.logStep(JsonFieldNames.loadEmailSuppressions, loadCounters);

		List<CcpJsonRepresentation> requests = new ArrayList<>(requestsByEmail.values());
		int suppressedEmails = 0;
		int emailsWithPassword = 0;
		int resendRequested = 0;
		CcpJsonRepresentation resendRefused = CcpOtherConstants.EMPTY_JSON;
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		List<CcpJsonRepresentation> emailsLeft = new ArrayList<>();
		int requestsCount = requests.size();
		for (int start = 0; start < requestsCount; start += BATCH_SIZE) {
			int end = Math.min(start + BATCH_SIZE, requestsCount);
			List<CcpJsonRepresentation> batch = requests.subList(start, end);
			CcpJsonRepresentation[] keys = batch.toArray(CcpJsonRepresentation[]::new);
			CcpSelectUnionAll unionAll = crud.unionAll(keys, JnDeleteKeysFromCache.INSTANCE, JnEntityEmailSuppressed.ENTITY, JsEntityLoginPassword.ENTITY);
			for (CcpJsonRepresentation request : batch) {
				boolean suppressed = JnEntityEmailSuppressed.ENTITY.isPresentInThisUnionAll(unionAll, request);
				if(suppressed) {
					suppressedEmails++;
					continue;
				}
				boolean hasPassword = JsEntityLoginPassword.ENTITY.isPresentInThisUnionAll(unionAll, request);
				if(hasPassword) {
					emailsWithPassword++;
					continue;
				}
				emailsLeft.add(request);
			}
		}
		int emailsLeftCount = emailsLeft.size();
		CcpJsonRepresentation filterCounters = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.suppressedEmails, suppressedEmails)
				.put(JsonFieldNames.emailsWithPassword, emailsWithPassword)
				.put(JsonFieldNames.emailsLeft, emailsLeftCount);
		this.logStep(JsonFieldNames.filterEmails, filterCounters);

		for (CcpJsonRepresentation request : emailsLeft) {
			try {
				JsServiceLogin.ResendLoginToken.execute(request);
				resendRequested++;
			} catch (CcpErrorFlowDisturb refused) {
				resendRefused = this.countRefusal(resendRefused, refused.status);
			} catch (CcpJsonValidationError invalid) {
				resendRefused = this.countRefusal(resendRefused, JsonFieldNames.invalidRequest);
			}
		}
		CcpJsonRepresentation resendCounters = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.resendRequested, resendRequested)
				.put(JsonFieldNames.resendRefused, resendRefused);
		this.logStep(JsonFieldNames.resendLoginToken, resendCounters);

		CcpJsonRepresentation result = json
				.put(JsonFieldNames.emailsWithToken, emailsWithToken)
				.put(JsonFieldNames.suppressedEmails, suppressedEmails)
				.put(JsonFieldNames.emailsWithPassword, emailsWithPassword)
				.put(JsonFieldNames.resendRequested, resendRequested)
				.put(JsonFieldNames.resendRefused, resendRefused);
		return result;
	}

	/**
	 * Writes, in one JSON line of the standard output (the log of the Cloud Run Job), the name of a step and its counters.
	 * @param step the step
	 * @param counters the counters of the step, never an e-mail
	 */
	private void logStep(JsonFieldNames step, CcpJsonRepresentation counters) {
		String stepName = step.getValue();
		CcpJsonRepresentation stepLine = counters.put(JsonFieldNames.step, stepName);
		String line = stepLine.asUgglyJson();
		System.out.println(line);
	}

	/**
	 * Adds one to the counter of a refusal.
	 * @param counters the counters by refusal
	 * @param refusal the refusal
	 * @return the counters with the new count
	 */
	private CcpJsonRepresentation countRefusal(CcpJsonRepresentation counters, CcpJsonFieldName refusal) {
		boolean counted = counters.containsField(refusal);
		int count = counted ? counters.getAsIntegerNumber(refusal) : 0;
		int incremented = count + 1;
		CcpJsonRepresentation countersWithRefusal = counters.put(refusal, incremented);
		return countersWithRefusal;
	}

	/**
	 * Step A, first half: the lines of the request log with the {@code CreateLoginToken} answered with 200 in the last 24 hours.
	 * @return the lines, as the request log gives them
	 */
	private List<CcpJsonRepresentation> getLogRequests() {
		CcpHttpRequestLog requestLog = CcpDependencyInjection.getDependency(CcpHttpRequestLog.class);
		long now = System.currentTimeMillis();
		long startTime = now - PERIOD_IN_MILLIS;
		String serviceName = JsonFieldNames.loginService.getValue();
		String pathRegex = JsonFieldNames.createLoginTokenPath.getValue();
		List<CcpJsonRepresentation> logRequests = requestLog.getRequestsSince(serviceName, CcpHttpMethods.POST, pathRegex, 200, startTime);
		return logRequests;
	}

	/**
	 * Step A, second half: the {@code CreateLoginToken} answered with 200 in the last 24 hours, one request (the last one) for each
	 * e-mail, with the fields asked by {@code ResendLoginToken}: email, user agent and IP.
	 * @param logRequests the lines of the request log of the period
	 * @return the last request of each e-mail, in the order of the first request of each one
	 */
	private Map<String, CcpJsonRepresentation> getLastRequestByEmail(List<CcpJsonRepresentation> logRequests) {
		String pathRegex = JsonFieldNames.createLoginTokenPath.getValue();
		Pattern pathPattern = Pattern.compile(pathRegex);
		Map<String, CcpJsonRepresentation> requestsByEmail = new LinkedHashMap<>();
		for (CcpJsonRepresentation logRequest : logRequests) {
			boolean incomplete = false == logRequest.containsAllFields(CcpHttpRequestLog.Fields.url, CcpHttpRequestLog.Fields.userAgent, CcpHttpRequestLog.Fields.ip);
			if(incomplete) {
				continue;
			}
			String url = logRequest.getAsString(CcpHttpRequestLog.Fields.url);
			Matcher matcher = pathPattern.matcher(url);
			boolean notTheEndpoint = false == matcher.find();
			if(notTheEndpoint) {
				continue;
			}
			String encodedEmail = matcher.group(1);
			String email = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8);
			String userAgent = logRequest.getAsString(CcpHttpRequestLog.Fields.userAgent);
			String ip = logRequest.getAsString(CcpHttpRequestLog.Fields.ip);
			CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
					.put(JnJsonCommonsFields.email, email)
					.put(JnJsonCommonsFields.userAgent, userAgent)
					.put(JnJsonCommonsFields.ip, ip);
			requestsByEmail.put(email, request);
		}
		return requestsByEmail;
	}
}
