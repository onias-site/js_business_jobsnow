package com.js.status.login;

import com.ccp.process.CcpProcessStatus;

/** Statuses of the requests to unlock and to resend the login token. */
public enum JsProcessStatusUnlockLoginToken implements CcpProcessStatus{
	/** Status 404: the token is not locked. */
	statusTokenNotLocked(404),
	/** Status 404: the token does not exist. */
	statusTokenNotExists(404),
	/** Status 409: the request was already made and is waiting for support. */
	statusAlreadyRequested(409),
	/** Status 429: the token was already resent. */
	statusTokenAlredyResent(429), 
	/** Status 429: the token was already unlocked. */
	statusTokenAlredyUnlocked(429), 
	;

	/** The HTTP status code. */
	public final int status;
	
	
	
	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JsProcessStatusUnlockLoginToken(int status) {
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
