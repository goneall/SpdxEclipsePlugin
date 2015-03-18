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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
 * Property page for SPDX File
 * @author Gary O'Neall
 *
 */
public class SpdxFilePropertyPage extends PropertyPage {
	
	public static final String ID="org.spdx.spdxeclipse.properties.SpdxFilePropertyPage";
	
	private IFile file;
	SpdxProject spdxProject;
	
	private String originalComment;
	private String originalConcludedLicense;
	private String[] originalContributors;
	private String originalCopyright;
	private String originalNotice;
	private String originalProject;
	private String originalProjectUrl;
	
	private Text txtComment;
	private LicenseSelectionGroup groupConcludedLicense;
	private StringSelectionGroup groupContributors;
	private Text txtCopyright;
	private Text txtNotice;
	private Text txtProject;
	private Text txtProjectUrl;

	private IProject project;
	
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		this.file = (IFile)(this.getElement());
		this.project = this.file.getProject();
		try {
			if (SpdxProjectProperties.isSpdxInitialized(project)) {
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
		}
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
	



	private Control createInitializedComposite(Composite parent) throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		this.originalComment = SpdxFileProperties.getComment(file);
		this.originalConcludedLicense = SpdxFileProperties.getConcludedLicense(file);
		this.originalContributors = SpdxFileProperties.getContributors(file);
		this.originalCopyright = SpdxFileProperties.getCopyright(file);
		this.originalNotice = SpdxFileProperties.getNotice(file);
		this.originalProject = SpdxFileProperties.getProjectName(file);
		this.originalProjectUrl = SpdxFileProperties.getProjectUrl(file);
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						spdxProject = SpdxProjectFactory.getSpdxProject(project.getName(), monitor);
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
			spdxProject = SpdxProjectFactory.getSpdxProject(project.getName(), new NullProgressMonitor());
		} catch (InterruptedException e) {
			spdxProject = SpdxProjectFactory.getSpdxProject(project.getName(), new NullProgressMonitor());
		}
		this.groupConcludedLicense =  new LicenseSelectionGroup(composite, SWT.NONE, 
				"File Concluded License", 
				spdxProject, 
				this.originalConcludedLicense);
		
		this.groupContributors = new StringSelectionGroup(composite, SWT.NONE, "File Contributors",
				this.originalContributors, 2);
		
		Label lblFileCopyright = new Label(composite, SWT.NONE);
		lblFileCopyright.setText("File Copyright: ");
		String fileCopyrightTooltip = "Declared file copyright";
		lblFileCopyright.setToolTipText(fileCopyrightTooltip);
		this.txtCopyright = new Text(composite, SWT.BORDER);
		GridData copyrightLayout = new GridData(GridData.FILL_HORIZONTAL);
		copyrightLayout.grabExcessHorizontalSpace = true;
		this.txtCopyright.setLayoutData(copyrightLayout);
		this.txtCopyright.setText(this.originalCopyright);
		this.txtCopyright.setToolTipText(fileCopyrightTooltip);
		this.txtCopyright.addListener(SWT.Verify, new Listener() {

			@Override
			public void handleEvent(Event event) {
				event.doit = !txtCopyright.getText().trim().isEmpty();
			}
			
		});
		
		Label lblNotice = new Label(composite, SWT.NONE);
		lblNotice.setText("Notice: ");
		this.txtNotice = new Text(composite, SWT.BORDER);
		GridData noticeLayout = new GridData(GridData.FILL_HORIZONTAL);
		noticeLayout.grabExcessHorizontalSpace = true;
		this.txtNotice.setLayoutData(noticeLayout);
		this.txtNotice.setText(originalNotice);
		this.txtNotice.setToolTipText("SPDX notice for file");
		
