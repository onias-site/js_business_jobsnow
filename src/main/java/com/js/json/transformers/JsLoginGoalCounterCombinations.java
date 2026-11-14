package com.js.json.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityVirtualLoginGoalCounter;

/**
 * Produces the group of {@code JsEntityVirtualLoginGoalCounter} every answer of the sign-up questionnaire is counted in:
 * always the same, the group of the counters by goal.
 */
public class JsLoginGoalCounterCombinations implements Function<CcpJsonRepresentation, List<CcpJsonRepresentation>>{
	/** The single instance: the class keeps no state. */
	public final static JsLoginGoalCounterCombinations INSTANCE = new JsLoginGoalCounterCombinations();

	private JsLoginGoalCounterCombinations() {}

	/**
	 * The combination of the group, whatever the record.
	 * @param json the record
	 * @return the combination
	 */
	public List<CcpJsonRepresentation> apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation combination = JsEntityVirtualLoginGoalCounter.getCombination();
		return Arrays.asList(combination);
	}
}
