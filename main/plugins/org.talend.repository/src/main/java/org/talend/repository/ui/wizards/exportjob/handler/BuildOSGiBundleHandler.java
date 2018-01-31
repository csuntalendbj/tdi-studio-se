// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2013 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.handler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.maven.model.MavenSystemFolders;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.OSGIJavaScriptForESBWithMavenManager;

/**
 * DOC yyan class global comment. 2018-1-15
 * 
 * For OSGi data service - REST
 */
public class BuildOSGiBundleHandler extends BuildJobHandler {

    private OSGIJavaScriptForESBWithMavenManager osgiMavenManager;

    public BuildOSGiBundleHandler(ProcessItem processItem, String version, String contextName,
            Map<ExportChoice, Object> exportChoiceMap) {
        super(processItem, version, contextName, exportChoiceMap);

        osgiMavenManager = new OSGIJavaScriptForESBWithMavenManager(exportChoiceMap, contextName, JobScriptsManager.LAUNCHER_ALL,
                IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);

        osgiMavenManager.setJobVersion(version);
        osgiMavenManager.setBundleVersion(version);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler#generateJobFiles(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    public IProcessor generateJobFiles(IProgressMonitor monitor) throws Exception {
        // IProcessor processor = super.generateJobFiles(monitor);

        List<ExportFileResource> resources = osgiMavenManager
                .getExportResources(new ExportFileResource[] { new ExportFileResource(processItem, "") });
        for (ExportFileResource resource : resources) {

            for (String relativePath : resource.getRelativePathList()) {
                String path = resource.getDirectoryName().isEmpty() ? relativePath : resource.getDirectoryName();

                for (URL url : resource.getResourcesByRelativePath(relativePath)) {
                    String resourceUrl = url.toString();
                    String fileName = resourceUrl.substring(resourceUrl.lastIndexOf('/') + 1, resourceUrl.length());
                    IFile target = getTargetFile(path, fileName, monitor);
                    if (target != null) {
                        setFileContent(new FileInputStream(url.getFile()), target, monitor);
                    }
                }
            }
        }
        // return processor;
        return null;
    }

    private IFile getTargetFile(String path, String fileName, IProgressMonitor monitor) {
        IFolder folder = null;
        if (path.startsWith(MavenSystemFolders.RESOURCES.getPath())) {
            String sub = path.replaceAll(MavenSystemFolders.RESOURCES.getPath(), "");
            folder = talendProcessJavaProject.createSubFolder(monitor, talendProcessJavaProject.getResourcesFolder(), sub);
        } else if (path.startsWith(MavenSystemFolders.JAVA.getPath())) {
            String sub = path.replaceAll(MavenSystemFolders.JAVA.getPath(), "");
            folder = talendProcessJavaProject.getSrcSubFolder(monitor, sub);
        }
         return folder == null ? talendProcessJavaProject.getProject().getFile(fileName) : folder.getFile(fileName);
    }

    private void setFileContent(InputStream inputStream, IFile file, IProgressMonitor monitor) throws CoreException {
        if (file.exists()) {
            file.setContents(inputStream, 0, monitor);
        } else {
            file.create(inputStream, 0, monitor);
        }
    }
}
