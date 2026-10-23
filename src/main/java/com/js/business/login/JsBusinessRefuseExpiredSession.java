package com.js.business.login;

import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.jn.entities.JnEntityDisposableRecord;
import com.js.entities.JsEntityLoginSessionValidation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.status.login.JsProcessStatusExecuteLogin;

/**
 * Refuses, without counting a wrong session token attempt, a session that is no longer open but existed: its disposable
 * copy (in {@code jn_disposable_record}) is still there, expired. Runs only when the session is not open; a token that
 * never belonged to a session of the e-mail, IP and user agent goes on to the status that counts the attempt.
 *
 * <p>An expired session is not a guess: until 2026-10-13 every request of an expired session counted an attempt, and a
 * browser tab left open for more than an hour locked the password of its owner after three requests (the screen of the
 * resume asks the status of the resume as soon as it opens).
 */
public class JsBusinessRefuseExpiredSession implements CcpBusiness {

	/** The single instance: the business keeps no state. */
	public static final JsBusinessRefuseExpiredSession INSTANCE = new JsBusinessRefuseExpiredSession();

	private JsBusinessRefuseExpiredSession() {}

	/**
	 * Looks for the disposable copy of the session (the same key {@code JnDisposableEntity} writes: entity name and the
	 * primary key after the field transformers).
	 * @param json the request with the session {@code token}, plus the entities loaded under {@code _entities}
	 * @return the same request, when the session never existed
	 * @throws CcpErrorFlowDisturb with {@code invalidSession} (401), when the session existed and expired
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpEntityMetaData metaData = JsEntityLoginSessionValidation.ENTITY.getEntityMetaData();
		CcpJsonRepresentation transformedRequest = JsEntityLoginSessionValidation.ENTITY.getHandledJson(json);
		Supplier<CcpJsonRepresentation> transformedRequestSupplier = transformedRequest.getJsonSupplier();
		CcpJsonRepresentation transformedPrimaryKey = metaData.getPrimaryKeyValues(transformedRequestSupplier);
		String serializedPrimaryKey = transformedPrimaryKey.asUgglyJson();

		CcpJsonRepresentation copyKeyWithEntity = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.entity, metaData.entityName);
		CcpJsonRepresentation copyKey = copyKeyWithEntity.put(JnJsonCommonsFields.id, serializedPrimaryKey);
		boolean neverExisted = false == JnEntityDisposableRecord.ENTITY.exists(copyKey);

		if(neverExisted) {
			return json;
		}

		CcpJsonRepresentation jsonWithoutEntities = json.removeFields(CcpEntity.JsonFieldNames._entities);
		CcpErrorFlowDisturb expiredSession = new CcpErrorFlowDisturb(jsonWithoutEntities, JsProcessStatusExecuteLogin.invalidSession);
		throw expiredSession;
	}
}
