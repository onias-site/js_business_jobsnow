package com.js.business.bots.contact.us;

import com.ccp.process.CcpProcessStatus;

/**
 * Diversions of the steps of the {@code answerContactUs} command, mapped in the {@code stepFlow} of each step: there is
 * no open ticket of the type for the e-mail ({@code ticketNotFound}, the session ends), the answer was not understood
 * ({@code invalidAnswer}, the same step asks again), the ticket was answered ({@code ticketAnswered}, the session ends),
 * the operator left the ticket open to go to the next one ({@code ticketSkipped}, the session ends and the bot shows
 * {@code /pendingTickets} from the next ticket), asked to ignore the user ({@code ignoreConfirmationAsked}, goes on to
 * the confirmation), gave up ignoring them ({@code ignoreCanceled}, back to the decision) or confirmed it
 * ({@code userIgnored}, the session ends), or the user is ignored by the support ({@code userNotAllowed}, the session
 * ends with a notice to the operator).
 */
public enum JsSupportContactUsStatus implements CcpProcessStatus{
	/** Status 404: there is no open ticket of the type for the e-mail. */
	ticketNotFound(404),
	/** Status 400: the answer was not understood. */
	invalidAnswer(400),
	/** Status 200: the ticket was answered. */
	ticketAnswered(200),
	/** Status 201: the user is now ignored by the support. */
	userIgnored(201),
	/** Status 202: the operator asked to ignore the user; the confirmation follows. */
	ignoreConfirmationAsked(202),
	/** Status 204: the operator left the ticket open to go to the next one. */
	ticketSkipped(204),
	/** Status 205: the operator gave up ignoring the user. */
	ignoreCanceled(205),
	/** Status 403: the user is ignored by the support. */
	userNotAllowed(403)
	;

	/** The HTTP status code. */
	public final int status;

	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JsSupportContactUsStatus(int status) {
		this.status = status;
	}

	/**
	 * Returns the HTTP status code.
	 * @return the status code
	 */
	public int asNumber() {
		return this.status;
	}
}
