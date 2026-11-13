package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.entities.JsEntityLoginTokenResentWithoutPassword;

/**
 * Run after each insertion in {@code js_login_password}: the user now has a password, so the record of
 * {@link JsEntityLoginTokenResentWithoutPassword} (token resent, no password) is deleted, when there is one.
 * <p>
 * Input: the password record, with the e-mail. Output: the input.
 */
public class JsBusinessDeleteTokenResentWithoutPassword implements CcpBusiness {

	/** Built by reflection, from the annotation of {@code JsEntityLoginPassword}. */
	private JsBusinessDeleteTokenResentWithoutPassword() {}

	/**
	 * Deletes the record of the e-mail, if any.
	 * @param json the password record
	 * @return the input
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation key = json.getJsonPiece(JnJsonCommonsFields.email);
		JsEntityLoginTokenResentWithoutPassword.ENTITY.delete(key);
		return json;
	}
}
