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
package org.talend.designer.runprocess.maven;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.login.AbstractLoginTask;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;

/**
 * created by ggu on 26 Mar 2015 Detailled comment
 *
 * install aggregator poms after all synchronize(codes file, java version settings) work done.
 */
public class MavenPomInstallLoginTask extends AbstractLoginTask implements IRunnableWithProgress {

    @Override
    public boolean isCommandlineTask() {
        return true;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            AggregatorPomsHelper helper = new AggregatorPomsHelper(ProjectManager.getInstance().getCurrentProject());
            helper.installRootPom(true);

            List<Project> references = ProjectManager.getInstance().getReferencedProjects();
            for (Project ref : references) {
                AggregatorPomsHelper refHelper = new AggregatorPomsHelper(ref);
                refHelper.installRootPom(true);
            }
            RepositoryWorkUnit workUnit = new RepositoryWorkUnit<Object>("update code project") { //$NON-NLS-1$

                @Override
                protected void run() {
                    updateCodeProject(monitor, ERepositoryObjectType.ROUTINES);
                    if (ProcessUtils.isRequiredPigUDFs(null)) {
                        updateCodeProject(monitor, ERepositoryObjectType.PIG_UDF);
                    }
                    if (ProcessUtils.isRequiredBeans(null)) {
                        updateCodeProject(monitor, ERepositoryObjectType.valueOf("BEANS")); //$NON-NLS-1$
                    }
                }
            };
            workUnit.setAvoidUnloadResources(true);
            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void updateCodeProject(IProgressMonitor monitor, ERepositoryObjectType codeProjectType) {
        try {
            ITalendProcessJavaProject codeProject = TalendJavaProjectManager.getTalendCodeJavaProject(codeProjectType);
            AggregatorPomsHelper.updateCodeProjectPom(monitor, codeProjectType, codeProject.getProjectPom());
            MavenPomSynchronizer.buildAndInstallCodesProject(codeProject, true);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

}
