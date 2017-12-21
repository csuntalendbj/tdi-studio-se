// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.maven.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.documentation.ERepositoryActionName;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class ProcessChangeListener implements PropertyChangeListener {

    private List<ERepositoryObjectType> allProcessType;

    public ProcessChangeListener() {
        allProcessType = ERepositoryObjectType.getAllTypesOfProcess();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if (propertyName.equals(ERepositoryActionName.PROPERTIES_CHANGE.getName())
                || propertyName.equals(ERepositoryActionName.MOVE.getName())) {
            deleteAllVersionJobProjects(event.getOldValue());
        } else if (propertyName.equals(ERepositoryActionName.DELETE_FOREVER.getName())) {
            deleteAllVersionJobProjects(event.getNewValue());
        } else if (propertyName.equals(ERepositoryActionName.FOLDER_RENAME.getName())) {
            deleteJobProjectsByRenamedFolder(event);
        } else if (propertyName.equals(ERepositoryActionName.FOLDER_MOVE.getName())) {
            deleteJobProjectsByMovedFolder(event);
        } else if (propertyName.equals(ERepositoryActionName.FOLDER_DELETE.getName())) {
            deleteJobProjectsByDeletedFolder(event);
        } else if (propertyName.equals(ERepositoryActionName.SAVE.getName())) {
            generatePom(event.getNewValue());
        }
    }

    private void deleteJobProjectsByRenamedFolder(PropertyChangeEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (oldValue instanceof IPath && newValue instanceof Object[]) {
            IPath folderPath = (IPath) oldValue;
            Object[] objects = (Object[]) newValue;
            ERepositoryObjectType processType = (ERepositoryObjectType) objects[1];
            TalendJavaProjectManager.deleteTalendJobProjectsUnderFolder(folderPath, processType, false);
        }
    }

    private void deleteJobProjectsByMovedFolder(PropertyChangeEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (oldValue instanceof IPath[] && newValue instanceof ERepositoryObjectType) {
            IPath[] paths = (IPath[]) oldValue;
            IPath folderPath = paths[0];
            ERepositoryObjectType processType = (ERepositoryObjectType) newValue;
            TalendJavaProjectManager.deleteTalendJobProjectsUnderFolder(folderPath, processType, false);
        }
    }

    private void deleteJobProjectsByDeletedFolder(PropertyChangeEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (oldValue instanceof IPath && newValue instanceof ERepositoryObjectType) {
            IPath folderPath = (IPath) oldValue;
            ERepositoryObjectType processType = (ERepositoryObjectType) newValue;
            TalendJavaProjectManager.deleteTalendJobProjectsUnderFolder(folderPath, processType, true);
        }
    }

    private void deleteAllVersionJobProjects(Object object) {
        if (object instanceof IRepositoryViewObject) {
            Property property = ((IRepositoryViewObject) object).getProperty();
            ERepositoryObjectType type = ERepositoryObjectType.getItemType(property.getItem());
            if (allProcessType.contains(type)) {
                TalendJavaProjectManager.deleteAllVersionTalendJobProject(property.getId());
            }
        }
    }

    private void generatePom(Object object) {
        if (object instanceof ProcessItem) {
            ProcessItem proceeItem = (ProcessItem) object;
            if (allProcessType.contains(ERepositoryObjectType.getItemType(proceeItem))) {
                IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
                IProcess process = service.getProcessFromProcessItem(proceeItem);
                IContext context = process.getContextManager().getDefaultContext();
                IProcessor processor = ProcessorUtilities.getProcessor(process, proceeItem.getProperty(), context);
                if (processor instanceof MavenJavaProcessor) {
                    LastGenerationInfo.getInstance().clearModulesNeededWithSubjobPerJob();
                    ((MavenJavaProcessor) processor).generatePom(0);
                }
            }
        }
    }

}
