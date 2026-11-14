package com.js.json.transformers;

import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.js.entities.JsEntityLoginAnswers;

/**
 * The key of the counter of the answers of the sign-up questionnaire by channel ({@code counterKeyClass} of a
 * {@code @CcpEntityGrouperChildCounter} counted by {@code JnGrouperChildCurrentCounterEntityBeforeWrite}): the
 * {@code channel} of the stored record; no key when it has none.
 */
public class JsLoginChannelCounterKeys implements Function<CcpJsonRepresentation, List<String>>{
	/** The single instance: the class keeps no state. */
	public final static JsLoginChannelCounterKeys INSTANCE = new JsLoginChannelCounterKeys();

	private JsLoginChannelCounterKeys() {}

	/**
	 * The channel of the record.
	 * @param json the record as stored
	 * @return the channel, empty when the record has none
	 */
	public List<String> apply(CcpJsonRepresentation json) {
		boolean noChannel = false == json.containsField(JsEntityLoginAnswers.Fields.channel);

		if(noChannel) {
			return List.of();
		}

		String channel = json.getAsString(JsEntityLoginAnswers.Fields.channel);
		return List.of(channel);
	}
}
