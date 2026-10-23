package com.js.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityAsyncWriter;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.annotations.messages.write.CcpAfterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError;
import com.ccp.especifications.db.utils.entity.decorators.annotations.messages.write.CcpAfterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldTransformer;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.jn.business.messages.JnMessages;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.decorators.engine.JnAsyncWriterEntity;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDoNothing;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * A request of the user to receive the login token again: its insertion notifies support of the pending request and its deletion (when support fulfills it) notifies the solution; the twin keeps the fulfilled requests.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_token_request_resend}</li>
 * <li>on {@code afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError} sends {@code JnNotifySupportAboutSolvedResendLoginToken}</li>
 * <li>on {@code afterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError} sends {@code JnNotifySupportAboutPendingResendLoginToken}</li>
 * <li>twin entity {@code js_login_token_fulfilled_resend}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>written asynchronously, through messaging</li>
 * <li>disposable: records expire by the {@code daily} granularity</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityAsyncWriter(JnAsyncWriterEntity.class)
@CcpEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.daily)
@CcpEntityFieldsValidator(JsEntityLoginTokenRequestResend.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpAfterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(JnMessages.JnNotifySupportAboutSolvedResendLoginToken.class)
@CcpAfterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(JnMessages.JnNotifySupportAboutPendingResendLoginToken.class)
@CcpEntityTwin(
		twinEntityName = "js_login_token_fulfilled_resend",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)
public class JsEntityLoginTokenRequestResend implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityLoginTokenRequestResend INSTANCE = new JsEntityLoginTokenRequestResend();

	private JsEntityLoginTokenRequestResend() {}

	/** The entity {@code js_login_token_request_resend}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginTokenRequestResend.class).entityInstance;
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}, transformed by {@code JnJsonTransformersFieldsEntityDoNothing}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpEntityFieldTransformer(JnJsonTransformersFieldsEntityDoNothing.class)
		email, 

		/** The {@code chatId} field: validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		chatId
		;
	}
}
