package com.js.business.bots.contact.us;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;
import com.js.entities.JsEntityContactUs;
import com.js.json.fields.validation.JsContactUsTypes;

/**
 * Texts sent to the support bot operator along the {@code answerContactUs} command, in the language of the session. The
 * texts themselves live in {@link JsSupportContactUsMessages}.
 */
final class JsSupportContactUsConversation {

	/** Utility class; not instantiable. */
	private JsSupportContactUsConversation() {}

	/**
	 * Returns the language of the session (the enum itself or its name), Portuguese by default.
	 * @param json the session
	 * @return the language
	 */
	static JnLanguage getLanguage(CcpJsonRepresentation json) {
		String languageName = json.getAsString(JnJsonCommonsFields.language);
		boolean noLanguage = languageName.isEmpty();
		JnLanguage language = noLanguage ? JnLanguage.portuguese : JnLanguage.valueOf(languageName);
		return language;
	}

	/**
	 * The key of the ticket of the session: the e-mail (readable) and the type.
	 * @param json the session
	 * @return the key
	 */
	static CcpJsonRepresentation getTicketKey(CcpJsonRepresentation json) {
		CcpJsonRepresentation ticketKey = json.getJsonPiece(JsEntityContactUs.Fields.email, JsEntityContactUs.Fields.type);
		return ticketKey;
	}

	/**
	 * The key of the ticket of the session with the label of its type ({@code typeDescription}), in the language of the
	 * session: the parameters of the texts about the ticket.
	 * @param json the session
	 * @return the parameters
	 */
	static CcpJsonRepresentation getTicketParameters(CcpJsonRepresentation json) {
		JnLanguage language = getLanguage(json);
		JsContactUsTypes type = json.getAsEnum(JsEntityContactUs.Fields.type, JsContactUsTypes.class);
		String typeDescription = type.getMessage(language);
		CcpJsonRepresentation ticketKey = getTicketKey(json);
		CcpJsonRepresentation ticketParameters = ticketKey.put(JsSupportContactUsMessageFields.typeDescription, typeDescription);
		return ticketParameters;
	}

	/**
	 * Returns the options offered to the operator.
	 * @param language the language
	 * @return the options
	 */
	static String getOptions(JnLanguage language) {
		String options = JsSupportContactUsMessages.options.getMessage(language);
		return options;
	}

	/**
	 * Asks the operator to confirm that the user will be ignored.
	 * @param json the session
	 * @return the question
	 */
	static String getIgnoreConfirmation(CcpJsonRepresentation json) {
		JnLanguage language = getLanguage(json);
		CcpJsonRepresentation parameters = json.getJsonPiece(JsEntityContactUs.Fields.email);
		String ignoreConfirmation = JsSupportContactUsMessages.ignoreConfirmation.getMessage(language, parameters);
		return ignoreConfirmation;
	}

	/**
	 * Tells that the answer was not understood and asks for the options again.
	 * @param json the session
	 * @return the text
	 */
	static String getNotUnderstood(CcpJsonRepresentation json) {
		JnLanguage language = getLanguage(json);
		String options = getOptions(language);
		CcpJsonRepresentation parameters = CcpOtherConstants.EMPTY_JSON.put(JsSupportContactUsMessageFields.options, options);
		String notUnderstood = JsSupportContactUsMessages.notUnderstood.getMessage(language, parameters);
		return notUnderstood;
	}

	/**
	 * Puts the text to send to the operator in {@code botReply}.
	 * @param json the session
	 * @param reply the text
	 * @return the session with the reply
	 */
	static CcpJsonRepresentation reply(CcpJsonRepresentation json, String reply) {
		CcpJsonRepresentation jsonWithReply = json.put(JnJsonInstantMessengerFields.botReply, reply);
		return jsonWithReply;
	}
}
