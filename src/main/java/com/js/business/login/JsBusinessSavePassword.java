package com.js.business.login;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerDelete;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerSave;
import com.ccp.especifications.db.bulk.handlers.CcpEntityBulkHandlerTransferRecordToTwinEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;

import java.util.Arrays;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.db.bulk.handlers.JnBulkHandlerRegisterLogin;
import com.jn.entities.JnEntityLoginPassword;
import com.js.entities.JsEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginTokenAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.js.services.JsServiceLogin;
import com.jn.utils.JnDeleteKeysFromCache;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;

/**
 * Saves (or changes) the password of the user, in one bulk operation: saves the new password, moves a locked password
 * back from the twin, deletes the wrong password attempts and registers a new session as the current one of the e-mail,
 * which ends every other session of it (see {@code JnBusinessCheckCurrentSession}).
 */
public class JsBusinessSavePassword implements CcpBusiness {
 
	/** The single instance. */
	public static final JsBusinessSavePassword INSTANCE = new JsBusinessSavePassword();
	
	/** Singleton; use {@link #INSTANCE}. */
	private JsBusinessSavePassword() {}

	/**
	 * Runs the password bulk operation; {@code sessionToken} becomes the {@code token} of the session.
	 * @param json the request
	 * @return an empty JSON
	 */
	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpEntityBulkHandlerTransferRecordToTwinEntity executeLogout = new CcpEntityBulkHandlerTransferRecordToTwinEntity(JnEntityLoginSessionValidation.ENTITY, x -> Arrays.asList());
		
		CcpEntity twinEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		CcpEntityBulkHandlerTransferRecordToTwinEntity registerPasswordUnlock = new CcpEntityBulkHandlerTransferRecordToTwinEntity(twinEntity, x -> Arrays.asList());
		CcpBulkHandlerDelete removePasswordAttempts = new CcpBulkHandlerDelete(JsEntityLoginPasswordAttempts.ENTITY);
		// the token was right: its wrong attempts start over (until 2026-10-08 they were kept, and after a successful use
		// one more wrong token was enough to lock it again)
		CcpBulkHandlerDelete removeTokenAttempts = new CcpBulkHandlerDelete(JnEntityLoginTokenAttempts.ENTITY);

		CcpJsonRepresentation jsonWithTokenField = json.renameField(CcpJsonCommonsFields.sessionToken, JnEntityLoginSessionValidation.Fields.token);
		CcpBulkHandlerSave updatePassword = new CcpBulkHandlerSave(JnEntityLoginPassword.ENTITY);
		JnExecuteBulkOperation.INSTANCE
		.executeSelectUnionAllThenExecuteBulkOperation(
				jsonWithTokenField
				, JnDeleteKeysFromCache.INSTANCE
				, updatePassword
				, registerPasswordUnlock
				, removePasswordAttempts
				, removeTokenAttempts
				, executeLogout
				// registers the new session as the current one of the e-mail (the create becomes an update when there is
				// one), which ends every older session; until 2026-10-11 JnBulkHandlerSolveLoginConflict came next and
				// deleted that record, so a session opened here was not a current login and a second login got in without 409
				, JnBulkHandlerRegisterLogin.INSTANCE
				);
		
		return CcpOtherConstants.EMPTY_JSON;
	}

	/**
	 * Validates the input with the rules of the {@code SavePassword} service.
	 * @return the validation class of {@code JsServiceLogin.SavePassword}
	 */
	public Class<?> getJsonValidationClass() {
		var jsonValidationClass = JsServiceLogin.SavePassword.getJsonValidationClass();
		return jsonValidationClass;
	}
	
}
