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

import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.dialogs.PropertyPage;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.ui.CommandAddSpdxToProject;
import org.spdx.spdxeclipse.ui.IncludedExcludedFilesComposite;

/**
 * Property page for SPDX Project Properties
 * @author Gary O'Neall
 *
 */
public class SpdxProjectPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	private String originalDocumentUrl;
	private String originalSpdxFileName;
	private String[] originalExcludedFilesPattern;
	private String[] origiginalIncludedDirectories;
	private Text txtSpdxFileName = null;
	private Text txtDocumentUrl = null;
	private IProject project = null;
	Pattern fileNameRegex = Pattern.compile("[_a-zA-Z0-9\\-\\.]+");
	IncludedExcludedFilesComposite ieComposite = null;

	public SpdxProjectPropertyPage() {
		
	}

	@Override
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {


		project = ((IResource)(this.getElement())).getProject();
		try {
			if (SpdxProjectProperties.isSpdxInitialized(project)) {
				return createInitializedComposite(parent, project);
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
		final Label lblNeedToCreate = new Label(composite, SWT.NONE);
		lblNeedToCreate.setText("SPDX has not be added to this project");
		lblNeedToCreate.setToolTipText("To enable SPDX, add SPDX to this project in the project menu\nor press the Add SPDX button");
		final Button btCreateSpdx = new Button(composite, SWT.PUSH);
		btCreateSpdx.setText("Add SPDX");
		btCreateSpdx.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				CommandAddSpdxToProject.createSpdxProject(getShell(), project.getName());
				btCreateSpdx.setText("SPDX Created");
				btCreateSpdx.setEnabled(false);
				lblNeedToCreate.setText("SPDX created - press OK to continue");
			}
			
		});
		return composite;
	}

	private Composite createInitializedComposite(Composite parent, IProject project) throws CoreException {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		this.originalDocumentUrl = SpdxProjectProperties.getSpdxDocUrl(project);
		this.originalSpdxFileName = SpdxProjectProperties.getSpdxFileName(project);
		this.originalExcludedFilesPattern = SpdxProjectProperties.getExcludedFilePatterns(project);
		this.origiginalIncludedDirectories = SpdxProjectProperties.getIncludedResourceDirectories(project);
		Label lblSpdxFileName = new Label(composite, SWT.NONE);
		lblSpdxFileName.setText("SPDX File Name: ");
		String fileNameToolTip = "Name of the SPDX file to store the SPDX date in the root of the project folder";
		lblSpdxFileName.setToolTipText(fileNameToolTip);
		txtSpdxFileName = new Text(composite, SWT.BORDER);
		GridData gdSpdxFileName = new GridData();
		gdSpdxFileName.horizontalAlignment = SWT.FILL;
		gdSpdxFileName.grabExcessHorizontalSpace = true;
		txtSpdxFileName.setLayoutData(gdSpdxFileName);
		txtSpdxFileName.setToolTipText(fileNameToolTip);
		txtSpdxFileName.setText(this.originalSpdxFileName);
		txtSpdxFileName.addListener(SWT.Verify, new Listener() {

			@Override
			public void handleEvent(Event event) {
				event.doit = fileNameRegex.matcher(txtSpdxFileName.getText()).matches();
			}
			
		});
		
		Label lblDocUrl = new Label(composite, SWT.None);
		lblDocUrl.setText("Document URL: ");
		this.txtDocumentUrl = new Text(composite, SWT.NONE | SWT.READ_ONLY);
		String docUrlToolTip = "SPDX document URL.  Note - this property can not be changed once the SPDX document is created";
		lblDocUrl.setToolTipText(docUrlToolTip);
		this.txtDocumentUrl.setToolTipText(docUrlToolTip);
		this.txtDocumentUrl.setText(this.originalDocumentUrl);
		ieComposite = new IncludedExcludedFilesComposite(composite, SWT.NONE, project, this.origiginalIncludedDirectories, this.originalExcludedFilesPattern);
		GridData gdIe = new GridData();
		gdIe.horizontalSpan = 2;
		gdIe.horizontalAlignment = SWT.FILL;
		gdIe.verticalAlignment = SWT.FILL;
		gdIe.grabExcessVerticalSpace = true;
		ieComposite.setLayoutData(gdIe);
		return composite;
	}

	@Override
	public void performApply() {
		final String newSpdxFileName = txtSpdxFileName.getText().trim();
		if (!this.originalSpdxFileName.equals(newSpdxFileName)) {
			try {
				SpdxProjectProperties.setSpdxFileName(project, newSpdxFileName);
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new SPDX file name: "+e.getCause().getMessage());
				return;
			}
		}
		if (!Arrays.equals(this.origiginalIncludedDirectories, this.ieComposite.getIncludedResourcePaths())) {
			try {
				SpdxProjectProperties.setIncludedResourceDirectories(project, this.ieComposite.getIncludedResourcePaths());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new SPDX included resource directories: "+e.getCause().getMessage());
				return;
			}
		}
		if (!Arrays.equals(this.originalExcludedFilesPattern, this.ieComposite.getExcludeFilePatterns())) {
			try {
				SpdxProjectProperties.setExcludedFilePatterns(project, this.ieComposite.getExcludeFilePatterns());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new SPDX excluded file patterns: "+e.getCause().getMessage());
				return;
			} catch (InvalidExcludedFilePattern e) {
				MessageDialog.openError(this.getShell(), "Error", e.getCause().getMessage());
				return;
			}
		}
	}
	
	@Override
	public boolean performOk() {
		final String newSpdxFileName = txtSpdxFileName.getText().trim();
		if (!this.originalSpdxFileName.equals(newSpdxFileName)) {
			try {
				SpdxProjectProperties.setSpdxFileName(project, newSpdxFileName);
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new SPDX file name: "+e.getCause().getMessage());
				return false;
			}
		}
		if (!Arrays.equals(this.origiginalIncludedDirectories, this.ieComposite.getIncludedResourcePaths())) {
			try {
				SpdxProjectProperties.setIncludedResourceDirectories(project, this.ieComposite.getIncludedResourcePaths());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new SPDX included resource directories: "+e.getCause().getMessage());
				return false;
			}
		}
		if (!Arrays.equals(this.originalExcludedFilesPattern, this.ieComposite.getExcludeFilePatterns())) {
			try {
				SpdxProjectProperties.setExcludedFilePatterns(project, this.ieComposite.getExcludeFilePatterns());
			} catch (CoreException e) {
				MessageDialog.openError(this.getShell(), "Error", "Error setting new SPDX excluded file patterns: "+e.getCause().getMessage());
				return false;
			} catch (InvalidExcludedFilePattern e) {
				MessageDialog.openError(this.getShell(), "Error", e.getCause().getMessage());
				return false;
			}
		}
		return super.performOk();
	}
	
	@Override
	public void performDefaults() {
		// put back to the original
		this.txtSpdxFileName.setText(this.originalSpdxFileName);
		this.ieComposite.setExcludedPatterns(this.originalExcludedFilesPattern);
		this.ieComposite.setIncludedDirectories(this.origiginalIncludedDirectories);
	}
}
