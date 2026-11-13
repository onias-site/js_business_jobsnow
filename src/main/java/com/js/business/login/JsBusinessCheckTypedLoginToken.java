package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.jn.business.session.JnBusinessCheckLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.entities.JsEntityLoginToken;

/**
 * Tells whether the token typed by the user is the login token e-mailed to them (since 2026-11-12): the validator
 * {@code loginToken} of {@code jn_session_validator}, which the user bot runs by class name through
 * {@link JnBusinessCheckLoginToken}. The token is kept as a BCrypt hash, compared by {@link CcpPasswordHandler}. An
 * e-mail without a token (never asked, expired or locked) has no right token. Counts no attempts: the caller keeps its
 * own count.
 */
public class JsBusinessCheckTypedLoginToken implements CcpBusiness {

	/** The single instance: the class keeps no state. */
	public final static JsBusinessCheckTypedLoginToken INSTANCE = new JsBusinessCheckTypedLoginToken();

	private JsBusinessCheckTypedLoginToken() {}

	/**
	 * Compares the typed token with the stored one.
	 * @param json {@code email} and the typed {@code token}
	 * @return the json with {@code rightToken}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String typedToken = json.getAsString(JnJsonCommonsFields.token);
		String typedTokenTrim = typedToken.trim();
		String storedToken = getStoredToken(json);
		boolean nothingToCompare = typedTokenTrim.isEmpty() || storedToken.isEmpty();
		if(nothingToCompare) {
			return json.put(JnBusinessCheckLoginToken.Fields.rightToken, false);
		}
		CcpPasswordHandler passwordHandler = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
		boolean rightToken = passwordHandler.matches(typedTokenTrim, storedToken);
		CcpJsonRepresentation checked = json.put(JnBusinessCheckLoginToken.Fields.rightToken, rightToken);
		return checked;
	}

	/**
	 * The hash of the token of the e-mail.
	 * @param json the request, with {@code email}
	 * @return the hash, or an empty text when the e-mail has no token
	 */
	private static String getStoredToken(CcpJsonRepresentation json) {
		String email = json.getAsString(JnJsonCommonsFields.email);
		CcpJsonRepresentation tokenKey = CcpOtherConstants.EMPTY_JSON.put(JsEntityLoginToken.Fields.email, email);
		try {
			CcpJsonRepresentation storedRecord = JsEntityLoginToken.ENTITY.getOneById(tokenKey);
			String storedToken = storedRecord.getAsString(JsEntityLoginToken.Fields.token);
			return storedToken;
		} catch (CcpErrorFlowDisturb noToken) {
			return "";
		}
	}
}
