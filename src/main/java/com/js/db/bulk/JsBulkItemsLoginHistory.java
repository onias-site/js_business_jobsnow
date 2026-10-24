package com.js.db.bulk;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;
import com.jn.entities.JnEntityGrouper;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;
import com.jn.entities.JnEntityLoginEmail;
import com.js.entities.JsEntityLoginSessionValidation;

/**
 * The bulk items that keep the history of the logins of a user in the group of its e-mail in {@code jn_grouper}
 * ({@code parentName} {@code jn_login_email}, {@code childName} {@code js_login_session_validation}): each login adds,
 * on the main side, the JSON text {@code {userAgent, ip, loginDate}}, and each logout adds, on the twin side
 * ({@code jn_inactive_grouper}), the JSON text {@code {userAgent, ip, logoutDate}}, both dated now
 * ({@code dd/MM/yyyy HH:mm:ss}). Each side keeps the last {@link #MAX_CHILDREN}. The items go in the same bulk as the
 * session (the login and the logout write by bulk, which no side effect of the entity sees).
 * <p>
 * The json received is the one the bulk handlers receive, already transformed: its e-mail is the SHA-1, the value the
 * group keeps in {@code parentJson} and whose id in {@code jn_login_email} is the {@code parentId}.
 */
public class JsBulkItemsLoginHistory implements Function<CcpJsonRepresentation, List<CcpBulkItem>> {

	/** The fields and texts of the history. */
	enum Fields implements CcpJsonFieldName {
		/** The date of the login. */
		loginDate,
		/** The date of the logout. */
		logoutDate,
		/** The format of the dates. */
		DATE_FORMAT {
			public String getValue() {
				return "dd/MM/yyyy HH:mm:ss";
			}
		},
	}

	/** The most items each side of the history keeps. */
	public static final int MAX_CHILDREN = 100;

	/** The history of a login: on the main side, with {@code loginDate}. */
	public static final JsBulkItemsLoginHistory LOGIN = new JsBulkItemsLoginHistory(CcpEntityPhase.mainEntity, Fields.loginDate);

	/** The history of a logout: on the twin side, with {@code logoutDate}. */
	public static final JsBulkItemsLoginHistory LOGOUT = new JsBulkItemsLoginHistory(CcpEntityPhase.twinEntity, Fields.logoutDate);

	/** The side of the grouper written. */
	private final CcpEntityPhase side;

	/** The field of the date of the event. */
	private final CcpJsonFieldName dateField;

	/**
	 * Names the side and the field of the date.
	 * @param side the side of the grouper written
	 * @param dateField the field of the date of the event
	 */
	private JsBulkItemsLoginHistory(CcpEntityPhase side, CcpJsonFieldName dateField) {
		this.side = side;
		this.dateField = dateField;
	}

	/**
	 * The item that adds the event to the group of the e-mail.
	 * @param json the session, already transformed, with {@code email}, {@code ip} and {@code userAgent}
	 * @return the bulk items, one
	 */
	public List<CcpBulkItem> apply(CcpJsonRepresentation json) {
		String dateFormat = Fields.DATE_FORMAT.getValue();
		CcpTimeDecorator now = new CcpTimeDecorator();
		String currentDate = now.getFormattedDateTime(dateFormat);
		CcpJsonRepresentation event = json.getJsonPiece(JnJsonCommonsFields.userAgent, JnJsonCommonsFields.ip);
		CcpJsonRepresentation eventWithDate = event.put(this.dateField, currentDate);
		String child = eventWithDate.asUgglyJson();

		CcpJsonRepresentation parentJson = json.getJsonPiece(JnEntityLoginEmail.Fields.email);
		String parentId = JnEntityLoginEmail.ENTITY.calculateId(parentJson);
		Map<String, CcpJsonRepresentation> parents = Map.of(parentId, parentJson);
		CcpEntityMetaData parentMetaData = JnEntityLoginEmail.ENTITY.getEntityMetaData();
		String parentName = parentMetaData.entityName;
		CcpEntityMetaData childMetaData = JsEntityLoginSessionValidation.ENTITY.getEntityMetaData();
		String childName = childMetaData.entityName;
		CcpEntity grouper = this.side.getEntity(JnEntityGrouper.ENTITY);
		List<CcpBulkItem> items = JnGroupers.getItemsToAppendChildKeepingTheLast(grouper, parentName, childName, child, MAX_CHILDREN, parents);
		return items;
	}
}
