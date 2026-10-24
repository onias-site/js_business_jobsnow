package com.js.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.messages.JnMessageTemplate;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.messages.JnRepeatableMessage;
import com.js.entities.JsEntityContactUs;
import com.js.json.fields.validation.JsContactUsTypes;

/**
 * Notice of a ticket of the "contact us" saved: to the user by e-mail, with what they filled in and telling that the
 * ticket is being analyzed, and to the support bot operator with the {@code /answerContactUs {type} {email}} command,
 * which also becomes a ticket of {@code /pendingTickets}, with the priority of the {@code type}.
 *
 * <p>Repeatable: every ticket is a new fact, so the notice goes out even if the same user already had one today.
 */
public class JsNotifySupportAndUserAboutContactUs implements JnMessageTemplate, JnRepeatableMessage{
	/** The single instance: the class keeps no state. */
	public final static JsNotifySupportAndUserAboutContactUs INSTANCE = new JsNotifySupportAndUserAboutContactUs();

	private JsNotifySupportAndUserAboutContactUs() {}

	/**
	 * Prepares the values of the messages ({@link JsContactUsMessageValues}) and the priority of the ticket of the operator.
	 * @param json the ticket
	 * @return the values of the templates
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation preparedJson = JsContactUsMessageValues.prepare(json);
		JsContactUsTypes type = json.getAsEnum(JsEntityContactUs.Fields.type, JsContactUsTypes.class);
		CcpJsonRepresentation jsonWithPriority = preparedJson.put(JnJsonInstantMessengerFields.priority, type.priority);
		return jsonWithPriority;
	}
}
