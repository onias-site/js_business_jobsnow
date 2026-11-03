package com.js.business.privacy;

import java.util.ArrayList;
import java.util.List;

import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.jn.messages.JnSystemMessage;
import com.jn.utils.JnLanguage;

/**
 * The privacy policy of the platform (page {@code /privacidade} of the front end, linked by the footer of every e-mail),
 * stored in {@code JnEntitySystemMessage} and seeded by {@code JsEntityContactUs}. The text is one string per language,
 * one block per line: a line starting with {@code "# "} is the title of the page, with {@code "## "} the title of a
 * section, with {@code "- "} an item of a list, and any other line a paragraph.
 */
public enum JsPrivacyPolicyMessages implements JnSystemMessage {

	/** The whole privacy policy. */
	privacyPolicy,
	;

	/**
	 * The privacy policy in Portuguese and English.
	 * @return the seed records
	 */
	public static List<CcpBulkItem> getSeeds() {
		String portuguesePolicy = String.join("\n"
				, "# Política de privacidade"
				, "## 1. Quem somos"
				, "O JobsNow (https://onias.site) é uma plataforma que aproxima candidatos e recrutadores. O controlador dos dados pessoais é Onias Vieira Junior, fundador do JobsNow, com endereço na Avenida Alda, 858, cj. 193, Torre 1, Centro, Diadema, SP, CEP 09910-170, Brasil."
				, "Para qualquer assunto sobre os seus dados pessoais, escreva para privacidade@onias.site."
				, "## 2. Quais dados tratamos"
				, "Tratamos apenas os dados necessários para o funcionamento da plataforma:"
				, "- Dados de acesso: e-mail, senha (guardada somente como hash irreversível), endereço IP e identificação do navegador usados no login, e o registro dos acessos."
				, "- Dados do currículo, informados pelo próprio candidato: endereço do LinkedIn, tipo de contratação (CLT, PJ), pretensão salarial, experiência, cargos, habilidades, idiomas, DDDs de interesse, disponibilidade, viagens, trabalho temporário e a indicação de pessoa com deficiência, quando o candidato escolhe informá-la. O e-mail do dono do currículo é guardado como hash e não é mostrado aos recrutadores."
				, "- Dados dos recrutadores: e-mail profissional, o domínio da empresa e os currículos que visualizaram."
				, "- Mensagens enviadas pelo formulário \"Fale conosco\" e as respostas da equipe de suporte."
				, "## 3. Para que usamos os dados"
				, "- Permitir o login, inclusive o envio por e-mail do token de acesso, que funciona como segundo fator de autenticação."
				, "- Mostrar os currículos aos recrutadores e enviar a eles, de segunda a sexta, no máximo um e-mail por dia com os currículos inseridos ou alterados nos últimos 7 dias."
				, "- Responder às mensagens do \"Fale conosco\" e resolver problemas de acesso."
				, "- Proteger a plataforma contra abuso, com limites diários de uso e registro de erros."
				, "## 4. Bases legais"
				, "Tratamos os dados com base na execução do serviço solicitado pelo titular, no consentimento do candidato ao cadastrar o currículo e no legítimo interesse de aproximar candidatos e recrutadores, sempre com a opção de descadastro, conforme a Lei Geral de Proteção de Dados (Lei 13.709/2018)."
				, "## 5. E-mails e descadastro"
				, "Todo e-mail de currículos enviado aos recrutadores traz um link de descadastro, e os provedores de e-mail compatíveis também oferecem o descadastro em um clique. Quem se descadastra, ou cujo endereço é recusado ou marcado como spam, não recebe mais esses e-mails. Os e-mails de acesso (token de login) são enviados somente quando solicitados."
				, "## 6. Com quem compartilhamos"
				, "Não vendemos dados pessoais. Usamos estes fornecedores para operar a plataforma:"
				, "- Google Cloud: hospedagem e banco de dados, na região de São Paulo, Brasil."
				, "- Twilio SendGrid: envio de e-mails, com servidores nos Estados Unidos."
				, "- Telegram: comunicação interna da equipe de suporte, que recebe apenas o necessário para resolver cada chamado."
				, "## 7. Cookies e armazenamento no navegador"
				, "Usamos um cookie para lembrar o idioma escolhido e o armazenamento local do navegador para manter a sessão de login. Não usamos cookies de publicidade nem ferramentas de rastreamento de terceiros."
				, "## 8. Por quanto tempo guardamos"
				, "Tokens de acesso, sessões e registros temporários são apagados automaticamente depois de horas ou dias. Os demais dados ficam guardados enquanto a conta estiver ativa ou até o titular pedir a exclusão, salvo quando a lei exigir que sejam mantidos."
				, "## 9. Seus direitos"
				, "Você pode pedir, a qualquer momento, a confirmação de que tratamos os seus dados, o acesso a eles, a correção, a anonimização, a portabilidade ou a exclusão, informações sobre o compartilhamento e a revogação do consentimento. Basta escrever para privacidade@onias.site a partir do e-mail cadastrado. Você também pode reclamar à Autoridade Nacional de Proteção de Dados (ANPD)."
				, "## 10. Segurança"
				, "Os dados trafegam por conexões criptografadas (HTTPS), as senhas são guardadas apenas como hash e o acesso aos servidores é restrito."
				, "## 11. Alterações desta política"
				, "Quando esta política mudar, a data abaixo será atualizada."
				, "Última atualização: 2 de novembro de 2026."
				);

		String englishPolicy = String.join("\n"
				, "# Privacy policy"
				, "## 1. Who we are"
				, "JobsNow (https://onias.site) is a platform that brings candidates and recruiters together. The controller of the personal data is Onias Vieira Junior, founder of JobsNow, at Avenida Alda, 858, cj. 193, Torre 1, Centro, Diadema, SP, ZIP 09910-170, Brazil."
				, "For any matter about your personal data, write to privacidade@onias.site."
				, "## 2. Which data we process"
				, "We process only the data the platform needs to work:"
				, "- Access data: e-mail, password (kept only as an irreversible hash), IP address and browser identification used at login, and the record of the accesses."
				, "- Resume data, given by the candidate: LinkedIn address, type of hiring (CLT, PJ), salary expectation, experience, positions, skills, languages, area codes of interest, availability, travel, temporary work and the indication of disability, when the candidate chooses to give it. The e-mail of the owner of the resume is kept as a hash and is not shown to recruiters."
				, "- Recruiter data: professional e-mail, the domain of the company and the resumes they viewed."
				, "- Messages sent through the \"Contact us\" form and the answers of the support team."
				, "## 3. What we use the data for"
				, "- To allow the login, including the e-mail with the access token, which works as a second authentication factor."
				, "- To show the resumes to recruiters and send them, from Monday to Friday, at most one e-mail a day with the resumes added or changed in the last 7 days."
				, "- To answer the \"Contact us\" messages and solve access problems."
				, "- To protect the platform against abuse, with daily usage limits and error records."
				, "## 4. Legal bases"
				, "We process the data based on the performance of the service requested by the data subject, on the consent of the candidate when registering the resume and on the legitimate interest of bringing candidates and recruiters together, always with the option to unsubscribe, under the Brazilian General Data Protection Law (Law 13,709/2018)."
				, "## 5. E-mails and unsubscribing"
				, "Every e-mail of resumes sent to recruiters has an unsubscribe link, and compatible e-mail providers also offer one-click unsubscribe. Whoever unsubscribes, or whose address bounces or is marked as spam, no longer receives these e-mails. Access e-mails (login token) are sent only when requested."
				, "## 6. Who we share with"
				, "We do not sell personal data. We use these providers to run the platform:"
				, "- Google Cloud: hosting and database, in the São Paulo region, Brazil."
				, "- Twilio SendGrid: sending of e-mails, with servers in the United States."
				, "- Telegram: internal communication of the support team, which receives only what is needed to solve each ticket."
				, "## 7. Cookies and browser storage"
				, "We use a cookie to remember the chosen language and the local storage of the browser to keep the login session. We do not use advertising cookies or third-party tracking tools."
				, "## 8. How long we keep the data"
				, "Access tokens, sessions and temporary records are deleted automatically after hours or days. The other data is kept while the account is active or until the data subject asks for its deletion, unless the law requires it to be kept."
				, "## 9. Your rights"
				, "You may ask, at any time, for the confirmation that we process your data, access to it, its correction, anonymization, portability or deletion, information about the sharing and the withdrawal of consent. Just write to privacidade@onias.site from the registered e-mail. You may also complain to the Brazilian National Data Protection Authority (ANPD)."
				, "## 10. Security"
				, "The data travels over encrypted connections (HTTPS), passwords are kept only as a hash and access to the servers is restricted."
				, "## 11. Changes to this policy"
				, "When this policy changes, the date below will be updated."
				, "Last update: November 2, 2026."
				);

		List<CcpBulkItem> seeds = new ArrayList<>();
		List<CcpBulkItem> portugueseSeeds = privacyPolicy.toBulkItems(JnLanguage.portuguese, portuguesePolicy);
		List<CcpBulkItem> englishSeeds = privacyPolicy.toBulkItems(JnLanguage.english, englishPolicy);
		seeds.addAll(portugueseSeeds);
		seeds.addAll(englishSeeds);
		return seeds;
	}
}
