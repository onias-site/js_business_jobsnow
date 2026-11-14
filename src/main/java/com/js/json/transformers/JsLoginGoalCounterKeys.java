package com.js.json.transformers;

import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityLoginAnswers;

/**
 * The key of the counter of the answers of the sign-up questionnaire by goal ({@code counterKeyClass} of a
 * {@code @CcpEntityGrouperChildCounter} counted by {@code JnGrouperChildCurrentCounterEntityBeforeWrite}): the
 * {@code goal} of the stored record; no key when it has none.
 */
public class JsLoginGoalCounterKeys implements Function<CcpJsonRepresentation, List<String>>{
	/** The single instance: the class keeps no state. */
	public final static JsLoginGoalCounterKeys INSTANCE = new JsLoginGoalCounterKeys();

	private JsLoginGoalCounterKeys() {}

	/**
	 * The goal of the record.
	 * @param json the record as stored
	 * @return the goal, empty when the record has none
	 */
	public List<String> apply(CcpJsonRepresentation json) {
		boolean noGoal = false == json.containsField(JsEntityLoginAnswers.Fields.goal);

		if(noGoal) {
			return List.of();
		}

		String goal = json.getAsString(JsEntityLoginAnswers.Fields.goal);
		return List.of(goal);
	}
}
