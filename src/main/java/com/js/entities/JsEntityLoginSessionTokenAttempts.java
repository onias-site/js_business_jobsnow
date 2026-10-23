package com.js.entities;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.js.business.login.JsBusinessIncrementSessionTokenAttempts;
import com.js.business.login.JsBusinessResetSessionTokenAttempts;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Counter of invalid session tokens per user; when the limit is reached the password is locked (see {@link #incrementAttempts} and {@link #resetAttempts}).
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_session_token_attempts}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JsEntityLoginSessionTokenAttempts.Fields.class)
public class JsEntityLoginSessionTokenAttempts implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityLoginSessionTokenAttempts INSTANCE = new JsEntityLoginSessionTokenAttempts();

	private JsEntityLoginSessionTokenAttempts() {}

	/** The entity {@code js_login_session_token_attempts}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginSessionTokenAttempts.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email, 
		/** The {@code attempts} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		attempts
		;
		
		
	}
	/**
	 * Builds the business that counts one more invalid session token: it reads the current counter from
	 * {@code _entities.js_login_session_token_attempts}; when the new count reaches {@code maxAttempts} it runs
	 * {@code whenExceedAttempts} (without saving the counter), otherwise it saves the incremented counter.
	 * @param maxAttempts the limit of attempts
	 * @param whenExceedAttempts business run when the limit is reached
	 * @return the business; it returns its input
	 */
	public static CcpBusiness incrementAttempts(Integer maxAttempts, CcpBusiness whenExceedAttempts) {
		CcpBusiness result = new JsBusinessIncrementSessionTokenAttempts(maxAttempts, whenExceedAttempts);
		return result;
	}

	/**
	 * Builds the business that deletes the counter of the user, when there is one in
	 * {@code _entities.js_login_session_token_attempts}.
	 * @return the business; it returns its input
	 */
	public static CcpBusiness resetAttempts() {
		CcpBusiness result = JsBusinessResetSessionTokenAttempts.INSTANCE;
		return result;
	}
}
