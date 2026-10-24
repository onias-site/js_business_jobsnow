package com.js.business.contact.us;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.jn.entities.JnEntityTicketDailyLimit;
import com.js.entities.JsEntityContactUs;
import com.js.json.fields.validation.JsContactUsTypes;

/**
 * Saves the ticket of the "contact us" and, when its {@code type} has a daily quota, spends the day of that quota
 * ({@link JnEntityTicketDailyLimit}): a bug spends nothing, a question spends its own quota and the other kinds the quota
 * they share with the skill suggestion and the skill hierarchy fix.
 */
public class JsBusinessSaveContactUs implements CcpBusiness {

	/** The ticket as it came in the request. */
	private final CcpJsonRepresentation json;

	/**
	 * Keeps the ticket as it came in the request.
	 * @param json the ticket
	 */
	public JsBusinessSaveContactUs(CcpJsonRepresentation json) {
		this.json = json;
	}

	/**
	 * Saves the ticket and spends the day of its quota.
	 * @param ticket the ticket, as the presence checks hand it over
	 * @return the saved ticket
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation ticket) {
		CcpBusiness saveTheTicket = CcpEntityOperationType.save.getOperationCallback(JsEntityContactUs.ENTITY);
		CcpJsonRepresentation savedTicket = saveTheTicket.execute(ticket);

		JsContactUsTypes type = this.json.getAsEnum(JsEntityContactUs.Fields.type, JsContactUsTypes.class);
		boolean unlimited = false == type.quotaGroup.isLimited();

		if(unlimited) {
			return savedTicket;
		}

		String email = this.json.getAsString(JsEntityContactUs.Fields.email);
		CcpJsonRepresentation dailyLimit = JnEntityTicketDailyLimit.getKey(email, type.quotaGroup);
		JnEntityTicketDailyLimit.ENTITY.save(dailyLimit);
		return savedTicket;
	}
}
