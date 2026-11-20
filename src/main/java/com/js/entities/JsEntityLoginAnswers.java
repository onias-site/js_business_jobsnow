package com.js.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChildCounter;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperParentCombinations;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityInsertionDate;
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
import com.jn.entities.decorators.engine.JnInsertionDateEntity;
import com.jn.entities.fields.counters.JnInsertionDayCounterKeys;
import com.jn.entities.fields.counters.JnInsertionYearMonthCounterKeys;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import com.jn.entities.decorators.engine.JnVersionablePurgeEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.business.login.JsBusinessPublishLoginAnswersSaved;
import com.js.json.transformers.JsInsertionDayCounterCombinations;
import com.js.json.transformers.JsInsertionMonthCounterCombinations;
import com.js.json.transformers.JsLoginChannelCounterCombinations;
import com.js.json.transformers.JsLoginChannelCounterKeys;
import com.js.json.transformers.JsLoginChannelGoalCounterCombinations;
import com.js.json.transformers.JsLoginChannelGoalCounterKeys;
import com.js.json.transformers.JsLoginGoalCounterCombinations;
import com.js.json.transformers.JsLoginGoalCounterKeys;

/**
 * Answers of the sign-up questionnaire: how the user arrived ({@code channel}) and the goal on the platform ({@code goal}).
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_answers}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in its history ({@code JnVersionableHistory}, in {@code jn_grouper})</li>
 * <li>after each save, the answers are announced to whoever listens in {@code jn_write_event_subscribers}
 * ({@link JsBusinessPublishLoginAnswersSaved}): vis turns the goal {@code recruiting} into a recruiter</li>
 * <li>counted in {@code jn_grouper} by goal ({@link JsEntityVirtualLoginGoalCounter}) and by channel
 * ({@link JsEntityVirtualLoginChannelCounter}): each group holds {@code {"<value>": n}}, the current state</li>
 * <li>keeps the moment of its insertion in {@code insertionDate}, counted in {@code jn_grouper} by day
 * ({@link JsEntityVirtualInsertionDayCounter}, {@code {"yyyyMMdd": n}}) and by month
 * ({@link JsEntityVirtualInsertionMonthCounter}, {@code {"yyyyMM": n}})</li>
 * </ul>
 */
@CcpAfterSaveFromMainEntity(execute = {JsBusinessPublishLoginAnswersSaved.class}, operationHandlers = {})
// the moment of the insertion of the answers, kept in insertionDate across the saves
@CcpEntityInsertionDate(JnInsertionDateEntity.class)
// the answers inserted in each day (yyyyMMdd) and in each month (yyyyMM) that still exist
@CcpEntityGrouperChildCounter(value = {JsEntityVirtualInsertionDayCounter.class}, counterKeyClass = JnInsertionDayCounterKeys.class, beforeDecoratorClass = JnGrouperChildCurrentCounterEntityBeforeWrite.class)
@CcpEntityGrouperParentCombinations(parent = JsEntityVirtualInsertionDayCounter.class, value = JsInsertionDayCounterCombinations.class)
@CcpEntityGrouperChildCounter(value = {JsEntityVirtualInsertionMonthCounter.class}, counterKeyClass = JnInsertionYearMonthCounterKeys.class, beforeDecoratorClass = JnGrouperChildCurrentCounterEntityBeforeWrite.class)
@CcpEntityGrouperParentCombinations(parent = JsEntityVirtualInsertionMonthCounter.class, value = JsInsertionMonthCounterCombinations.class)
// one group counts the answers by goal and another by channel: a save that changes the value moves the count, the
// delete subtracts it
@CcpEntityGrouperChildCounter(value = {JsEntityVirtualLoginGoalCounter.class}, counterKeyClass = JsLoginGoalCounterKeys.class, beforeDecoratorClass = JnGrouperChildCurrentCounterEntityBeforeWrite.class)
@CcpEntityGrouperParentCombinations(parent = JsEntityVirtualLoginGoalCounter.class, value = JsLoginGoalCounterCombinations.class)
@CcpEntityGrouperChildCounter(value = {JsEntityVirtualLoginChannelCounter.class}, counterKeyClass = JsLoginChannelCounterKeys.class, beforeDecoratorClass = JnGrouperChildCurrentCounterEntityBeforeWrite.class)
@CcpEntityGrouperParentCombinations(parent = JsEntityVirtualLoginChannelCounter.class, value = JsLoginChannelCounterCombinations.class)
// 2026-11-17: the channel crossed with the goal, the chart "Origem x objetivo" of the screen Curiosities
@CcpEntityGrouperChildCounter(value = {JsEntityVirtualLoginChannelGoalCounter.class}, counterKeyClass = JsLoginChannelGoalCounterKeys.class, beforeDecoratorClass = JnGrouperChildCurrentCounterEntityBeforeWrite.class)
@CcpEntityGrouperParentCombinations(parent = JsEntityVirtualLoginChannelGoalCounter.class, value = JsLoginChannelGoalCounterCombinations.class)
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
		goal,
		/**
		 * The {@code insertionDate} field: the moment the answers were inserted, kept by {@code @CcpEntityInsertionDate}
		 * (the value of the input is ignored), validated as in {@code JnJsonCommonsFields}.
		 */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		insertionDate,
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

