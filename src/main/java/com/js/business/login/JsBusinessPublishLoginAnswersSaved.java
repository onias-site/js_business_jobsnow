package com.js.business.login;

import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.mensageria.JnBusinessPublishWriteEvent;

/**
 * Announces each save of the sign-up answers ({@link JsEntityLoginAnswers}) to whoever listens to it in
 * {@code jn_write_event_subscribers}; js does not know the listeners (vis turns the answers with the goal
 * {@code recruiting} into a recruiter).
 */
public class JsBusinessPublishLoginAnswersSaved extends JnBusinessPublishWriteEvent {

	/** Names the event: each save of {@code js_login_answers}. */
	public JsBusinessPublishLoginAnswersSaved() {
		super(JsEntityLoginAnswers.ENTITY, CcpEntityDecoratorOperationType.save);
	}
}
