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
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.login.AbstractLoginTask;
import org.talend.repository.ProjectManager;

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

            AggregatorPomsHelper.updateCodeProjects(monitor);

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

}
