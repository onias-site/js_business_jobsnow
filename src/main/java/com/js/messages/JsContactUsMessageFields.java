package com.js.messages;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Placeholders of the e-mail templates of the "contact us" that are not fields of {@code JsEntityContactUs}: the label of
 * the {@code type} in the language of the message and the texts typed by the user and by the support, escaped for the
 * HTML body of the e-mail.
 */
public enum JsContactUsMessageFields implements CcpJsonFieldName{
	/** The {@code typeDescription} field: the label of the {@code type}. */
	typeDescription,
	/** The {@code escapedTitle} field: the {@code title}, escaped. */
	escapedTitle,
	/** The {@code escapedDescription} field: the {@code description}, escaped. */
	escapedDescription,
	/** The {@code escapedSupportResponse} field: the {@code supportResponse}, escaped. */
	escapedSupportResponse,
}