		Label lblComment = new Label(composite, SWT.NONE);
		lblComment.setText("Comment: ");
		this.txtComment = new Text(composite, SWT.BORDER);
		GridData commentLayout = new GridData(GridData.FILL_HORIZONTAL);
		commentLayout.grabExcessHorizontalSpace = true;
		this.txtComment.setLayoutData(commentLayout);
		this.txtComment.setText(originalComment);
		this.txtComment.setToolTipText("SPDX Comments for file");
		
		Label lblProjectName = new Label(composite, SWT.NONE);
		lblProjectName.setText("Proejct Name: ");
		this.txtProject = new Text(composite, SWT.BORDER);
		GridData projectLayout = new GridData(GridData.FILL_HORIZONTAL);
		projectLayout.grabExcessHorizontalSpace = true;
		this.txtProject.setLayoutData(projectLayout);
		this.txtProject.setText(originalProject);
		this.txtProject.setToolTipText("SPDX ArtifactOf Project - Project name where this file originated");
		
		Label lblProjectUrl = new Label(composite, SWT.NONE);
		lblProjectUrl.setText("Proejct Home Page: ");
		this.txtProjectUrl = new Text(composite, SWT.BORDER);
		GridData projectUrlLayout = new GridData(GridData.FILL_HORIZONTAL);
		projectUrlLayout.grabExcessHorizontalSpace = true;
		this.txtProjectUrl.setLayoutData(projectUrlLayout);
		this.txtProjectUrl.setText(originalProjectUrl);
		this.txtProjectUrl.setToolTipText("SPDX ArtifactOf Project Home Page - Project home page where this file originated");

		return composite;
	}
	
	@Override
	public void performApply() {
		if (!this.originalCopyright.equals(this.txtCopyright.getText().trim())) {
			try {
				SpdxFileProperties.setCopyright(this.file, this.txtCopyright.getText().trim());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new file copyright: "+e.getCause().getMessage());
				return;
			}
		}
		
		if (!this.originalComment.equals(this.txtComment.getText().trim())) {
			try {
				SpdxFileProperties.setComment(this.file, this.txtComment.getText().trim());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new file comment: "+e.getCause().getMessage());
				return;
			}
		}

		if (!this.originalConcludedLicense.equals(this.groupConcludedLicense.getSelectedLicense())) {
			try {
				SpdxFileProperties.setConcludedLicense(this.file, this.groupConcludedLicense.getSelectedLicense());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new concluded license: "+e.getCause().getMessage());
				return;
			}
		}
		
		if (!SpdxProjectDefaultFilePropertyPage.arraysEquivelent(this.originalContributors, this.groupContributors.getList())) {
			try {
				SpdxFileProperties.setContributors(this.file, this.groupContributors.getList());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new contributors: "+e.getCause().getMessage());
				return;
			}
		}
		
		if (!this.originalNotice.equals(this.txtNotice.getText().trim())) {
			try {
				SpdxFileProperties.setComment(this.file, this.txtNotice.getText().trim());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new file notice: "+e.getCause().getMessage());
				return;
			}
		}
		
		if (!this.originalProject.equals(this.txtProject.getText().trim())) {
			try {
				SpdxFileProperties.setComment(this.file, this.txtProject.getText().trim());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new file project: "+e.getCause().getMessage());
				return;
			}
		}
		
		if (!this.originalProjectUrl.equals(this.txtProjectUrl.getText().trim())) {
			try {
				SpdxFileProperties.setComment(this.file, this.txtProjectUrl.getText().trim());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new file project home page: "+e.getCause().getMessage());
				return;
			}
		}
	}
	
	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}
	
	@Override
	public void performDefaults() {
		this.groupConcludedLicense.setLicense(this.originalConcludedLicense);
		this.groupContributors.setList(this.originalContributors);
		this.txtComment.setText(this.originalComment);
		this.txtCopyright.setText(this.originalCopyright);
		this.txtNotice.setText(this.originalNotice);
		this.txtProject.setText(this.originalProject);
		this.txtProjectUrl.setText(this.originalProjectUrl);
	}


}
