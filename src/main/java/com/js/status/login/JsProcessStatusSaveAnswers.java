package com.js.status.login;

import com.ccp.process.CcpProcessStatus;

/** Statuses of the saving of the onboarding answers. */
public enum JsProcessStatusSaveAnswers implements CcpProcessStatus{
	/** Status 400: the e-mail is invalid. */
	invalidEmail(400),
	/** Status 403: the login token is locked. */
	lockedToken(403),
	/** Status 404: the user has no login token. */
	missingToken(404),
	/** Status 423: the password is locked (427 until 2026-10-14, the status of the wrong password in the login). */
	lockedPassword(423),
	/** Status 409: the user already has an open session. */
	loginConflict(409),
	/** Status 202: the user has not defined a password yet. */
	missingPassword(202),
	/** Status 200: success. */
	expectedStatus(200),
	;

	/** The HTTP status code. */
	public final int status;
	
	
	
	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JsProcessStatusSaveAnswers(int status) {
		this.status = status;
	}

	/**
	 * Returns the HTTP status code.
	 * @return the status code
	 */
	public int asNumber() {
		return status;
	}
}
