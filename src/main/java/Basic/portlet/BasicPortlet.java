package Basic.portlet;

import Basic.constants.BasicPortletKeys;

import com.liferay.document.library.kernel.exception.DuplicateFolderNameException;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.File;
import java.io.InputStream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.ProcessAction;

import org.osgi.service.component.annotations.Component;

/**
 * @author 10608
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=Basic",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + BasicPortletKeys.BASIC,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class BasicPortlet extends MVCPortlet {
	@ProcessAction(name = "uploadFileAction")
	public final void uploadFile(final ActionRequest request, final ActionResponse response) {
		// ParamUtil.get
		System.out.println("uploadFile");
		final UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(request);
		final File file = uploadPortletRequest.getFile("sampleFile");
		
		if (file.exists()) {
			ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			long folderId = createDLFolder(themeDisplay,"Evidence","Evidence Collection Folder");
			String url = fileUploadByDL(file,folderId,themeDisplay);
			System.out.println("uploadFile url:"+url);
			String path = request.getContextPath() + "/documents/20123/35307/"+file.getName();
			System.out.println("<img src=" + path + "></img>");
		}
	}

	public long createDLFolder( ThemeDisplay themeDisplay, String folderName,
			String description) {
		long folderId = 0L;
		long userId = themeDisplay.getUserId();
		long groupId = themeDisplay.getScopeGroupId();
		long repositoryId = themeDisplay.getScopeGroupId();// repository id is same as groupId
		boolean mountPoint = false; // mountPoint which is a boolean specifying whether the folder is a facade for
									// mounting a third-party repository
		long parentFolderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID; // or 0
		boolean hidden = false; // true if you want to hidden the folder
		DLFolder dlFolder = null;
		try {
			ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
			dlFolder = DLFolderLocalServiceUtil.addFolder(userId, groupId, repositoryId, mountPoint,
					parentFolderId, folderName, description, hidden, serviceContext);
			folderId = dlFolder.getFolderId();
		}catch(DuplicateFolderNameException dfne) { 
			try {
				dlFolder = DLFolderLocalServiceUtil.getFolder(groupId, 0L, folderName);
				folderId = dlFolder.getFolderId();
			} catch (PortalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}catch (PortalException e1) {
			e1.printStackTrace();
		} catch (SystemException e1) {
			e1.printStackTrace();
		}
		return folderId;
	}

	public String fileUploadByDL(File file, long folderId, ThemeDisplay themeDisplay) {
		long userId = themeDisplay.getUserId();
		long groupId = themeDisplay.getScopeGroupId();
		long repositoryId = themeDisplay.getScopeGroupId();
		String mimeType = MimeTypesUtil.getContentType(file);
		String title = file.getName();
		String description = "This file is added via programatically";
		String changeLog = "hi";
		String fileEntryUrl = "";
		try {
			DLFolder dlFolder = DLFolderLocalServiceUtil.getFolder(folderId);
			long fileEntryTypeId = dlFolder.getDefaultFileEntryTypeId();
			ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
			//InputStream is = new FileInputStream(file);
			System.out.println("--total space--"+file.getTotalSpace());
			DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.addFileEntry(userId, groupId, repositoryId,
					dlFolder.getFolderId(), file.getName(), mimeType, title, description, changeLog, fileEntryTypeId,
					null, file, null, file.getTotalSpace(), serviceContext);

			InputStream cs= dlFileEntry.getContentStream();
			System.out.println(cs);
			// Change mode of Draft to Approved
			DLFileEntryLocalServiceUtil.updateFileEntry(userId, dlFileEntry.getFileEntryId(), file.getName(),
					MimeTypesUtil.getContentType(file), title, description, "Draft to save", true,
					dlFileEntry.getFileEntryTypeId(), null, file, null, file.getTotalSpace(), serviceContext);
			
			fileEntryUrl = "/documents/"+""+dlFileEntry.getGroupId()+"/"+folderId+"/"+dlFileEntry.getFileName();
		} catch (Exception e) {
			System.out.println("Exception");
			e.printStackTrace();
		}
		return fileEntryUrl;
	}
}