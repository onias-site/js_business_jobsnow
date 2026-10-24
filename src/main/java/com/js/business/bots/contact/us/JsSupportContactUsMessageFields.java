package com.js.business.bots.contact.us;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Placeholders of the {@link JsSupportContactUsMessages} templates that are not fields of the ticket.
 */
public enum JsSupportContactUsMessageFields implements CcpJsonFieldName{
	/** The {@code typeDescription} field: the label of the {@code type} of the ticket. */
	typeDescription,
	/** The {@code options} field. */
	options,
	/** The {@code ignoreConfirmation} field. */
	ignoreConfirmation,
	/** The {@code inexistentField} field: the procedures of the steps answer with the json they received, so they return no field. */
	inexistentField
}
