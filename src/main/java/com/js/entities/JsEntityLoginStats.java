package com.js.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeTimeBefore;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;

/**
 * Login statistics of the user: balance, last access, access count, opened and closed tickets and balance transactions count.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code js_login_stats}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JsEntityLoginStats.Fields.class)
public class JsEntityLoginStats implements CcpEntityConfigurator {
	
	/** The entity {@code js_login_stats}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityLoginStats.class).entityInstance;
	
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, text. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(minLength = 35, maxLength = 50)
		email, 
		/** The {@code balance} field: required, decimal number. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumber(minValue = -1_000_000, maxValue = 1_000_000)
		balance, 
		/** The {@code lastAccess} field: required, past timestamp. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeTimeBefore(intervalType = CcpEntityExpurgableOptions.yearly, maxValue = 100, minValue = 0)
		lastAccess, 
		/** The {@code countAccess} field: required, non-negative integer. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		countAccess,
		/** The {@code openedTickets} field: required, non-negative integer. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		openedTickets, 
		/** The {@code closedTickets} field: required, non-negative integer. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		closedTickets, 
		/** The {@code balanceTransacionsCount} field: required, non-negative integer. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		balanceTransacionsCount
		;
	}
}
