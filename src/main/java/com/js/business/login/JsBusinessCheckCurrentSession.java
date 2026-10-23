package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatus;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.fields.transformers.JsJsonTransformersFieldEntityTokenHash;
import com.js.status.login.JsProcessStatusExecuteLogin;
import com.js.status.login.JsProcessStatusExecuteLogout;

/**
 * Accepts the session only when it is the current session of the e-mail: the hash of its {@code token} must be the one
 * stored in {@code JsEntityLoginSessionConflict}, loaded under {@code _entities} before this check. Each e-mail has one
 * current session; a login or a password save replaces it, and every older session of the e-mail stops being accepted
 * (until 2026-10-11 "undo the other current login" and the password changes left the old sessions valid).
 *
 * <p>A session refused here does not count as a wrong session token attempt: it was valid until another one replaced it,
 * and an old browser tab repeating requests would otherwise lock the password of its owner.
 */
public class JsBusinessCheckCurrentSession implements CcpBusiness {

	/** The check of the session validation: a replaced session is an invalid session (401). */
	public static final JsBusinessCheckCurrentSession TO_VALIDATE_LOGIN = new JsBusinessCheckCurrentSession(JsProcessStatusExecuteLogin.invalidSession);

	/**
	 * The check of the logout: a replaced session is no longer logged in (404), and its logout must not end the current
	 * session.
	 */
	public static final JsBusinessCheckCurrentSession TO_EXECUTE_LOGOUT = new JsBusinessCheckCurrentSession(JsProcessStatusExecuteLogout.missingLogin);

	/** The status thrown when the session is not the current one. */
	private final CcpProcessStatus statusWhenNotCurrent;

	/**
	 * Builds the check.
	 * @param statusWhenNotCurrent the status thrown when the session is not the current one
	 */
	private JsBusinessCheckCurrentSession(CcpProcessStatus statusWhenNotCurrent) {
		this.statusWhenNotCurrent = statusWhenNotCurrent;
	}

	/**
	 * Compares the hash of the session {@code token} with the one of the current session.
	 * @param json the request with the session {@code token}, plus the current session under {@code _entities}
	 * @return the request, when the session is the current one
	 * @throws CcpErrorFlowDisturb with the status of this check, when it is not
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String currentSessionTokenHash = json.getValueFromPath("", CcpEntity.JsonFieldNames._entities, JsEntityLoginSessionConflict.ENTITY, JsEntityLoginSessionConflict.Fields.currentSessionTokenHash);

		JsJsonTransformersFieldEntityTokenHash tokenHash = new JsJsonTransformersFieldEntityTokenHash();
		CcpJsonRepresentation jsonWithTokenHash = tokenHash.execute(json);
		String sessionTokenHash = jsonWithTokenHash.getAsString(JsEntityLoginSessionValidation.Fields.token);

		boolean isTheCurrentSession = sessionTokenHash.equals(currentSessionTokenHash);

		if(isTheCurrentSession) {
			return json;
		}

		CcpJsonRepresentation jsonWithoutEntities = json.removeFields(CcpEntity.JsonFieldNames._entities);
		CcpErrorFlowDisturb notTheCurrentSession = new CcpErrorFlowDisturb(jsonWithoutEntities, this.statusWhenNotCurrent);
		throw notTheCurrentSession;
	}
}
