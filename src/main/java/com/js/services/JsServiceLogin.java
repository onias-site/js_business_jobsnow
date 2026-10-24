package com.js.services;

import com.jn.services.JnService;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.js.business.login.JsBusinessCheckCurrentSession;
import com.js.business.login.JsBusinessEvaluateAttempts;
import com.js.business.login.JsBusinessExecuteLogin;
import com.js.business.login.JsBusinessExecuteLogout;
import com.js.business.login.JsBusinessSavePassword;
import com.jn.business.messages.JnMessages;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityEmailReportedAsSpam;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginPasswordAttempts;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.JsEntityLoginToken;
import com.js.entities.JsEntityLoginTokenAttempts;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.js.entities.JsEntityLoginTokenRequestUnlock;
import com.jn.entities.JnEntityMessageDidNotSent;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.mensageria.JnFunctionMensageriaSender;
import com.js.status.login.JsProcessStatusCreateLoginEmail;
import com.js.status.login.JsProcessStatusCreateLoginToken;
import com.js.status.login.JsProcessStatusExecuteLogin;
import com.js.status.login.JsProcessStatusExecuteLogout;
import com.js.status.login.JsProcessStatusExistsLoginEmail;
import com.js.status.login.JsProcessStatusSaveAnswers;
import com.js.status.login.JsProcessStatusUnlockLoginToken;
import com.js.status.login.JsProcessStatusUpdatePassword;
import com.jn.utils.JnDeleteKeysFromCache;

import com.ccp.json.fields.validation.CcpJsonCommonsFields;

/**
 * The login services. Each one reads, in one search, the records of the user in several entities and checks them in
 * order with {@code CcpGetEntityId}: the first check that holds ends the flow with its status (or runs its action). Each
 * service takes its input rules from the class of the same name in this package.
 */
public enum JsServiceLogin implements JnService {

