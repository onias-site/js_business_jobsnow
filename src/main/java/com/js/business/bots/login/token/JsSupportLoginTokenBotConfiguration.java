package com.js.business.bots.login.token;

import java.util.ArrayList;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.business.bots.JnBotConfigurationSeeds;
import com.jn.business.messages.JnMessageType;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;
import com.js.business.bots.JsCommandNamesInPortuguese;
import com.js.business.bots.JsSupportBotCommands;

/**
 * The configuration of the {@code solveLoginTokenTicket} command of the support bot, seeded in the bot entities of jn:
 * {@code /solveLoginTokenTicket <ticketType> <email>}, with its explanation; without an open ticket of the type, the
 * session ends with a notice to the operator. Also seeds the templates of the notices that tell the support team about
 * a new ticket, one per ticket type.
 */
public final class JsSupportLoginTokenBotConfiguration {

	private JsSupportLoginTokenBotConfiguration() {}

	/**
	 * The seeds of the command and of the notices of its tickets.
	 * @return the seed records
	 */
	public static List<CcpBulkItem> getSeeds() {
		String commandName = JsSupportBotCommands.solveLoginTokenTicket.name();
		String email = "{" + JnJsonCommonsFields.email + "}";
		String ticketType = "{" + JsSupportLoginToken.JsonFields.ticketType + "}";

		CcpJsonRepresentation portuguese = JnEntityBotCommandStep.getStepFlowMessage(JnLanguage.portuguese, "O e-mail '" + email + "' não possui aberto ticket do tipo '" + ticketType + "'");
		CcpJsonRepresentation english = JnEntityBotCommandStep.getStepFlowMessage(JnLanguage.english, "The e-mail '" + email + "' has no open ticket of the type '" + ticketType + "'");
		CcpJsonRepresentation spanish = JnEntityBotCommandStep.getStepFlowMessage(JnLanguage.spanish, "El correo '" + email + "' no tiene ticket abierto del tipo '" + ticketType + "'");
		CcpJsonRepresentation stepFlow = JnEntityBotCommandStep.getStepFlow(CcpProcessStatusDefault.NOT_FOUND.status, "", portuguese, english, spanish);
		CcpJsonRepresentation step = JnEntityBotCommandStep.getBotCommandStep(commandName, JsSupportLoginToken.class, "", stepFlow);

		JnBotConfigurationSeeds seeds = new JnBotConfigurationSeeds(JnMessageType.JnBotType.support)
				.command(commandName, JsSupportLoginToken.JsonFields.ticketType, JnJsonCommonsFields.email)
				.portugueseNames(JsCommandNamesInPortuguese.solveLoginTokenTicket)
				.steps(step)
				.commandExplanation(commandName, JnLanguage.portuguese, "Quando o usuário alega que seu token de login está bloqueado ou não foi enviado, ele abre uma solicitação que é recebida pelo time de suporte, então o operador de suporte evoca este comando para solucionar a solicitação que o usuário abriu")
				.commandExplanation(commandName, JnLanguage.english, "When the user claims that their login token is locked or was not sent, they open a request that the support team receives, then the support operator invokes this command to solve the request the user opened");
		List<CcpBulkItem> botItems = seeds.getBulkItems();
		List<CcpBulkItem> allSeeds = new ArrayList<>(botItems);

		JsSupportLoginTokenTypes[] ticketTypes = JsSupportLoginTokenTypes.values();
		for (JsSupportLoginTokenTypes type : ticketTypes) {
			List<CcpBulkItem> noticeTemplate = type.getInstantMessageTemplate();
			allSeeds.addAll(noticeTemplate);
		}
		return allSeeds;
	}
}
