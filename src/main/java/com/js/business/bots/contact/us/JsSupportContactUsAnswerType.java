package com.js.business.bots.contact.us;

/**
 * Kinds of answer the operator gives along the {@code answerContactUs} command: answering the ticket ({@code respond}),
 * leaving it open and going to the next one ({@code next}), asking to ignore the user ({@code ignore}) and the answer
 * to its confirmation ({@code yes}, {@code no}).
 */
enum JsSupportContactUsAnswerType {
	/** Answer the ticket, with the answer to the user. */
	respond,
	/** Leave the ticket open and go to the next one. */
	next,
	/** Ignore the user. */
	ignore,
	/** Confirm. */
	yes,
	/** Decline. */
	no,
	/** Anything else. */
	notUnderstood
}
