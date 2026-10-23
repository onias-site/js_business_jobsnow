package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Prepares the login token JSON before it is saved in the main entity. Applies the {@code token} transformer (which
 * generates the random token and keeps the original in {@code originalToken}) and copies the
 * original values: {@code originalEmail} to {@code email} and {@code chatId}, and
 * {@code originalToken} to {@code token}.
 */
public class JsBusinessPrepareLoginTokenBeforeSave implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JsBusinessPrepareLoginTokenBeforeSave INSTANCE = new JsBusinessPrepareLoginTokenBeforeSave();

	private JsBusinessPrepareLoginTokenBeforeSave() {}

	/**
	 * Prepares the token record (see the class description).
	 * @param json the record
	 * @return the prepared record
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		// the token is born here: the transformer only hashes a token it is given
		String originalToken = json.getOrDefault(JnJsonCommonsFields.originalToken, () -> JnJsonTransformersFieldsEntityDefault.getOriginalToken());
		CcpJsonRepresentation jsonWithOriginalToken = json.put(JnJsonCommonsFields.originalToken, originalToken);
		CcpJsonRepresentation jsonWithGeneratedToken = jsonWithOriginalToken
				.getTransformedJson(JnJsonTransformersFieldsEntityDefault.token);
		CcpJsonRepresentation jsonWithOriginalEmailCopied = jsonWithGeneratedToken
				.duplicateValueFromField(JsonFieldNames.originalEmail, JnJsonCommonsFields.email,
						JnJsonInstantMessengerFields.chatId);
		CcpJsonRepresentation preparedJson = jsonWithOriginalEmailCopied
				.duplicateValueFromField(JnJsonCommonsFields.originalToken, JnJsonTransformersFieldsEntityDefault.token)
				;
		return preparedJson;
	}

}
