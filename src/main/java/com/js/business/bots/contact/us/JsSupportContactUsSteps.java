package com.js.business.bots.contact.us;

/**
 * Steps of the {@code answerContactUs} command after the first one (the first step has the name of the command itself):
 * deciding the ticket and confirming that the user will be ignored.
 */
public enum JsSupportContactUsSteps {
	/** Answering the ticket, leaving it open to go to the next one or asking to ignore the user. */
	answerContactUsDecide,
	/** Confirming that the user will be ignored. */
	answerContactUsConfirmIgnore
}
