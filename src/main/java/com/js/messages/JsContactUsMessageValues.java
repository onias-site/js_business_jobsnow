package com.js.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.messages.JnEmailHtml;
import com.jn.utils.JnLanguage;
import com.jn.utils.JnSystemProperties;
import com.js.entities.JsEntityContactUs;
import com.js.json.fields.validation.JsContactUsTypes;

/**
 * The values every message of a ticket of the "contact us" needs, prepared from the ticket: the readable e-mail back
 * in {@code email} (the transformer of the entity left the hash there), the label of the {@code type} and the texts typed
 * by the user and by the support, escaped for the HTML body of the e-mail.
 */
final class JsContactUsMessageValues {

	/** Utility class; not instantiable. */
	private JsContactUsMessageValues() {}

	/**
	 * Prepares the values of the messages of the ticket.
	 * @param json the ticket
	 * @return the ticket with the values of the messages
	 */
	static CcpJsonRepresentation prepare(CcpJsonRepresentation json) {
		CcpJsonRepresentation jsonWithOriginalEmail = json
				.renameField(JnJsonTransformersFieldsEntityDefault.JsonFieldNames.originalEmail, JnJsonCommonsFields.email);

		// the label goes in the same language that JnMessageType.email will use to pick the template
		String languageInTheJson = json.getAsString(JnJsonCommonsFields.language);
		boolean noLanguageInTheJson = languageInTheJson.isEmpty();
		String languageName = noLanguageInTheJson ? JnSystemProperties.INSTANCE.supportLanguage() : languageInTheJson;
		JnLanguage language = JnLanguage.valueOf(languageName);
		JsContactUsTypes type = json.getAsEnum(JsEntityContactUs.Fields.type, JsContactUsTypes.class);
		String typeDescription = type.getMessage(language);

		String title = json.getAsString(JsEntityContactUs.Fields.title);
		String description = json.getAsString(JsEntityContactUs.Fields.description);
		String supportResponse = json.getAsString(JsEntityContactUs.Fields.supportResponse);
		String escapedTitle = JnEmailHtml.escape(title);
		String escapedDescription = JnEmailHtml.escape(description);
		String escapedSupportResponse = JnEmailHtml.escape(supportResponse);

		CcpJsonRepresentation jsonWithTypeDescription = jsonWithOriginalEmail.put(JsContactUsMessageFields.typeDescription, typeDescription);
		CcpJsonRepresentation jsonWithEscapedTitle = jsonWithTypeDescription.put(JsContactUsMessageFields.escapedTitle, escapedTitle);
		CcpJsonRepresentation jsonWithEscapedDescription = jsonWithEscapedTitle.put(JsContactUsMessageFields.escapedDescription, escapedDescription);
		CcpJsonRepresentation preparedJson = jsonWithEscapedDescription.put(JsContactUsMessageFields.escapedSupportResponse, escapedSupportResponse);
		return preparedJson;
	}
}
