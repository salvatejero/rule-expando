<#assign aui = PortletJspTagLibs["/META-INF/aui.tld"] />
<#assign liferay_ui = PortletJspTagLibs["/META-INF/liferay-ui.tld"] />

<#setting number_format="computer">

<#if !columns?has_content >
	<div class="alert alert-warning">
		<strong><@liferay_ui["message"] key="expandorule.noexpando.for.user" /></strong>

	</div>
<#else>
	<@aui["select"] label="Columnas" name="column">
		<#list columns as column>
			<@aui["option"] label="${column.getDisplayName(locale)}" selected=(column.getColumnId() == columnName) value=column.getColumnId() />
		</#list>
	</@>
		
	<@aui["input"] name="columnValue" label="Valor" value=columnValue />

</#if>