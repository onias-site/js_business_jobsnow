package com.js.business.bots.login.token;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Step of the {@code solveLoginTokenTicket} command: solves a ticket of the given type (resend or unlock the login
 * token).
 */
public class JsSupportLoginToken implements CcpBusiness{
	/** The single instance: the class keeps no state. */
	public final static JsSupportLoginToken INSTANCE = new JsSupportLoginToken();

	private JsSupportLoginToken() {}

	/**
	 * Solves the ticket of the type in {@code ticketType}.
	 * @param json the session, with {@code ticketType} and {@code email}
	 * @return the result of the type
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		JsSupportLoginTokenTypes ticketType = json.getAsEnum(JsonFields.ticketType, JsSupportLoginTokenTypes.class);
		CcpJsonRepresentation execute = ticketType.execute(json);
		return execute;
	}
	
	/**
	 * Validates the input with {@link JsonFields}.
	 * @return the validation class
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFields.class;
	}
	
	
	/** Input fields of the step. */
	public static enum JsonFields implements CcpJsonFieldName{
		
		/** The {@code ticketType} field: required, text. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(allowedValuesEnum = JsSupportLoginTokenTypes.class)
		ticketType
	}

}
