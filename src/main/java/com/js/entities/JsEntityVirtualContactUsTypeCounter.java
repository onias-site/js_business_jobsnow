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
 * Virtual entity whose id identifies the only group of {@code JnEntityGrouper} that counts the tickets of the contact us
 * ({@link JsEntityContactUs}) ever opened, by {@code type}: its child is a JSON text with, for each type, how many
 * tickets were opened ({@code @CcpEntityGrouperChildCounter} with {@code JnGrouperChildCounterEntityBeforeWrite} and
 * {@code JsContactUsTypeCounterKeys}, a history): the opening of a ticket adds 1, its answer (the move to the twin) and a
 * new save of the same open ticket count nothing. Its only field always holds {@code type}. Cached for 1 hour.
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JsEntityVirtualContactUsTypeCounter.Fields.class)
public class JsEntityVirtualContactUsTypeCounter {

	/** The entity {@code js_virtual_contact_us_type_counter}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityVirtualContactUsTypeCounter.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code counter} field: the primary key, always {@code type}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 50)
		counter,
		;
	}

	/**
	 * The combination of the only group: {@code {"counter": "type"}}.
	 * @return the combination
	 */
	public static CcpJsonRepresentation getCombination() {
		String counterName = JsEntityContactUs.Fields.type.getValue();
		CcpJsonRepresentation combination = CcpOtherConstants.EMPTY_JSON.put(Fields.counter, counterName);
		return combination;
	}
}
