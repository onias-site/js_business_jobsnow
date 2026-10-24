package com.js.business.bots.contact.us;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * What the operator typed in answer to a step of the {@code answerContactUs} command, in Portuguese or English: respond
 * (responder) followed by the answer to the user; next (próximo, with or without the accent) to leave the ticket open and
 * go to the next one; ignore (ignorar) to ignore the user; and yes or no (sim, não with or without the accent) to confirm
 * it. Case is ignored.
 */
final class JsSupportContactUsAnswer {

	/** The words the operator may type. */
	enum Words implements CcpJsonFieldName {
		/** Respond, in Portuguese. */
		responder,
		/** Respond, in English. */
		respond,
		/** Respond, in English. */
		answer,
		/** Next, in Portuguese, with the accent. */
		proximoWithAccent { public String getValue() { return "próximo"; } },
		/** Next, in Portuguese, without the accent. */
		proximo,
		/** Next, in English. */
		next,
		/** Ignore, in Portuguese. */
		ignorar,
		/** Ignore, in English. */
		ignore,
		/** Yes, in Portuguese. */
		sim,
		/** Yes, in English. */
		yes,
		/** No, in Portuguese, with the accent. */
		naoWithAccent { public String getValue() { return "não"; } },
		/** No, in Portuguese, without the accent. */
		nao,
		/** No, in English. */
		no,
	}

	/** The kind of answer. */
	final JsSupportContactUsAnswerType type;

	/** The answer to the user, when the operator responds; empty otherwise. */
	final String response;

	/**
	 * Builds the answer.
	 * @param type the kind of answer
	 * @param response the answer to the user
	 */
	private JsSupportContactUsAnswer(JsSupportContactUsAnswerType type, String response) {
		this.type = type;
		this.response = response;
	}

	/**
	 * Reads the answer from {@code typedValue}.
	 * @param json the session
	 * @return the answer ({@code notUnderstood} when it matches nothing)
	 */
	static JsSupportContactUsAnswer read(CcpJsonRepresentation json) {

		String typedValue = json.getAsString(JnJsonCommonsFields.typedValue);
		String answer = typedValue.trim();
		String lowerCaseAnswer = answer.toLowerCase();

		boolean isNext = isOneOf(lowerCaseAnswer, Words.proximoWithAccent, Words.proximo, Words.next);

		if(isNext) {
			return new JsSupportContactUsAnswer(JsSupportContactUsAnswerType.next, "");
		}

		boolean isIgnore = isOneOf(lowerCaseAnswer, Words.ignorar, Words.ignore);

		if(isIgnore) {
			return new JsSupportContactUsAnswer(JsSupportContactUsAnswerType.ignore, "");
		}

		boolean isYes = isOneOf(lowerCaseAnswer, Words.sim, Words.yes);

		if(isYes) {
			return new JsSupportContactUsAnswer(JsSupportContactUsAnswerType.yes, "");
		}

		boolean isNo = isOneOf(lowerCaseAnswer, Words.naoWithAccent, Words.nao, Words.no);

		if(isNo) {
			return new JsSupportContactUsAnswer(JsSupportContactUsAnswerType.no, "");
		}

		String[] firstWordAndTheRest = answer.split("\\s+", 2);
		String firstWord = firstWordAndTheRest[0].toLowerCase();
		boolean isRespond = isOneOf(firstWord, Words.responder, Words.respond, Words.answer);

		if(false == isRespond) {
			return new JsSupportContactUsAnswer(JsSupportContactUsAnswerType.notUnderstood, "");
		}

		boolean hasTheRest = firstWordAndTheRest.length > 1;
		String response = hasTheRest ? firstWordAndTheRest[1].trim() : "";
		return new JsSupportContactUsAnswer(JsSupportContactUsAnswerType.respond, response);
	}

	/**
	 * Tells whether the typed text is one of the words.
	 * @param typedText the text, in lower case
	 * @param words the words
	 * @return {@code true} when it is one of them
	 */
	private static boolean isOneOf(String typedText, Words... words) {
		for (Words word : words) {
			String wordValue = word.getValue();
			boolean same = wordValue.equals(typedText);

			if(same) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tells whether the operator answered the ticket with an answer of 10 to 500 characters: it goes to the user in the
	 * e-mail.
	 * @return {@code true} for a valid answer
	 */
	boolean isValidResponse() {
		boolean isRespond = JsSupportContactUsAnswerType.respond == this.type;
		int responseLength = this.response.length();
		boolean validLength = responseLength >= 10 && responseLength <= 500;
		boolean validResponse = isRespond && validLength;
		return validResponse;
	}
}
