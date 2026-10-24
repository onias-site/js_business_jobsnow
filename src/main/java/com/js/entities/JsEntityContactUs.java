package com.js.entities;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperParentCombinations;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.annotations.messages.write.CcpAfterDeleteFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError;
import com.ccp.especifications.db.utils.entity.decorators.annotations.messages.write.CcpAfterSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.business.messages.JnMessageType;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityCommandNotAllowedToUser;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityWriteEventSubscribers;
import com.jn.entities.decorators.engine.JnGrouperChildAsJsonEntityAfterWrite;
import com.jn.entities.decorators.engine.JnGrouperChildAsJsonOnBothSidesEntityBeforeWrite;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.json.transformers.JnLoginEmailParentCombinations;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnLanguage;
import com.js.business.bots.contact.us.JsSupportContactUsBotConfiguration;
import com.js.business.bots.contact.us.ignored.JsSupportIgnoredContactUsBotConfiguration;
import com.js.business.contact.us.JsBusinessDiscardContactUsOfIgnoredUser;
import com.js.json.fields.validation.JsContactUsTypes;
import com.js.messages.JsContactUsMessageFields;
import com.js.messages.JsNotifySupportAndUserAboutContactUs;
import com.js.messages.JsNotifyUserAboutAnsweredContactUs;

/**
 * The tickets of the "contact us" of the front end: what the user wrote in its modal (a bug, a compliment, a criticism,
 * a suggestion, a comment or a question, the {@code type}), with a {@code title}, a {@code description} and, when the
 * front end caught one, the last stack trace of the page ({@code stackTrace}). The "contact us" has no session: it must
 * work even when the login screen is the one with the bug, so the {@code email} is the one the user is logged in with or
 * the one the login modal keeps. One open ticket per user and {@code type}: a new one of the same type takes the place of
 * the open one.
 * <p>
 * Saving a ticket e-mails the user what they filled in, telling that the ticket is being analyzed, and sends the support
 * bot operator {@code /answerContactUs {type} {email}}, which also becomes a ticket of {@code /pendingTickets}
 * ({@link JsNotifySupportAndUserAboutContactUs}). Answering it (the {@code supportResponse}) deletes it, which moves it to
 * the twin entity {@code js_contact_us_solved} and e-mails the user the answer ({@link JsNotifyUserAboutAnsweredContactUs}).
 * <p>
 * Each ticket is a ticket of the user in the group of the e-mail ({@code jn_login_email}) of {@code jn_grouper}, as a JSON
 * text, while it waits for the support; the answer moves it to the same group of {@code jn_inactive_grouper}, with the
 * answer ({@link JnGrouperChildAsJsonOnBothSidesEntityBeforeWrite}): the screen of the tickets of the user reads both by id.
 * <p>
 * A user ignored by the support operator ({@link JnEntityCommandNotAllowedToUser}) can not open tickets, and has their open
 * tickets discarded when ignored ({@link JsBusinessDiscardContactUsOfIgnoredUser}). Created on 2026-10-23.
 */
@CcpEntityGrouperChild(value = {JnEntityLoginEmail.class}, beforeDecoratorClass = JnGrouperChildAsJsonOnBothSidesEntityBeforeWrite.class, afterDecoratorClass = JnGrouperChildAsJsonEntityAfterWrite.class)
@CcpEntityGrouperParentCombinations(parent = JnEntityLoginEmail.class, value = JnLoginEmailParentCombinations.class)
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JsEntityContactUs.Fields.class)
@CcpAfterSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(JsNotifySupportAndUserAboutContactUs.class)
@CcpAfterDeleteFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(JsNotifyUserAboutAnsweredContactUs.class)
@CcpEntityTwin(
		twinEntityName = "js_contact_us_solved",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)
