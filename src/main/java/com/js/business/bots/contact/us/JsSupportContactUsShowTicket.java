package com.js.business.bots.contact.us;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.jn.entities.JnEntityCommandNotAllowedToUser;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.js.entities.JsEntityContactUs;

/**
 * First step of the {@code answerContactUs} command ({@code /answerContactUs <type> <email>}): shows the operator the
 * ticket of the "contact us" the user opened and asks for the decision.
 *
 * <p>A user ignored by the support (the ignoring is global, {@link JnEntityCommandNotAllowedToUser}) is not answered, even
 * if the operator runs the command for them: the flow is diverted with {@code userNotAllowed}. Without an open ticket of
 * the type (already answered, or discarded) the flow is diverted with {@code ticketNotFound}. Both checks and the read of
 * the ticket are a single read by id.
 */
public class JsSupportContactUsShowTicket implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JsSupportContactUsShowTicket INSTANCE = new JsSupportContactUsShowTicket();

	private JsSupportContactUsShowTicket() {}

	/**
	 * Shows the ticket.
	 * @param json the session, with {@code email} and {@code type}
	 * @return the session with the reply
	 * @throws CcpErrorFlowDisturb with {@code userNotAllowed} or {@code ticketNotFound}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation resultingData = new CcpGetEntityId(json)
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityCommandNotAllowedToUser.ENTITY).returnStatus(JsSupportContactUsStatus.userNotAllowed).and()
			.ifThisIdIsNotPresentInEntity(JsEntityContactUs.ENTITY).returnStatus(JsSupportContactUsStatus.ticketNotFound).and()
			.ifThisIdIsPresentInEntity(JsEntityContactUs.ENTITY).executeAction(JsSupportContactUsTicketText.INSTANCE)
			.andFinallyReturningTheseFields(JnJsonInstantMessengerFields.botReply)
		.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
		String ticketText = resultingData.getAsString(JnJsonInstantMessengerFields.botReply);
		CcpJsonRepresentation jsonWithReply = JsSupportContactUsConversation.reply(json, ticketText);
		return jsonWithReply;
	}
}
