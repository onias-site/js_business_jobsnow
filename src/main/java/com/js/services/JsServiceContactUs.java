package com.js.services;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.jn.entities.JnEntityCommandNotAllowedToUser;
import com.jn.entities.JnEntityTicketDailyLimit;
import com.jn.services.JnService;
import com.jn.utils.JnDeleteKeysFromCache;
import com.js.business.contact.us.JsBusinessSaveContactUs;
import com.js.entities.JsEntityContactUs;
import com.js.json.fields.validation.JsContactUsTypes;
import com.js.status.contact.us.JsProcessStatusSaveContactUs;

/**
 * Service of the "contact us": the tickets the user opens from the modal of the front end. It has no session (the
 * login screen may be the one with the bug): any e-mail with a valid syntax is accepted. Each item takes its input rules
 * from the class of the same name in this package (see {@code JnService}).
 */
public enum JsServiceContactUs implements JnService {

	/**
	 * Saves the ticket ({@link JsEntityContactUs}), which e-mails the user and opens a ticket to the support bot operator.
	 * A user that the operator chose to ignore (in any command: the ignoring is global,
	 * {@link JnEntityCommandNotAllowedToUser}) gets {@code userNotAllowed} (403). A user who already spent, in the last 24
	 * hours, the daily quota of the group of the {@code type} ({@link JnEntityTicketDailyLimit}: the quota shared with the
	 * skill suggestion and the skill hierarchy fix, or the quota of the questions) gets {@code dailyLimitReached} (429); a
	 * bug has no quota, so it is never refused for it.
	 */
	SaveContactUs{
		/**
		 * Runs the checks and saves the ticket.
		 * @param json the ticket
		 * @return the same JSON
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			JsContactUsTypes type = json.getAsEnum(JsEntityContactUs.Fields.type, JsContactUsTypes.class);
			// the quota group completes the key of the daily limit; a bug has its own group, never written
			String quotaGroup = type.quotaGroup.name();
			CcpJsonRepresentation jsonWithQuotaGroup = json.put(JnEntityTicketDailyLimit.Fields.quotaGroup, quotaGroup);
			CcpBusiness saveTheTicket = new JsBusinessSaveContactUs(json);

			new CcpGetEntityId(jsonWithQuotaGroup)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JnEntityCommandNotAllowedToUser.ENTITY).returnStatus(JsProcessStatusSaveContactUs.userNotAllowed).and()
				.ifThisIdIsPresentInEntity(JnEntityTicketDailyLimit.ENTITY).returnStatus(JsProcessStatusSaveContactUs.dailyLimitReached).and()
				.executeAction(saveTheTicket)
				.andFinallyReturningTheseFields(JsContactUsResponse.inexistentField)
			.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
			return json;
		}
	},
	;
}
