package com.js.services;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.messages.JnMessageType.JnBotType;

/** Input rules of {@link JsServiceTelegramWebhook#ReceiveTelegramUpdate}. */
enum ReceiveTelegramUpdate implements CcpJsonFieldName{
	/** The {@code botName} field: required, one of the bots of the platform. */
	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeString(allowedValuesEnum = JnBotType.class)
	botName,

	/** The {@code webhookSecret} field: text, empty when the request did not bring it: that is a 401 of the service, not a 422. */
	@CcpJsonFieldTypeString(allowsEmptyString = true)
	webhookSecret,

	/** The {@code update_id} field: required, the id of the update. */
	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeNumber(minValue = 0)
	update_id,
}
