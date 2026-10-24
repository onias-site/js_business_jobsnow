package com.js.business.bots.contact.us;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.jn.business.bots.JnBotConfigurationSeeds;
import com.jn.business.bots.command.allowed.JnSupportAllowCommandToUserBotConfiguration;
import com.jn.business.messages.JnMessageType;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.utils.JnLanguage;
import com.js.business.bots.JsCommandNamesInPortuguese;
import com.js.business.bots.JsSupportBotCommands;
import com.js.entities.JsEntityContactUs;

/**
 * Configuration of {@code /answerContactUs <type> <email>} in the support bot: shows the ticket of the "contact us", and
 * the operator answers it, leaves it open to go to the next ticket, or ignores the user (with a confirmation). Seeded by
 * {@code JsEntityContactUs}.
 */
public final class JsSupportContactUsBotConfiguration {

	private JsSupportContactUsBotConfiguration() {}

	/**
	 * The seed records of the command.
	 * @return the bulk items
	 */
	public static List<CcpBulkItem> getSeeds() {
		String commandName = JsSupportBotCommands.answerContactUs.name();
		String decide = JsSupportContactUsSteps.answerContactUsDecide.name();
		String confirmIgnore = JsSupportContactUsSteps.answerContactUsConfirmIgnore.name();

		CcpJsonRepresentation showStep = getShowStep(commandName, decide);
		CcpJsonRepresentation decideStep = getDecideStep(decide, confirmIgnore);
		CcpJsonRepresentation confirmIgnoreStep = getConfirmIgnoreStep(decide, confirmIgnore);
		List<CcpJsonRepresentation> endMessages = JnEntityBotCommandStepEndMessage.getBotReplyEndMessages(commandName, decide, confirmIgnore);

		// the e-mail goes last, the same order of the notice sent to the operator
		JnBotConfigurationSeeds seeds = new JnBotConfigurationSeeds(JnMessageType.JnBotType.support)
				.command(commandName, JsEntityContactUs.Fields.type, JsEntityContactUs.Fields.email)
				.portugueseNames(JsCommandNamesInPortuguese.answerContactUs)
				.steps(showStep, decideStep, confirmIgnoreStep)
				.endMessages(endMessages)
				.commandExplanation(commandName, JnLanguage.portuguese, "Quando o usuário abre um ticket no fale conosco (um bug, um elogio, uma crítica, uma sugestão, um comentário ou uma dúvida), o operador de suporte evoca este comando para responder o ticket por e-mail, deixá-lo aberto e ir para o próximo, ou ignorar o usuário")
				.commandExplanation(commandName, JnLanguage.english, "When the user opens a ticket in the contact us (a bug, a compliment, a criticism, a suggestion, a comment or a question), the support operator invokes this command to answer the ticket by e-mail, leave it open and go to the next one, or ignore the user")
				.systemMessage(JsSupportContactUsMessages.ticket
						, "Fale conosco de {email}\n\n"
						+ "Tipo: {typeDescription}\n"
						+ "Título: {title}\n"
						+ "Descrição: {description}\n"
						+ "Stack trace: {stackTrace}\n\n"
						+ "{options}"
						, "Contact us from {email}\n\n"
						+ "Type: {typeDescription}\n"
						+ "Title: {title}\n"
						+ "Description: {description}\n"
						+ "Stack trace: {stackTrace}\n\n"
						+ "{options}")
				.systemMessage(JsSupportContactUsMessages.options
						, "Responda com uma das opções:\n"
						+ "• responder <resposta> — envia a resposta ao usuário por e-mail e encerra o ticket\n"
						+ "• próximo — deixa este ticket aberto e vai para o próximo ticket pendente\n"
						+ "• ignorar — descarta os tickets do usuário e ignora as próximas solicitações dele em todos os comandos\n"
						+ "A resposta vai para o usuário e precisa ter de 10 a 500 caracteres."
						, "Answer with one of the options:\n"
						+ "• respond <answer> — sends the answer to the user by e-mail and closes the ticket\n"
						+ "• next — leaves this ticket open and goes to the next pending ticket\n"
						+ "• ignore — discards the tickets of the user and ignores their next requests in every command\n"
						+ "The answer goes to the user and needs 10 to 500 characters.")
				.systemMessage(JsSupportContactUsMessages.notUnderstood
						, "Não entendi a resposta. A resposta ao usuário precisa ter de 10 a 500 caracteres.\n\n{options}"
						, "I did not understand the answer. The answer to the user needs 10 to 500 characters.\n\n{options}")
				.systemMessage(JsSupportContactUsMessages.ticketAnswered
						, "O ticket ({typeDescription}) de {email} foi respondido. O usuário receberá a resposta por e-mail."
						, "The ticket ({typeDescription}) from {email} was answered. The user will get the answer by e-mail.")
				.systemMessage(JsSupportContactUsMessages.ticketSkipped
						, "O ticket ({typeDescription}) de {email} continua aberto."
						, "The ticket ({typeDescription}) from {email} stays open.")
				.systemMessage(JsSupportContactUsMessages.ignoreConfirmation
						, "Confirma que o usuário {email} será ignorado pelo suporte, em todos os comandos? "
						+ "Os tickets abertos dele serão descartados sem aviso ao usuário e as próximas solicitações não chegarão mais a você.\n"
						+ "Responda: sim ou não"
						, "Do you confirm that the user {email} will be ignored by the support, in every command? "
						+ "Their open tickets will be discarded without notifying the user and the next requests will no longer reach you.\n"
						+ "Answer: yes or no")
				.systemMessage(JsSupportContactUsMessages.ignoreConfirmationNotUnderstood
						, "Não entendi a resposta.\n\n{ignoreConfirmation}"
						, "I did not understand the answer.\n\n{ignoreConfirmation}")
				.systemMessage(JsSupportContactUsMessages.ignoreCanceled
						, "O usuário não será ignorado.\n\n{options}"
						, "The user will not be ignored.\n\n{options}")
				.systemMessage(JsSupportContactUsMessages.userIgnored
						, "O usuário {email} foi ignorado pelo suporte, em todos os comandos. Os tickets abertos dele foram descartados."
						, "The user {email} was ignored by the support, in every command. Their open tickets were discarded.");
		List<CcpBulkItem> bulkItems = seeds.getBulkItems();
		return bulkItems;
	}