public class JsEntityContactUs implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JsEntityContactUs INSTANCE = new JsEntityContactUs();

	private JsEntityContactUs() {}

	/** The entity {@code js_contact_us}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JsEntityContactUs.class).entityInstance;

	/** The texts of the seeds that are not placeholders. */
	enum SeedTexts implements CcpJsonFieldName {
		/** The sender of the e-mails. */
		sender { public String getValue() { return "devs.jobsnow@gmail.com"; } },
	}

	/**
	 * Seeds the notices of a saved ticket (the e-mail to the user, the command to the support bot operator and their
	 * sending parameters), the e-mail of the answer, the label of each {@link JsContactUsTypes} in Portuguese and English,
	 * the commands {@code /answerContactUs} and {@code /ignoredContactUs} of the support bot, and the subscriber of the
	 * ignoring of a user, which discards their open tickets.
	 * @return the seed records
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		String savedTemplateId = JsNotifySupportAndUserAboutContactUs.class.getName();

		String commandToTheOperator = "/answerContactUs {" + Fields.type + "} {" + Fields.email + "}";
		CcpJsonRepresentation templateWithTemplateId = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.templateId, savedTemplateId);
		CcpJsonRepresentation templateWithMessage = templateWithTemplateId
				.put(JnJsonCommonsFields.message, commandToTheOperator);
		CcpJsonRepresentation portugueseTemplate = templateWithMessage
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese);
		CcpJsonRepresentation englishTemplate = templateWithMessage
				.put(JnJsonCommonsFields.language, JnLanguage.english);

		CcpJsonRepresentation parametersWithInstantMessageType = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		CcpJsonRepresentation parametersWithTemplateId = parametersWithInstantMessageType
				.put(JnJsonCommonsFields.templateId, savedTemplateId);
		CcpJsonRepresentation parametersWithMaxTries = parametersWithTemplateId
				.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.maxTriesToSendMessage, 10);
		CcpJsonRepresentation parametersWithSleepTime = parametersWithMaxTries
				.addToItem(JnJsonCommonsFields.moreParameters, JnJsonCommonsFields.sleepToSendMessage, 3000);
		CcpJsonRepresentation parametersWithBotName = parametersWithSleepTime
				.put(JnJsonInstantMessengerFields.botName, JnMessageType.JnBotType.support);
		CcpJsonRepresentation parametersToTheOperator = parametersWithBotName
				.put(JnJsonInstantMessengerFields.chatId, 751717896L);

		String typeDescription = "{" + JsContactUsMessageFields.typeDescription + "}";
		String escapedTitle = "{" + JsContactUsMessageFields.escapedTitle + "}";
		String escapedDescription = "{" + JsContactUsMessageFields.escapedDescription + "}";
		String escapedSupportResponse = "{" + JsContactUsMessageFields.escapedSupportResponse + "}";

		String portugueseTicketDetails = "<ul><li><b>Tipo:</b> " + typeDescription + "</li>"
				+ "<li><b>Título:</b> " + escapedTitle + "</li>"
				+ "<li><b>Descrição:</b> " + escapedDescription + "</li></ul>";
		String englishTicketDetails = "<ul><li><b>Type:</b> " + typeDescription + "</li>"
				+ "<li><b>Title:</b> " + escapedTitle + "</li>"
				+ "<li><b>Description:</b> " + escapedDescription + "</li></ul>";

		String portugueseSavedMessage = "<html><body><p>Olá, recebemos a sua mensagem do fale conosco.</p>" + portugueseTicketDetails
				+ "<p>Nosso time está analisando o seu ticket e te responderá o quanto antes.</p></body></html>";
		String englishSavedMessage = "<html><body><p>Hello, we received your message from the contact us.</p>" + englishTicketDetails
				+ "<p>Our team is analyzing your ticket and will get back to you as soon as possible.</p></body></html>";
		List<CcpBulkItem> savedEmailItems = this.getEmailTemplateAndParameters(savedTemplateId
				, "Recebemos a sua mensagem do fale conosco", portugueseSavedMessage
				, "We received your message from the contact us", englishSavedMessage);

		String answeredTemplateId = JsNotifyUserAboutAnsweredContactUs.class.getName();
		String portugueseAnsweredMessage = "<html><body><p>Olá, o nosso time respondeu o seu ticket do fale conosco.</p>" + portugueseTicketDetails
				+ "<p><b>Resposta do suporte:</b> " + escapedSupportResponse + "</p></body></html>";
		String englishAnsweredMessage = "<html><body><p>Hello, our team answered your ticket from the contact us.</p>" + englishTicketDetails
				+ "<p><b>Answer of the support:</b> " + escapedSupportResponse + "</p></body></html>";
		List<CcpBulkItem> answeredEmailItems = this.getEmailTemplateAndParameters(answeredTemplateId
				, "O seu ticket do fale conosco foi respondido", portugueseAnsweredMessage
				, "Your ticket from the contact us was answered", englishAnsweredMessage);

		List<CcpBulkItem> templateItems = CcpEntityConfigurator.super.toCreateBulkItems(JnEntityInstantMessengerTemplateMessage.ENTITY, portugueseTemplate, englishTemplate);
		List<CcpBulkItem> parametersItems = CcpEntityConfigurator.super.toCreateBulkItems(JnEntityInstantMessengerParametersToSend.ENTITY, parametersToTheOperator);

		List<CcpBulkItem> firstRecords = new ArrayList<>(templateItems);
		firstRecords.addAll(parametersItems);
		firstRecords.addAll(savedEmailItems);
		firstRecords.addAll(answeredEmailItems);

		List<CcpBulkItem> typeLabels = getTypeLabels();
		firstRecords.addAll(typeLabels);

		List<CcpBulkItem> answerCommandItems = JsSupportContactUsBotConfiguration.getSeeds();
		List<CcpBulkItem> ignoredContactUsItems = JsSupportIgnoredContactUsBotConfiguration.getSeeds();
		firstRecords.addAll(answerCommandItems);
		firstRecords.addAll(ignoredContactUsItems);

		// ignoring a user (in any command, of any cost center) discards their open tickets
		List<CcpBulkItem> subscriberItems = JnEntityWriteEventSubscribers.getSeeds(JnEntityCommandNotAllowedToUser.ENTITY, CcpEntityDecoratorOperationType.save, JsBusinessDiscardContactUsOfIgnoredUser.class);
		firstRecords.addAll(subscriberItems);
		return firstRecords;
	}

	/**
	 * The label of each kind of ticket, in Portuguese and English: the options of the combobox of the modal.
	 * @return the seed records
	 */
	private static List<CcpBulkItem> getTypeLabels() {
		List<CcpBulkItem> typeLabels = new ArrayList<>();
		typeLabels.addAll(JsContactUsTypes.reportBug.toBulkItems(JnLanguage.portuguese, "Reportar um bug"));
		typeLabels.addAll(JsContactUsTypes.reportBug.toBulkItems(JnLanguage.english, "Report a bug"));
		typeLabels.addAll(JsContactUsTypes.makeCompliment.toBulkItems(JnLanguage.portuguese, "Fazer um elogio"));
		typeLabels.addAll(JsContactUsTypes.makeCompliment.toBulkItems(JnLanguage.english, "Give a compliment"));
		typeLabels.addAll(JsContactUsTypes.makeCriticism.toBulkItems(JnLanguage.portuguese, "Fazer uma crítica"));
		typeLabels.addAll(JsContactUsTypes.makeCriticism.toBulkItems(JnLanguage.english, "Make a criticism"));
		typeLabels.addAll(JsContactUsTypes.makeSuggestion.toBulkItems(JnLanguage.portuguese, "Fazer uma sugestão"));
		typeLabels.addAll(JsContactUsTypes.makeSuggestion.toBulkItems(JnLanguage.english, "Make a suggestion"));
		typeLabels.addAll(JsContactUsTypes.makeComment.toBulkItems(JnLanguage.portuguese, "Fazer um comentário"));
		typeLabels.addAll(JsContactUsTypes.makeComment.toBulkItems(JnLanguage.english, "Make a comment"));
		typeLabels.addAll(JsContactUsTypes.clarifyDoubts.toBulkItems(JnLanguage.portuguese, "Esclarecer dúvidas"));
		typeLabels.addAll(JsContactUsTypes.clarifyDoubts.toBulkItems(JnLanguage.english, "Clarify doubts"));
		return typeLabels;
	}

	/**
	 * Email template (Portuguese and English) and sending parameters of the given template id.
	 */
	private List<CcpBulkItem> getEmailTemplateAndParameters(String templateId, String portugueseSubject, String portugueseMessage, String englishSubject, String englishMessage) {

		CcpJsonRepresentation templateWithTemplateId = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.templateId, templateId);

		CcpJsonRepresentation portugueseTemplateWithLanguage = templateWithTemplateId
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese);
		CcpJsonRepresentation portugueseTemplateWithSubject = portugueseTemplateWithLanguage
				.put(JnJsonCommonsFields.subject, portugueseSubject);
		CcpJsonRepresentation portugueseTemplate = portugueseTemplateWithSubject
				.put(JnJsonCommonsFields.message, portugueseMessage);

		CcpJsonRepresentation englishTemplateWithLanguage = templateWithTemplateId
				.put(JnJsonCommonsFields.language, JnLanguage.english);
		CcpJsonRepresentation englishTemplateWithSubject = englishTemplateWithLanguage
				.put(JnJsonCommonsFields.subject, englishSubject);
		CcpJsonRepresentation englishTemplate = englishTemplateWithSubject
				.put(JnJsonCommonsFields.message, englishMessage);

		String sender = SeedTexts.sender.getValue();
		CcpJsonRepresentation parametersWithSender = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.sender, sender);
		CcpJsonRepresentation parametersWithSubjectType = parametersWithSender
				.put(JnJsonCommonsFields.subjectType, templateId);
		CcpJsonRepresentation parameters = parametersWithSubjectType
				.put(JnJsonCommonsFields.templateId, templateId);

		List<CcpBulkItem> templateItems = CcpEntityConfigurator.super.toCreateBulkItems(JnEntityEmailTemplateMessage.ENTITY, portugueseTemplate, englishTemplate);
		List<CcpBulkItem> parametersItems = CcpEntityConfigurator.super.toCreateBulkItems(JnEntityEmailParametersToSend.ENTITY, parameters);

		List<CcpBulkItem> templateAndParameters = new ArrayList<>(templateItems);
		templateAndParameters.addAll(parametersItems);
		return templateAndParameters;
	}

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key (kept as SHA-1 hash), validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email,

		/** The {@code type} field: part of the primary key, the kind of ticket ({@link JsContactUsTypes}). */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(allowedValuesEnum = JsContactUsTypes.class)
		type,

		/** The {@code title} field: required, text of 5 to 50 characters. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(minLength = 5, maxLength = 50)
		title,

		/** The {@code description} field: required, validated as in {@code JnJsonCommonsFields} (10 to 500 characters). */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		description,

		/** The {@code stackTrace} field: text, the last stack trace of the page caught by the front end, when there is one. */
		@CcpJsonFieldTypeString(maxLength = 50_000)
		stackTrace,

		/** The {@code supportResponse} field: text of 10 to 500 characters, the answer of the support, given when it solves the ticket. */
		@CcpJsonFieldTypeString(minLength = 10, maxLength = 500)
		supportResponse,

		/**
		 * The {@code date} field: text, the date and time of the last save ({@code dd/MM/yyyy HH:mm:ss}), filled by the
		 * decorator of {@code @CcpEntityGrouperChild}; it goes with the ticket to the groups of the user.
		 */
		@CcpJsonFieldTypeString(exactLength = 19)
		date,
	}
}
