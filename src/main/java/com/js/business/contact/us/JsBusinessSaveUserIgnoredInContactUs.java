package com.js.business.contact.us;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityCommandNotAllowedToUser;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnUserRequestCommands;
import com.js.entities.JsEntityContactUs;

/**
 * Action of {@link JsBusinessContactUsIgnoreUser}: records the user in {@link JnEntityCommandNotAllowedToUser}, with the
 * ticket being answered ({@code type}, {@code title} and {@code description}) in the {@code description}, and its title as
 * the summary shown in the lists of ignored users.
 */
class JsBusinessSaveUserIgnoredInContactUs implements CcpBusiness {

	/** The key of the ticket (email + type), with the readable email. */
	private final CcpJsonRepresentation ticketKey;

	/**
	 * Keeps the key of the ticket.
	 * @param ticketKey the key of the ticket, with the readable email
	 */
	JsBusinessSaveUserIgnoredInContactUs(CcpJsonRepresentation ticketKey) {
		this.ticketKey = ticketKey;
	}

	/**
	 * Records the ignored user.
	 * @param jsonWithTheStoredTicket the json of the procedure, holding the ticket under {@code _entities}
	 * @return the same json
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation jsonWithTheStoredTicket) {
		// empty when the ticket is no longer open
		CcpJsonRepresentation storedTicket = jsonWithTheStoredTicket.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JsEntityContactUs.ENTITY);
		CcpJsonRepresentation completeTicket = storedTicket.mergeWithAnotherJson(this.ticketKey);
		// the email of the record is kept as hash; the readable one goes in the description, for the operator to list the ignored users
		CcpJsonRepresentation requestDescription = completeTicket.getJsonPiece(JsEntityContactUs.Fields.email, JsEntityContactUs.Fields.type, JsEntityContactUs.Fields.title, JsEntityContactUs.Fields.description);
		String title = completeTicket.getAsString(JsEntityContactUs.Fields.title);
		CcpJsonRepresentation description = requestDescription.put(JnJsonCommonsFields.request, title);

		String email = this.ticketKey.getAsString(JsEntityContactUs.Fields.email);
		CcpJsonRepresentation ignoredUserWithEmail = CcpOtherConstants.EMPTY_JSON.put(JnEntityCommandNotAllowedToUser.Fields.email, email);
		CcpJsonRepresentation ignoredUserWithCommand = ignoredUserWithEmail.put(JnEntityCommandNotAllowedToUser.Fields.commandName, JnUserRequestCommands.answerContactUs);
		CcpJsonRepresentation ignoredUser = ignoredUserWithCommand.put(JnEntityCommandNotAllowedToUser.Fields.description, description);
		// saving the user discards this ticket and every other open request of the user
		JnEntityCommandNotAllowedToUser.ENTITY.save(ignoredUser);
		return jsonWithTheStoredTicket;
	}
}
