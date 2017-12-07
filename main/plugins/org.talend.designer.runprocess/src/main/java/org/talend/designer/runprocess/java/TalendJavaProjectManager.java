// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.java;

import static org.talend.designer.maven.model.TalendJavaProjectConstants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.maven.tools.creator.CreateMavenCodeProject;
import org.talend.designer.maven.utils.TalendCodeProjectUtil;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.utils.DeploymentConfsUtils;
import org.talend.utils.io.FilesUtils;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class TalendJavaProjectManager {

    private static Map<ERepositoryObjectType, ITalendProcessJavaProject> talendCodeJavaProjects = new HashMap<>();

    private static Map<String, ITalendProcessJavaProject> talendJobJavaProjects = new HashMap<>();

    private static ITalendProcessJavaProject tempJavaProject;

    public static void initJavaProjects(IProgressMonitor monitor, Project project) {

        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("create aggregator poms") { //$NON-NLS-1$

            @Override
            protected void run() {
                try {
                    // create aggregator poms
                    AggregatorPomsHelper helper = new AggregatorPomsHelper(project);
                    // create poms folder.
                    IFolder poms = createFolderIfNotExist(helper.getProjectPomsFolder(), monitor);

                    // deployments
                    if (PluginChecker.isTIS()) {
                        createFolderIfNotExist(poms.getFolder(DIR_DEPLOYMENTS), monitor);
                    }

                    // codes
                    IFolder code = createFolderIfNotExist(poms.getFolder(DIR_CODES), monitor);
                    // routines
                    createFolderIfNotExist(code.getFolder(DIR_ROUTINES), monitor);
                    // pigudfs
                    if (ProcessUtils.isRequiredPigUDFs(null)) {
                        createFolderIfNotExist(code.getFolder(DIR_PIGUDFS), monitor);
                    }
                    // beans
                    if (ProcessUtils.isRequiredBeans(null)) {
                        createFolderIfNotExist(code.getFolder(DIR_BEANS), monitor);
                    }

                    // jobs
                    IFolder jobs = createFolderIfNotExist(poms.getFolder(DIR_JOBS), monitor);
                    // process
                    createFolderIfNotExist(jobs.getFolder(DIR_PROCESS), monitor);
                    // process_mr
                    if (PluginChecker.isMapReducePluginLoader()) {
                        createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_MR), monitor);
                    }
                    // process_storm
                    if (PluginChecker.isStormPluginLoader()) {
                        createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_STORM), monitor);
                    }
                    // routes
                    if (PluginChecker.isRouteLoaded()) {
                        createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_ROUTES), monitor);
                    }
                    // services
                    if (PluginChecker.isServiceLoaded()) {
                        createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_SERVICES), monitor);
                    }
                    helper.createRootPom(poms, monitor);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
    }

    public static void installRootPom(boolean current) {
        AggregatorPomsHelper helper = new AggregatorPomsHelper(ProjectManager.getInstance().getCurrentProject());
        try {
            helper.installRootPom(current);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    public static ITalendProcessJavaProject getTalendCodeJavaProject(ERepositoryObjectType type) {
        Project project = ProjectManager.getInstance().getCurrentProject();
        AggregatorPomsHelper helper = new AggregatorPomsHelper(project);
        ITalendProcessJavaProject talendCodeJavaProject = talendCodeJavaProjects.get(type);
        if (talendCodeJavaProject == null || talendCodeJavaProject.getProject() == null
                || !talendCodeJavaProject.getProject().exists()) {
            try {
                IProgressMonitor monitor = new NullProgressMonitor();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IFolder codeProjectFolder = helper.getProjectPomsFolder().getFolder(type.getFolder());
                IProject codeProject = root.getProject((project.getTechnicalLabel() + "_" + type.name()).toUpperCase()); //$NON-NLS-1$
                if (!codeProject.exists() || TalendCodeProjectUtil.needRecreate(monitor, codeProject)) {
                    createMavenJavaProject(monitor, codeProject, codeProjectFolder);
                }
                IJavaProject javaProject = JavaCore.create(codeProject);
                if (!javaProject.isOpen()) {
                    javaProject.open(monitor);
                }
                AggregatorPomsHelper.updateCodeProjectPom(monitor, type, codeProject.getFile(TalendMavenConstants.POM_FILE_NAME));
                talendCodeJavaProject = new TalendProcessJavaProject(javaProject);
                talendCodeJavaProject.cleanMavenFiles(monitor);
                talendCodeJavaProjects.put(type, talendCodeJavaProject);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return talendCodeJavaProject;
    }

    public static ITalendProcessJavaProject getTalendJobJavaProject(Property property) {
        if (property == null) {
            return getTempJavaProject();
        }
        if (property.getItem() instanceof JobletProcessItem) {
            return getTempJavaProject();
        }
        if (!(property.getItem() instanceof ProcessItem)) {
            return null;
        }
        ITalendProcessJavaProject talendJobJavaProject = null;
        try {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                        .getDefault().getService(ITestContainerProviderService.class);
                if (testContainerService.isTestContainerItem(property.getItem())) {
                    property = testContainerService.getParentJobItem(property.getItem()).getProperty();
                }
            }
            String projectTechName = ProjectManager.getInstance().getProject(property).getTechnicalLabel();
            Project project = ProjectManager.getInstance().getProjectFromProjectTechLabel(projectTechName);
            AggregatorPomsHelper helper = new AggregatorPomsHelper(project);
            String jobProjectId = AggregatorPomsHelper.getJobProjectId(property);
            talendJobJavaProject = talendJobJavaProjects.get(jobProjectId);
            if (talendJobJavaProject == null || talendJobJavaProject.getProject() == null
                    || !talendJobJavaProject.getProject().exists()) {
                IProgressMonitor monitor = new NullProgressMonitor();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IProject jobProject = root.getProject(AggregatorPomsHelper.getJobProjectName(project, property));
                IPath itemRelativePath = ItemResourceUtil.getItemRelativePath(property);
                String jobFolderName = AggregatorPomsHelper.getJobProjectFolderName(property);
                ERepositoryObjectType type = ERepositoryObjectType.getItemType(property.getItem());
                IFolder jobFolder = helper.getProcessFolder(type).getFolder(itemRelativePath).getFolder(jobFolderName);
                if (!jobProject.exists() || TalendCodeProjectUtil.needRecreate(monitor, jobProject)) {
                    createMavenJavaProject(monitor, jobProject, jobFolder);
                    AggregatorPomsHelper.updatePomIfCreate(monitor, jobProject.getFile(TalendMavenConstants.POM_FILE_NAME),
                            property);
                }
                IJavaProject javaProject = JavaCore.create(jobProject);
                if (!javaProject.isOpen()) {
                    javaProject.open(monitor);
                }
                talendJobJavaProject = new TalendProcessJavaProject(javaProject, property);
                if (talendJobJavaProject != null) {
                    MavenPomSynchronizer pomSynchronizer = new MavenPomSynchronizer(talendJobJavaProject);
                    pomSynchronizer.syncTemplates(false);
                    pomSynchronizer.cleanMavenFiles(monitor);
                }
                talendJobJavaProjects.put(jobProjectId, talendJobJavaProject);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return talendJobJavaProject;
    }

    public static ITalendProcessJavaProject getTempJavaProject() {
        NullProgressMonitor monitor = new NullProgressMonitor();
        if (tempJavaProject == null) {
            try {
                IProject project = TalendCodeProjectUtil.initCodeProject(monitor);
                if (project != null) {
                    IJavaProject javaProject = JavaCore.create(project);
                    if (!javaProject.isOpen()) {
                        javaProject.open(monitor);
                    }
                    tempJavaProject = new TalendProcessJavaProject(javaProject);
                    tempJavaProject.cleanMavenFiles(monitor);
                    tempJavaProject.createSubFolder(monitor, tempJavaProject.getSrcFolder(), JavaUtils.JAVA_INTERNAL_DIRECTORY);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return tempJavaProject;
    }

    public static ITalendProcessJavaProject getExistingTalendProject(IProject project) {
        List<ITalendProcessJavaProject> talendProjects = new ArrayList<>();
        talendProjects.addAll(talendCodeJavaProjects.values());
        talendProjects.addAll(talendJobJavaProjects.values());
        talendProjects.add(tempJavaProject);
        for (ITalendProcessJavaProject talendProject : talendProjects) {
            if (project == talendProject.getProject()) {
                return talendProject;
            }
        }
        return null;
    }

    public static ITalendProcessJavaProject getExistingTalendJobProject(String id, String version) {
        return talendJobJavaProjects.get(AggregatorPomsHelper.getJobProjectId(id, version));
    }

    public static Set<ITalendProcessJavaProject> getExistingAllVersionTalendJobProject(String id) {
        Set<ITalendProcessJavaProject> allVersionProjects = new HashSet<>();
        for (Entry<String, ITalendProcessJavaProject> entry : talendJobJavaProjects.entrySet()) {
            String key = entry.getKey();
            if (key.contains(id)) {
                allVersionProjects.add(entry.getValue());
            }
        }
        return allVersionProjects;
    }

    public static void deleteAllVersionTalendJobProject(String id) {

        RepositoryWorkUnit workUnit = new RepositoryWorkUnit<Object>("Delete job project") { //$NON-NLS-1$

            @Override
            protected void run() {
                try {
                    AggregatorPomsHelper helper = new AggregatorPomsHelper(ProjectManager.getInstance().getCurrentProject());
                    List<IRepositoryViewObject> allVersionObjects = ProxyRepositoryFactory.getInstance().getAllVersion(id);
                    Set<String> removedVersions = new HashSet<>();
                    Iterator<String> iterator = talendJobJavaProjects.keySet().iterator();
                    // delete exist project
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (key.contains(id)) {
                            ITalendProcessJavaProject projectToDelete = talendJobJavaProjects.get(key);
                            projectToDelete.getProject().delete(true, true, null);
                            String version = key.split("\\|")[1]; //$NON-NLS-1$
                            removedVersions.add(version);
                            iterator.remove();
                        }
                    }
                    // for logically deleted project, delete the folder directly
                    for (IRepositoryViewObject object : allVersionObjects) {
                        String realVersion = object.getVersion();
                        if (!removedVersions.contains(realVersion)) {
                            IPath path = DeploymentConfsUtils.getJobProjectPath(object.getProperty(), realVersion);
                            File projectFolder = path.toFile();
                            if (projectFolder.exists()) {
                                FilesUtils.deleteFolder(projectFolder, true);
                            }
                        }
                    }
                    helper.getProjectPomsFolder().refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
    }

    public static void deleteRemovedOrRenamedJobProject(String id) {
        // TODO check rename and move actions
        // also all versions to be remove.
    }

    private static void createMavenJavaProject(IProgressMonitor monitor, IProject jobProject, IFolder projectFolder)
            throws CoreException, Exception {
        if (jobProject.exists()) {
            if (jobProject.isOpen()) {
                jobProject.close(monitor);
            }
            jobProject.delete(true, true, monitor);
        }
        CreateMavenCodeProject createProject = new CreateMavenCodeProject(jobProject);
        createProject.setProjectLocation(projectFolder.getLocation());
        createProject.setPomFile(projectFolder.getFile(TalendMavenConstants.POM_FILE_NAME));
        createProject.create(monitor);
        jobProject = createProject.getProject();
        if (!jobProject.isOpen()) {
            jobProject.open(IProject.BACKGROUND_REFRESH, monitor);
        } else {
            if (!jobProject.isSynchronized(IProject.DEPTH_INFINITE)) {
                jobProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            }
        }

    }

    private static IFolder createFolderIfNotExist(IFolder folder, IProgressMonitor monitor) throws CoreException {
        if (!folder.exists()) {
            folder.create(true, true, monitor);
        }
        return folder;
    }

    public static void deleteEclipseProjectByNatureId(String natureId) throws CoreException {
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for (IProject project : projects) {
                    if (project.hasNature(natureId)) {
                        IFile eclipseClasspath = project.getFile(CLASSPATH_FILE_NAME);
                        if (eclipseClasspath.exists()) {
                            eclipseClasspath.delete(true, monitor);
                        }
                        IFile projectFile = project.getFile(PROJECT_FILE_NAME);
                        if (projectFile.exists()) {
                            projectFile.delete(true, monitor);
                        }
                        project.delete(false, true, monitor);
                    }
                }
            };

        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            ISchedulingRule schedulingRule = workspace.getRoot();
            // the update the project files need to be done in the workspace runnable to avoid all
            // notification
            // of changes before the end of the modifications.
            workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
        } catch (CoreException e) {
            if (e.getCause() != null) {
                ExceptionHandler.process(e.getCause());
            } else {
                ExceptionHandler.process(e);
            }
        }

    }

}
