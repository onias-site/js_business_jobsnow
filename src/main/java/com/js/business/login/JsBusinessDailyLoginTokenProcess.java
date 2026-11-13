package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * The daily process of the login token (Cloud Run Job {@code jobsnow-reenvio-token-login}, every hour): first
 * {@link JsBusinessResendLoginTokenToUsersWithoutPassword}, right after it
 * {@link JsBusinessSaveTokensResentWithoutPassword}.
 * <p>
 * Input: none of its own. Output: the input plus the counters of both.
 */
public class JsBusinessDailyLoginTokenProcess implements CcpBusiness {

	/** The single instance: the business keeps no state. */
	public static final JsBusinessDailyLoginTokenProcess INSTANCE = new JsBusinessDailyLoginTokenProcess();

	private JsBusinessDailyLoginTokenProcess() {}

	/**
	 * Runs the two processes in order.
	 * @param json the input of the scheduled process
	 * @return the input plus the counters
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation resent = JsBusinessResendLoginTokenToUsersWithoutPassword.INSTANCE.execute(json);
		CcpJsonRepresentation saved = JsBusinessSaveTokensResentWithoutPassword.INSTANCE.execute(resent);
		return saved;
	}
}
