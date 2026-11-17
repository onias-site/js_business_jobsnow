package com.js.json.transformers;

import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityLoginAnswers;

/**
 * The key of the counter of the answers of the sign-up questionnaire by channel and goal ({@code counterKeyClass} of a
 * {@code @CcpEntityGrouperChildCounter} counted by {@code JnGrouperChildCurrentCounterEntityBeforeWrite}):
 * {@code <channel>|<goal>} of the stored record; no key when it lacks one of them.
 */
public class JsLoginChannelGoalCounterKeys implements Function<CcpJsonRepresentation, List<String>>{
	/** The single instance: the class keeps no state. */
	public final static JsLoginChannelGoalCounterKeys INSTANCE = new JsLoginChannelGoalCounterKeys();

	private JsLoginChannelGoalCounterKeys() {}

	enum Values implements CcpJsonFieldName {
		SEPARATOR {
			public String getValue() {
				return "|";
			}
		},
	}

	/**
	 * The channel and the goal of the record.
	 * @param json the record as stored
	 * @return {@code <channel>|<goal>}, empty when the record lacks the channel or the goal
	 */
	public List<String> apply(CcpJsonRepresentation json) {
		boolean incomplete = false == json.containsAllFields(JsEntityLoginAnswers.Fields.channel, JsEntityLoginAnswers.Fields.goal);

		if(incomplete) {
			return List.of();
		}

		String channel = json.getAsString(JsEntityLoginAnswers.Fields.channel);
		String goal = json.getAsString(JsEntityLoginAnswers.Fields.goal);
		String separator = Values.SEPARATOR.getValue();
		String key = channel + separator + goal;
		return List.of(key);
	}
}
