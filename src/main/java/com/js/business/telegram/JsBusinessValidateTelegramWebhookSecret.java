package com.js.business.telegram;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.js.status.telegram.JsProcessStatusTelegramWebhook;

/**
 * Refuses a request to the webhook that did not come with the secret of the bot: the URL of the webhook is public, and
 * whoever posts an update to it speaks as any user of the bot, including the ones allowed to the support commands.
 */
public class JsBusinessValidateTelegramWebhookSecret implements CcpBusiness {

	/** The fields of the request. */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code webhookSecret} field: the secret that came in the request. */
		webhookSecret
	}

	/** The single instance. */
	public static final JsBusinessValidateTelegramWebhookSecret INSTANCE = new JsBusinessValidateTelegramWebhookSecret();

	private JsBusinessValidateTelegramWebhookSecret() {}

	/**
	 * Compares the secret of the request with the one of the bot. A bot without a configured secret refuses everything.
	 * @param json the request
	 * @return the same JSON
	 * @throws CcpErrorFlowDisturb with {@code invalidWebhookSecret} when the secret is not the one of the bot
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String botName = json.getAsString(JnJsonInstantMessengerFields.botName);
		String informedSecret = json.getAsString(Fields.webhookSecret);
		String botSecret = JsTelegramWebhookSecret.INSTANCE.getSecret(botName);
		CcpStringDecorator botSecretDecorator = new CcpStringDecorator(botSecret);
		CcpHashDecorator comparableSecret = botSecretDecorator.hash();
		boolean botHasNoSecret = botSecret.isEmpty();
		boolean secretIsWrong = false == comparableSecret.isTheSameSecret(informedSecret);

		if(botHasNoSecret || secretIsWrong) {
			CcpErrorFlowDisturb invalidSecret = new CcpErrorFlowDisturb(JsProcessStatusTelegramWebhook.invalidWebhookSecret);
			throw invalidSecret;
		}

		return json;
	}
}
