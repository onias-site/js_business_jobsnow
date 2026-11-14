package com.js.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChildCounter;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperParentCombinations;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionablePurge;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpAfterSaveFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.decorators.engine.JnGrouperChildCurrentCounterEntityBeforeWrite;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import com.jn.entities.decorators.engine.JnVersionablePurgeEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.business.login.JsBusinessPublishLoginAnswersSaved;
import com.js.json.transformers.JsLoginChannelCounterCombinations;
import com.js.json.transformers.JsLoginChannelCounterKeys;
import com.js.json.transformers.JsLoginGoalCounterCombinations;
import com.js.json.transformers.JsLoginGoalCounterKeys;

/**
 * Answers of the sign-up questionnaire: how the user arrived ({@code channel}) and the goal on the platform ({@code goal}).
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_answers}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * <li>after each save, the answers are announced to whoever listens in {@code jn_write_event_subscribers}
 * ({@link JsBusinessPublishLoginAnswersSaved}): vis turns the goal {@code recruiting} into a recruiter</li>
 * <li>counted in {@code jn_grouper} by goal ({@link JsEntityVirtualLoginGoalCounter}) and by channel
 * ({@link JsEntityVirtualLoginChannelCounter}): each group holds {@code {"<value>": n}}, the current state</li>
 * </ul>
 */
@CcpAfterSaveFromMainEntity(execute = {JsBusinessPublishLoginAnswersSaved.class}, operationHandlers = {})
// one group counts the answers by goal and another by channel: a save that changes the value moves the count, the
// delete subtracts it
@CcpEntityGrouperChildCounter(value = {JsEntityVirtualLoginGoalCounter.class}, counterKeyClass = JsLoginGoalCounterKeys.class, beforeDecoratorClass = JnGrouperChildCurrentCounterEntityBeforeWrite.class)
@CcpEntityGrouperParentCombinations(parent = JsEntityVirtualLoginGoalCounter.class, value = JsLoginGoalCounterCombinations.class)
@CcpEntityGrouperChildCounter(value = {JsEntityVirtualLoginChannelCounter.class}, counterKeyClass = JsLoginChannelCounterKeys.class, beforeDecoratorClass = JnGrouperChildCurrentCounterEntityBeforeWrite.class)
@CcpEntityGrouperParentCombinations(parent = JsEntityVirtualLoginChannelCounter.class, value = JsLoginChannelCounterCombinations.class)
@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityVersionablePurge(JnVersionablePurgeEntity.class)
@CcpEntityFieldsValidator(JsEntityLoginAnswers.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JsEntityLoginAnswers implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityLoginAnswers INSTANCE = new JsEntityLoginAnswers();

	private JsEntityLoginAnswers() {}

	/** The entity {@code js_login_answers}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginAnswers.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email, 
		/** The {@code channel} field: required, text. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(allowedValuesEnum = VisChannelTypes.class)
		channel, 
		/** The {@code goal} field: required, text. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(allowedValuesEnum = VisGoalTypes.class)
		goal
		;
	}
	
	/** How the user arrived at the platform. */
	public static enum VisChannelTypes{
		/** Through LinkedIn. */
		linkedin,
		/** Through Telegram. */
		telegram,
		/** Through friends. */
		friends,
		/** Another channel. */
		others}
	/** The goal of the user on the platform. */
	public static enum VisGoalTypes{
		/** Looking for jobs. */
		jobs,
		/** Recruiting. */
		recruiting}
}

