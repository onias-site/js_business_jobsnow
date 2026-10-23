package com.js.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * The current login session of each e-mail (one per e-mail): while it exists, a new login with the password is refused
 * as a conflict (409), and only the session whose token hash is stored here is valid.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_session_conflict}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>disposable: records expire by the {@code hourly} granularity</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsValidator(JsEntityLoginSessionConflict.Fields.class)
@CcpEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.hourly)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JsEntityLoginSessionConflict implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityLoginSessionConflict INSTANCE = new JsEntityLoginSessionConflict();

	private JsEntityLoginSessionConflict() {}

	/** The entity {@code js_login_session_conflict}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginSessionConflict.class).entityInstance;
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
		 * The {@code currentSessionTokenHash} field: the hash of the token of the current session of the e-mail, computed by
		 * {@code JsBulkHandlerRegisterLogin} (not by a field transformer, see there why; the name is out of the default
		 * transformer catalog on purpose, where {@code token} would get BCrypt). Only this session is valid: a login or a
		 * password save replaces it, and with it every other session of the e-mail stops being accepted (until 2026-10-11 the
		 * old sessions stayed valid).
		 */
		@CcpJsonFieldTypeString
		currentSessionTokenHash;
	}
}
