package com.js.services;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityDisposableRecord;

/** Turns an expiration copy into the original record plus its dates (see {@code JnEntityDisposableRecord.getDataWithTimeStamp}). */
class JsonTransformer implements CcpBusiness{
	/** The single instance. */
	public static final JsonTransformer INSTANCE = new JsonTransformer();
	
	/** Singleton; use {@link #INSTANCE}. */
	private JsonTransformer() {}

	/**
	 * Returns the original record with its dates, merged with the copy.
	 * @param json the expiration copy
	 * @return the original record with the dates
	 */
	@Override
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
	CcpJsonRepresentation dataWithTimeStamp = JnEntityDisposableRecord.getDataWithTimeStamp(json);
		CcpJsonRepresentation mergeWithAnotherJson = dataWithTimeStamp.mergeWithAnotherJson(json);
		return mergeWithAnotherJson;
	}
	
}
