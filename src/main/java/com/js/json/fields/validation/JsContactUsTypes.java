package com.js.json.fields.validation;

import com.jn.json.fields.validation.JnTicketQuotaGroups;
import com.jn.messages.JnSystemMessage;

/**
 * The kinds of ticket of the "contact us" (the combobox of its modal in the front end), the allowed values of the
 * {@code type} of {@code JsEntityContactUs}. Each kind has:
 * <ul>
 * <li>its label, written in the language of the reader (a text of {@code JnEntitySystemMessage}, seeded by
 * {@code JsEntityContactUs}), shown in the e-mails to the user and to the support bot operator;</li>
 * <li>the priority of its ticket in {@code /pendingTickets} (the lower, the sooner it is shown);</li>
 * <li>its group of daily quota: a bug is never limited (until the operator ignores the user), a question has a quota
 * of its own, and the other kinds share the quota of the skill suggestion and of the skill hierarchy fix.</li>
 * </ul>
 */
public enum JsContactUsTypes implements JnSystemMessage {
	/** Reporting a bug: priority 0, no daily limit. */
	reportBug(0, JnTicketQuotaGroups.unlimited),
	/** Giving a compliment: priority 5, shared daily quota. */
	makeCompliment(5, JnTicketQuotaGroups.sharedTickets),
	/** Making a criticism: priority 2, shared daily quota. */
	makeCriticism(2, JnTicketQuotaGroups.sharedTickets),
	/** Making a suggestion: priority 4, shared daily quota. */
	makeSuggestion(4, JnTicketQuotaGroups.sharedTickets),
	/** Making a comment: priority 5, shared daily quota. */
	makeComment(5, JnTicketQuotaGroups.sharedTickets),
	/** Clarifying doubts: priority 1, daily quota of its own. */
	clarifyDoubts(1, JnTicketQuotaGroups.clarifyDoubts),
	;

	/** The priority of the ticket of the support operator: the lower, the sooner it is shown. */
	public final int priority;

	/** The group of daily quota the ticket spends. */
	public final JnTicketQuotaGroups quotaGroup;

	/**
	 * Associates the priority and the group of daily quota.
	 * @param priority the priority of the ticket
	 * @param quotaGroup the group of daily quota
	 */
	private JsContactUsTypes(int priority, JnTicketQuotaGroups quotaGroup) {
		this.priority = priority;
		this.quotaGroup = quotaGroup;
	}
}
