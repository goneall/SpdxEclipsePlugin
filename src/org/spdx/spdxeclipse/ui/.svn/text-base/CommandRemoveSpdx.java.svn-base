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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.spdxeclipse.project.SpdxProjectException;
import org.spdx.spdxeclipse.project.SpdxProjectFactory;

/**
 * Command to remove SPDX management by the plugin
 * @author Gary O'Neall
 *
 */
public class CommandRemoveSpdx implements IHandler {

	ArrayList<IHandlerListener> handlerListeners = new ArrayList<IHandlerListener>();
	ISelectionListener selectionListener = null;
	IProject selectedProject = null;

	/**
	 * Command to remove SPDX management by the plugin
	 */
	public CommandRemoveSpdx() {
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
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
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
		try {
			if (!isSpdxProjectEnable(projectName)) {
				MessageDialog.openError(shell, "SPDX Not Enabled", "SPDX is not enabled for this project");
				return null;
			}
		} catch (InvocationTargetException e) {
			MessageDialog.openError(shell, "Error", "Error getting current SPDX state");
			return null;
		} catch (InterruptedException e) {
			return null;
		}
		
		if (MessageDialog.openConfirm(shell, 
				"Confirm Delete SPDX", "Confirm disabling SPDX updates for "+projectName+
				".  Note: SPDX file will not be removed, but will no longer be updated.")) {
			removeSpdx(shell, projectName);
		}

		return null;
	}

	private boolean isSpdxProjectEnable(final String projectName) throws InvocationTargetException, InterruptedException {
		final boolean[] result = new boolean[1];
		PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
	
			@Override
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				try {
					result[0] = SpdxProjectFactory.getSpdxProject(projectName, monitor).isCreated();
				} catch (IOException e) {
					throw(new InvocationTargetException(e));
				} catch (InvalidSPDXAnalysisException e) {
					throw(new InvocationTargetException(e));
				} catch (SpdxProjectException e) {
					throw(new InvocationTargetException(e));
				}
			}			
		});
		return result[0];
	}

	/**
	 * Remove SPDX from the project
	 * @param projectName
	 */
	private void removeSpdx(Shell parent, final String projectName) {
		
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						SpdxProjectFactory.removeSpdxProject(projectName, monitor);
					} catch (IOException e) {
						throw(new InvocationTargetException(e));
					} catch (InvalidSPDXAnalysisException e) {
						throw(new InvocationTargetException(e));
					} catch (SpdxProjectException e) {
						throw(new InvocationTargetException(e));
					} catch (CoreException e) {
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
	}

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
