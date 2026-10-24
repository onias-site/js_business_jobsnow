package com.js.business.bots.contact.us;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnLanguage;
import com.js.business.contact.us.JsBusinessAnswerContactUs;
import com.js.entities.JsEntityContactUs;

/**
 * Second step of the {@code answerContactUs} command: the operator answers the ticket, with an answer to the user of 10
 * to 500 characters, leaves it open to go to the next ticket, or asks to ignore the user.
 *
 * <p>An answer moves the ticket to {@code js_contact_us_solved} and e-mails the user ({@link JsBusinessAnswerContactUs}),
 * finishing the session ({@code ticketAnswered}); a ticket that left the entity meanwhile (discarded because the user was
 * ignored) ends the session with {@code ticketNotFound}. Going to the next ticket ends the session keeping the ticket
 * open ({@code ticketSkipped}, with {@link JnJsonInstantMessengerFields#skipTheTicket}): the bot shows
 * {@code /pendingTickets} from the ticket after it. Choosing to ignore the user asks for the confirmation
 * ({@code ignoreConfirmationAsked}). Any other answer, including an answer to the user without a valid length, is asked
 * again ({@code invalidAnswer}).
 */
public class JsSupportContactUsDecide implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JsSupportContactUsDecide INSTANCE = new JsSupportContactUsDecide();

	private JsSupportContactUsDecide() {}

	/**
	 * Handles the answer of the operator.
	 * @param json the session, with {@code email} and {@code type}
	 * @return never returns normally
	 * @throws CcpErrorFlowDisturb with {@code ticketAnswered}, {@code ticketNotFound}, {@code ticketSkipped},
	 * {@code ignoreConfirmationAsked} or {@code invalidAnswer}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		JsSupportContactUsAnswer answer = JsSupportContactUsAnswer.read(json);
		JnLanguage language = JsSupportContactUsConversation.getLanguage(json);

		if(JsSupportContactUsAnswerType.ignore == answer.type) {
			String ignoreConfirmation = JsSupportContactUsConversation.getIgnoreConfirmation(json);
			CcpJsonRepresentation jsonWithConfirmation = JsSupportContactUsConversation.reply(json, ignoreConfirmation);
			return JsSupportContactUsStatus.ignoreConfirmationAsked.throwException(jsonWithConfirmation);
		}

		if(JsSupportContactUsAnswerType.next == answer.type) {
			CcpJsonRepresentation ticketParameters = JsSupportContactUsConversation.getTicketParameters(json);
			String ticketSkipped = JsSupportContactUsMessages.ticketSkipped.getMessage(language, ticketParameters);
			CcpJsonRepresentation jsonWithReply = JsSupportContactUsConversation.reply(json, ticketSkipped);
			CcpJsonRepresentation jsonSkippingTheTicket = jsonWithReply.put(JnJsonInstantMessengerFields.skipTheTicket, true);
			return JsSupportContactUsStatus.ticketSkipped.throwException(jsonSkippingTheTicket);
		}

		boolean invalidAnswer = false == answer.isValidResponse();

		if(invalidAnswer) {
			String notUnderstood = JsSupportContactUsConversation.getNotUnderstood(json);
			CcpJsonRepresentation jsonAskingAgain = JsSupportContactUsConversation.reply(json, notUnderstood);
			return JsSupportContactUsStatus.invalidAnswer.throwException(jsonAskingAgain);
		}

		CcpJsonRepresentation ticketKey = JsSupportContactUsConversation.getTicketKey(json);
		CcpBusiness answerTheTicket = new JsBusinessAnswerContactUs(ticketKey, answer.response);

		new CcpGetEntityId(ticketKey)
		.toBeginProcedureAnd()
			.ifThisIdIsNotPresentInEntity(JsEntityContactUs.ENTITY).returnStatus(JsSupportContactUsStatus.ticketNotFound).and()
			.ifThisIdIsPresentInEntity(JsEntityContactUs.ENTITY).executeAction(answerTheTicket)
			.andFinallyReturningTheseFields(JsSupportContactUsMessageFields.inexistentField)
		.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;

		CcpJsonRepresentation ticketParameters = JsSupportContactUsConversation.getTicketParameters(json);
		String ticketAnswered = JsSupportContactUsMessages.ticketAnswered.getMessage(language, ticketParameters);
		CcpJsonRepresentation jsonWithReply = JsSupportContactUsConversation.reply(json, ticketAnswered);
		return JsSupportContactUsStatus.ticketAnswered.throwException(jsonWithReply);
	}
}
