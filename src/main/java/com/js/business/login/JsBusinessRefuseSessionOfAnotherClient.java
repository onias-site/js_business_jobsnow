package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.fields.transformers.JsJsonTransformersFieldEntityTokenHash;
import com.js.status.login.JsProcessStatusExecuteLogin;

/**
 * Refuses, without counting a wrong session token attempt, the token of the current session of the e-mail sent by
 * another client (another user agent or IP): the session of that client does not exist, but the token is right, so it
 * is not a guess against the password. Runs only when the session is not open for the client of the request; the hash
 * of the token is compared with the one of the current session, loaded under {@code _entities} before this check.
 *
 * <p>Until 2026-10-14 three such requests locked the password (423) while the session went on valid on its own client.
 */
public class JsBusinessRefuseSessionOfAnotherClient implements CcpBusiness {

	/** The single instance: the business keeps no state. */
	public static final JsBusinessRefuseSessionOfAnotherClient INSTANCE = new JsBusinessRefuseSessionOfAnotherClient();

	private JsBusinessRefuseSessionOfAnotherClient() {}

	/**
	 * Compares the hash of the session {@code token} with the one of the current session of the e-mail.
	 * @param json the request with the session {@code token}, plus the current session under {@code _entities}
	 * @return the same request, when the token is not the one of the current session
	 * @throws CcpErrorFlowDisturb with {@code invalidSession} (401), when it is the token of the current session
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String currentSessionTokenHash = json.getValueFromPath("", CcpEntity.JsonFieldNames._entities, JsEntityLoginSessionConflict.ENTITY, JsEntityLoginSessionConflict.Fields.currentSessionTokenHash);

		JsJsonTransformersFieldEntityTokenHash tokenHash = new JsJsonTransformersFieldEntityTokenHash();
		CcpJsonRepresentation jsonWithTokenHash = tokenHash.execute(json);
		String sessionTokenHash = jsonWithTokenHash.getAsString(JsEntityLoginSessionValidation.Fields.token);

		boolean isAnotherToken = false == sessionTokenHash.equals(currentSessionTokenHash);

		if(isAnotherToken) {
			return json;
		}

		CcpJsonRepresentation jsonWithoutEntities = json.removeFields(CcpEntity.JsonFieldNames._entities);
		CcpErrorFlowDisturb sessionOfAnotherClient = new CcpErrorFlowDisturb(jsonWithoutEntities, JsProcessStatusExecuteLogin.invalidSession);
		throw sessionOfAnotherClient;
	}
}
