package com.js.services;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.bots.JnBusinessHandleBotUpdate;
import com.jn.services.JnService;
import com.js.business.telegram.JsBusinessValidateTelegramWebhookSecret;

/**
 * Service of the webhook of Telegram: the way the bots receive their messages in production. Locally they are read by
 * polling ({@code JbInstantMessengerReaderStarter}), since Telegram does not reach a developer machine. Each item takes
 * its input rules from the class of the same name in this package (see {@code JnService}).
 */
public enum JsServiceTelegramWebhook implements JnService {

	/**
	 * Receives an update pushed by Telegram: refuses a request without the secret of the bot and hands the update to the
	 * bot ({@code JnBusinessHandleBotUpdate}), in this same request. The bot runs here and not through the messaging
	 * because it handles the messages of a chat one at a time, in order, which the messaging does not guarantee.
	 */
	ReceiveTelegramUpdate{
		/**
		 * Validates the secret and handles the update.
		 * @param json the update, with {@code botName} and {@code webhookSecret}
		 * @return an empty JSON: Telegram reads only the status
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			JsBusinessValidateTelegramWebhookSecret.INSTANCE.execute(json);
			// from here on the JSON may reach the logs of the bot and of the errors, and the secret must not
			CcpJsonRepresentation update = json.removeFields(JsBusinessValidateTelegramWebhookSecret.Fields.webhookSecret);
			JnBusinessHandleBotUpdate.INSTANCE.execute(update);
			return CcpOtherConstants.EMPTY_JSON;
		}
	},
	;
}
