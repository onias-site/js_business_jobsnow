package com.js.json.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityVirtualInsertionMonthCounter;

/**
 * Produces the group of {@code JsEntityVirtualInsertionMonthCounter} every record is counted in: always the same, the
 * group of the counters by month of insertion of its entity.
 */
public class JsInsertionMonthCounterCombinations implements Function<CcpJsonRepresentation, List<CcpJsonRepresentation>>{
	/** The single instance: the class keeps no state. */
	public final static JsInsertionMonthCounterCombinations INSTANCE = new JsInsertionMonthCounterCombinations();

	private JsInsertionMonthCounterCombinations() {}

	/**
	 * The combination of the group, whatever the record.
	 * @param json the record
	 * @return the combination
	 */
	public List<CcpJsonRepresentation> apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation combination = JsEntityVirtualInsertionMonthCounter.getCombination();
		return Arrays.asList(combination);
	}
}
