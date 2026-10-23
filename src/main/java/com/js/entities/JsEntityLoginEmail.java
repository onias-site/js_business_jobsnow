package com.js.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * The e-mails registered as users of the platform; permanent.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_email}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JsEntityLoginEmail.Fields.class)
public class JsEntityLoginEmail implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityLoginEmail INSTANCE = new JsEntityLoginEmail();

	private JsEntityLoginEmail() {}

	/** The entity {@code js_login_email}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginEmail.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email,
		/**
		 * The language of the user, recorded on each request of a login token (it comes in the URL of that request). Read
		 * when the support resets the token, so the new token goes in the language of the user; a record from before
		 * 2026-10-07 has none, and the message falls back to the language of the system.
		 */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		language
		;
	}
}
