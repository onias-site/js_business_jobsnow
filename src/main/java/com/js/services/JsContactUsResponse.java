package com.js.services;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Fields of the answers of {@link JsServiceContactUs}: it answers with the json it received, so its procedure returns no
 * field ({@code inexistentField}).
 */
enum JsContactUsResponse implements CcpJsonFieldName{
	/** The {@code inexistentField} field. */
	inexistentField,
}
