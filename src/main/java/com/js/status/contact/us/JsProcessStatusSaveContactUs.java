package com.js.status.contact.us;

import com.ccp.process.CcpProcessStatus;

/** Statuses of the ticket of the "contact us" refused before being saved. */
public enum JsProcessStatusSaveContactUs implements CcpProcessStatus{
	/** Status 403: the support team chose to ignore the user. */
	userNotAllowed(403),
	/** Status 429: the user already spent, in the last 24 hours, the daily quota of the group of the {@code type}. */
	dailyLimitReached(429),
	;

	/** The HTTP status code. */
	public final int status;

	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JsProcessStatusSaveContactUs(int status) {
		this.status = status;
	}

	/**
	 * Returns the HTTP status code.
	 * @return the status code
	 */
	public int asNumber() {
		return this.status;
	}
}
