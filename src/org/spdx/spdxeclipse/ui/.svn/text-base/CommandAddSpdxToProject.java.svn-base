/**
 * Copyright (c) 2015 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.spdx.spdxeclipse.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.spdxeclipse.project.SpdxProject;
import org.spdx.spdxeclipse.project.SpdxProjectException;
import org.spdx.spdxeclipse.project.SpdxProjectFactory;
import org.spdx.spdxeclipse.properties.SpdxProjectProperties;

/**
 * Command to add and SPDX file and tracking to a project.  An incremental builder and nature
 * will also be added to the project to continuously maintain the SPDX file.
 * @author Gary O'Neall
 *
 */
public class CommandAddSpdxToProject implements IHandler {
	
	ArrayList<IHandlerListener> handlerListeners = new ArrayList<IHandlerListener>();
	ISelectionListener selectionListener = null;
	IProject selectedProject = null;
	
	public CommandAddSpdxToProject() {
		selectionListener = new ISelectionListener() {

			@Override
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				if(selection instanceof StructuredSelection) {
					Object element = ((StructuredSelection)selection).getFirstElement();
					if (element instanceof IResource) {
						selectedProject = ((IResource)element).getProject();
					}
				}
			}
			
		};
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWindow != null) {
			activeWindow.getSelectionService().addSelectionListener(selectionListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#addHandlerListener(org.eclipse.core.commands.IHandlerListener)
	 */
	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		this.handlerListeners.add(handlerListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#dispose()
	 */
	@Override
	public void dispose() {
		if (selectionListener != null) {
			IWorkbench workBench = PlatformUI.getWorkbench();
			if (workBench != null) {				
				IWorkbenchWindow window = workBench.getActiveWorkbenchWindow();
				if (window != null) {
					ISelectionService selectionService = window.getSelectionService();
					if (selectionService != null) {
						selectionService.removeSelectionListener(selectionListener);
					}
				}
			}
		}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String projectName = null;
		if (selectedProject != null) {
			projectName = selectedProject.getName();
		}
		if (projectName == null) {
			projectName = UIHelper.chooseProject();
		}
		if (projectName == null) {
			MessageDialog.openWarning(shell, "No Project Select", 
					"No project selected - please select a valid Eclipse project");
			return null;
		}

		createSpdxProject(shell, projectName);
		return null;
	}
	
	public static void createSpdxProject(Shell parent, final String projectName) {
		final SpdxProject[] spdxProjects = new SpdxProject[1];
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						spdxProjects[0] = SpdxProjectFactory.getSpdxProject(projectName, monitor);
					} catch (IOException e) {
						throw(new InvocationTargetException(e));
					} catch (InvalidSPDXAnalysisException e) {
						throw(new InvocationTargetException(e));
					} catch (SpdxProjectException e) {
						throw(new InvocationTargetException(e));
					}
				}
				
			});
		} catch (InvocationTargetException e) {
			MessageDialog.openError(parent, "Error", 
			"Error opening SPDX project: "+e.getTargetException().getMessage());
		} catch (InterruptedException e) {
			return;
		}
		
		SpdxProject spdx = spdxProjects[0];

		if (spdx.isCreated()) {
			MessageDialog.openWarning(parent, "Already Added", 
					"Warning: SPDX has already been added to the project "+projectName);
			return;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		String fileName;
		try {
			fileName = SpdxProjectProperties.getSpdxFileName(project);
		} catch (CoreException e1) {
			MessageDialog.openError(parent, "Error",
					"Unexpected error getting default SPDX file name for project: "+e1.getMessage());
			return;
		}
		IFile spdxResource = project.getFile(fileName);
		if (spdxResource.exists()) {
			MessageDialog.openError(parent, "SPDX File Already Exists", 
					"The SPDX file "+spdxResource.getName()+" already exists for this project.  Either rename the file, delete the file, or change the SPDX file name in the project preferences for this project.");
			return;
		}
		Wizard createWizard = new CreateSpdxProjectWizard(spdx);
		WizardDialog wizardDialog = new WizardDialog(parent, createWizard);
		if (wizardDialog.open() == WizardDialog.OK) {
			Job job = new CreateSpdxJob(spdx, spdxResource);
			job.setRule(project);
			job.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(parent, job);
		}
	}

//	private String getSelectedProject() {
//		IViewReference[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
//		for (int i = 0; i < views.length; i++) {
//				TreeViewer pkgTreeView = (TreeViewer)views[i].getView(false);
//				return pkgTreeView.getSelection().toString();
//			}
//		}
//		// not found - return null
//		return null;
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#isHandled()
	 */
	@Override
	public boolean isHandled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#removeHandlerListener(org.eclipse.core.commands.IHandlerListener)
	 */
	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		this.handlerListeners.remove(handlerListener);
	}

}
