package com.js.business.bots.contact.us;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.utils.JnLanguage;
import com.js.entities.JsEntityContactUs;
import com.js.json.fields.validation.JsContactUsTypes;

/**
 * Action of {@link JsSupportContactUsShowTicket}: writes, in {@code botReply}, the ticket found under {@code _entities}
 * (its type, title, description and stack trace) followed by the options of the operator.
 */
class JsSupportContactUsTicketText implements CcpBusiness {

	/** The single instance: the class keeps no state. */
	static final JsSupportContactUsTicketText INSTANCE = new JsSupportContactUsTicketText();

	private JsSupportContactUsTicketText() {}

	/**
	 * At most this many characters of the stack trace are shown: a message of the bot has a limit of size.
	 */
	private static final int STACK_TRACE_MAX_LENGTH = 1500;

	/**
	 * Writes the ticket for the operator.
	 * @param json the session, with the ticket under {@code _entities}
	 * @return the session with {@code botReply}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation storedTicket = json.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JsEntityContactUs.ENTITY);
		JnLanguage language = JsSupportContactUsConversation.getLanguage(json);

		JsContactUsTypes type = json.getAsEnum(JsEntityContactUs.Fields.type, JsContactUsTypes.class);
		String typeDescription = type.getMessage(language);
		String stackTrace = storedTicket.getAsString(JsEntityContactUs.Fields.stackTrace);
		boolean longStackTrace = stackTrace.length() > STACK_TRACE_MAX_LENGTH;
		String shownStackTrace = longStackTrace ? stackTrace.substring(0, STACK_TRACE_MAX_LENGTH) : stackTrace;
		String options = JsSupportContactUsConversation.getOptions(language);

		CcpJsonRepresentation ticketKey = JsSupportContactUsConversation.getTicketKey(json);
		CcpJsonRepresentation ticketDetails = storedTicket.getJsonPiece(JsEntityContactUs.Fields.title, JsEntityContactUs.Fields.description);
		CcpJsonRepresentation parametersWithTheTicket = ticketKey.mergeWithAnotherJson(ticketDetails);
		CcpJsonRepresentation parametersWithStackTrace = parametersWithTheTicket.put(JsEntityContactUs.Fields.stackTrace, shownStackTrace);
		CcpJsonRepresentation parametersWithType = parametersWithStackTrace.put(JsSupportContactUsMessageFields.typeDescription, typeDescription);
		CcpJsonRepresentation parameters = parametersWithType.put(JsSupportContactUsMessageFields.options, options);
		String ticketText = JsSupportContactUsMessages.ticket.getMessage(language, parameters);

		CcpJsonRepresentation jsonWithReply = JsSupportContactUsConversation.reply(json, ticketText);
		return jsonWithReply;
	}
}
