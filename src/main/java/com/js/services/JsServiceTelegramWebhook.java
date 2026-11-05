package com.js.services;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.bots.JnBusinessHandleBotUpdate;
import com.jn.mensageria.JnFunctionMensageriaSender;
import com.jn.services.JnService;
import com.js.business.telegram.JsBusinessValidateTelegramWebhookSecret;

/**
 * Service of the webhook of Telegram: the way the bots receive their messages in production. Locally they are read by
 * polling ({@code JbInstantMessengerReaderStarter}), since Telegram does not reach a developer machine. Each item takes
 * its input rules from the class of the same name in this package (see {@code JnService}).
 */
public enum JsServiceTelegramWebhook implements JnService {

	/**
	 * Receives an update pushed by Telegram: refuses a request without the secret of the bot and publishes the update to
	 * the messaging (topic {@code JnBusinessHandleBotUpdate}), whose consumer hands it to the bot. The request does no
	 * more than that: the commands of a bot may run complex queries, and since the bots will also serve the public, not
	 * only the support team, running them here would break OQL (until 2026-11-04 the bot ran inside this request).
	 * The consumer of the topic has a single thread, so the updates of a bot are handled one at a time, as they were here.
	 */
	ReceiveTelegramUpdate{
		/**
		 * Validates the secret and publishes the update.
		 * @param json the update, with {@code botName} and {@code webhookSecret}
		 * @return an empty JSON: Telegram reads only the status
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			JsBusinessValidateTelegramWebhookSecret.INSTANCE.execute(json);
			// from here on the JSON reaches the messaging and the logs of the bot and of the errors, and the secret must not
			CcpJsonRepresentation update = json.removeFields(JsBusinessValidateTelegramWebhookSecret.Fields.webhookSecret);
			JnFunctionMensageriaSender handleBotUpdate = new JnFunctionMensageriaSender(JnBusinessHandleBotUpdate.INSTANCE);
			handleBotUpdate.execute(update);
			return CcpOtherConstants.EMPTY_JSON;
		}
	},
	;
}
