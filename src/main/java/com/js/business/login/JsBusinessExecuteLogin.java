package com.js.business.login;

import java.util.Arrays;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpEntityBulkHandlerTransferRecordToTwinEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.js.db.bulk.handlers.JsBulkHandlerRegisterLogin;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginPasswordAttempts;
import com.js.entities.JsEntityLoginSessionValidation;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Logs the user in after the password was validated, in one bulk operation: moves a locked password back from the twin
 * (unlock), deletes the wrong password attempts and registers the session (see {@code JsBulkHandlerRegisterLogin}).
 */
public class JsBusinessExecuteLogin implements CcpBusiness {
		

	/** The single instance. */
	public static final JsBusinessExecuteLogin INSTANCE = new JsBusinessExecuteLogin();
	
	/** Singleton; use {@link #INSTANCE}. */
	private JsBusinessExecuteLogin() {}
	
	/**
	 * Runs the login bulk operation; {@code sessionToken} becomes the {@code token} of the session.
	 * @param json the login request
	 * @return an empty JSON
	 */
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation jsonWithTokenField = json.renameField(CcpJsonCommonsFields.sessionToken, JsEntityLoginSessionValidation.Fields.token);
		
		CcpEntity twinEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		CcpEntityBulkHandlerTransferRecordToTwinEntity executeUnlock = new CcpEntityBulkHandlerTransferRecordToTwinEntity(twinEntity, x -> Arrays.asList());
		CcpEntity entityAttempts = JsEntityLoginPasswordAttempts.ENTITY;
		
		CcpBulkHandlerDelete removeAttempts = new CcpBulkHandlerDelete(entityAttempts);

		JnExecuteBulkOperation.INSTANCE.
		executeSelectUnionAllThenExecuteBulkOperation(
				jsonWithTokenField
				,JnDeleteKeysFromCache.INSTANCE
				, executeUnlock
				, removeAttempts
				, JsBulkHandlerRegisterLogin.INSTANCE
				);
		
		return CcpOtherConstants.EMPTY_JSON;
	}

}
