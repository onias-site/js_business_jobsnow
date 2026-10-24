package com.js.business.bots;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * The Portuguese names of the commands of {@link JsSupportBotCommands}.
 */
public enum JsCommandNamesInPortuguese implements CcpJsonFieldName{
	solveLoginTokenTicket { public String getValue() { return "solucionarTicketsDeTokenDeLogin"; } },
	answerContactUs { public String getValue() { return "responderFaleConosco"; } },
	ignoredContactUs { public String getValue() { return "listarFaleConoscoIgnorados"; } },
	;
}
