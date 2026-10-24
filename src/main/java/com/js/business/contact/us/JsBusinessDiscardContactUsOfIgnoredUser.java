package com.js.business.contact.us;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.query.CcpQueryExecutorDecorator;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.jn.business.messages.JnBusinessCancelSupportPendingCommand;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.entities.JsEntityContactUs;
import com.js.messages.JsNotifySupportAndUserAboutContactUs;

/**
 * Subscriber of the save of {@code JnEntityCommandNotAllowedToUser} (seeded by {@link JsEntityContactUs} in
 * {@code jn_write_event_subscribers}), that is, of the decision of the support bot operator to ignore the user: discards
 * every open ticket of the "contact us" of the user, as if it had never existed. Each ticket leaves the entity by
 * {@code deleteAnyWhere}, which also takes it out of the tickets of the user in {@code jn_grouper}, and its ticket leaves
 * {@code /pendingTickets}. Nothing is answered and the user is not notified.
 *
 * <p>Runs in the consumer of the messaging, never in an online request, so querying the tickets by email is allowed.
 */
public class JsBusinessDiscardContactUsOfIgnoredUser implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JsBusinessDiscardContactUsOfIgnoredUser INSTANCE = new JsBusinessDiscardContactUsOfIgnoredUser();

	private JsBusinessDiscardContactUsOfIgnoredUser() {}

	/**
	 * Runs the business described in the class documentation.
	 * @param json the ignored user
	 * @return the same JSON
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		// the entity keeps the hash of the email, and deleting needs the readable one, since the entity hashes it again
		// to find the record
		boolean emailAlreadyHashed = json.containsField(JnJsonTransformersFieldsEntityDefault.JsonFieldNames.originalEmail);
		CcpJsonRepresentation jsonWithHashedEmail = emailAlreadyHashed ? json : json.getTransformedJson(JnJsonTransformersFieldsEntityDefault.email);
		String hashedEmail = jsonWithHashedEmail.getAsString(JnJsonCommonsFields.email);
		String readableEmail = jsonWithHashedEmail.getAsString(JnJsonTransformersFieldsEntityDefault.JsonFieldNames.originalEmail);

		CcpQueryOptions ticketsOfTheUser = CcpQueryOptions.INSTANCE
				.startQuery()
				.startBool()
				.startMust()
				.term(JsEntityContactUs.Fields.email, hashedEmail)
				.endMustAndBackToBool()
				.endBoolAndBackToQuery()
				.endQueryAndBackToRequest()
				.maxResults();
		CcpQueryExecutorDecorator openTicketsQuery = ticketsOfTheUser.selectFrom(JsEntityContactUs.ENTITY);
		List<CcpJsonRepresentation> openTickets = openTicketsQuery.getResultAsList();

		String noticeTemplateId = JsNotifySupportAndUserAboutContactUs.class.getName();

		for (CcpJsonRepresentation openTicket : openTickets) {
			CcpJsonRepresentation completeTicket = openTicket.put(JsEntityContactUs.Fields.email, readableEmail);
			JnBusinessCancelSupportPendingCommand.INSTANCE.cancel(noticeTemplateId, completeTicket);
			JsEntityContactUs.ENTITY.deleteAnyWhere(completeTicket);
		}

		return json;
	}
}
