package com.js.json.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityVirtualContactUsPendingTypeCounter;

/**
 * Produces the group of {@code JsEntityVirtualContactUsPendingTypeCounter} every ticket is counted in: always the same,
 * the group of the tickets waiting for the support by type.
 */
public class JsContactUsPendingTypeCounterCombinations implements Function<CcpJsonRepresentation, List<CcpJsonRepresentation>>{
	/** The single instance: the class keeps no state. */
	public final static JsContactUsPendingTypeCounterCombinations INSTANCE = new JsContactUsPendingTypeCounterCombinations();

	private JsContactUsPendingTypeCounterCombinations() {}

	/**
	 * The combination of the group, whatever the record.
	 * @param json the record
	 * @return the combination
	 */
	public List<CcpJsonRepresentation> apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation combination = JsEntityVirtualContactUsPendingTypeCounter.getCombination();
		return Arrays.asList(combination);
	}
}
