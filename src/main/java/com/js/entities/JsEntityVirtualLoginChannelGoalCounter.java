package com.js.entities;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;

/**
 * Virtual entity whose id identifies the group of {@code JnEntityGrouper} that counts the answers of the sign-up
 * questionnaire ({@link JsEntityLoginAnswers}) by channel and goal together: its child is a JSON text with, for each
 * {@code <channel>|<goal>}, how many answers hold it now ({@code @CcpEntityGrouperChildCounter} with
 * {@code JnGrouperChildCurrentCounterEntityBeforeWrite} and {@code JsLoginChannelGoalCounterKeys}): which channel brings
 * the candidates and which brings the recruiters. Its only field always holds {@code channelGoal}. Cached for 1 hour.
 * Created on 2026-11-17 for the screen Curiosities.
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JsEntityVirtualLoginChannelGoalCounter.Fields.class)
public class JsEntityVirtualLoginChannelGoalCounter {

	/** The entity {@code js_virtual_login_channel_goal_counter}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityVirtualLoginChannelGoalCounter.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code counter} field: the primary key, always {@code channelGoal}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 50)
		counter,
		;
	}

	/** The value of the field {@code counter}. */
	static enum Values implements CcpJsonFieldName{
		channelGoal,
	}

	/**
	 * The combination of the group: {@code {"counter": "channelGoal"}}.
	 * @return the combination
	 */
	public static CcpJsonRepresentation getCombination() {
		String counterName = Values.channelGoal.getValue();
		CcpJsonRepresentation combination = CcpOtherConstants.EMPTY_JSON.put(Fields.counter, counterName);
		return combination;
	}
}
