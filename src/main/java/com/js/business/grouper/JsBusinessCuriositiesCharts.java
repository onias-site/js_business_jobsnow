package com.js.business.grouper;

import java.util.Arrays;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.utils.JnGrouperChart;
import com.js.entities.JsEntityContactUs;
import com.js.entities.JsEntityLoginAnswers;
import com.js.entities.JsEntityVirtualContactUsTypeCounter;
import com.js.entities.JsEntityVirtualInsertionDayCounter;
import com.js.entities.JsEntityVirtualInsertionMonthCounter;
import com.js.entities.JsEntityVirtualLoginChannelCounter;
import com.js.entities.JsEntityVirtualLoginChannelGoalCounter;
import com.js.entities.JsEntityVirtualLoginGoalCounter;

/**
 * The provider of the charts of js in the screen Curiosities, run by class name by {@code JnBusinessGetGrouperCharts}
 * (seeded in {@code JnEntityGrouperChartsProviders} by {@link JsEntityContactUs}): the groups of {@code jn_grouper} behind
 * each chart, read by id.
 */
public class JsBusinessCuriositiesCharts implements CcpBusiness {

	/** The charts of js. No input. */
	public static enum Fields implements CcpJsonFieldName {
		/** The answers of the sign-up questionnaire inserted in each day ({@code {yyyyMMdd: n}}): the new users. */
		newUsersByDay,
		/** The answers of the sign-up questionnaire inserted in each month ({@code {yyyyMM: n}}). */
		newUsersByMonth,
		/** The goals of the users ({@code {goal: n}}). */
		goals,
		/** How the users arrived ({@code {channel: n}}). */
		channels,
		/** The tickets ever opened, by type ({@code {type: n}}). */
		contactUsTypes,
		/** The channel crossed with the goal of the users ({@code {"<channel>|<goal>": n}}), since 2026-11-17. */
		channelGoals,
	}

	/** The single instance. */
	public static final JsBusinessCuriositiesCharts INSTANCE = new JsBusinessCuriositiesCharts();

	private JsBusinessCuriositiesCharts() {}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}

	/**
	 * The groups of the charts of js.
	 * @param json not used
	 * @return {@code {"charts": [...]}}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation dayCombination = JsEntityVirtualInsertionDayCounter.getCombination();
		CcpJsonRepresentation byDay = JnGrouperChart.counters(Fields.newUsersByDay, JsEntityVirtualInsertionDayCounter.ENTITY, JsEntityLoginAnswers.ENTITY, dayCombination);
		CcpJsonRepresentation monthCombination = JsEntityVirtualInsertionMonthCounter.getCombination();
		CcpJsonRepresentation byMonth = JnGrouperChart.counters(Fields.newUsersByMonth, JsEntityVirtualInsertionMonthCounter.ENTITY, JsEntityLoginAnswers.ENTITY, monthCombination);
		CcpJsonRepresentation goalCombination = JsEntityVirtualLoginGoalCounter.getCombination();
		CcpJsonRepresentation goals = JnGrouperChart.counters(Fields.goals, JsEntityVirtualLoginGoalCounter.ENTITY, JsEntityLoginAnswers.ENTITY, goalCombination);
		CcpJsonRepresentation channelCombination = JsEntityVirtualLoginChannelCounter.getCombination();
		CcpJsonRepresentation channels = JnGrouperChart.counters(Fields.channels, JsEntityVirtualLoginChannelCounter.ENTITY, JsEntityLoginAnswers.ENTITY, channelCombination);
		CcpJsonRepresentation typeCombination = JsEntityVirtualContactUsTypeCounter.getCombination();
		CcpJsonRepresentation contactUsTypes = JnGrouperChart.counters(Fields.contactUsTypes, JsEntityVirtualContactUsTypeCounter.ENTITY, JsEntityContactUs.ENTITY, typeCombination);
		CcpJsonRepresentation channelGoalCombination = JsEntityVirtualLoginChannelGoalCounter.getCombination();
		CcpJsonRepresentation channelGoals = JnGrouperChart.counters(Fields.channelGoals, JsEntityVirtualLoginChannelGoalCounter.ENTITY, JsEntityLoginAnswers.ENTITY, channelGoalCombination);
		List<CcpJsonRepresentation> charts = Arrays.asList(byDay, byMonth, goals, channels, contactUsTypes, channelGoals);
		CcpJsonRepresentation output = JnGrouperChart.output(charts);
		return output;
	}
}
