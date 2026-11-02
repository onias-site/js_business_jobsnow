package com.js.business.telegram;

/** Raised when the webhook of a bot without a secret in {@code telegramWebhookSecrets} is registered. */
@SuppressWarnings("serial")
public class JsErrorTelegramBotHasNoWebhookSecret extends RuntimeException {
	/**
	 * Names the bot.
	 * @param botName the bot
	 */
	JsErrorTelegramBotHasNoWebhookSecret(String botName) {
		super("The bot '" + botName + "' has no secret in the system property telegramWebhookSecrets");
	}
}
