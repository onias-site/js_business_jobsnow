package com.js.business.bots.login.resend;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.business.bots.JnBotConfigurationSeeds;
import com.jn.business.messages.JnMessageType;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;
import com.js.business.bots.JsCommandNamesInPortuguese;
import com.js.business.bots.JsUserBotCommands;
import com.js.status.login.JsProcessStatusUnlockLoginToken;

/**
 * Configuration of {@code /resendLoginToken <email>} in the user bot (since 2026-11-12): one step, which makes the
 * request of the login screen and answers with its outcome, in the texts of that screen. Seeded by
 * {@code JsEntityLoginTokenRequestResend}.
 */
public final class JsUserResendLoginTokenBotConfiguration {

	private JsUserResendLoginTokenBotConfiguration() {}

	/**
	 * The seed records of the command.
	 * @return the bulk items
	 */
	public static List<CcpBulkItem> getSeeds() {
		String commandName = JsUserBotCommands.resendLoginToken.name();
		// every request not made ends the session with its reply: invalid e-mail (400), no token (404), already requested
		// (409) or already resent (429)
		CcpJsonRepresentation[] botReply = JnEntityBotCommandStep.getBotReplyInEveryLanguage();
		int invalidEmail = CcpProcessStatusDefault.BAD_REQUEST.asNumber();
		int tokenNotFound = JsProcessStatusUnlockLoginToken.statusTokenNotExists.asNumber();
		int alreadyRequested = JsProcessStatusUnlockLoginToken.statusAlreadyRequested.asNumber();
		int alreadyResent = JsProcessStatusUnlockLoginToken.statusTokenAlredyResent.asNumber();
		CcpJsonRepresentation invalidEmailFlow = JnEntityBotCommandStep.getStepFlow(invalidEmail, "", botReply);
		CcpJsonRepresentation tokenNotFoundFlow = JnEntityBotCommandStep.getStepFlow(tokenNotFound, "", botReply);
		CcpJsonRepresentation alreadyRequestedFlow = JnEntityBotCommandStep.getStepFlow(alreadyRequested, "", botReply);
		CcpJsonRepresentation alreadyResentFlow = JnEntityBotCommandStep.getStepFlow(alreadyResent, "", botReply);
		CcpJsonRepresentation step = JnEntityBotCommandStep.getBotCommandStep(commandName, JsUserResendLoginToken.class, "", invalidEmailFlow,
				tokenNotFoundFlow, alreadyRequestedFlow, alreadyResentFlow);
		List<CcpJsonRepresentation> endMessages = JnEntityBotCommandStepEndMessage.getBotReplyEndMessages(commandName);
		String email = "{" + JnJsonCommonsFields.email + "}";
		String dateItWasSaved = "{" + JnJsonCommonsFields.dateItWasSaved + "}";
		String expirationDate = "{" + JnJsonCommonsFields.expirationDate + "}";
		JnBotConfigurationSeeds seeds = new JnBotConfigurationSeeds(JnMessageType.JnBotType.user)
				.command(commandName, JnJsonCommonsFields.email)
				.portugueseNames(JsCommandNamesInPortuguese.resendLoginToken)
				.steps(step)
				.endMessages(endMessages)
				.commandExplanation(commandName, JnLanguage.portuguese, "Pede o reenvio do token de login ao e-mail informado, para quem não recebeu ou perdeu o token original. Uso: /resendLoginToken seu@email.com")
				.commandExplanation(commandName, JnLanguage.english, "Asks to resend the login token to the e-mail informed, for whoever did not receive or lost the original token. Usage: /resendLoginToken your@email.com")
				.systemMessage(JsUserResendLoginTokenMessages.requested
						, "A solicitação de reenvio do token para o e-mail '" + email + "' foi efetuada com sucesso, por favor, verifique a caixa de entrada, spam / lixo eletrônico deste e-mail para localizar o token que enviamos."
						, "The request to resend the token to the e-mail '" + email + "' was made successfully, please check the inbox and the spam / junk folder of this e-mail to find the token we sent.")
				.systemMessage(JsUserResendLoginTokenMessages.alreadyRequested
						, "A solicitação de reenvio do token para o e-mail '" + email + "' já foi feita na data " + dateItWasSaved + ", se necessário, poderá ser refeita na data " + expirationDate
						, "The request to resend the token to the e-mail '" + email + "' was already made on " + dateItWasSaved + "; if needed, it can be made again on " + expirationDate)
				.systemMessage(JsUserResendLoginTokenMessages.alreadyResent
						, "A solicitação de reenvio do token para o e-mail '" + email + "' já foi resolvida na data " + dateItWasSaved + ", por favor, verifique a caixa de entrada, spam / lixo eletrônico deste e-mail para localizar o e-mail com instruções. Caso a mensagem não tenha chegado, envie-nos um e-mail ao endereço onias85@gmail.com ou se preferir, aguarde até a data " + expirationDate + " para refazer a sua solicitação de reenvio do token, que reenviaremos o token a este e-mail."
						, "The request to resend the token to the e-mail '" + email + "' was already solved on " + dateItWasSaved + ", please check the inbox and the spam / junk folder of this e-mail to find the e-mail with instructions. If the message did not arrive, send us an e-mail at onias85@gmail.com or, if you prefer, wait until " + expirationDate + " to make your request again, and we will resend the token to this e-mail.")
				.systemMessage(JsUserResendLoginTokenMessages.tokenNotFound
						, "O e-mail '" + email + "' não tem token de login. Para receber um, entre no Onias.Site com este e-mail."
						, "The e-mail '" + email + "' has no login token. To get one, log in to Onias.Site with this e-mail.")
				.systemMessage(JsUserResendLoginTokenMessages.invalidEmail
						, "O e-mail '" + email + "' informado é inválido, por favor, informe um e-mail válido. Uso: /resendLoginToken seu@email.com"
						, "The e-mail '" + email + "' is invalid, please inform a valid e-mail. Usage: /resendLoginToken your@email.com");
		List<CcpBulkItem> bulkItems = seeds.getBulkItems();
		return bulkItems;
	}
}
