package com.js.business.bots.contact.us;

import com.jn.messages.JnSystemMessage;

/**
 * Texts sent to the support bot operator along the {@code answerContactUs} command, stored in
 * {@code JnEntitySystemMessage} and seeded by {@code JsEntityContactUs} (through
 * {@link JsSupportContactUsBotConfiguration}). The placeholders of the templates are the fields of
 * {@link JsSupportContactUsMessageFields} and of the ticket ({@code email}, {@code title}, {@code description},
 * {@code stackTrace}).
 */
public enum JsSupportContactUsMessages implements JnSystemMessage {

	/**
	 * The ticket shown to the operator: {@code {email}}, {@code {typeDescription}}, {@code {title}},
	 * {@code {description}}, {@code {stackTrace}} and the {@code {options}}.
	 */
	ticket,

	/** The options the operator has to decide the ticket. */
	options,

	/** Answer that is none of the options, or an answer to the user without 10 to 500 characters; precedes the options asked again. */
	notUnderstood,

	/** The ticket was answered: {@code {email}} and {@code {typeDescription}}. */
	ticketAnswered,

	/** The ticket stays open and the operator goes to the next one: {@code {email}} and {@code {typeDescription}}. */
	ticketSkipped,

	/** Asks the operator to confirm that the user will be ignored: {@code {email}}. */
	ignoreConfirmation,

	/** Answer to the ignore confirmation that is neither yes nor no; asks the {@code {ignoreConfirmation}} again. */
	ignoreConfirmationNotUnderstood,

	/** The operator gave up ignoring the user and goes back to the {@code {options}}. */
	ignoreCanceled,

	/** The operator confirmed that the user is ignored: {@code {email}}. */
	userIgnored,
	;
}
