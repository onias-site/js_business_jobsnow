package com.js.entities.fields.transformers;

import com.jn.entities.fields.transformers.JnJsonTransformersFieldEntityFieldCalculateHash;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;

import com.js.entities.JsEntityLoginSessionValidation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;

import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Transforms the {@code token} of the session into its SHA-1 hash, keeping the original in {@code originalToken}
 * (a new token is generated when there is none).
 */
public class JsJsonTransformersFieldEntityTokenHash extends JnJsonTransformersFieldEntityFieldCalculateHash{
	/** Configures the transformer for the session token. */
	public JsJsonTransformersFieldEntityTokenHash() {
		super(JnJsonCommonsFields.originalToken, JsEntityLoginSessionValidation.Fields.token, JsonFieldNames.tokenHash);
	}
}
