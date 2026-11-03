package com.js.services;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.services.JnService;
import com.jn.utils.JnLanguage;
import com.js.business.privacy.JsPrivacyPolicyMessages;

/**
 * Service of the privacy policy, public (no session): the page {@code /privacidade} of the front end. Each item takes
 * its input rules from the class of the same name in this package (see {@code JnService}).
 */
public enum JsServicePrivacyPolicy implements JnService {

	/**
	 * The privacy policy in the {@code language} asked, read by id from {@code JnEntitySystemMessage}; a language with no
	 * text of its own gets the English one.
	 */
	GetPrivacyPolicy {
		/**
		 * Reads the text of the policy.
		 * @param json the {@code language}
		 * @return the {@code language} asked and the text under {@code message}
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			JnLanguage language = json.getAsEnum(JnJsonCommonsFields.language, JnLanguage.class);
			String privacyPolicy = JsPrivacyPolicyMessages.privacyPolicy.getMessage(language);
			CcpJsonRepresentation jsonWithLanguage = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.language, language);
			CcpJsonRepresentation response = jsonWithLanguage.put(JnJsonCommonsFields.message, privacyPolicy);
			return response;
		}
	},
	;
}