	/**
	 * Shows the ticket and goes on to the decision. Without an open ticket the session ends with a notice to the operator;
	 * a user ignored by the support is not answered, and the operator is told how to stop ignoring them.
	 */
	private static CcpJsonRepresentation getShowStep(String commandName, String decide) {
		CcpJsonRepresentation ticketNotFoundFlow = getTicketNotFoundFlow();

		String email = "{" + JsEntityContactUs.Fields.email + "}";
		int userNotAllowed = JsSupportContactUsStatus.userNotAllowed.asNumber();
		CcpJsonRepresentation userNotAllowedFlow = JnSupportAllowCommandToUserBotConfiguration.getUserNotAllowedFlow(userNotAllowed, email);

		CcpJsonRepresentation step = JnEntityBotCommandStep.getBotCommandStep(commandName, JsSupportContactUsShowTicket.class, decide, ticketNotFoundFlow, userNotAllowedFlow);
		return step;
	}

	/**
	 * Answer the ticket or leave it open (ending the session) or ignore the user (going on to its confirmation). An answer
	 * not understood repeats this step; a ticket discarded meanwhile ends the session.
	 */
	private static CcpJsonRepresentation getDecideStep(String decide, String confirmIgnore) {
		CcpJsonRepresentation[] botReply = JnEntityBotCommandStep.getBotReplyInEveryLanguage();
		CcpJsonRepresentation ticketNotFoundFlow = getTicketNotFoundFlow();

		int invalidAnswer = JsSupportContactUsStatus.invalidAnswer.asNumber();
		int ticketAnswered = JsSupportContactUsStatus.ticketAnswered.asNumber();
		int ticketSkipped = JsSupportContactUsStatus.ticketSkipped.asNumber();
		int ignoreConfirmationAsked = JsSupportContactUsStatus.ignoreConfirmationAsked.asNumber();
		CcpJsonRepresentation invalidAnswerFlow = JnEntityBotCommandStep.getStepFlow(invalidAnswer, decide, botReply);
		CcpJsonRepresentation ticketAnsweredFlow = JnEntityBotCommandStep.getStepFlow(ticketAnswered, "", botReply);
		CcpJsonRepresentation ticketSkippedFlow = JnEntityBotCommandStep.getStepFlow(ticketSkipped, "", botReply);
		CcpJsonRepresentation ignoreConfirmationAskedFlow = JnEntityBotCommandStep.getStepFlow(ignoreConfirmationAsked, confirmIgnore, botReply);

		CcpJsonRepresentation step = JnEntityBotCommandStep.getBotCommandStep(decide, JsSupportContactUsDecide.class, "", invalidAnswerFlow, ticketAnsweredFlow, ticketSkippedFlow, ignoreConfirmationAskedFlow, ticketNotFoundFlow);
		return step;
	}

