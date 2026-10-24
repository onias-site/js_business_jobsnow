package com.js.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.messages.JnMessageTemplate;
import com.jn.messages.JnRepeatableMessage;

/**
 * E-mail to the user when the support answers their ticket of the "contact us" (the ticket moves to
 * {@code js_contact_us_solved}): what they had written and the answer of the support.
 *
 * <p>Repeatable: every answer is a new fact.
 */
public class JsNotifyUserAboutAnsweredContactUs implements JnMessageTemplate, JnRepeatableMessage{
	/** The single instance: the class keeps no state. */
	public final static JsNotifyUserAboutAnsweredContactUs INSTANCE = new JsNotifyUserAboutAnsweredContactUs();

	private JsNotifyUserAboutAnsweredContactUs() {}

	/**
	 * Prepares the values of the message ({@link JsContactUsMessageValues}).
	 * @param json the answered ticket, with {@code supportResponse}
	 * @return the values of the template
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation preparedJson = JsContactUsMessageValues.prepare(json);
		return preparedJson;
	}
}
