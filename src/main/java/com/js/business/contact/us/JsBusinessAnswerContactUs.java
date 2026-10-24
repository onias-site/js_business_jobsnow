package com.js.business.contact.us;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.entities.JsEntityContactUs;

/**
 * Answers a ticket of the "contact us": deletes it with the answer of the support ({@code supportResponse}), which moves
 * it, whole, to {@code js_contact_us_solved} and e-mails the answer to the user. The ticket is the one found under
 * {@code _entities} by the presence check that runs this action.
 */
public class JsBusinessAnswerContactUs implements CcpBusiness {

	/** The key of the ticket (readable e-mail and type). */
	private final CcpJsonRepresentation ticketKey;

	/** The answer of the support. */
	private final String supportResponse;

	/**
	 * Keeps the key of the ticket and the answer.
	 * @param ticketKey the key of the ticket, with the readable e-mail
	 * @param supportResponse the answer of the support, 10 to 500 characters
	 */
	public JsBusinessAnswerContactUs(CcpJsonRepresentation ticketKey, String supportResponse) {
		this.ticketKey = ticketKey;
		this.supportResponse = supportResponse;
	}

	/**
	 * Answers the ticket.
	 * @param jsonWithTheStoredTicket the json of the procedure, holding the ticket under {@code _entities}
	 * @return the same json
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation jsonWithTheStoredTicket) {
		CcpJsonRepresentation storedTicket = jsonWithTheStoredTicket.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JsEntityContactUs.ENTITY);
		// the stored e-mail is the hash: the readable one goes back, for the e-mail of the answer and for the twin, that
		// receives the record as it is given
		CcpJsonRepresentation completeTicket = storedTicket.mergeWithAnotherJson(this.ticketKey);
		CcpJsonRepresentation answeredTicket = completeTicket.put(JsEntityContactUs.Fields.supportResponse, this.supportResponse);
		JsEntityContactUs.ENTITY.delete(answeredTicket);
		return jsonWithTheStoredTicket;
	}
}
