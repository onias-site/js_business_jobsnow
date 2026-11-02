package com.js.status.telegram;

import com.ccp.process.CcpProcessStatus;

/** Statuses of a request refused by the webhook of Telegram. */
public enum JsProcessStatusTelegramWebhook implements CcpProcessStatus{
	/** Status 401: the request did not come with the secret given to Telegram, so it did not come from Telegram. */
	invalidWebhookSecret(401),
	;

	/** The HTTP status code. */
	public final int status;

	/**
	 * Associates the HTTP status code.
	 * @param status the HTTP status code
	 */
	private JsProcessStatusTelegramWebhook(int status) {
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
