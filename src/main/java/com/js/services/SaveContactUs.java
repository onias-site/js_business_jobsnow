package com.js.services;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.js.entities.JsEntityContactUs;

/**
 * Body validation of {@link JsServiceContactUs#SaveContactUs}. Each field copies the rules straight from the entity
 * that declares them, because {@code CcpJsonCopyFieldValidationsFrom} is not recursive.
 */
enum SaveContactUs implements CcpJsonFieldName{
	/** The {@code email} field: validated as in {@code JsEntityContactUs}, required. */
	@CcpJsonCopyFieldValidationsFrom(JsEntityContactUs.Fields.class)
	@CcpJsonFieldValidatorRequired
	email,

	/** The {@code type} field: validated as in {@code JsEntityContactUs}, required. */
	@CcpJsonCopyFieldValidationsFrom(JsEntityContactUs.Fields.class)
	@CcpJsonFieldValidatorRequired
	type,

	/** The {@code title} field: validated as in {@code JsEntityContactUs}, required. */
	@CcpJsonCopyFieldValidationsFrom(JsEntityContactUs.Fields.class)
	@CcpJsonFieldValidatorRequired
	title,

	/** The {@code description} field: validated as in {@code JsEntityContactUs}, required. */
	@CcpJsonCopyFieldValidationsFrom(JsEntityContactUs.Fields.class)
	@CcpJsonFieldValidatorRequired
	description,

	/** The {@code stackTrace} field: validated as in {@code JsEntityContactUs}. */
	@CcpJsonCopyFieldValidationsFrom(JsEntityContactUs.Fields.class)
	stackTrace,
}
