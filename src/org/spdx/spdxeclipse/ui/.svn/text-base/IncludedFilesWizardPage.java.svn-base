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




import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.project.SpdxProject;
import org.spdx.spdxeclipse.properties.InvalidExcludedFilePattern;
import org.spdx.spdxeclipse.properties.SpdxProjectProperties;

/**
 * @author Source Auditor
 *
 */
public class IncludedFilesWizardPage extends WizardPage implements IWizardPage, IResourceTreeSelectionChangeListener {
	
	static final String FAKE_SHA1 = "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final String[] DEFAULED_EXCLUDED_FILES = new String[] {
		"^\\..*"
	};
	
	private IProject project;
	private IncludedExcludedFilesComposite ieComposite = null;
	
	public IncludedFilesWizardPage(SpdxProject spdxProject) {
		super("Included Files");
		this.setDescription("Files to be included in the SPDX project");
		this.setPageComplete(false);
		this.setTitle("Included Files");
		this.project = spdxProject.getProject();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		ieComposite = new IncludedExcludedFilesComposite(parent, SWT.NONE, project, new String[] {}, DEFAULED_EXCLUDED_FILES);
		this.setControl(ieComposite);
		ieComposite.addIncludeFileTreeListener(this);
	}

	public boolean updateSpdx(SpdxProject spdxProject)  {
		try {
			SpdxProjectProperties.setIncludedResourceDirectories(spdxProject.getProject(), this.ieComposite.getIncludedResourcePaths());
		} catch (CoreException e) {
			Activator.getDefault().logError("Error setting SPDX property included resource directories", e);
			MessageDialog.openError(getShell(), "Error", "Error setting the included resource directories property for the project");
			return false;
		}
		try {
			SpdxProjectProperties.setExcludedFilePatterns(project, this.ieComposite.getExcludeFilePatterns());
		} catch (CoreException e) {
			Activator.getDefault().logError("Error setting SPDX property excluded file patterns", e);
			MessageDialog.openError(getShell(), "Error", "Error setting the excluded file pattern property for the project");
			return false;
		} catch (InvalidExcludedFilePattern e) {
			Activator.getDefault().logError("Excluded file pattern error", e);
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void checkStateChanged() {
		if (this.ieComposite.hasSelectedResourcePaths()) {
			this.setPageComplete(true);
		} else {
			this.setPageComplete(false);
		}
	}
}
