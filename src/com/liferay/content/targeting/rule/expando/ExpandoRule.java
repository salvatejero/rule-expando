/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.content.targeting.rule.expando;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.liferay.content.targeting.anonymous.users.model.AnonymousUser;
import com.liferay.content.targeting.api.model.BaseRule;
import com.liferay.content.targeting.api.model.Rule;
import com.liferay.content.targeting.model.RuleInstance;
import com.liferay.content.targeting.rule.categories.UserAttributesRuleCategory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;

/**
 * @author Brian Chan
 */
@Component(immediate = true, service = Rule.class)
public class ExpandoRule extends BaseRule {

	@Activate
	@Override
	public void activate() {
		super.activate();
	}

	@Deactivate
	@Override
	public void deActivate() {
		super.deActivate();
	}

	@Override
	public boolean evaluate(
			HttpServletRequest request, RuleInstance ruleInstance,
			AnonymousUser anonymousUser)
		throws Exception {

		User user = anonymousUser.getUser();

		if (user == null) {
			return false;
		}

		JSONObject jsonObj = JSONFactoryUtil.createJSONObject(
			ruleInstance.getTypeSettings());

		String column = jsonObj.getString("column");
		String value = jsonObj.getString("columnValue");

		try{
			String columnName = ExpandoColumnLocalServiceUtil.getColumn(Long.valueOf(column)).getName();
			
			Serializable columnUser = user.getExpandoBridge().getAttribute(columnName);
			
			if(columnUser != null && columnUser.toString().equalsIgnoreCase(value)){
				return true;
			}
		}catch(Exception e){}
		return false;
	}

	@Override
	public String getIcon() {
		return "icon-puzzle";
	}

	@Override
	public String getRuleCategoryKey() {
		return UserAttributesRuleCategory.KEY;
	}

	@Override
	public String getSummary(RuleInstance ruleInstance, Locale locale) {
		return LanguageUtil.get(locale, ruleInstance.getTypeSettings());
	}

	@Override
	public String processRule(
		PortletRequest request, PortletResponse response, String id,
		Map<String, String> values) {

		int column = GetterUtil.getInteger(values.get("column"));
		String columnValue = GetterUtil.getString(values.get("columnValue"));

		JSONObject jsonObj = JSONFactoryUtil.createJSONObject();

		jsonObj.put("column", column);
		jsonObj.put("columnValue", columnValue);

		return jsonObj.toString();
	}

	@Override
	protected void populateContext(
		RuleInstance ruleInstance, Map<String, Object> context,
		Map<String, String> values) {
		
		long column = 0;
		String value = "";
		
		if (!values.isEmpty()) {
			column = GetterUtil.getLong(values.get("column"));
			value = GetterUtil.getString(values.get("columnValue"));
		}
		else if (ruleInstance != null) {
			String typeSettings = ruleInstance.getTypeSettings();

			try {
				JSONObject jsonObj = JSONFactoryUtil.createJSONObject(
					typeSettings);

				column = jsonObj.getLong("column");
				value = jsonObj.getString("columnValue");
			}
			catch (JSONException jse) {
			}
		}

		context.put("columnName", column);
		context.put("columnValue", value);
		
		Company company = (Company)context.get("company");
		try {
			ExpandoTable table = ExpandoTableLocalServiceUtil.getDefaultTable(company.getCompanyId(), User.class.getName());
			List<ExpandoColumn> columns = ExpandoColumnLocalServiceUtil.getColumns(company.getCompanyId(), User.class.getName(), table.getName());
			context.put("columns", columns);
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
			
			
		
	}

}