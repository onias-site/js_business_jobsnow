package com.js.business.bots.contact.us.ignored;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.jn.business.bots.JnBotConfigurationSeeds;
import com.jn.business.bots.ignored.users.JnSupportIgnoredUsersStatus;
import com.jn.business.messages.JnMessageType;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.utils.JnLanguage;
import com.js.business.bots.JsCommandNamesInPortuguese;
import com.js.business.bots.JsSupportBotCommands;

/**
 * Configuration of {@code /ignoredContactUs} in the support bot: {@code /ignoredUsers} over the users the operator ignored
 * while answering a ticket of the "contact us", with the options of stopping ignoring the user shown (the bot then runs
 * {@code /allowCommandToUser} for the user and shows this list again) and going to the next one. The texts are the ones of
 * {@code /ignoredUsers}, seeded by jn. Seeded by {@code JsEntityContactUs}.
 */
public final class JsSupportIgnoredContactUsBotConfiguration {

	private JsSupportIgnoredContactUsBotConfiguration() {}

	/**
	 * The seed records of the command.
	 * @return the bulk items
	 */
	public static List<CcpBulkItem> getSeeds() {
		String commandName = JsSupportBotCommands.ignoredContactUs.name();
		String choose = JsSupportIgnoredContactUsSteps.ignoredContactUsChoose.name();
		CcpJsonRepresentation[] botReply = JnEntityBotCommandStep.getBotReplyInEveryLanguage();

		// without any ignored user the session ends with that notice
		int noIgnoredUser = JnSupportIgnoredUsersStatus.noIgnoredUser.asNumber();
		CcpJsonRepresentation noIgnoredUserFlowOfTheList = JnEntityBotCommandStep.getStepFlow(noIgnoredUser, "", botReply);
		CcpJsonRepresentation showStep = JnEntityBotCommandStep.getBotCommandStep(commandName, JsSupportIgnoredContactUsShow.class, choose, noIgnoredUserFlowOfTheList);

		// stop ignoring the user shown or go to the next one; an answer not understood repeats the step and a list that
		// became empty in the meantime ends the session
		int invalidAnswer = JnSupportIgnoredUsersStatus.invalidAnswer.asNumber();
		int userChosen = JnSupportIgnoredUsersStatus.userChosen.asNumber();
		CcpJsonRepresentation invalidAnswerFlow = JnEntityBotCommandStep.getStepFlow(invalidAnswer, choose, botReply);
		CcpJsonRepresentation noIgnoredUserFlow = JnEntityBotCommandStep.getStepFlow(noIgnoredUser, "", botReply);
		CcpJsonRepresentation userChosenFlow = JnEntityBotCommandStep.getStepFlow(userChosen, "", botReply);
		CcpJsonRepresentation chooseStep = JnEntityBotCommandStep.getBotCommandStep(choose, JsSupportIgnoredContactUsChoose.class, choose, invalidAnswerFlow, noIgnoredUserFlow, userChosenFlow);

		List<CcpJsonRepresentation> endMessages = JnEntityBotCommandStepEndMessage.getBotReplyEndMessages(commandName, choose);

		JnBotConfigurationSeeds seeds = new JnBotConfigurationSeeds(JnMessageType.JnBotType.support)
				.command(commandName)
				.portugueseNames(JsCommandNamesInPortuguese.ignoredContactUs)
				.steps(showStep, chooseStep)
				.endMessages(endMessages)
				.commandExplanation(commandName, JnLanguage.portuguese, "Lista os usuários que o suporte ignorou ao responder um ticket do fale conosco, para voltar a atendê-los ou ir ao próximo")
				.commandExplanation(commandName, JnLanguage.english, "Lists the users the support ignored while answering a ticket of the contact us, to review their requests again or go to the next one");
		List<CcpBulkItem> bulkItems = seeds.getBulkItems();
		return bulkItems;
	}
}