	/**
	 * Logs in with the password. In order: a locked token (403), no login e-mail (404), a locked password (423), an open
	 * session (409), no password (202), no onboarding answers (201); otherwise the password is evaluated
	 * ({@code createFunctionToEvaluatePasswordAttempts}), and the login runs asynchronously when it is right.
	 */
	ExecuteLogin {
		/**
		 * Runs the login checks.
		 * @param json the login request
		 * @return the resulting fields of the user and the session token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			// the session token is generated before the checks: only a successful login hands it over. Until 2026-10-14 every
			// refusal (the 409 of an open session, for one) answered a new token that never became a session
			try {
				CcpJsonRepresentation loggedIn = this.login(json);
				return loggedIn;
			} catch (CcpErrorFlowDisturb notLoggedIn) {
				CcpJsonRepresentation jsonWithoutSessionToken = notLoggedIn.json.removeFields(CcpJsonCommonsFields.sessionToken);
				List<CcpJsonFieldName> fieldList = new ArrayList<>(Arrays.asList(notLoggedIn.fields));
				fieldList.remove(CcpJsonCommonsFields.sessionToken);
				CcpJsonFieldName[] fieldsWithoutSessionToken = fieldList.toArray(new CcpJsonFieldName[0]);
				String message = notLoggedIn.getMessage();
				CcpErrorFlowDisturb withoutSessionToken = new CcpErrorFlowDisturb(jsonWithoutSessionToken, notLoggedIn.status, message, fieldsWithoutSessionToken);
				throw withoutSessionToken;
			}
		}

		/**
		 * Runs the login checks.
		 * @param json the login request
		 * @return the resulting fields of the user and the session token
		 */
		private CcpJsonRepresentation login(CcpJsonRepresentation json) {

			CcpBusiness functionToEvaluatePasswordAttempts = this.createFunctionToEvaluatePasswordAttempts();
			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(json);

			CcpJsonRepresentation findById =  new CcpGetEntityId(parametersToSearchInAllEntities)
			.toBeginProcedureAnd()
				.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
				.loadThisIdFromEntity(JsEntityLoginPassword.ENTITY).and()
				.loadThisIdFromEntity(JsEntityLoginPasswordAttempts.ENTITY).and()
				.ifThisIdIsPresentInEntity(JsEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusExecuteLogin.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JsProcessStatusExecuteLogin.missingSavingEmail).and()
				.ifThisIdIsPresentInEntity(JsEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusExecuteLogin.lockedPassword).and()
				.ifThisIdIsPresentInEntity(JsEntityLoginSessionConflict.ENTITY).returnStatus(JsProcessStatusExecuteLogin.loginConflict).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginPassword.ENTITY).returnStatus(JsProcessStatusExecuteLogin.missingSavePassword).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginAnswers.ENTITY).returnStatus(JsProcessStatusExecuteLogin.missingSaveAnswers).and()
				.ifThisIdIsPresentInEntity(JsEntityLoginPassword.ENTITY).executeAction(functionToEvaluatePasswordAttempts).andFinallyReturningTheseFields(
						JnJsonCommonsFields.userAgent,
						JnJsonCommonsFields.attempts,
						JnJsonCommonsFields.timestamp,
						JnJsonCommonsFields.ip,
						JnJsonCommonsFields.email,
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved,
						CcpJsonCommonsFields.sessionToken
	 					)
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
			return findById;
		}
	},
	/**
	 * Registers the login e-mail (the first step of the login). In order: a locked token (403), a locked password (423), an
	 * open session (409); a new e-mail is saved; then no onboarding answers (201) and no password (202) tell which screen
	 * comes next.
	 */
	CreateLoginEmail {
		/**
		 * Runs the checks of the login e-mail.
		 * @param json the request
		 * @return the request
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = CcpEntityOperationType.save.getOperationCallback(JnEntityLoginEmail.ENTITY);
			CcpJsonRepresentation result = new CcpGetEntityId(json)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JsEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusCreateLoginEmail.lockedToken).and()
				.ifThisIdIsPresentInEntity(JsEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusCreateLoginEmail.lockedPassword).and()
				.ifThisIdIsPresentInEntity(JsEntityLoginSessionConflict.ENTITY).returnStatus(JsProcessStatusCreateLoginEmail.loginConflict).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).executeAction(action).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginAnswers.ENTITY).returnStatus(JsProcessStatusCreateLoginEmail.missingSaveAnswers).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginPassword.ENTITY).returnStatus(JsProcessStatusCreateLoginEmail.missingSavePassword).andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}

	},
	/**
	 * Tells how complete the registration of the e-mail is: a locked token (403), no login e-mail (404), a locked password
	 * (423), an open session (409), no onboarding answers (201), no password (202), or complete (200).
	 */
	ExistsLoginEmail {
		/**
		 * Runs the checks of the login e-mail.
		 * @param json the request
		 * @return the request, when the registration is complete
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

				new CcpGetEntityId(json)
				.toBeginProcedureAnd()
					.ifThisIdIsPresentInEntity(JsEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusExistsLoginEmail.lockedToken).and()
					.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JsProcessStatusExistsLoginEmail.missingEmail).and()
					.ifThisIdIsPresentInEntity(JsEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusExistsLoginEmail.lockedPassword).and()
					.ifThisIdIsPresentInEntity(JsEntityLoginSessionConflict.ENTITY).returnStatus(JsProcessStatusExistsLoginEmail.loginConflict).and()
					.ifThisIdIsNotPresentInEntity(JsEntityLoginAnswers.ENTITY).returnStatus(JsProcessStatusExistsLoginEmail.missingAnswers).and()
					.ifThisIdIsNotPresentInEntity(JsEntityLoginPassword.ENTITY).returnStatus(JsProcessStatusExistsLoginEmail.missingPassword)
					.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
				.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
				;
			 return json;
		}
	},
	/**
	 * Logs out: without an open session, or with a session replaced by a newer one of the same e-mail (404); otherwise the
	 * logout runs asynchronously.
	 */
	ExecuteLogout {
		/**
		 * Runs the logout checks.
		 * @param json the request with the session
		 * @return the request
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = new JnFunctionMensageriaSender(JsBusinessExecuteLogout.INSTANCE);
			new CcpGetEntityId(json)
			.toBeginProcedureAnd()
			.loadThisIdFromEntity(JsEntityLoginSessionConflict.ENTITY).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginSessionValidation.ENTITY).returnStatus(JsProcessStatusExecuteLogout.missingLogin).and()
				// the logout of a replaced session would delete the record of the current one
				.executeAction(JsBusinessCheckCurrentSession.TO_EXECUTE_LOGOUT).and()
				.executeAction(action)
				.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
			.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;

			return json;
		}

	},
	/**
	 * Saves the onboarding answers. In order: a locked token (403), no login e-mail (404), an open session (409), a locked
	 * password (423); answers not saved yet are saved; then no password (202).
	 */
	SaveAnswers {
		/**
		 * Runs the checks and saves the answers.
		 * @param json the answers
		 * @return the request
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpBusiness action = CcpEntityOperationType.save.getOperationCallback(JsEntityLoginAnswers.ENTITY);

			new CcpGetEntityId(json)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JsEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusSaveAnswers.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JsProcessStatusSaveAnswers.missingToken).and()
				.ifThisIdIsPresentInEntity(JsEntityLoginSessionConflict.ENTITY).returnStatus(JsProcessStatusSaveAnswers.loginConflict).and()
				.ifThisIdIsPresentInEntity(JsEntityLoginPassword.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusSaveAnswers.lockedPassword).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginAnswers.ENTITY).executeAction(action)
	 			.and().ifThisIdIsNotPresentInEntity(JsEntityLoginPassword.ENTITY).returnStatus(JsProcessStatusSaveAnswers.missingPassword)

				.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
			.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
			return json;
		}
	},
	/**
	 * Creates and sends the login token. In order: a locked token (403), an e-mail that can not receive the message, for
	 * example reported as spam (422), a token sent recently (409), no login e-mail (404); a token not created yet is saved,
	 * which sends it by e-mail.
	 */
	CreateLoginToken {
		/**
		 * Runs the checks and creates the token.
		 * @param json the request
		 * @return the data about the token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpJsonRepresentation jsonWithSubjectType = json.put(JnJsonCommonsFields.subjectType, JnMessages.JnNotifyUserAboutLoginToken.class.getName());

			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(jsonWithSubjectType);

			CcpBusiness saveTheToken = CcpEntityOperationType.save.getOperationCallback(JsEntityLoginToken.ENTITY);

			CcpBusiness saveTheLoginEmail = CcpEntityOperationType.save.getOperationCallback(JnEntityLoginEmail.ENTITY);

			// the language comes only in the URL of this request; the login e-mail keeps it for the token reset by the support
			CcpBusiness sendUserToken = new CcpBusiness() {
				public CcpJsonRepresentation apply(CcpJsonRepresentation tokenRequest) {
					CcpJsonRepresentation emailAndLanguage = tokenRequest.getJsonPiece(JnJsonCommonsFields.email, JnJsonCommonsFields.language);
					saveTheLoginEmail.execute(emailAndLanguage);
					CcpJsonRepresentation savedToken = saveTheToken.execute(tokenRequest);
					return savedToken;
				}
			};

			CcpJsonRepresentation result = new CcpGetEntityId(parametersToSearchInAllEntities)
			.toBeginProcedureAnd()
				.ifThisIdIsPresentInEntity(JsEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusCreateLoginToken.statusLockedToken).and()
				.ifThisIdIsPresentInEntity(JnEntityMessageDidNotSent.ENTITY).returnStatus(JsProcessStatusCreateLoginToken.statusCanNotSendThisMessage).and()
				.ifThisIdIsPresentInEntity(JnEntityDisposableRecord.ENTITY).returnStatus(JsProcessStatusCreateLoginToken.statusAlreadySentToken).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JsProcessStatusCreateLoginToken.statusMissingEmail).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginToken.ENTITY).executeAction(sendUserToken)
				.andFinallyReturningTheseFields(
						JnJsonCommonsFields.userAgent,
						JnJsonCommonsFields.attempts,
						JnJsonCommonsFields.timestamp,
						JnJsonCommonsFields.ip,
						JnJsonCommonsFields.email,
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved
						)
			.endThisProcedureRetrievingTheResultingData(this, LoadDataAboutToken.INSTANCE, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}

	},
	/**
	 * Defines (or changes) the password with the login token. In order: a locked token (403), no onboarding answers (201),
	 * no login e-mail (404), no token (404); then the token is evaluated
	 * ({@code createFunctionToEvaluateTokenAttempts}), and the password is saved asynchronously when it is right.
	 */
	SavePassword {
		/**
		 * Runs the checks and evaluates the token.
		 * @param json the request with the token and the new password
		 * @return the resulting fields of the user and the session token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpBusiness functionToEvaluateTokenAttempts = this.createFunctionToEvaluateTokenAttempts();

			CcpJsonRepresentation[] parametersToSearchInAllEntities = this.createParametersToSearchInAllEntities(json);

			CcpJsonRepresentation result =  new CcpGetEntityId(parametersToSearchInAllEntities)
			.toBeginProcedureAnd()
			.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
				.loadThisIdFromEntity(JsEntityLoginTokenAttempts.ENTITY).and()
				.ifThisIdIsPresentInEntity(JsEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusUpdatePassword.lockedToken).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginAnswers.ENTITY).returnStatus(JsProcessStatusUpdatePassword.missingSaveAnswers).and()
				.ifThisIdIsNotPresentInEntity(JnEntityLoginEmail.ENTITY).returnStatus(JsProcessStatusUpdatePassword.missingEmail).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginToken.ENTITY).returnStatus(JsProcessStatusUpdatePassword.missingToken).and()
				.executeAction(functionToEvaluateTokenAttempts).andFinallyReturningTheseFields(
						JnJsonCommonsFields.userAgent,
						JnJsonCommonsFields.timestamp,
						JnJsonCommonsFields.ip,
						JnJsonCommonsFields.email,
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved,
						CcpJsonCommonsFields.sessionToken
						)
			.endThisProcedureRetrievingTheResultingData(this, CcpOtherConstants.DO_NOTHING, LoadDataAboutToken.INSTANCE, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}
	},
	/**
	 * Asks the support team to resend the login token: no token (404), already resent (429), already requested (409);
	 * otherwise the request is saved.
	 */
	ResendLoginToken{
		/**
		 * Runs the checks and saves the request.
		 * @param json the request
		 * @return the data about the token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpEntity entity = JsEntityLoginTokenRequestResend.ENTITY;

			CcpEntity twinEntity = entity.getTwinEntity();

			CcpBusiness save = CcpEntityOperationType.save.getOperationCallback(entity);

			CcpGetEntityId ccpGetEntityId = super.getCcpGetEntityId(json, entity);

			CcpJsonRepresentation result = ccpGetEntityId
			.toBeginProcedureAnd()
			.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
			.ifThisIdIsNotPresentInEntity(JsEntityLoginToken.ENTITY).returnStatus(JsProcessStatusUnlockLoginToken.statusTokenNotExists).and()
			.ifThisIdIsPresentInEntity(twinEntity).returnStatus(JsProcessStatusUnlockLoginToken.statusTokenAlredyResent).and()
				.ifThisIdIsPresentInEntity(entity).returnStatus(JsProcessStatusUnlockLoginToken.statusAlreadyRequested)
				.andFinallyReturningTheseFields(
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved,
						JnJsonCommonsFields.timestamp
						)
			.endThisProcedureRetrievingTheResultingData(this, LoadDataAboutToken.INSTANCE, save, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}


	},
	/**
	 * Asks the support team to unlock the login token: a token not locked (404), already unlocked (429), already requested
	 * (409); otherwise the request is saved.
	 */
	UnlockLoginToken{
		/**
		 * Runs the checks and saves the request.
		 * @param json the request
		 * @return the data about the token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpEntity entity = JsEntityLoginTokenRequestUnlock.ENTITY;

			CcpEntity twinEntity = entity.getTwinEntity();

			CcpBusiness save = CcpEntityOperationType.save.getOperationCallback(entity);

			CcpGetEntityId ccpGetEntityId = super.getCcpGetEntityId(json, entity);

			CcpJsonRepresentation result = ccpGetEntityId
			.toBeginProcedureAnd()
				.loadThisIdFromEntity(JnEntityDisposableRecord.ENTITY).and()
				.ifThisIdIsNotPresentInEntity(JsEntityLoginToken.ENTITY.getTwinEntity()).returnStatus(JsProcessStatusUnlockLoginToken.statusTokenNotLocked).and()
				.ifThisIdIsPresentInEntity(twinEntity).returnStatus(JsProcessStatusUnlockLoginToken.statusTokenAlredyUnlocked).and()
				.ifThisIdIsPresentInEntity(entity).returnStatus(JsProcessStatusUnlockLoginToken.statusAlreadyRequested)
				.andFinallyReturningTheseFields(
						JnJsonCommonsFields.expirationDate,
						JnJsonCommonsFields.dateItWasSaved,
						JnJsonCommonsFields.timestamp
						)
			.endThisProcedureRetrievingTheResultingData(this, LoadDataAboutToken.INSTANCE, save, JnDeleteKeysFromCache.INSTANCE);

			return result;
		}
	}
	;
	/**
	 * Builds the evaluation of the password: a wrong one returns 427, the last allowed one deletes (locks) the password and
	 * returns 429, a right one runs {@code JsBusinessExecuteLogin} asynchronously.
	 * @return the evaluation
	 */
	protected CcpBusiness createFunctionToEvaluatePasswordAttempts() {
		CcpBusiness lockPassword = CcpEntityOperationType.delete.getOperationCallback(JsEntityLoginPassword.ENTITY);
		JnFunctionMensageriaSender executeLogin = new JnFunctionMensageriaSender(JsBusinessExecuteLogin.INSTANCE);
		CcpBusiness functionToEvaluatePasswordAttempts = JsBusinessEvaluateAttempts.builder()
				.entityToGetTheAttempts(JsEntityLoginPasswordAttempts.ENTITY)
				.entityToGetTheSecret(JsEntityLoginPassword.ENTITY)
				.databaseFieldName(JnJsonCommonsFields.password)
				.userFieldName(JnJsonCommonsFields.password)
				.statusWhenExceedAttempts(JsProcessStatusExecuteLogin.passwordLockedRecently)
				.statusWhenWrongType(JsProcessStatusExecuteLogin.wrongPassword)
				.lockUsing(lockPassword)
				.onSuccess(executeLogin)
				.attemptsFieldName(JnJsonCommonsFields.attempts)
				.emailFieldName(JnJsonCommonsFields.email)
				.build();
		return functionToEvaluatePasswordAttempts;
	}

	/**
	 * Builds the evaluation of the token: a wrong one returns 427, the last allowed one deletes (locks) the token and returns
	 * 429, a right one runs {@code JsBusinessSavePassword} asynchronously.
	 * @return the evaluation
	 */
	protected CcpBusiness createFunctionToEvaluateTokenAttempts() {
		CcpBusiness lockToken = CcpEntityOperationType.delete.getOperationCallback(JsEntityLoginToken.ENTITY);
		JnFunctionMensageriaSender updatePassword = new JnFunctionMensageriaSender(JsBusinessSavePassword.INSTANCE);

		CcpBusiness evaluateTokenAttempts = JsBusinessEvaluateAttempts.builder()
				.entityToGetTheAttempts(JsEntityLoginTokenAttempts.ENTITY)
				.entityToGetTheSecret(JsEntityLoginToken.ENTITY)
				.databaseFieldName(JsEntityLoginToken.Fields.token)
				.userFieldName(JsEntityLoginToken.Fields.token)
				.statusWhenExceedAttempts(JsProcessStatusUpdatePassword.tokenLockedRecently)
				.statusWhenWrongType(JsProcessStatusUpdatePassword.wrongToken)
				.lockUsing(lockToken)
				.onSuccess(updatePassword)
				.attemptsFieldName(JnJsonCommonsFields.attempts)
				.emailFieldName(JnJsonCommonsFields.email)
				.build();
		return evaluateTokenAttempts;
	}

	/**
	 * Builds the search parameters: the request plus a new session token and the keys of the "message not sent" record
	 * (spam report of the token e-mail), and the keys of the expiration copies of the token and of the locked token.
	 * @param json the request
	 * @return the search parameters
	 */
	protected CcpJsonRepresentation[] createParametersToSearchInAllEntities(CcpJsonRepresentation json) {
		CcpJsonRepresentation generatedSessionToken = CcpOtherConstants.EMPTY_JSON
				.getTransformedJson(JnJsonTransformersFieldsEntityDefault.tokenHash)
				.renameField(JnJsonCommonsFields.originalToken, CcpJsonCommonsFields.sessionToken)
				.removeFields(JsEntityLoginSessionValidation.Fields.token)
				;

		CcpEntityMetaData entityMetaData = JnEntityEmailReportedAsSpam.ENTITY.getEntityMetaData();

		String subjectType = JnMessages.JnNotifyUserAboutLoginToken.class.getName();

		CcpJsonRepresentation parametersToSearchInMessageNotSend = generatedSessionToken
				.put(JnEntityMessageDidNotSent.Fields.reasonType, entityMetaData.entityName)
				.put(JnJsonCommonsFields.subjectType, subjectType)
				;

		CcpJsonRepresentation parametersToSearchInAllOtherEntities = json.mergeWithAnotherJson(parametersToSearchInMessageNotSend);
		CcpJsonRepresentation parametersToSearchDataAboutToken = JsEntityLoginToken.ENTITY.getIdToSearchDisposableRecord(json);
		CcpJsonRepresentation parametersToSearchDataAboutLockedToken = JsEntityLoginToken.ENTITY.getTwinEntity().getIdToSearchDisposableRecord(json);

		CcpJsonRepresentation[] parametersToSearchInAllEntities = new CcpJsonRepresentation[] {parametersToSearchInAllOtherEntities, parametersToSearchDataAboutToken, parametersToSearchDataAboutLockedToken};
		return parametersToSearchInAllEntities;
	}

	/**
	 * Builds the search of a support request entity: the request plus the keys of the expiration copies of the entity and of
	 * its twin.
	 * @param json the request
	 * @param entity the support request entity
	 * @return the search
	 */
	CcpGetEntityId getCcpGetEntityId(CcpJsonRepresentation json, CcpEntity entity) {
		CcpEntity twin = entity.getTwinEntity();

		CcpJsonRepresentation mainDisposableToSearch = entity.getIdToSearchDisposableRecord(json);
		CcpJsonRepresentation twinDisposableToSearch = twin.getIdToSearchDisposableRecord(json);

		CcpGetEntityId ccpGetEntityId = new CcpGetEntityId(json, twinDisposableToSearch, mainDisposableToSearch);
		return ccpGetEntityId;
	}

	/** Fields used by the services. */
	public static enum JsonFieldNames implements CcpJsonFieldName{
		/** A field never present, used to return no field at the end of a check. */
		inexistentField
	}
}
