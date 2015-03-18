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
package org.spdx.spdxeclipse.properties;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.project.SpdxProject;
import org.spdx.spdxeclipse.project.SpdxProjectException;
import org.spdx.spdxeclipse.project.SpdxProjectFactory;
import org.spdx.spdxeclipse.ui.CommandAddSpdxToProject;
import org.spdx.spdxeclipse.ui.LicenseSelectionGroup;
import org.spdx.spdxeclipse.ui.StringSelectionGroup;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxProjectDefaultFilePropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	IProject project = null;
	SpdxProject spdxProject = null;
	private String originalDefaultFileCopyright;
	private String originalDefaultFileLicense;
	private String[] originalDefaultContributors;
	private String originalDefaultFileNotice;
	
	private Text txtDefaultFileCopyright;
	private LicenseSelectionGroup defaultFileLicenseGroup;
	private StringSelectionGroup defaultFileContributorsGroup;
	private Text txtDefaultFileNotice;
	/**
	 * 
	 */
	public SpdxProjectDefaultFilePropertyPage() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		project = ((IResource)(this.getElement())).getProject();
		try {
			if (SpdxProjectProperties.isSpdxInitialized(project)) {
				initializeSpdxProject(project.getName());
				return createInitializedComposite(parent);
			} else {
				// create a composite with an enable SPDX button
				return createUninitializedSpdxComposite(parent, project);
			}


		} catch (CoreException ex) {
			Composite composite = new Composite(parent, SWT.None);
			GridLayout layout = new GridLayout();
			composite.setLayout(layout);
			GridData data = new GridData(GridData.FILL);
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			composite.setLayoutData(data);
			Label lblError = new Label(composite, SWT.BORDER);
			Activator.getDefault().logError("Error occurred fetching property values.  See log for details", ex);
			lblError.setText("Error occurred fetching property values.  See log for details");
			return composite;
		} catch (IOException e) {
			Composite composite = new Composite(parent, SWT.None);
			GridLayout layout = new GridLayout();
			composite.setLayout(layout);
			GridData data = new GridData(GridData.FILL);
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			composite.setLayoutData(data);
			Label lblError = new Label(composite, SWT.BORDER);
			Activator.getDefault().logError("I/O Error occurred fetching SPDX project", e);
			lblError.setText("I/O Error occurred fetching SPDX project.  See log for details");
			return composite;
		} catch (InvalidSPDXAnalysisException e) {
			Composite composite = new Composite(parent, SWT.None);
			GridLayout layout = new GridLayout();
			composite.setLayout(layout);
			GridData data = new GridData(GridData.FILL);
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			composite.setLayoutData(data);
			Label lblError = new Label(composite, SWT.BORDER);
			Activator.getDefault().logError("Invalid SPDX analysis error occurred fetching SPDX project", e);
			lblError.setText("Invalid SPDX analysis error fetching SPDX project.  See log for details");
			return composite;
		} catch (SpdxProjectException e) {
			Composite composite = new Composite(parent, SWT.None);
			GridLayout layout = new GridLayout();
			composite.setLayout(layout);
			GridData data = new GridData(GridData.FILL);
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			composite.setLayoutData(data);
			Label lblError = new Label(composite, SWT.BORDER);
			Activator.getDefault().logError("Error occurred fetching SPDX project", e);
			lblError.setText("Error occurred fetching SPDX project.  See log for details");
			return composite;
		} catch (InterruptedException e) {
			Composite composite = new Composite(parent, SWT.None);
			GridLayout layout = new GridLayout();
			composite.setLayout(layout);
			GridData data = new GridData(GridData.FILL);
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			composite.setLayoutData(data);
			Label lblError = new Label(composite, SWT.BORDER);
			lblError.setText("Opening SPDX project cancelled by user.  Re-open properties page.");
			return composite;
		}
	}

	private void initializeSpdxProject(final String projectName) throws SpdxProjectException, InterruptedException {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						spdxProject = SpdxProjectFactory.getSpdxProject(projectName, monitor);
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
			throw(new SpdxProjectException("Unable to open SPDX project",e.getCause()));
		} 
	}

	private Control createInitializedComposite(Composite parent) throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		this.originalDefaultFileCopyright = SpdxProjectProperties.getDefaultFileCopyright(project);
		this.originalDefaultFileLicense = SpdxProjectProperties.getDefaultFileLicense(project);
		this.originalDefaultContributors = SpdxProjectProperties.getDefaultFileContributors(project);
		this.originalDefaultFileNotice = SpdxProjectProperties.getDefaultFileNotice(project);
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		Label lblDefaultFileCopyright = new Label(composite, SWT.NONE);
		lblDefaultFileCopyright.setText("Copyright: ");
		String fileCopyrightTooltip = "Declared copyright for any new file.  The copyright can be \n"+
		"updated in the SPDX file properties.";
		lblDefaultFileCopyright.setToolTipText(fileCopyrightTooltip);
		txtDefaultFileCopyright = new Text(composite, SWT.BORDER);
		GridData defaultCopyrightLayout = new GridData(GridData.FILL_HORIZONTAL);
		defaultCopyrightLayout.grabExcessHorizontalSpace = true;
		txtDefaultFileCopyright.setLayoutData(defaultCopyrightLayout);
		txtDefaultFileCopyright.setText(SpdxProjectProperties.getDefaultFileCopyright(project));
		txtDefaultFileCopyright.setToolTipText(fileCopyrightTooltip);
		txtDefaultFileCopyright.addListener(SWT.Verify, new Listener() {

			@Override
			public void handleEvent(Event event) {
				event.doit = !txtDefaultFileCopyright.getText().trim().isEmpty();
			}
			
		});
		

		this.defaultFileContributorsGroup = new StringSelectionGroup(composite, SWT.NONE, "Default File Contributors",
				SpdxProjectProperties.getDefaultFileContributors(project), 2);
		
		this.defaultFileLicenseGroup = new LicenseSelectionGroup(composite, SWT.NONE, 
				"Default File License", 
				spdxProject, this.originalDefaultFileLicense);
		
		Label lblFileName = new Label(composite, SWT.NONE);
		lblFileName.setText("Notice: ");
		this.txtDefaultFileNotice = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gdDefaultFileNotice = new GridData(GridData.FILL_BOTH);
		gdDefaultFileNotice.grabExcessHorizontalSpace = true;
		gdDefaultFileNotice.grabExcessVerticalSpace = true;
		this.txtDefaultFileNotice.setLayoutData(gdDefaultFileNotice);
		this.txtDefaultFileNotice.setText(originalDefaultFileNotice);
		this.txtDefaultFileNotice.setToolTipText("Default file notice");
		return composite;
	}
	
	private Control createUninitializedSpdxComposite(Composite parent,
			final IProject project) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		Label lblNeedToCreate = new Label(composite, SWT.NONE);
		lblNeedToCreate.setText("SPDX has not be added to this project");
		lblNeedToCreate.setToolTipText("To enable SPDX, add SPDX to this project in the project menu\nor press the Add SPDX button");
		Button btCreateSpdx = new Button(composite, SWT.PUSH);
		btCreateSpdx.setText("Add SPDX");
		btCreateSpdx.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				CommandAddSpdxToProject.createSpdxProject(getShell(), project.getName());
				performOk();	// close the dialog
			}
			
		});
		return composite;
	}
	
	@Override
	public void performApply() {
		if (!this.originalDefaultFileCopyright.equals(this.txtDefaultFileCopyright.getText())) {
			try {
				SpdxProjectProperties.setDefaultFileCopyright(this.project, this.txtDefaultFileCopyright.getText().trim());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new default file copyright: "+e.getCause().getMessage());
				return;
			}
		}
		if (!this.originalDefaultFileLicense.equals(this.defaultFileLicenseGroup.getSelectedLicense())) {
			try {
				SpdxProjectProperties.setDefaultFileLicense(project, this.defaultFileLicenseGroup.getSelectedLicense());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new default file license: "+e.getCause().getMessage());
				return;
			}
		}
		if (!arraysEquivelent(this.originalDefaultContributors, this.defaultFileContributorsGroup.getList())) {
			try {
				SpdxProjectProperties.setDefaulFileContributors(project, this.defaultFileContributorsGroup.getList());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new default file contributors: "+e.getCause().getMessage());
				return;
			}
		}
		if (!this.originalDefaultFileNotice.equals(this.txtDefaultFileNotice.getText())) {
			try {
				SpdxProjectProperties.setDefaultFileNotice(project, this.txtDefaultFileNotice.getText());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new default file notice: "+e.getCause().getMessage());
				return;
			}
		}
	}
	
	static boolean arraysEquivelent(String[] s1,
			String[] s2) {
		if (s1.length != s2.length) {
			return false;
		}
		for (int i = 0; i < s1.length; i++) {
			boolean found = false;
			for (int j = 0; j < s2.length; j++) {
				if (s1[i].equals(s2[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}
	
	@Override
	public void performDefaults() {
		// put back to the original
		this.txtDefaultFileCopyright.setText(originalDefaultFileCopyright);
		this.defaultFileLicenseGroup.setLicense(this.originalDefaultFileLicense);
		this.defaultFileContributorsGroup.setList(this.originalDefaultContributors);
		this.txtDefaultFileNotice.setText(this.originalDefaultFileNotice);
	}
}
