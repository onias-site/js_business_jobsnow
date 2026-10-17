package com.js.services;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.json.fields.validation.JnJsonCommonsFields;

import com.ccp.json.fields.validation.CcpJsonCommonsFields;

/**
 * Builds the data about the token returned by the login services: the record kept in its expiration copy (with its
 * dates), plus the {@code email} and the {@code sessionToken}.
 */
class LoadDataAboutToken implements CcpBusiness{
	
	/** The single instance. */
	public static final LoadDataAboutToken INSTANCE = new LoadDataAboutToken();

	/**
	 * Builds the data about the token.
	 * @param json the request plus the records under {@code _entities}
	 * @return the data about the token
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation innerJsonFromPath = json.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JnEntityDisposableRecord.ENTITY);
		CcpJsonRepresentation whenAnyFieldsAreFound = innerJsonFromPath.whenAnyFieldsAreFound(JsonTransformer.INSTANCE, JnJsonCommonsFields.timestamp);
		CcpJsonRepresentation jsonPiece = json.getJsonPiece(JnJsonCommonsFields.email, CcpJsonCommonsFields.sessionToken);
		CcpJsonRepresentation mergedJson = whenAnyFieldsAreFound.mergeWithAnotherJson(jsonPiece);
		return mergedJson;
		
	}
}
