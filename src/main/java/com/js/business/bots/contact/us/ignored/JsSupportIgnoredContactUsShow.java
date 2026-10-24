package com.js.business.bots.contact.us.ignored;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.bots.ignored.users.JnSupportIgnoredUsersList;
import com.jn.business.bots.ignored.users.JnSupportIgnoredUsersShow;
import com.jn.json.fields.validation.JnUserRequestCommands;

/**
 * First step of the {@code ignoredContactUs} command ({@code /ignoredContactUs}): the first step of {@code /ignoredUsers},
 * over the users the operator ignored while answering a ticket of the "contact us" ({@code answerContactUs}).
 */
public class JsSupportIgnoredContactUsShow extends JnSupportIgnoredUsersShow {
	/** The single instance: the class keeps no state. */
	public final static JsSupportIgnoredContactUsShow INSTANCE = new JsSupportIgnoredContactUsShow();

	private JsSupportIgnoredContactUsShow() {}

	/**
	 * The users ignored in {@code answerContactUs}.
	 * @return the ignored users
	 */
	protected List<CcpJsonRepresentation> getIgnoredUsers() {
		List<CcpJsonRepresentation> ignoredUsers = JnSupportIgnoredUsersList.getIgnoredUsers(JnUserRequestCommands.answerContactUs);
		return ignoredUsers;
	}
}
