package com.js.business.telegram;

/** Raised when the webhook of a bot without a token in the system properties is registered or deleted. */
@SuppressWarnings("serial")
public class JsErrorTelegramBotHasNoToken extends RuntimeException {
	/**
	 * Names the bot.
	 * @param botName the bot
	 */
	JsErrorTelegramBotHasNoToken(String botName) {
		super("The bot '" + botName + "' has no token in the system properties");
	}
}
