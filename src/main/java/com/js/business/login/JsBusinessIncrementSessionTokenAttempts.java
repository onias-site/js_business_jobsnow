package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.entities.JsEntityLoginSessionTokenAttempts;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Counts one more invalid session token: reads the current counter from
 * {@code _entities.js_login_session_token_attempts}; when the new count reaches the limit it runs the given business
 * (without saving the counter), otherwise it saves the incremented counter. Built by
 * {@code JsEntityLoginSessionTokenAttempts.incrementAttempts}.
 */
public class JsBusinessIncrementSessionTokenAttempts implements CcpBusiness {

	/** The limit of attempts. */
	private final Integer maxAttempts;

	/** Business run when the limit is reached. */
	private final CcpBusiness whenExceedAttempts;

	/**
	 * Keeps the limit and what to do when it is reached.
	 * @param maxAttempts the limit of attempts
	 * @param whenExceedAttempts business run when the limit is reached
	 */
	public JsBusinessIncrementSessionTokenAttempts(Integer maxAttempts, CcpBusiness whenExceedAttempts) {
		this.maxAttempts = maxAttempts;
		this.whenExceedAttempts = whenExceedAttempts;
	}

	/**
	 * Counts the attempt.
	 * @param json the input, holding the counter under {@code _entities}
	 * @return the input
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation record = json.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JsEntityLoginSessionTokenAttempts.ENTITY);
		boolean hasAttempts = record.containsField(JnJsonCommonsFields.attempts);
		Double attempts = hasAttempts ? record.getAsDoubleNumber(JnJsonCommonsFields.attempts) : 0d;
		Double updatedAttempts = attempts + 1;

		boolean excedeedAttempts = updatedAttempts >= this.maxAttempts;

		if(excedeedAttempts) {
			this.whenExceedAttempts.execute(json);
			return json;
		}

		CcpJsonRepresentation jsonPiece = json.getJsonPiece(JnJsonCommonsFields.email);
		int intValue = updatedAttempts.intValue();
		CcpJsonRepresentation put = record.put(JnJsonCommonsFields.attempts, intValue);
		CcpJsonRepresentation mergeWithAnotherJson = put.mergeWithAnotherJson(jsonPiece);
		JsEntityLoginSessionTokenAttempts.ENTITY.save(mergeWithAnotherJson);
		return json;
	}
}
