package com.js.business.login.solve.token;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnMessages;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityEmailMessageSent;
import com.js.entities.JsEntityLoginEmail;
import com.js.entities.JsEntityLoginToken;
import com.js.entities.JsEntityLoginTokenAttempts;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnSystemProperties;

/** Resets the login token of a user, deleting it from every index, so a new token can be generated. */
public class JsBusinessResetLoginToken implements CcpBusiness{
	
	/** Input fields. */
	enum JsonFieldNames implements CcpJsonFieldName{
		/** The {@code email} field: validated as in {@code JnJsonCommonsFields}, required. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,
	}
	
	/** Singleton; use {@link #INSTANCE}. */
	private JsBusinessResetLoginToken(){}
	
	/** The single instance. */
	public static final JsBusinessResetLoginToken INSTANCE = new JsBusinessResetLoginToken();
	
	/**
	 * Deletes, in a single round trip to the database, the token in the main entity, the token in the twin (where it stays
	 * while locked) and the record that marks the token e-mail as already sent (it would reject the e-mail of the new token
	 * as a repetition). Deleting a record that is not there does no harm: the token is either in the main entity or in the
	 * twin, never in both. The JSON first goes through the e-mail transformer, which computes the hash used by the primary
	 * keys.
	 * <p>
	 * The {@code language} returned is the language of the user, so the new token goes in it: the one recorded in the
	 * login e-mail on the last request of a token or, for a record without it, the language of the system. Until
	 * 2026-10-07 it was always Portuguese.
	 * @param json the request with {@code email}
	 * @return the request plus the {@code language} of the user
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String tokenEmail = JnMessages.JnNotifyUserAboutLoginToken.class.getName();

		CcpJsonRepresentation plainJson = CcpOtherConstants.EMPTY_JSON.redoJson(json);
		CcpJsonRepresentation withTheHashedEmail = plainJson.getTransformedJson(JnJsonTransformersFieldsEntityDefault.email);
		CcpJsonRepresentation recordsToDelete = withTheHashedEmail.put(JnJsonCommonsFields.subjectType, tokenEmail);

		CcpEntity lockedToken = JsEntityLoginToken.ENTITY.getTwinEntity();

		JnExecuteBulkOperation.INSTANCE.executeBulk(
				recordsToDelete,
				CcpBulkEntityOperationType.delete,
				JnDeleteKeysFromCache.INSTANCE,
				JsEntityLoginToken.ENTITY,
				lockedToken,
				JnEntityEmailMessageSent.ENTITY,
				// the new token starts with no wrong attempts (until 2026-10-08 the ones that locked the old token were
				// kept, and one more wrong token locked the new one right away)
				JsEntityLoginTokenAttempts.ENTITY
				);

		String userLanguage = this.getUserLanguage(json);
		CcpJsonRepresentation jsonWithLanguage = json.put(JnJsonCommonsFields.language, userLanguage);
		return jsonWithLanguage;
	}

	/**
	 * The language recorded in the login e-mail of the user, or the language of the system when there is none.
	 * @param json the request with {@code email}
	 * @return the language
	 */
	private String getUserLanguage(CcpJsonRepresentation json) {
		String systemLanguage = JnSystemProperties.INSTANCE.supportLanguage();
		boolean loginEmailIsMissing = false == JsEntityLoginEmail.ENTITY.exists(json);
		if(loginEmailIsMissing) {
			return systemLanguage;
		}
		CcpJsonRepresentation loginEmail = JsEntityLoginEmail.ENTITY.getOneById(json);
		boolean languageIsMissing = false == loginEmail.containsField(JnJsonCommonsFields.language);
		if(languageIsMissing) {
			return systemLanguage;
		}
		String userLanguage = loginEmail.getAsString(JnJsonCommonsFields.language);
		return userLanguage;
	}


	/**
	 * Validates the input with {@link JsonFieldNames}.
	 * @return the validation class
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
