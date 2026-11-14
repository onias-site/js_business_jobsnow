package com.js.json.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityVirtualInsertionDayCounter;

/**
 * Produces the group of {@code JsEntityVirtualInsertionDayCounter} every record is counted in: always the same, the
 * group of the counters by day of insertion of its entity.
 */
public class JsInsertionDayCounterCombinations implements Function<CcpJsonRepresentation, List<CcpJsonRepresentation>>{
	/** The single instance: the class keeps no state. */
	public final static JsInsertionDayCounterCombinations INSTANCE = new JsInsertionDayCounterCombinations();

	private JsInsertionDayCounterCombinations() {}

	/**
	 * The combination of the group, whatever the record.
	 * @param json the record
	 * @return the combination
	 */
	public List<CcpJsonRepresentation> apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation combination = JsEntityVirtualInsertionDayCounter.getCombination();
		return Arrays.asList(combination);
	}
}
