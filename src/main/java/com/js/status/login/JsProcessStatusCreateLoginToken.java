package com.js.status.login;

import com.ccp.process.CcpProcessStatus;

/** Statuses of the creation and sending of the login token. */
public enum JsProcessStatusCreateLoginToken implements CcpProcessStatus{
	/** Status 422: the message can not be sent to this e-mail. */
	statusCanNotSendThisMessage(422),
	/** Status 429: the token was already sent recently. */
	statusAlreadySentToken(429),
	/** Status 400: the e-mail is invalid. */
	statusInvalidEmail(400),
	/** Status 404: the e-mail has no login token yet. */
	statusMissingEmail(404),
	/** Status 201: the user has not answered the onboarding questions yet. */
	missingSaveAnswers(201),
	/** Status 403: the login token is locked. */
	statusLockedToken(403),
	/** Status 200: success. */
	expectedStatus(200),
	;

	/** The HTTP status code. */
	public final int status;
	
	
	
	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JsProcessStatusCreateLoginToken(int status) {
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
