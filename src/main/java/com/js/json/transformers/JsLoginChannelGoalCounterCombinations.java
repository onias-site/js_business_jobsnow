package com.js.json.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityVirtualLoginChannelGoalCounter;

/**
 * Produces the group of {@code JsEntityVirtualLoginChannelGoalCounter} every answer of the sign-up questionnaire is
 * counted in: always the same, the group of the counters by channel and goal.
 */
public class JsLoginChannelGoalCounterCombinations implements Function<CcpJsonRepresentation, List<CcpJsonRepresentation>>{
	/** The single instance: the class keeps no state. */
	public final static JsLoginChannelGoalCounterCombinations INSTANCE = new JsLoginChannelGoalCounterCombinations();

	private JsLoginChannelGoalCounterCombinations() {}

	/**
	 * The combination of the group, whatever the record.
	 * @param json the record
	 * @return the combination
	 */
	public List<CcpJsonRepresentation> apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation combination = JsEntityVirtualLoginChannelGoalCounter.getCombination();
		return Arrays.asList(combination);
	}
}
