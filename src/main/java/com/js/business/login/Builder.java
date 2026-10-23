package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.process.CcpProcessStatus;

/** Builder of {@link JsBusinessEvaluateAttempts}. */
public class Builder { 
	/** The entity of the attempts counter. */
	CcpEntity entityToGetTheAttempts;
	/** The entity of the stored secret. */
	CcpEntity entityToGetTheSecret;
	/** The field of the stored secret. */
	CcpJsonFieldName databaseFieldName;
	/** The field of the secret typed by the user. */
	CcpJsonFieldName userFieldName;
	/** Status thrown when the attempts are exceeded. */
	CcpProcessStatus statusToReturnWhenExceedAttempts;
	/** Status thrown on a wrong secret. */
	CcpProcessStatus statusToReturnWhenWrongType;
	/** Business that locks the secret when the attempts are exceeded. */
	CcpBusiness topicToCreateTheLockWhenExceedTries;
	/** Business run when the secret is right. */
	CcpBusiness topicToRegisterSuccess;
	/** The attempts field. */
	CcpJsonFieldName fieldAttempsName;
	/** The e-mail field. */
	CcpJsonFieldName fieldEmailName;  

	/**
	 * Sets the entity of the attempts counter.
	 * @param entity the entity
	 * @return this builder
	 */
	public Builder entityToGetTheAttempts(CcpEntity entity) {
		this.entityToGetTheAttempts = entity;
		return this;
	}
	/**
	 * Sets the entity of the stored secret.
	 * @param entity the entity
	 * @return this builder
	 */
	public Builder entityToGetTheSecret(CcpEntity entity) {
		this.entityToGetTheSecret = entity;
		return this;
	}
	/**
	 * Sets the field of the stored secret.
	 * @param field the field
	 * @return this builder
	 */
	public Builder databaseFieldName(CcpJsonFieldName field) {
		this.databaseFieldName = field;
		return this;
	}
	/**
	 * Sets the field of the secret typed by the user.
	 * @param field the field
	 * @return this builder
	 */
	public Builder userFieldName(CcpJsonFieldName field) {
		this.userFieldName = field;
		return this;
	}
	/**
	 * Sets the status thrown when the attempts are exceeded.
	 * @param status the status
	 * @return this builder
	 */
	public Builder statusWhenExceedAttempts(CcpProcessStatus status) {
		this.statusToReturnWhenExceedAttempts = status;
		return this;
	}
	/**
	 * Sets the status thrown on a wrong secret.
	 * @param status the status
	 * @return this builder
	 */
	public Builder statusWhenWrongType(CcpProcessStatus status) {
		this.statusToReturnWhenWrongType = status;
		return this;
	}
	/**
	 * Sets the business that locks the secret.
	 * @param business the business
	 * @return this builder
	 */
	public Builder lockUsing(CcpBusiness business) {
		this.topicToCreateTheLockWhenExceedTries = business;
		return this;
	}
	/**
	 * Sets the business run when the secret is right.
	 * @param business the business
	 * @return this builder
	 */
	public Builder onSuccess(CcpBusiness business) {
		this.topicToRegisterSuccess = business;
		return this;
	}
	/**
	 * Sets the attempts field.
	 * @param field the field
	 * @return this builder
	 */
	public Builder attemptsFieldName(CcpJsonFieldName field) {
		this.fieldAttempsName = field;
		return this;
	}
	/**
	 * Sets the e-mail field.
	 * @param field the field
	 * @return this builder
	 */
	public Builder emailFieldName(CcpJsonFieldName field) {
		this.fieldEmailName = field;
		return this;
	}
	/**
	 * Builds the evaluator.
	 * @return the evaluator
	 */
	public JsBusinessEvaluateAttempts build() {
		JsBusinessEvaluateAttempts jnBusinessEvaluateAttempts = new JsBusinessEvaluateAttempts(this);
		return jnBusinessEvaluateAttempts;
	}
}
