package com.js.business.grouper;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.business.grouper.JnBusinessGetGroup;
import com.jn.entities.JnEntityLoginEmail;

/**
 * The endpoint of the groups serves only the groups of the logged-in user: the parent must be {@code jn_login_email}
 * and its id the one of the e-mail of the session. Until 2026-11-10 the {@code parentId} of the path was read as it
 * came, and since it is calculated only from the e-mail (the SHA-1 of the SHA-1), any logged-in user who knew another
 * one's e-mail read that one's login history (IPs, browsers, dates) and tickets. Another group answers 404, the same
 * answer of a group that does not exist, so the endpoint does not tell which groups exist.
 */
public class JsBusinessOnlyTheGroupsOfTheSessionOwner implements CcpBusiness {

	/** The single instance. */
	public static final JsBusinessOnlyTheGroupsOfTheSessionOwner INSTANCE = new JsBusinessOnlyTheGroupsOfTheSessionOwner();

	private JsBusinessOnlyTheGroupsOfTheSessionOwner() {}

	/**
	 * Compares the parent of the group asked with the e-mail of the session.
	 * @param json the session values (the e-mail validated by the session filter) and the primary key of the group
	 * @return the same JSON
	 * @throws CcpErrorFlowDisturb with {@code NOT_FOUND} when the group is not of the logged-in user
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String email = json.getAsString(JnEntityLoginEmail.Fields.email);
		CcpStringDecorator emailDecorator = new CcpStringDecorator(email);
		CcpHashDecorator emailHashDecorator = emailDecorator.hash();
		String emailHash = emailHashDecorator.asString(CcpHashAlgorithm.SHA1);
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(JnEntityLoginEmail.Fields.email, emailHash);
		String ownerParentId = JnEntityLoginEmail.ENTITY.calculateId(parentJson);
		CcpEntityMetaData ownerMetaData = JnEntityLoginEmail.ENTITY.getEntityMetaData();
		String ownerParentName = ownerMetaData.entityName;
		String parentName = json.getAsString(JnBusinessGetGroup.Fields.parentName);
		String parentId = json.getAsString(JnBusinessGetGroup.Fields.parentId);
		boolean anotherParent = false == ownerParentName.equals(parentName);
		boolean anotherOwner = false == ownerParentId.equals(parentId);

		if(anotherParent || anotherOwner) {
			throw new CcpErrorFlowDisturb(json, CcpProcessStatusDefault.NOT_FOUND);
		}

		return json;
	}
}
