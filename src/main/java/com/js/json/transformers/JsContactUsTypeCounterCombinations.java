package com.js.json.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityVirtualContactUsTypeCounter;

/**
 * Produces the group of {@code JsEntityVirtualContactUsTypeCounter} every ticket is counted in: always the same, the
 * only group of the tickets by type.
 */
public class JsContactUsTypeCounterCombinations implements Function<CcpJsonRepresentation, List<CcpJsonRepresentation>>{
	/** The single instance: the class keeps no state. */
	public final static JsContactUsTypeCounterCombinations INSTANCE = new JsContactUsTypeCounterCombinations();

	private JsContactUsTypeCounterCombinations() {}

	/**
	 * The combination of the group, whatever the record.
	 * @param json the record
	 * @return the combination
	 */
	public List<CcpJsonRepresentation> apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation combination = JsEntityVirtualContactUsTypeCounter.getCombination();
		return Arrays.asList(combination);
	}
}
