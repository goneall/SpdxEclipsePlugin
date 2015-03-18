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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.preferences.SpdxProjectPreferences;
import org.spdx.spdxeclipse.project.SpdxProject;
import org.spdx.spdxeclipse.properties.SpdxProjectProperties;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * Wizard page for package license information
 * @author Gary O'Neall
 *
 */
public class SpdxProjectLicenseWizardPage extends WizardPage implements
		IWizardPage, Listener {
	
	private Text tbPackageConcludedLicense;
	
	private Text tbFileNotice;

	private CCombo comboConcludedLicense;

	private Text tbPackageLicenseComment;
	
	private SpdxProject spdxProject;

	private Button btConcludedSameAsDeclared;

	private Button btDefaultSameAsDeclared;

	private CCombo comboDefaultLicense;

	private Text tbDefaultFileLicense;

	private IProject project;

	private LicenseSelectionGroup declaredLicenseGroup;

	public SpdxProjectLicenseWizardPage(SpdxProject spdxProject) {
		super("Project License Information");
		this.spdxProject = spdxProject;
		this.setDescription("Project license information for SPDX");
		this.setPageComplete(false);
		this.setTitle("SPDX Project License");
		this.project = spdxProject.getProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite licGroup = new Composite(parent, SWT.NONE);
		this.setControl(licGroup);
		licGroup.setLayout(new GridLayout(2, false));
		GridData gridDataGroup = new GridData();
		gridDataGroup.grabExcessHorizontalSpace = true;
		gridDataGroup.horizontalAlignment = SWT.FILL;
		gridDataGroup.grabExcessVerticalSpace = true;
		gridDataGroup.verticalAlignment = SWT.FILL;
		licGroup.setLayoutData(gridDataGroup);
		
		createDeclaredGroup(licGroup);
		createConcludedGroup(licGroup);
		createDefaultFileGroup(licGroup);
		
		Label lblLicenseComment = new Label(licGroup, SWT.None);
		lblLicenseComment.setText("License Comment: ");
		this.tbPackageLicenseComment = new Text(licGroup, SWT.BORDER);
		this.tbPackageLicenseComment.setToolTipText("Comment on the licenses (optional)");
		GridData gdPkgLicComment = new GridData();
		gdPkgLicComment.grabExcessHorizontalSpace = true;
		gdPkgLicComment.horizontalAlignment = SWT.FILL;
		gdPkgLicComment.horizontalSpan = 1;
		this.tbPackageLicenseComment.setLayoutData(gdPkgLicComment);
		
		Label lblFileNotice = new Label(licGroup, SWT.None);
		lblFileNotice.setText("File Notice Text: ");
		this.tbFileNotice = new Text(licGroup, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		this.tbFileNotice.setToolTipText("Default notice text for files (optional)");
		GridData gdFileNotice = new GridData();
		gdFileNotice.grabExcessHorizontalSpace = true;
		gdFileNotice.grabExcessVerticalSpace = true;
		gdFileNotice.horizontalAlignment = SWT.FILL;
		gdFileNotice.verticalAlignment = SWT.FILL;
		gdFileNotice.horizontalSpan = 1;
		this.tbFileNotice.setLayoutData(gdFileNotice);
		setPageComplete(verify());
	}
	
	private boolean verify() {
		if (this.declaredLicenseGroup.getSelectedLicense() == null) {
			return false;
		}
		if (this.declaredLicenseGroup.getSelectedLicense().isEmpty()) {
			return false;
		}
		if (!this.btConcludedSameAsDeclared.getSelection()) {
			if (this.tbPackageConcludedLicense == null) {
				return false;
			}
			if (this.tbPackageConcludedLicense.getText().isEmpty()) {
				return false;
			}
		}
		if (!this.btDefaultSameAsDeclared.getSelection()) {
			if (this.tbDefaultFileLicense == null) {
				return false;
			}
			if (this.tbDefaultFileLicense.getText().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private void createDefaultFileGroup(Composite parent) {
		Group defaultFileLicenseGroup = new Group(parent, SWT.NONE);
		defaultFileLicenseGroup.setLayout(new GridLayout(2, false));
		defaultFileLicenseGroup.setText("Default File License");
		defaultFileLicenseGroup.setToolTipText("Choose a license from the dropdown and use the AND, OR and X buttons to construct a license");
		GridData gdDefaultLicenseGroup = new GridData();
		gdDefaultLicenseGroup.horizontalAlignment = SWT.FILL;
		gdDefaultLicenseGroup.grabExcessHorizontalSpace = true;
		gdDefaultLicenseGroup.verticalAlignment = SWT.FILL;
		gdDefaultLicenseGroup.heightHint = 110;
		gdDefaultLicenseGroup.horizontalSpan = 2;
		defaultFileLicenseGroup.setLayoutData(gdDefaultLicenseGroup);
		
		btDefaultSameAsDeclared = new Button(defaultFileLicenseGroup, SWT.CHECK);
		btDefaultSameAsDeclared.setText("Same As Declared");
		GridData gdSameAs = new GridData();
		gdSameAs.horizontalAlignment = SWT.LEFT;
		gdSameAs.horizontalSpan = 2;
		btDefaultSameAsDeclared.setLayoutData(gdSameAs);

		comboDefaultLicense = LicenseSelectionGroup.createLicenseComposite(defaultFileLicenseGroup,
				this.spdxProject.getAvailableLicenseNames());	
		
		Composite licensePlusMinus = new Composite(defaultFileLicenseGroup, SWT.NONE);
		licensePlusMinus.setLayout(new GridLayout(3, true));
		GridData licPlusMin = new GridData();
		licPlusMin.horizontalAlignment = SWT.CENTER;
		licPlusMin.grabExcessHorizontalSpace = false;
		final Button btAnd = new Button(licensePlusMinus, SWT.PUSH | SWT.CENTER);
		btAnd.setText("AND");
		btAnd.setToolTipText("Add the license for concluded licenses");
		btAnd.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (comboDefaultLicense.getText() != null && !comboDefaultLicense.getText().isEmpty()) {
					String licString = tbDefaultFileLicense.getText();
					licString = LicenseSelectionGroup.appendLicenseString(licString, comboDefaultLicense.getText(), "AND");
					tbDefaultFileLicense.setText(licString);
				}
			}
			
		});
		final Button btOr = new Button(licensePlusMinus, SWT.PUSH | SWT.CENTER);
		btOr.setText("OR");
		btOr.setToolTipText("Add license as a choice of licenses");
		btOr.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (comboDefaultLicense.getText() != null && !comboDefaultLicense.getText().isEmpty()) {
					String licString = tbDefaultFileLicense.getText();
					licString = LicenseSelectionGroup.appendLicenseString(licString, comboDefaultLicense.getText(), "OR");
					tbDefaultFileLicense.setText(licString);
				}
			}		
		});
		
		final Button btClearLicense = new Button(licensePlusMinus, SWT.CENTER | SWT.PUSH);
		btClearLicense.setText("X");
		btClearLicense.setToolTipText("Clear Concluded license");
		btClearLicense.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				tbDefaultFileLicense.setText("");
			}
			
		});	
		this.tbDefaultFileLicense = new Text(defaultFileLicenseGroup, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		this.tbDefaultFileLicense.setToolTipText("Default license for files within this package");
		GridData gdDefaultFileLicense = new GridData();
		gdDefaultFileLicense.grabExcessHorizontalSpace = true;
		gdDefaultFileLicense.horizontalAlignment = SWT.FILL;
		gdDefaultFileLicense.grabExcessVerticalSpace = true;
		gdDefaultFileLicense.verticalAlignment = SWT.FILL;
		gdDefaultFileLicense.horizontalSpan = 2;
		this.tbDefaultFileLicense.setLayoutData(gdDefaultFileLicense);
		this.tbDefaultFileLicense.setText(SpdxProjectPreferences.getDefaultFileLicense(this.project));
		
		btDefaultSameAsDeclared.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (btDefaultSameAsDeclared.getSelection()) {
					comboDefaultLicense.setEnabled(false);
					btAnd.setEnabled(false);
					btOr.setEnabled(false);
					btClearLicense.setEnabled(false);
					tbDefaultFileLicense.setEnabled(false);
				} else {
					comboDefaultLicense.setEnabled(true);
					btAnd.setEnabled(true);
					btOr.setEnabled(true);
					btClearLicense.setEnabled(true);
					tbDefaultFileLicense.setEnabled(true);
				}
			}			
		});
		this.tbDefaultFileLicense.addListener(SWT.Modify, this);
		this.btDefaultSameAsDeclared.addListener(SWT.Selection, this);
	}

	private void createConcludedGroup(Composite parent) {
		Group concludedGroup = new Group(parent, SWT.NONE);
		concludedGroup.setLayout(new GridLayout(2, false));
		concludedGroup.setText("Concluded License");
		concludedGroup.setToolTipText("Choose a license from the dropdown and use the AND, OR and X buttons to construct a license");
		GridData gdConcludedGroup = new GridData();
		gdConcludedGroup.horizontalAlignment = SWT.FILL;
		gdConcludedGroup.grabExcessHorizontalSpace = true;
		gdConcludedGroup.verticalAlignment = SWT.FILL;
		gdConcludedGroup.heightHint = 110;
		gdConcludedGroup.horizontalSpan = 2;
		concludedGroup.setLayoutData(gdConcludedGroup);
		
		btConcludedSameAsDeclared = new Button(concludedGroup, SWT.CHECK);
		btConcludedSameAsDeclared.setText("Same As Declared");
		GridData gdSameAs = new GridData();
		gdSameAs.horizontalAlignment = SWT.LEFT;
		gdSameAs.horizontalSpan = 2;
		btConcludedSameAsDeclared.setLayoutData(gdSameAs);

		comboConcludedLicense = LicenseSelectionGroup.createLicenseComposite(concludedGroup,
				this.spdxProject.getAvailableLicenseNames());
		
		Composite licensePlusMinus = new Composite(concludedGroup, SWT.NONE);
		licensePlusMinus.setLayout(new GridLayout(3, true));
		GridData licPlusMin = new GridData();
		licPlusMin.horizontalAlignment = SWT.CENTER;
		licPlusMin.grabExcessHorizontalSpace = false;
		final Button btAnd = new Button(licensePlusMinus, SWT.PUSH | SWT.CENTER);
		btAnd.setText("AND");
		btAnd.setToolTipText("Add the license for concluded licenses");
		btAnd.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (comboConcludedLicense.getText() != null && !comboConcludedLicense.getText().isEmpty()) {
					
					String licString = tbPackageConcludedLicense.getText();
					licString = LicenseSelectionGroup.appendLicenseString(licString, comboConcludedLicense.getText(), "AND");
					tbPackageConcludedLicense.setText(licString);
				}
			}
			
		});
		final Button btOr = new Button(licensePlusMinus, SWT.PUSH | SWT.CENTER);
		btOr.setText("OR");
		btOr.setToolTipText("Add license as a choice of licenses");
		btOr.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (comboConcludedLicense.getText() != null && !comboConcludedLicense.getText().isEmpty()) {
					String licString = tbPackageConcludedLicense.getText();
					licString = LicenseSelectionGroup.appendLicenseString(licString, comboConcludedLicense.getText(), "OR");
					tbPackageConcludedLicense.setText(licString);
				}
			}		
		});
		
		final Button btClearLicense = new Button(licensePlusMinus, SWT.CENTER | SWT.PUSH);
		btClearLicense.setText("X");
		btClearLicense.setToolTipText("Clear Concluded license");
		btClearLicense.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				tbPackageConcludedLicense.setText("");
			}
			
		});	
		this.tbPackageConcludedLicense = new Text(concludedGroup, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		this.tbPackageConcludedLicense.setToolTipText("License as concluded by the creator of the SPDX document");
		GridData gdPkgConcludedLicense = new GridData();
		gdPkgConcludedLicense.grabExcessHorizontalSpace = true;
		gdPkgConcludedLicense.horizontalAlignment = SWT.FILL;
		gdPkgConcludedLicense.grabExcessVerticalSpace = true;
		gdPkgConcludedLicense.verticalAlignment = SWT.FILL;
		gdPkgConcludedLicense.horizontalSpan = 2;
		this.tbPackageConcludedLicense.setLayoutData(gdPkgConcludedLicense);
		this.tbPackageConcludedLicense.setText(SpdxProjectPreferences.getDefaultConcludedLicense(project));

		btConcludedSameAsDeclared.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (btConcludedSameAsDeclared.getSelection()) {
					comboConcludedLicense.setEnabled(false);
					btAnd.setEnabled(false);
					btOr.setEnabled(false);
					btClearLicense.setEnabled(false);
					tbPackageConcludedLicense.setEnabled(false);
				} else {
					comboConcludedLicense.setEnabled(true);
					btAnd.setEnabled(true);
					btOr.setEnabled(true);
					btClearLicense.setEnabled(true);
					tbPackageConcludedLicense.setEnabled(true);
				}
			}			
		});
		this.btConcludedSameAsDeclared.addListener(SWT.Selection, this);
		this.tbPackageConcludedLicense.addListener(SWT.Modify, this);
	}

	private void createDeclaredGroup(Composite parent) {
		this.declaredLicenseGroup = new LicenseSelectionGroup(parent, SWT.NONE, 
				"Declared License", this.spdxProject, SpdxProjectPreferences.getDefaultDeclaredLicense(project));	
		this.declaredLicenseGroup.addListener(this);
	}



	public boolean updateSpdx(SpdxProject spdxProjectToUpdate) {
		SPDXLicenseInfo declaredLicense = null;
		if (this.declaredLicenseGroup.getSelectedLicense() != null && !this.declaredLicenseGroup.getSelectedLicense().isEmpty()) {
			try {
				declaredLicense = SPDXLicenseInfoFactory.parseSPDXLicenseString(this.declaredLicenseGroup.getSelectedLicense());
			} catch (InvalidLicenseStringException e) {
				Activator.getDefault().logError("Error parsing the declared license", e);
				MessageDialog.openError(getShell(), "Error", "Error parsing the declared license: "+e.getMessage());
				return false;
			}
			try {
				spdxProjectToUpdate.getSpdxDoc().getSpdxPackage().setDeclaredLicense(declaredLicense);
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error updating the SPDX document for the declared license", e);
				MessageDialog.openError(getShell(), "Error", "Error updating the SPDX document for the declared license: "+e.getMessage());
				return false;
			}
		}
		if (this.btConcludedSameAsDeclared.getSelection()) {
			if (declaredLicense != null) {
				try {
					spdxProjectToUpdate.getSpdxDoc().getSpdxPackage().setConcludedLicenses(declaredLicense);
				} catch (InvalidSPDXAnalysisException e) {
					Activator.getDefault().logError("Error updating the SPDX document for the concluded license",e);
					MessageDialog.openError(getShell(), "Error", "Error updating the SPDX document for the concluded license: "+e.getMessage());
					return false;
				}
			}
		} else if (this.tbPackageConcludedLicense != null && !this.tbPackageConcludedLicense.getText().isEmpty()) {
			SPDXLicenseInfo concludedLicense;
			try {
				concludedLicense = SPDXLicenseInfoFactory.parseSPDXLicenseString(this.tbPackageConcludedLicense.getText());
			} catch (InvalidLicenseStringException e) {
				Activator.getDefault().logError("Error parsing the concluded license", e);
				MessageDialog.openError(getShell(), "Error", "Error parsing the concluded license: "+e.getMessage());
				return false;
			}
			try {
				spdxProjectToUpdate.getSpdxDoc().getSpdxPackage().setConcludedLicenses(concludedLicense);
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error updating the SPDX document for the concluded license", e);
				MessageDialog.openError(getShell(), "Error", "Error updating the SPDX document for the concluded license: "+e.getMessage());
				return false;
			}
		}
		if (this.btDefaultSameAsDeclared.getSelection()) {
			if (declaredLicense != null) {
				try {
					SpdxProjectProperties.setDefaultFileLicense(project, this.declaredLicenseGroup.getSelectedLicense());
				} catch (CoreException e) {
					Activator.getDefault().logError("Error updating the project properites for the default file license", e);
					MessageDialog.openError(getShell(), "Error", "Error updating the project properites for the default file license: "+e.getMessage());
					return false;
				}
			}
		} else if (this.tbDefaultFileLicense != null && !this.tbDefaultFileLicense.getText().isEmpty()) {
			try {
				@SuppressWarnings("unused")
				SPDXLicenseInfo fileLicense = SPDXLicenseInfoFactory.parseSPDXLicenseString(this.tbDefaultFileLicense.getText());
			} catch (InvalidLicenseStringException e) {
				Activator.getDefault().logError("Error parsing the file license", e);
				MessageDialog.openError(getShell(), "Error", "Error parsing the file license: "+e.getMessage());
				return false;
			}
			try {
				SpdxProjectProperties.setDefaultFileLicense(project, this.tbDefaultFileLicense.getText());
			} catch (CoreException e) {
				Activator.getDefault().logError("Error updating the project properites for the default file license", e);
				MessageDialog.openError(getShell(), "Error", "Error updating the project properites for the default file license: "+e.getMessage());
				return false;
			}
		}
		
		if (this.tbFileNotice != null && !this.tbFileNotice.getText().trim().isEmpty()) {
			try {
				SpdxProjectProperties.setDefaultFileNotice(project, this.tbFileNotice.getText().trim());
			} catch (CoreException e) {
				Activator.getDefault().logError("Error updating the project properites for the default file notice", e);
				MessageDialog.openError(getShell(), "Error", "Error updating the project properites for the default file notice: "+e.getMessage());
				return false;
			}
		}
		return true;
	}

	@Override
	public void handleEvent(Event event) {
		setPageComplete(verify());
	}

}
