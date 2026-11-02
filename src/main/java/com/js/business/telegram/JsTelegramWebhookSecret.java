package com.js.business.telegram;

import com.ccp.decorators.CcpJsonFieldName;
import com.jn.utils.JnSystemProperties;

/**
 * The secret that Telegram sends in every request to the webhook of a bot: the {@code telegramWebhookSecrets} system
 * property, a JSON with one secret per bot. It has 1 to 256 of the characters A-Z, a-z, 0-9, {@code _} and {@code -}.
 * <p>
 * It is a value of its own, not related to the token of the bot, because the two protect different things: the token
 * lets the system speak as the bot, and the secret lets a request speak as a user of the bot, including the ones allowed
 * to the support commands. Whoever gets one of them must not get the other. A new secret requires registering the webhook
 * again.
 */
public class JsTelegramWebhookSecret {

	/** The system properties. */
	enum Fields implements CcpJsonFieldName{
		/** The secrets of the webhooks, by bot. */
		telegramWebhookSecrets
	}

	/** The single instance. */
	public static final JsTelegramWebhookSecret INSTANCE = new JsTelegramWebhookSecret();

	private JsTelegramWebhookSecret() {}

	/**
	 * Returns the secret of the bot.
	 * @param botName the bot
	 * @return the secret, or an empty text when the bot has none
	 */
	public String getSecret(String botName) {
		String secret = JnSystemProperties.INSTANCE.getSystemInnerProperty(Fields.telegramWebhookSecrets, () -> botName);
		return secret;
	}
}
