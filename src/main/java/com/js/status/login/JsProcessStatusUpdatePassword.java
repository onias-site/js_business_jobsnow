package com.js.status.login;

import com.ccp.process.CcpProcessStatus;

/** Statuses of the definition (or change) of the password with the login token. */
public enum JsProcessStatusUpdatePassword implements CcpProcessStatus{
	/** Status 400: the e-mail is invalid. */
	invalidEmail(400),
	/** Status 403: the login token is locked. */
	lockedToken(403),
	/** Status 404: the e-mail has no login token yet. */
	missingEmail(404),
	/** Status 404: the user has no login token. */
	missingToken(404),
	/** Status 201: the user has not answered the pre-registration yet. */
	missingSaveAnswers(201),
	/** Status 427: the token is wrong. */
	wrongToken(427),
	/** Status 422: the request is invalid. */
	invalidJson(422),
	/** Status 429: the token was locked by this attempt. */
	tokenLockedRecently(429),
	/** Status 200: success. */
	expectedStatus(200),
	;

	/** The HTTP status code. */
	public final int status;
	
	
	
	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JsProcessStatusUpdatePassword(int status) {
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
