package com.js.json.transformers;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityContactUs;

/**
 * The key of the counter of the tickets by type ({@code counterKeyClass} of a {@code @CcpEntityGrouperChildCounter}
 * counted by {@code JnGrouperChildCounterEntityBeforeWrite}, one key per record): the {@code type} of the ticket, part of
 * its primary key, so always present.
 */
public class JsContactUsTypeCounterKeys implements Function<CcpJsonRepresentation, String>{
	/** The single instance: the class keeps no state. */
	public final static JsContactUsTypeCounterKeys INSTANCE = new JsContactUsTypeCounterKeys();

	private JsContactUsTypeCounterKeys() {}

	/**
	 * The type of the ticket.
	 * @param json the ticket
	 * @return the type
	 */
	public String apply(CcpJsonRepresentation json) {
		String type = json.getAsString(JsEntityContactUs.Fields.type);
		return type;
	}
}
