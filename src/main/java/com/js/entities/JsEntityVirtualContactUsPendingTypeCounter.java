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
 * Virtual entity whose id identifies the group of {@code JnEntityGrouper} that counts the tickets still waiting for the
 * support ({@link JsEntityContactUs}, the main side), by type: its child is a JSON text {@code {"<type>": n}}
 * ({@code @CcpEntityGrouperChildCounter} with {@code JnGrouperChildCurrentCounterEntityBeforeWrite}, the current state):
 * the opening adds 1 and the answer (the move to the twin {@code js_contact_us_solved}) subtracts 1. Together with the
 * history of {@link JsEntityVirtualContactUsTypeCounter}, the screen Curiosities shows the answered and the pending
 * tickets of each type. Its only field always holds {@code pendingType}. Cached for 1 hour. Created on 2026-11-17.
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JsEntityVirtualContactUsPendingTypeCounter.Fields.class)
public class JsEntityVirtualContactUsPendingTypeCounter {

	/** The entity {@code js_virtual_contact_us_pending_type_counter}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityVirtualContactUsPendingTypeCounter.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code counter} field: the primary key, always {@code pendingType}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 50)
		counter,
		;
	}

	/** The value of the field {@code counter}. */
	static enum Values implements CcpJsonFieldName{
		pendingType,
	}

	/**
	 * The combination of the group: {@code {"counter": "pendingType"}}.
	 * @return the combination
	 */
	public static CcpJsonRepresentation getCombination() {
		String counterName = Values.pendingType.getValue();
		CcpJsonRepresentation combination = CcpOtherConstants.EMPTY_JSON.put(Fields.counter, counterName);
		return combination;
	}
}
