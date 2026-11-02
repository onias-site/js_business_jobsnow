package com.js.business.telegram;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Tells Telegram to stop pushing the updates of a bot, which goes back to being read by polling. The updates not delivered
 * yet are kept for the polling to read.
 */
public class JsBusinessDeleteTelegramWebhook implements CcpBusiness {

	/** The resources of the API. */
	enum Fields implements CcpJsonFieldName{
		/** The resource of the API. */
		deleteWebhook {
			public String getValue() {
				return "/deleteWebhook";
			}
		},
	}

	/** The single instance. */
	public static final JsBusinessDeleteTelegramWebhook INSTANCE = new JsBusinessDeleteTelegramWebhook();

	private JsBusinessDeleteTelegramWebhook() {}

	/**
	 * Deletes the webhook of the bot.
	 * @param json {@code botName}
	 * @return the answer of Telegram
	 * @throws JsErrorTelegramBotHasNoToken when the bot has no token
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String botName = json.getAsString(JnJsonInstantMessengerFields.botName);
		String deleteWebhook = Fields.deleteWebhook.getValue();
		CcpHttpHandler httpHandler = JsBusinessRegisterTelegramWebhook.getHttpHandler(botName, deleteWebhook);
		CcpJsonRepresentation response = httpHandler.executeHttpRequest(this.name(), CcpHttpMethods.POST, CcpOtherConstants.EMPTY_JSON, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);
		return response;
	}
}
