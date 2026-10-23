package com.js.entities;

import java.util.List;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
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
import com.js.business.bots.login.token.JsSupportLoginTokenBotConfiguration;
/**
 * A request of the user to unlock the login token: like {@code JsEntityLoginTokenRequestResend}, with the unlock notifications; the twin keeps the fulfilled requests.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_token_request_unlock}</li>
 * <li>on {@code afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError} sends {@code JnNotifySupportAboutSolvedLockedLoginToken}</li>
 * <li>on {@code afterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError} sends {@code JnNotifySupportAboutPendingLockedLoginToken}</li>
 * <li>twin entity {@code js_login_token_fulfilled_unlock}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>written asynchronously, through messaging</li>
 * <li>disposable: records expire by the {@code daily} granularity</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityAsyncWriter(JnAsyncWriterEntity.class)
@CcpEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.daily)
@CcpEntityFieldsValidator(JsEntityLoginTokenRequestUnlock.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpAfterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(JnMessages.JnNotifySupportAboutSolvedLockedLoginToken.class)
@CcpAfterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(JnMessages.JnNotifySupportAboutPendingLockedLoginToken.class)
@CcpEntityTwin(
		twinEntityName = "js_login_token_fulfilled_unlock",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)


public class JsEntityLoginTokenRequestUnlock implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityLoginTokenRequestUnlock INSTANCE = new JsEntityLoginTokenRequestUnlock();

	private JsEntityLoginTokenRequestUnlock() {}

	/** The entity {@code js_login_token_request_unlock}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginTokenRequestUnlock.class).entityInstance;
	 
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

	/**
	 * Seeds the {@code solveLoginTokenTicket} command of the support bot, which solves these tickets and the ones to resend
	 * the token, and the templates of the notices of both.
	 * @return the seed records
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		List<CcpBulkItem> botSeeds = JsSupportLoginTokenBotConfiguration.getSeeds();
		return botSeeds;
	}
}
