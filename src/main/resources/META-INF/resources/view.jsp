<%@ include file="/init.jsp" %>

<p>
	<b><liferay-ui:message key="basic.caption"/></b>
</p>
<portlet:actionURL var="uploadURL" name="uploadFileAction"/>

<aui:form action="<%= uploadURL %>" method="post" enctype="multipart/form-data">
        <aui:input type="file" name="sampleFile" accept="image/*;cature=camera" />
         <img src="${themeDisplay.getPathThemeImages()}/portlet/help.png" onblur="Liferay.Portal.ToolTip.hide()" onmouseover="Liferay.Portal.ToolTip.show(this)">
 		<aui:button type="submit" cssClass="">Upload</aui:button>
</aui:form>