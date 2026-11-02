package com.js.business.telegram;

import java.util.Arrays;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.jn.business.messages.JnMessageType;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnSystemProperties;

/**
 * Tells Telegram where to push the updates of a bot. From then on {@code getUpdates} answers 409 for that bot, so a
 * reading by polling needs another bot, or {@link JsBusinessDeleteTelegramWebhook} first.
 * <p>
 * The webhook takes only the updates with a message, which is all the bot reads, and a single connection: the bot was made
 * for one message at a time in each chat, as in the polling, and it is that single connection that makes Telegram deliver
 * the updates in order.
 */
public class JsBusinessRegisterTelegramWebhook implements CcpBusiness {

	/** The fields of the {@code setWebhook} request. */
	public enum Fields implements CcpJsonFieldName{
		/** The secret Telegram sends back in every request to the webhook. */
		secret_token,
		/** The kinds of update delivered. */
		allowed_updates,
		/** How many simultaneous connections Telegram opens. */
		max_connections,
		/** The resource of the API. */
		setWebhook {
			public String getValue() {
				return "/setWebhook";
			}
		},
		/** The path of the webhook in the API, before the name of the bot. */
		webhookPath {
			public String getValue() {
				return "/telegram-webhook/";
			}
		},
	}

	/** The single instance. */
	public static final JsBusinessRegisterTelegramWebhook INSTANCE = new JsBusinessRegisterTelegramWebhook();

	/** One connection: the updates come one at a time, in order. */
	private static final Integer SINGLE_CONNECTION = 1;

	private JsBusinessRegisterTelegramWebhook() {}

	/**
	 * Registers the webhook of the bot.
	 * @param json {@code botName} and {@code url}, the public HTTPS address of this API
	 * @return the answer of Telegram
	 * @throws JsErrorTelegramBotHasNoWebhookSecret when the bot has no secret, since a webhook without one accepts anybody
	 * @throws JsErrorTelegramBotHasNoToken when the bot has no token
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String botName = json.getAsString(JnJsonInstantMessengerFields.botName);
		String secret = JsTelegramWebhookSecret.INSTANCE.getSecret(botName);
		boolean botHasNoSecret = secret.isEmpty();

		if(botHasNoSecret) {
			JsErrorTelegramBotHasNoWebhookSecret jsErrorTelegramBotHasNoWebhookSecret = new JsErrorTelegramBotHasNoWebhookSecret(botName);
			throw jsErrorTelegramBotHasNoWebhookSecret;
		}

		String apiAddress = json.getAsString(JnJsonCommonsFields.url);
		String webhookPath = Fields.webhookPath.getValue();
		String webhookUrl = apiAddress + webhookPath + botName;
		String messageUpdates = JnJsonInstantMessengerFields.message.name();
		List<String> allowedUpdates = Arrays.asList(messageUpdates);

		CcpJsonRepresentation body = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.url, webhookUrl)
				.put(Fields.secret_token, secret)
				.put(Fields.allowed_updates, allowedUpdates)
				.put(Fields.max_connections, SINGLE_CONNECTION)
				;

		String setWebhook = Fields.setWebhook.getValue();
		CcpHttpHandler httpHandler = getHttpHandler(botName, setWebhook);
		CcpJsonRepresentation response = httpHandler.executeHttpRequest(this.name(), CcpHttpMethods.POST, CcpOtherConstants.EMPTY_JSON, body, CcpHttpResponseType.singleRecord);
		return response;
	}

	/**
	 * Builds the HTTP handler of a resource of the bot, which accepts only the status 200.
	 * @param botName the bot
	 * @param resource the resource
	 * @return the handler
	 * @throws JsErrorTelegramBotHasNoToken when the bot has no token
	 */
	static CcpHttpHandler getHttpHandler(String botName, String resource) {
		String botToken = JnSystemProperties.INSTANCE.getSystemInnerProperty(JnMessageType.InstantMessengerApiFields.bots, () -> botName);
		boolean botHasNoToken = botToken.isEmpty();

		if(botHasNoToken) {
			JsErrorTelegramBotHasNoToken jsErrorTelegramBotHasNoToken = new JsErrorTelegramBotHasNoToken(botName);
			throw jsErrorTelegramBotHasNoToken;
		}

		String apiAddress = JnSystemProperties.INSTANCE.urlInstantMessengerKey();
		String url = apiAddress + botToken + resource;
		CcpHttpHandler httpHandler = new CcpHttpHandler(200, url);
		return httpHandler;
	}
}
