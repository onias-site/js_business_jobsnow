package com.js.json.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityVirtualLoginChannelCounter;

/**
 * Produces the group of {@code JsEntityVirtualLoginChannelCounter} every answer of the sign-up questionnaire is counted
 * in: always the same, the group of the counters by channel.
 */
public class JsLoginChannelCounterCombinations implements Function<CcpJsonRepresentation, List<CcpJsonRepresentation>>{
	/** The single instance: the class keeps no state. */
	public final static JsLoginChannelCounterCombinations INSTANCE = new JsLoginChannelCounterCombinations();

	private JsLoginChannelCounterCombinations() {}

	/**
	 * The combination of the group, whatever the record.
	 * @param json the record
	 * @return the combination
	 */
	public List<CcpJsonRepresentation> apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation combination = JsEntityVirtualLoginChannelCounter.getCombination();
		return Arrays.asList(combination);
	}
}
