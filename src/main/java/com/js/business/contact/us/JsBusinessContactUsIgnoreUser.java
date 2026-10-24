package com.js.business.contact.us;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.jn.entities.JnEntityCommandNotAllowedToUser;
import com.jn.utils.JnDeleteKeysFromCache;
import com.js.entities.JsEntityContactUs;

/**
 * The support bot operator ignores the user of a ticket of the "contact us": records them in
 * {@link JnEntityCommandNotAllowedToUser}, with the ticket in the {@code description}
 * ({@link JsBusinessSaveUserIgnoredInContactUs}). Saving the user discards their open tickets, of every cost center,
 * through the subscribers of the write. A single read by id loads the ticket, when it is still open.
 */
public class JsBusinessContactUsIgnoreUser implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public static final JsBusinessContactUsIgnoreUser INSTANCE = new JsBusinessContactUsIgnoreUser();

	private JsBusinessContactUsIgnoreUser() {}

	/** Fields of the procedure. */
	enum JsonFieldNames implements CcpJsonFieldName{
		/** The procedure answers with the json it received, so it returns no field. */
		inexistentField
	}

	/**
	 * Ignores the user.
	 * @param json the session, with the readable {@code email} and the {@code type}
	 * @return the same JSON
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation ticketKey = json.getJsonPiece(JsEntityContactUs.Fields.email, JsEntityContactUs.Fields.type);
		CcpBusiness saveTheIgnoredUser = new JsBusinessSaveUserIgnoredInContactUs(ticketKey);

		new CcpGetEntityId(ticketKey)
		.toBeginProcedureAnd()
			.loadThisIdFromEntity(JsEntityContactUs.ENTITY).and()
			.executeAction(saveTheIgnoredUser)
			.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
		.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
		return json;
	}
}
