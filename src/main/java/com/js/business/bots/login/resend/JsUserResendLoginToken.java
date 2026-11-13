package com.js.business.bots.login.resend;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;
import com.js.services.JsServiceLogin;
import com.js.status.login.JsProcessStatusUnlockLoginToken;

/**
 * The only step of {@code /resendLoginToken <email>} in the user bot (since 2026-11-12): runs
 * {@code JsServiceLogin.ResendLoginToken}, the request of the login screen, and answers each of its outcomes as that
 * screen does: made, already made (409), token already resent (429), e-mail without token (404) or invalid e-mail.
 */
public class JsUserResendLoginToken implements CcpBusiness {

	/** The single instance: the class keeps no state. */
	public final static JsUserResendLoginToken INSTANCE = new JsUserResendLoginToken();

	private JsUserResendLoginToken() {}

	/** Where the request comes from: the service requires the address and the agent of the requester. */
	enum RequestOrigin implements CcpJsonFieldName {
		/** In place of the address of the requester, whom the bot does not know. */
		botIp {
			public String getValue() {
				return "telegram";
			}
		},
		/** The agent of the request. */
		botUserAgent {
			public String getValue() {
				return "Telegram user bot";
			}
		},
	}

	/**
	 * Makes the request and replies with its outcome.
	 * @param json the session, with {@code email}
	 * @return the session with the reply of the request made
	 * @throws CcpErrorFlowDisturb with the reply, by the status of the request not made (400 for an invalid e-mail, 404,
	 * 409 or 429), mapped in the {@code stepFlow} of the step, which ends the session; with the outcome as it came, when
	 * the request does not foresee its status
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String email = json.getAsString(JnJsonCommonsFields.email);
		String ip = RequestOrigin.botIp.getValue();
		String userAgent = RequestOrigin.botUserAgent.getValue();
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, email)
				.put(JnJsonCommonsFields.ip, ip)
				.put(JnJsonCommonsFields.userAgent, userAgent);
		try {
			CcpJsonRepresentation result = JsServiceLogin.ResendLoginToken.execute(request);
			// the result carries the e-mail as stored (hashed): the text shows the one the user typed
			CcpJsonRepresentation outcome = result.put(JnJsonCommonsFields.email, email);
			CcpJsonRepresentation jsonWithReply = reply(json, JsUserResendLoginTokenMessages.requested, outcome);
			return jsonWithReply;
		} catch (CcpJsonValidationError invalidRequest) {
			CcpJsonRepresentation jsonWithReply = reply(json, JsUserResendLoginTokenMessages.invalidEmail, request);
			return CcpProcessStatusDefault.BAD_REQUEST.throwException(jsonWithReply);
		} catch (CcpErrorFlowDisturb notMade) {
			JsUserResendLoginTokenMessages message = getMessage(notMade);
			CcpJsonRepresentation outcome = notMade.json.put(JnJsonCommonsFields.email, email);
			CcpJsonRepresentation jsonWithReply = reply(json, message, outcome);
			return notMade.status.throwException(jsonWithReply);
		}
	}

	/**
	 * The answer to a request not made.
	 * @param notMade the outcome of the request
	 * @return the answer
	 * @throws CcpErrorFlowDisturb the same outcome, when the request does not foresee it
	 */
	private static JsUserResendLoginTokenMessages getMessage(CcpErrorFlowDisturb notMade) {
		CcpProcessStatus status = notMade.status;
		if(JsProcessStatusUnlockLoginToken.statusAlreadyRequested == status) {
			return JsUserResendLoginTokenMessages.alreadyRequested;
		}
		if(JsProcessStatusUnlockLoginToken.statusTokenAlredyResent == status) {
			return JsUserResendLoginTokenMessages.alreadyResent;
		}
		if(JsProcessStatusUnlockLoginToken.statusTokenNotExists == status) {
			return JsUserResendLoginTokenMessages.tokenNotFound;
		}
		throw notMade;
	}

	/**
	 * Puts in the session the text in the language of the session (Portuguese when it has none) as the reply.
	 * @param json the session
	 * @param message the text
	 * @param parameters the values of the text
	 * @return the session with the reply
	 */
	private static CcpJsonRepresentation reply(CcpJsonRepresentation json, JsUserResendLoginTokenMessages message, CcpJsonRepresentation parameters) {
		String languageName = json.getAsString(JnJsonCommonsFields.language);
		boolean noLanguage = languageName.isEmpty();
		JnLanguage language = noLanguage ? JnLanguage.portuguese : JnLanguage.valueOf(languageName);
		String text = message.getMessage(language, parameters);
		CcpJsonRepresentation jsonWithReply = json.put(JnJsonInstantMessengerFields.botReply, text);
		return jsonWithReply;
	}
}
