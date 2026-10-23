package com.js.db.bulk.handlers;

import java.util.ArrayList;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.crud.CcpHandleWithSearchResultsInTheEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.db.bulk.JsBulkItemsLoginHistory;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.fields.transformers.JsJsonTransformersFieldEntityTokenHash;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.js.entities.JsEntityLoginTokenRequestUnlock;

/**
 * Bulk handler that registers the login: whether or not there was a session conflict, it creates the session conflict
 * record and the session record, deletes the pending requests to resend and to unlock the login token, and adds the
 * login to the history in the group of the e-mail ({@code JsBulkItemsLoginHistory.LOGIN}).
 */
public class JsBulkHandlerRegisterLogin implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>{

	
	/** Singleton; use {@link #INSTANCE}. */
	private JsBulkHandlerRegisterLogin() {}
	
	/** The single instance. */
	public static final JsBulkHandlerRegisterLogin INSTANCE = new JsBulkHandlerRegisterLogin();
	
	/**
	 * Returns the items that register the login.
	 * @param json the login
	 * @param recordFound the existing session conflict
	 * @return the bulk items
	 */
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {

		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}

	/**
	 * Builds the items that create the session conflict and the session, and delete the pending token requests.
	 * @param json the login
	 * @return the bulk items
	 */
	private List<CcpBulkItem> getBulkItems(CcpJsonRepresentation json) {
		// the current session record keeps the hash of the session token, computed here and not by a field transformer: this
		// json comes already transformed by JsEntityLoginSessionConflict (the entity searched), and a transformer there
		// would hand the session below a token already hashed, to be hashed again
		JsJsonTransformersFieldEntityTokenHash tokenHash = new JsJsonTransformersFieldEntityTokenHash();
		CcpJsonRepresentation jsonWithTokenHash = tokenHash.execute(json);
		String sessionTokenHash = jsonWithTokenHash.getAsString(JsEntityLoginSessionValidation.Fields.token);
		CcpJsonRepresentation jsonWithTheCurrentSession = json.put(JsEntityLoginSessionConflict.Fields.currentSessionTokenHash, sessionTokenHash);
		CcpJsonRepresentation session = JsEntityLoginSessionConflict.ENTITY.getHandledJson(jsonWithTheCurrentSession);
		var newSession = JsEntityLoginSessionConflict.ENTITY.toBulkItems(session, CcpBulkEntityOperationType.create);
		CcpJsonRepresentation jsonCopy = new CcpJsonRepresentation(json.content);
		CcpJsonRepresentation login = JsEntityLoginSessionValidation.ENTITY.getHandledJson(jsonCopy);
		var newLogin = JsEntityLoginSessionValidation.ENTITY.toBulkItems(login, CcpBulkEntityOperationType.create);

		var allBulkItems = new ArrayList<CcpBulkItem>();
		allBulkItems.addAll(newSession);
		allBulkItems.addAll(newLogin);
		List<CcpBulkItem> otherBulkItems = this.getOtherBulkItems(json, CcpBulkEntityOperationType.delete, JsEntityLoginTokenRequestResend.ENTITY, JsEntityLoginTokenRequestUnlock.ENTITY);
		allBulkItems.addAll(otherBulkItems);
		List<CcpBulkItem> loginHistory = JsBulkItemsLoginHistory.LOGIN.apply(json);
		allBulkItems.addAll(loginHistory);
		return allBulkItems;
	} 
	
	/**
	 * Builds the items of the operation in each entity.
	 * @param json the record
	 * @param operation the bulk operation
	 * @param entities the entities
	 * @return the bulk items
	 */
	private List<CcpBulkItem> getOtherBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation, CcpEntity... entities){
		List<CcpBulkItem> response = new ArrayList<CcpBulkItem>();
		for (CcpEntity entity : entities) {
			List<CcpBulkItem> bulkItems = entity.toBulkItems(json, operation);
			response.addAll(bulkItems);
		}
		return response;
	}

	/**
	 * Returns the items that register the login.
	 * @param json the login
	 * @return the bulk items
	 */
	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}

	/**
	 * The entity searched: {@code js_login_session_conflict}.
	 * @return the entity
	 */
	public CcpEntity getEntityToSearch() {
		return JsEntityLoginSessionConflict.ENTITY;
	}

}
