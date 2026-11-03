package com.js.services;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** Input validation of {@link JsServicePrivacyPolicy#GetPrivacyPolicy}. */
enum GetPrivacyPolicy implements CcpJsonFieldName{
	/** The {@code language} field: one of {@code JnLanguage}, validated as in {@code JnJsonCommonsFields}, required. */
	@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
	@CcpJsonFieldValidatorRequired
	language,
}
