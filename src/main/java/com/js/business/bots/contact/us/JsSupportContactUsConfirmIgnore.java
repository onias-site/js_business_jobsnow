package com.js.business.bots.contact.us;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.utils.JnLanguage;
import com.js.business.contact.us.JsBusinessContactUsIgnoreUser;
import com.js.entities.JsEntityContactUs;

/**
 * Step of the {@code answerContactUs} command that confirms the intention of the operator to ignore the user. Yes
 * records the user in {@code JnEntityCommandNotAllowedToUser}, which discards their open tickets without notifying them,
 * and ends the session ({@code userIgnored}); no goes back to the decision ({@code ignoreCanceled}); any other answer
 * asks again ({@code invalidAnswer}).
 */
public class JsSupportContactUsConfirmIgnore implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JsSupportContactUsConfirmIgnore INSTANCE = new JsSupportContactUsConfirmIgnore();

	private JsSupportContactUsConfirmIgnore() {}

	/**
	 * Handles the confirmation.
	 * @param json the session
	 * @return never returns normally
	 * @throws CcpErrorFlowDisturb with {@code userIgnored}, {@code ignoreCanceled} or {@code invalidAnswer}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		JsSupportContactUsAnswer answer = JsSupportContactUsAnswer.read(json);
		JnLanguage language = JsSupportContactUsConversation.getLanguage(json);

		if(JsSupportContactUsAnswerType.yes == answer.type) {
			JsBusinessContactUsIgnoreUser.INSTANCE.execute(json);
			CcpJsonRepresentation userIgnoredParameters = json.getJsonPiece(JsEntityContactUs.Fields.email);
			String userIgnoredText = JsSupportContactUsMessages.userIgnored.getMessage(language, userIgnoredParameters);
			CcpJsonRepresentation jsonWithReply = JsSupportContactUsConversation.reply(json, userIgnoredText);
			return JsSupportContactUsStatus.userIgnored.throwException(jsonWithReply);
		}

		if(JsSupportContactUsAnswerType.no == answer.type) {
			String options = JsSupportContactUsConversation.getOptions(language);
			CcpJsonRepresentation ignoreCanceledParameters = CcpOtherConstants.EMPTY_JSON.put(JsSupportContactUsMessageFields.options, options);
			String backToOptions = JsSupportContactUsMessages.ignoreCanceled.getMessage(language, ignoreCanceledParameters);
			CcpJsonRepresentation jsonWithReply = JsSupportContactUsConversation.reply(json, backToOptions);
			return JsSupportContactUsStatus.ignoreCanceled.throwException(jsonWithReply);
		}

		String ignoreConfirmation = JsSupportContactUsConversation.getIgnoreConfirmation(json);
		CcpJsonRepresentation askAgainParameters = CcpOtherConstants.EMPTY_JSON.put(JsSupportContactUsMessageFields.ignoreConfirmation, ignoreConfirmation);
		String askAgain = JsSupportContactUsMessages.ignoreConfirmationNotUnderstood.getMessage(language, askAgainParameters);
		CcpJsonRepresentation jsonAskingAgain = JsSupportContactUsConversation.reply(json, askAgain);
		return JsSupportContactUsStatus.invalidAnswer.throwException(jsonAskingAgain);
	}
}