	/**
	 * Confirmation of the intention to ignore the user: yes ends the session with the user ignored, no goes back to the
	 * decision, and an answer not understood repeats this step.
	 */
	private static CcpJsonRepresentation getConfirmIgnoreStep(String decide, String confirmIgnore) {
		CcpJsonRepresentation[] botReply = JnEntityBotCommandStep.getBotReplyInEveryLanguage();

		int invalidAnswer = JsSupportContactUsStatus.invalidAnswer.asNumber();
		int userIgnored = JsSupportContactUsStatus.userIgnored.asNumber();
		int ignoreCanceled = JsSupportContactUsStatus.ignoreCanceled.asNumber();
		CcpJsonRepresentation invalidAnswerFlow = JnEntityBotCommandStep.getStepFlow(invalidAnswer, confirmIgnore, botReply);
		CcpJsonRepresentation userIgnoredFlow = JnEntityBotCommandStep.getStepFlow(userIgnored, "", botReply);
		CcpJsonRepresentation ignoreCanceledFlow = JnEntityBotCommandStep.getStepFlow(ignoreCanceled, decide, botReply);

		CcpJsonRepresentation step = JnEntityBotCommandStep.getBotCommandStep(confirmIgnore, JsSupportContactUsConfirmIgnore.class, "", invalidAnswerFlow, userIgnoredFlow, ignoreCanceledFlow);
		return step;
	}

	/**
	 * The session ends telling the operator that there is no open ticket of the type for the e-mail.
	 */
	private static CcpJsonRepresentation getTicketNotFoundFlow() {
		String email = "{" + JsEntityContactUs.Fields.email + "}";
		String type = "{" + JsEntityContactUs.Fields.type + "}";
		CcpJsonRepresentation portuguese = JnEntityBotCommandStep.getStepFlowMessage(JnLanguage.portuguese, "Não há ticket aberto do fale conosco do tipo '" + type + "' para o e-mail '" + email + "'");
		CcpJsonRepresentation english = JnEntityBotCommandStep.getStepFlowMessage(JnLanguage.english, "There is no open ticket of the contact us of the type '" + type + "' for the e-mail '" + email + "'");
		CcpJsonRepresentation spanish = JnEntityBotCommandStep.getStepFlowMessage(JnLanguage.spanish, "No hay ticket abierto del contáctenos del tipo '" + type + "' para el correo '" + email + "'");
		int ticketNotFound = JsSupportContactUsStatus.ticketNotFound.asNumber();
		CcpJsonRepresentation ticketNotFoundFlow = JnEntityBotCommandStep.getStepFlow(ticketNotFound, "", portuguese, english, spanish);
		return ticketNotFoundFlow;
	}
}
