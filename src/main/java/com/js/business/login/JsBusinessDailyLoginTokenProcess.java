package com.js.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.database.JnBusinessPurgeExpiredDisposableRecords;

/**
 * The hourly process of the Cloud Run Job {@code jobsnow-reenvio-token-login} (every hour): first
 * {@link JsBusinessResendLoginTokenToUsersWithoutPassword} (which also loads the suppression lists of the e-mail
 * provider), right after it {@link JsBusinessSaveTokensResentWithoutPassword} and, since 2026-11-17, the purge of the
 * expired disposable records ({@link JnBusinessPurgeExpiredDisposableRecords}), which ran once a day in the daily process.
 * <p>
 * Input: none of its own. Output: the input plus the counters of the token processes and {@code purge}, the counters of
 * the purge.
 */
public class JsBusinessDailyLoginTokenProcess implements CcpBusiness {

	/** The fields of the output. */
	public static enum Fields implements CcpJsonFieldName {
		/** The counters of the purge of the expired disposable records. */
		purge,
	}

	/** The single instance: the business keeps no state. */
	public static final JsBusinessDailyLoginTokenProcess INSTANCE = new JsBusinessDailyLoginTokenProcess();

	private JsBusinessDailyLoginTokenProcess() {}

	/**
	 * Runs the two processes of the token and then the purge.
	 * @param json the input of the scheduled process
	 * @return the input plus the counters
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation resent = JsBusinessResendLoginTokenToUsersWithoutPassword.INSTANCE.execute(json);
		CcpJsonRepresentation saved = JsBusinessSaveTokensResentWithoutPassword.INSTANCE.execute(resent);
		CcpJsonRepresentation purge = JnBusinessPurgeExpiredDisposableRecords.INSTANCE.execute(json);
		CcpJsonRepresentation purgeCounters = purge.getJsonPiece(JnBusinessPurgeExpiredDisposableRecords.Fields.values());
		CcpJsonRepresentation savedWithPurge = saved.put(Fields.purge, purgeCounters);
		return savedWithPurge;
	}
}
