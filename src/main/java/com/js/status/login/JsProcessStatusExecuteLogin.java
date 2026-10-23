package com.js.status.login;

import com.ccp.business.CcpBusiness;
import com.ccp.flow.CcpBusinessThrowFlowDisturb;
import com.ccp.process.CcpProcessStatus;

/** Statuses of the login with password; {@link #flowDisturb()} interrupts the flow with one of them. */
public enum JsProcessStatusExecuteLogin implements CcpProcessStatus{
	/** Status 429: the password was locked by this attempt. */
	passwordLockedRecently(429),
	/** Status 401: the session token is missing. */
	missingSessionToken(401),
	/** Status 202: the user has not defined a password yet. */
	missingSavePassword(202),
	/** Status 201: the user has not answered the pre-registration yet. */
	missingSaveAnswers(201),
	/** Status 423: the password is locked. */
	lockedPassword(423),
	/** Status 200: success. */
	expectedStatus(200),
	/** Status 401: the session is invalid. */
	invalidSession(401),
	/** Status 427: the password is wrong. */
	wrongPassword(427),
	/** Status 409: the user already has an open session. */
	loginConflict(409),
	/** Status 400: the e-mail is invalid. */
	invalidEmail(400),
	/** Status 404: the e-mail has no login token yet. */
	missingSavingEmail(404),
	/** Status 422: the password is weak. */
	weakPassword(422),
	/** Status 403: the login token is locked. */
	lockedToken(403),
	;

	/** The HTTP status code. */
	public final int status;
	
	
	
	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JsProcessStatusExecuteLogin(int status) {
		this.status = status;
	}

	/**
	 * Returns the HTTP status code.
	 * @return the status code
	 */
	public int asNumber() {
		return status;
	}
	
	/**
	 * Builds a business that interrupts the flow with this status.
	 * @return a business that throws {@code CcpErrorFlowDisturb} with its input and this status
	 */
	public CcpBusiness flowDisturb() {
		
		CcpBusiness result = new CcpBusinessThrowFlowDisturb(this);
		return result;
	}
}
