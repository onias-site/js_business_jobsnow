package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.js.db.bulk.handlers.JsBulkHandlerRegisterLogout;
import com.js.entities.JsEntityLoginSessionConflict;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Logs the user out. In an atomic bulk operation: transfers the active session to the
 * twin entity js_login_session_terminated (invalidating the session), adds the logout to the history in
 * the group of the e-mail ({@code JsBulkHandlerRegisterLogout}) and removes any session conflict record.
 */
public class JsBusinessExecuteLogout implements CcpBusiness{
		

	/** The single instance. */
	public static final JsBusinessExecuteLogout INSTANCE = new JsBusinessExecuteLogout();
	
	/** Singleton; use {@link #INSTANCE}. */
	private JsBusinessExecuteLogout() {}
	
	/**
	 * Moves the session to the twin (terminated) and deletes the session conflict, in one bulk operation.
	 * @param json the session
	 * @return the same JSON
	 */
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpBulkHandlerDelete deleteLoginSessionConflict = new CcpBulkHandlerDelete(JsEntityLoginSessionConflict.ENTITY);
		JnExecuteBulkOperation.INSTANCE.
		executeSelectUnionAllThenExecuteBulkOperation(
				json
				,JnDeleteKeysFromCache.INSTANCE
				, JsBulkHandlerRegisterLogout.INSTANCE
				, deleteLoginSessionConflict
				);
		
		return json;
	}

}
