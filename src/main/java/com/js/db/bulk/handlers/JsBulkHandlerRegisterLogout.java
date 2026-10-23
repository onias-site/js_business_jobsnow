package com.js.db.bulk.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.handlers.CcpEntityBulkHandlerTransferRecordToTwinEntity;
import com.ccp.especifications.db.crud.CcpHandleWithSearchResultsInTheEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.db.bulk.JsBulkItemsLoginHistory;
import com.js.entities.JsEntityLoginSessionValidation;

/**
 * Bulk handler that registers the logout: transfers the active session to the twin entity
 * {@code js_login_session_terminated} (invalidating it) and adds the logout to the history in the group of the e-mail
 * ({@code JsBulkItemsLoginHistory.LOGOUT}); a session not found writes nothing.
 */
public class JsBulkHandlerRegisterLogout implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>> {

	/** The single instance. */
	public static final JsBulkHandlerRegisterLogout INSTANCE = new JsBulkHandlerRegisterLogout();

	/** The transfer of the session to the twin entity. */
	private final CcpEntityBulkHandlerTransferRecordToTwinEntity transferToTheTwin = new CcpEntityBulkHandlerTransferRecordToTwinEntity(JsEntityLoginSessionValidation.ENTITY, x -> Arrays.asList());

	private JsBulkHandlerRegisterLogout() {}

	/**
	 * Returns the items of the transfer of the session and of the history of the logout.
	 * @param json the logout, already transformed
	 * @param recordFound the session found
	 * @return the bulk items
	 */
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {
		List<CcpBulkItem> transferItems = this.transferToTheTwin.whenRecordWasFoundInTheEntitySearch(json, recordFound);
		List<CcpBulkItem> logoutHistory = JsBulkItemsLoginHistory.LOGOUT.apply(json);
		List<CcpBulkItem> allBulkItems = new ArrayList<>();
		allBulkItems.addAll(transferItems);
		allBulkItems.addAll(logoutHistory);
		return allBulkItems;
	}

	/**
	 * Delegates the session not found to the transfer, which writes nothing.
	 * @param json the logout, already transformed
	 * @return the bulk items of the transfer
	 */
	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		List<CcpBulkItem> notFoundItems = this.transferToTheTwin.whenRecordWasNotFoundInTheEntitySearch(json);
		return notFoundItems;
	}

	/**
	 * The entity searched: {@code js_login_session_validation}.
	 * @return the entity
	 */
	public CcpEntity getEntityToSearch() {
		return JsEntityLoginSessionValidation.ENTITY;
	}
}
