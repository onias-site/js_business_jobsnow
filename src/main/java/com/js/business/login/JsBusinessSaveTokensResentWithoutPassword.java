package com.js.business.login;

import java.util.ArrayList;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.js.entities.JsEntityLoginTokenResentWithoutPassword;

/**
 * Daily process run right after {@link JsBusinessResendLoginTokenToUsersWithoutPassword}. In order:
 * <ol>
 * <li>reads every record of {@code js_login_token_fulfilled_resend} (the resendings of the token that support fulfilled)
 * and keeps the ones whose e-mail is not in {@code js_login_password};</li>
 * <li>writes them, by bulk, in {@link JsEntityLoginTokenResentWithoutPassword} (the e-mail, the primary key, as its
 * SHA-1 hash). A record already there is kept.</li>
 * </ol>
 * The insertion of the password deletes the record ({@link JsBusinessDeleteTokenResentWithoutPassword}). It reads a whole
 * index, so it runs only in the scheduled process (OQL).
 * <p>
 * Input: none of its own. Output: the input plus the counters of {@link JsonFieldNames}.
 */
public class JsBusinessSaveTokensResentWithoutPassword implements CcpBusiness {

	/** The single instance: the business keeps no state. */
	public static final JsBusinessSaveTokensResentWithoutPassword INSTANCE = new JsBusinessSaveTokensResentWithoutPassword();

	private JsBusinessSaveTokensResentWithoutPassword() {}

	/** The scroll of the reading and the output counters. */
	enum JsonFieldNames implements CcpJsonFieldName {
		/** How long each page of the scroll stays open. */
		scrollTime {
			public String getValue() {
				return "5m";
			}
		},
		/** The records of {@code js_login_token_fulfilled_resend}. */
		fulfilledResendings,
		/** The ones left out because the e-mail already has a password. */
		withPassword,
		/** The ones written in {@code js_login_token_resent_without_password}. */
		savedWithoutPassword,
		/** The name of the step, in the line of the log. */
		step,
		/** Step E: the record of the resendings whose e-mail still has no password. */
		saveTokensResentWithoutPassword,
	}

	/** How many records each page of the scroll and each union all bring. */
	private static final int BATCH_SIZE = 500;

	/**
	 * Runs the two steps.
	 * @param json the input of the scheduled process
	 * @return the input plus the counters
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		List<CcpJsonRepresentation> fulfilled = this.getFulfilledResendings();
		int fulfilledCount = fulfilled.size();
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		List<CcpBulkItem> items = new ArrayList<>();
		int withPassword = 0;
		for (int start = 0; start < fulfilledCount; start += BATCH_SIZE) {
			int end = Math.min(start + BATCH_SIZE, fulfilledCount);
			List<CcpJsonRepresentation> batch = fulfilled.subList(start, end);
			CcpJsonRepresentation[] keys = batch.toArray(CcpJsonRepresentation[]::new);
			CcpSelectUnionAll unionAll = crud.unionAll(keys, JnDeleteKeysFromCache.INSTANCE, JsEntityLoginPassword.ENTITY);
			for (CcpJsonRepresentation key : batch) {
				boolean hasPassword = JsEntityLoginPassword.ENTITY.isPresentInThisUnionAll(unionAll, key);
				if(hasPassword) {
					withPassword++;
					continue;
				}
				// the bulk skips the decorators, the transformer of the fields among them: the SHA-1 of the e-mail goes here
				CcpJsonRepresentation handledKey = JsEntityLoginTokenResentWithoutPassword.ENTITY.getHandledJson(key);
				CcpJsonRepresentation hashedEmail = handledKey.getJsonPiece(JnJsonCommonsFields.email);
				List<CcpBulkItem> keyItems = JsEntityLoginTokenResentWithoutPassword.ENTITY.toBulkItems(hashedEmail, CcpBulkEntityOperationType.create);
				items.addAll(keyItems);
			}
		}
		int saved = fulfilledCount - withPassword;
		CcpJsonRepresentation result = json
				.put(JsonFieldNames.fulfilledResendings, fulfilledCount)
				.put(JsonFieldNames.withPassword, withPassword)
				.put(JsonFieldNames.savedWithoutPassword, saved);
		CcpJsonRepresentation counters = result.getJsonPiece(JsonFieldNames.fulfilledResendings, JsonFieldNames.withPassword,
				JsonFieldNames.savedWithoutPassword);
		String stepName = JsonFieldNames.saveTokensResentWithoutPassword.getValue();
		CcpJsonRepresentation stepLine = counters.put(JsonFieldNames.step, stepName);
		String line = stepLine.asUgglyJson();
		// one line in the log of the Cloud Run Job, with the counters only (never an e-mail)
		System.out.println(line);
		boolean nothingToSave = items.isEmpty();
		if(nothingToSave) {
			return result;
		}
		JnExecuteBulkOperation.INSTANCE.executeBulk(items, JnDeleteKeysFromCache.INSTANCE);
		return result;
	}

	/**
	 * Step A, first half: the e-mail of every record of {@code js_login_token_fulfilled_resend}, kept in clear there.
	 * @return one key (only the e-mail) for each record
	 */
	private List<CcpJsonRepresentation> getFulfilledResendings() {
		CcpEntity fulfilledEntity = JsEntityLoginTokenRequestResend.ENTITY.getTwinEntity();
		String scrollTime = JsonFieldNames.scrollTime.getValue();
		String emailField = JnJsonCommonsFields.email.getValue();
		List<CcpJsonRepresentation> records = new ArrayList<>();
		CcpQueryOptions.INSTANCE.matchAll().selectFrom(fulfilledEntity).consumeQueryResult(scrollTime, BATCH_SIZE, record -> records.add(record), emailField);
		List<CcpJsonRepresentation> keys = new ArrayList<>();
		for (CcpJsonRepresentation record : records) {
			String email = record.getAsString(JnJsonCommonsFields.email);
			CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, email);
			keys.add(key);
		}
		return keys;
	}
}
