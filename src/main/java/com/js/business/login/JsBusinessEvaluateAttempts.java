package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatus;
import com.jn.utils.JnSystemProperties;

/**
 * Evaluates a secret typed by the user (password or token) against the stored one, both read from the JSON (the stored
 * one and the attempts under {@code _entities}). A right secret runs the success business. A wrong one counts one more
 * attempt: when the count reaches {@code JnSystemProperties.maxAttempts()} the lock business runs and the "exceeded
 * attempts" status is thrown; otherwise the counter is saved and the "wrong" status is thrown with the attempts.
 */
public class JsBusinessEvaluateAttempts implements CcpBusiness{ 
	/** Fields read by the evaluator. */
	enum JsonFieldNames implements CcpJsonFieldName{
		/** The {@code entities} field. */
		entities
	}

	/** The entity of the stored secret. */
	private final CcpEntity entityToGetTheSecret;
	 
	/** The entity of the attempts counter. */
	private final CcpEntity entityToGetTheAttempts;

	/** The field of the secret typed by the user. */
	private final CcpJsonFieldName userFieldName;
	
	/** The field of the stored secret. */
	private final CcpJsonFieldName databaseFieldName;

	/** Status thrown on a wrong secret. */
	private final CcpProcessStatus statusToReturnWhenWrongType;
	
	/** Status thrown when the attempts are exceeded. */
	private final CcpProcessStatus statusToReturnWhenExceedAttempts;
	
	/** Business run when the secret is right. */
	private final CcpBusiness topicToRegisterSuccess;

	/** Business that locks the secret when the attempts are exceeded. */
	private final CcpBusiness topicToCreateTheLockWhenExceedTries;
	
	/** The attempts field. */
	private final CcpJsonFieldName fieldAttempsName;
	
	/** The e-mail field. */
	private final CcpJsonFieldName fieldEmailName;

	/**
	 * Builds the evaluator from the builder.
	 * @param builder the builder
	 */
	JsBusinessEvaluateAttempts(Builder builder) {
		this.entityToGetTheAttempts             = builder.entityToGetTheAttempts;
		this.entityToGetTheSecret               = builder.entityToGetTheSecret;
		this.databaseFieldName                  = builder.databaseFieldName;
		this.userFieldName                      = builder.userFieldName;
		this.statusToReturnWhenExceedAttempts   = builder.statusToReturnWhenExceedAttempts;
		this.statusToReturnWhenWrongType        = builder.statusToReturnWhenWrongType;
		this.topicToCreateTheLockWhenExceedTries = builder.topicToCreateTheLockWhenExceedTries;
		this.topicToRegisterSuccess             = builder.topicToRegisterSuccess;
		this.fieldAttempsName                   = builder.fieldAttempsName;
		this.fieldEmailName                     = builder.fieldEmailName;
	}

	/**
	 * Starts building an evaluator.
	 * @return a new builder
	 */
	public static Builder builder() {
		Builder builder = new Builder();
		return builder;
	}



	/**
	 * Compares the typed secret with the stored hash through {@code CcpPasswordHandler.matches}.
	 * @param json the request plus the records under {@code _entities}
	 * @return the JSON without {@code entities}, when the secret is right
	 * @throws CcpErrorFlowDisturb with the "wrong" status (and the attempts) or the "exceeded attempts" status
	 * @throws JnErrorSecretFromDatabaseIsEmpty when there is no stored secret
	 * @throws JnErrorSecretFromUserIsEmpty when the user typed nothing
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String secretFromDatabase = json.getValueFromPath("",CcpEntity.JsonFieldNames._entities, this.entityToGetTheSecret, this.databaseFieldName);
		String secretFromDatabaseTrim = secretFromDatabase.trim();
		boolean secretFromDatabaseTrimEmpty = secretFromDatabaseTrim.isEmpty();

		if(secretFromDatabaseTrimEmpty) {
			JnErrorSecretFromDatabaseIsEmpty jnErrorSecretFromDatabaseIsEmpty = new JnErrorSecretFromDatabaseIsEmpty();
			throw jnErrorSecretFromDatabaseIsEmpty;
		}

		String secretFromUser = json.getAsString(this.userFieldName);
		String secretFromUserTrim = secretFromUser.trim();
		boolean secretFromUserTrimEmpty = secretFromUserTrim.isEmpty();

		if(secretFromUserTrimEmpty) {
			JnErrorSecretFromUserIsEmpty jnErrorSecretFromUserIsEmpty = new JnErrorSecretFromUserIsEmpty();
			throw jnErrorSecretFromUserIsEmpty;
		}
		
		CcpPasswordHandler passwordHandler = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);

		boolean correctSecret = passwordHandler.matches(secretFromUser, secretFromDatabase);
		
		CcpJsonRepresentation toReturn = json.removeFields(JsonFieldNames.entities);
		
		if(correctSecret) {
			this.topicToRegisterSuccess.execute(toReturn); 
			return toReturn;
		}

		Double attemptsFromDatabase = json.getValueFromPath(0d, CcpEntity.JsonFieldNames._entities, this.entityToGetTheAttempts, this.fieldAttempsName);
		
		int maxAttempts = JnSystemProperties.INSTANCE.maxAttempts();
		double updatedAttempts = attemptsFromDatabase + 1;
		boolean exceededAttempts = updatedAttempts >= maxAttempts;
		if(exceededAttempts) {
			this.topicToCreateTheLockWhenExceedTries.execute(toReturn);
			CcpErrorFlowDisturb exceededAttemptsError = new CcpErrorFlowDisturb(toReturn, this.statusToReturnWhenExceedAttempts);
			throw exceededAttemptsError;
		}

		String email = json.getAsString(this.fieldEmailName);
		CcpJsonRepresentation jsonWithAttempts = CcpOtherConstants.EMPTY_JSON
				.put(this.fieldAttempsName, updatedAttempts);
				CcpJsonRepresentation attemptsRecord = jsonWithAttempts
				.put(this.fieldEmailName, email)
				;
		this.entityToGetTheAttempts.save(attemptsRecord);
		CcpJsonFieldName[] returnedFields = new CcpJsonFieldName[] {
				this.fieldAttempsName
		};
		CcpJsonRepresentation jsonWithUpdatedAttempts = toReturn.put(this.fieldAttempsName, updatedAttempts);
		CcpErrorFlowDisturb wrongSecretError = new CcpErrorFlowDisturb(jsonWithUpdatedAttempts, this.statusToReturnWhenWrongType, returnedFields);
		throw wrongSecretError;
	}

	/** Raised when there is no stored secret to compare with. */
	@SuppressWarnings("serial")
	private static class JnErrorSecretFromDatabaseIsEmpty extends RuntimeException {
	}

	/** Raised when the user typed no secret. */
	@SuppressWarnings("serial")
	private static class JnErrorSecretFromUserIsEmpty extends RuntimeException {
	}
}
