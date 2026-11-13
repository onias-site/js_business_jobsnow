package com.js.business.bots.login.resend;

import com.jn.messages.JnSystemMessage;

/**
 * The answers of {@code /resendLoginToken <email>} in the user bot (since 2026-11-12), the same ones the login screen
 * shows for each answer of the request, seeded by {@link JsUserResendLoginTokenBotConfiguration}. Parameters:
 * {@code {email}}, and {@code {dateItWasSaved}} and {@code {expirationDate}} of the request already made.
 */
public enum JsUserResendLoginTokenMessages implements JnSystemMessage {
	/** The request was made (200). */
	requested,
	/** The request was already made and waits for the support team (409). */
	alreadyRequested,
	/** The token was already resent (429). */
	alreadyResent,
	/** The e-mail has no login token (404). */
	tokenNotFound,
	/** The e-mail is not valid (400). */
	invalidEmail,
	;
}
