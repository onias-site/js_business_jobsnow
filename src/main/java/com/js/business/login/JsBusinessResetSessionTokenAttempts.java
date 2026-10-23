package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.entities.JsEntityLoginSessionTokenAttempts;

/**
 * Deletes the counter of invalid session tokens of the user, when there is one in
 * {@code _entities.js_login_session_token_attempts}. Returned by {@code JsEntityLoginSessionTokenAttempts.resetAttempts}.
 */
public class JsBusinessResetSessionTokenAttempts implements CcpBusiness {

	/** The only instance: the business keeps no state. */
	public final static JsBusinessResetSessionTokenAttempts INSTANCE = new JsBusinessResetSessionTokenAttempts();

	private JsBusinessResetSessionTokenAttempts() {
	}

	/**
	 * Deletes the counter.
	 * @param json the input, holding the counter under {@code _entities}
	 * @return the input
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation record = json.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JsEntityLoginSessionTokenAttempts.ENTITY);

		boolean noAttemps = record.isEmpty();

		if(noAttemps) {
			return json;
		}

		JsEntityLoginSessionTokenAttempts.ENTITY.delete(record);
		return json;
	}
}
