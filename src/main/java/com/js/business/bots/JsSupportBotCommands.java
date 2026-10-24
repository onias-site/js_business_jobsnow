package com.js.business.bots;

/**
 * Catalogs the commands of the support bot that belong to js. The bot (jb) finds them by {@code botName} in the
 * configuration seeded in {@code JnEntityBotCommand} and runs their steps by class name.
 */
public enum JsSupportBotCommands {

	/** Solves the login token tickets (resend or unlock the token). */
	solveLoginTokenTicket,
	/** Answers a ticket of the "contact us", leaves it open to go to the next one, or ignores the user. */
	answerContactUs,
	/** Lists the users ignored while answering a ticket of the "contact us", to stop ignoring them. */
	ignoredContactUs,
	;
}
