package com.js.json.transformers;

import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityContactUs;

/**
 * The key of the counter of the tickets waiting for the support by type ({@code counterKeyClass} of a
 * {@code @CcpEntityGrouperChildCounter} counted by {@code JnGrouperChildCurrentCounterEntityBeforeWrite}): the
 * {@code type} of the ticket, part of its primary key, so always present.
 */
public class JsContactUsPendingTypeCounterKeys implements Function<CcpJsonRepresentation, List<String>>{
	/** The single instance: the class keeps no state. */
	public final static JsContactUsPendingTypeCounterKeys INSTANCE = new JsContactUsPendingTypeCounterKeys();

	private JsContactUsPendingTypeCounterKeys() {}

	/**
	 * The type of the ticket.
	 * @param json the ticket as stored
	 * @return the type
	 */
	public List<String> apply(CcpJsonRepresentation json) {
		String type = json.getAsString(JsEntityContactUs.Fields.type);
		return List.of(type);
	}
}
