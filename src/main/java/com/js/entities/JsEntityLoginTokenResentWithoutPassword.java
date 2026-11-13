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
 * The users whose login token support already resent ({@code js_login_token_fulfilled_resend}) and who still have no
 * password. Written in bulk by the daily process {@code JsBusinessSaveTokensResentWithoutPassword}; the insertion of the
 * password ({@code js_login_password}) deletes the record ({@code JsBusinessDeleteTokenResentWithoutPassword}).
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_token_resent_without_password}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>the e-mail, the primary key, is kept as its SHA-1 hash</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsValidator(JsEntityLoginTokenResentWithoutPassword.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JsEntityLoginTokenResentWithoutPassword implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityLoginTokenResentWithoutPassword INSTANCE = new JsEntityLoginTokenResentWithoutPassword();

	private JsEntityLoginTokenResentWithoutPassword() {}

	/** The entity {@code js_login_token_resent_without_password}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginTokenResentWithoutPassword.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: the primary key, validated as in {@code JnJsonCommonsFields}, kept as its SHA-1 hash. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email
		;
	}
}
