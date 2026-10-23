package com.js.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityAsyncWriter;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.annotations.messages.write.CcpAfterInsertFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError;
import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpBeforeSaveFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.js.business.login.JsBusinessPrepareLoginTokenBeforeSave;
import com.jn.business.messages.JnMessages;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.decorators.engine.JnAsyncWriterEntity;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * The token e-mailed to the user to define the password (onboarding or recovery); the twin holds the tokens locked after too many wrong attempts.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_token}</li>
 * <li>records cached for 86400 seconds</li>
 * <li>on {@code beforeSaveFromMainEntity} runs {@code JsBusinessPrepareLoginTokenBeforeSave}</li>
 * <li>on {@code afterInsertFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError} sends {@code JnNotifyUserAboutLoginToken}</li>
 * <li>written asynchronously, through messaging</li>
 * <li>twin entity {@code js_login_token_locked}</li>
 * <li>disposable: records expire by the {@code monthly} granularity</li>
 * </ul>
 */
@CcpEntityCache(86400)
@CcpEntityAsyncWriter(JnAsyncWriterEntity.class)
@CcpEntityFieldsValidator(JsEntityLoginToken.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.monthly)
@CcpBeforeSaveFromMainEntity(execute = {JsBusinessPrepareLoginTokenBeforeSave.class}, operationHandlers = {})
@CcpAfterInsertFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(JnMessages.JnNotifyUserAboutLoginToken.class)
@CcpEntityTwin(
		twinEntityName = "js_login_token_locked",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)
public class JsEntityLoginToken implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityLoginToken INSTANCE = new JsEntityLoginToken();

	private JsEntityLoginToken() {}

	/** The entity {@code js_login_token}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginToken.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey 
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email,  
		/** The {@code token} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		token
		;
	}
}
