package com.js.business.bots.contact.us.ignored;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.bots.ignored.users.JnSupportIgnoredUsersChoose;
import com.jn.business.bots.ignored.users.JnSupportIgnoredUsersList;
import com.jn.json.fields.validation.JnUserRequestCommands;
import com.js.business.bots.JsSupportBotCommands;

/**
 * Second step of the {@code ignoredContactUs} command: the second step of {@code /ignoredUsers} ({@code 1} stops ignoring
 * the user shown, {@code 2} goes to the next one), over the users the operator ignored while answering a ticket of the
 * "contact us"; when {@code /allowCommandToUser} finishes, this list is shown again.
 */
public class JsSupportIgnoredContactUsChoose extends JnSupportIgnoredUsersChoose {
	/** The single instance: the class keeps no state. */
	public final static JsSupportIgnoredContactUsChoose INSTANCE = new JsSupportIgnoredContactUsChoose();

	private JsSupportIgnoredContactUsChoose() {}

	/**
	 * The users ignored in {@code answerContactUs}.
	 * @return the ignored users
	 */
	protected List<CcpJsonRepresentation> getIgnoredUsers() {
		List<CcpJsonRepresentation> ignoredUsers = JnSupportIgnoredUsersList.getIgnoredUsers(JnUserRequestCommands.answerContactUs);
		return ignoredUsers;
	}

	/**
	 * This command, shown again when {@code /allowCommandToUser} finishes.
	 * @return the command, with its slash
	 */
	protected String getThisCommand() {
		String thisCommand = "/" + JsSupportBotCommands.ignoredContactUs;
		return thisCommand;
	}
}
