package com.js.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatusDefault;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.business.grouper.JnBusinessGetGroup;
import com.jn.business.grouper.JnBusinessGetGroupOnBothSides;
import com.jn.entities.JnEntityGrouper;
import com.jn.services.JnService;

/** The groups of {@code jn_grouper}, read by their primary key through the common read of the groups. */
public enum JsServiceGrouper implements JnService {

	/**
	 * A group by id ({@code parentName}, {@code childName} and {@code parentId}), on the side asked ({@code mainEntity} by
	 * default, or {@code twinEntity}); a group that does not exist on that side answers 404.
	 */
	GetGroup {
		/**
		 * Reads the group with {@code JnBusinessGetGroup}.
		 * @param json the session values, the primary key of the group and the side
		 * @return the group
		 * @throws CcpErrorFlowDisturb with {@code NOT_FOUND} when the group does not exist on that side
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation group = JnBusinessGetGroup.INSTANCE.execute(json);
			boolean notFound = group.isEmpty();

			if(notFound) {
				throw new CcpErrorFlowDisturb(json, CcpProcessStatusDefault.NOT_FOUND);
			}

			return group;
		}

		/**
		 * Validates the input with the fields of the common read of the groups.
		 * @return the validation class
		 */
		public Class<?> getJsonValidationClass() {
			return JnBusinessGetGroup.Fields.class;
		}
	},
	/**
	 * A group by id ({@code parentName}, {@code childName} and {@code parentId}) on both sides at once, each one under the
	 * name of its entity ({@code jn_grouper} and {@code jn_inactive_grouper}); a group that exists on neither side
	 * answers 404.
	 */
	GetGroupOnBothSides {
		/**
		 * Reads the group with {@code JnBusinessGetGroupOnBothSides}.
		 * @param json the session values and the primary key of the group
		 * @return the group of each side, an empty json on a side where it does not exist
		 * @throws CcpErrorFlowDisturb with {@code NOT_FOUND} when the group exists on neither side
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation groupOnBothSides = JnBusinessGetGroupOnBothSides.INSTANCE.execute(json);
			CcpJsonRepresentation activeGroup = groupOnBothSides.getInnerJson(JnEntityGrouper.ENTITY);
			CcpEntity inactiveGrouper = JnEntityGrouper.ENTITY.getTwinEntity();
			CcpJsonRepresentation inactiveGroup = groupOnBothSides.getInnerJson(inactiveGrouper);
			boolean notFound = activeGroup.isEmpty() && inactiveGroup.isEmpty();

			if(notFound) {
				throw new CcpErrorFlowDisturb(json, CcpProcessStatusDefault.NOT_FOUND);
			}

			return groupOnBothSides;
		}

		/**
		 * Validates the input with the fields of the read of the groups on both sides.
		 * @return the validation class
		 */
		public Class<?> getJsonValidationClass() {
			return JnBusinessGetGroupOnBothSides.Fields.class;
		}
	},
	;
}
