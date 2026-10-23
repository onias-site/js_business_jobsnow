package com.js.entities;

import java.util.List;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldTransformer;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntitySessionValidator;
import com.jn.entities.JnSessionValidatorNames;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.js.business.login.JsBusinessValidateLogin;
import com.js.entities.fields.transformers.JsJsonTransformersFieldEntityTokenHash;

/**
 * An active login session, keyed by e-mail, token hash, IP and user agent; the twin receives the session at logout.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_session_validation}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>twin entity {@code js_login_session_terminated}</li>
 * <li>disposable: records expire by the {@code hourly} granularity</li>
 * </ul>
 * The login and the logout keep a history in the group of the e-mail in {@code jn_grouper}, written by bulk with the
 * session ({@code JsBulkItemsLoginHistory}).
 */
@CcpEntityCache(3600)
@CcpEntityFieldsValidator(JsEntityLoginSessionValidation.Fields.class)
@CcpEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.hourly)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityTwin(
		twinEntityName = "js_login_session_terminated",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)
public class JsEntityLoginSessionValidation implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityLoginSessionValidation INSTANCE = new JsEntityLoginSessionValidation();

	private JsEntityLoginSessionValidation() {}

	/** The entity {@code js_login_session_validation}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginSessionValidation.class).entityInstance;
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email, 
		
		/** The {@code token} field: part of the primary key, validated as in {@code JnJsonCommonsFields}, transformed by {@code JsJsonTransformersFieldEntityTokenHash}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpEntityFieldTransformer(JsJsonTransformersFieldEntityTokenHash.class)
		token,
		
		/** The {@code ip} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		ip,
		
		/** The {@code coordinates} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		coordinates, 
		/** The {@code macAddress} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		macAddress, 
		/** The {@code userAgent} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		userAgent
		;
	}

	/**
	 * Seeds {@link JsBusinessValidateLogin} as the validator of the session of the user, which the APIs of the other
	 * cost centers run by its class name.
	 * @return the seed records
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		List<CcpBulkItem> sessionValidatorSeeds = JnEntitySessionValidator.getSeeds(JnSessionValidatorNames.userSession, JsBusinessValidateLogin.class);
		return sessionValidatorSeeds;
	}
}
