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

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.preferences.SpdxProjectPreferences;
import org.spdx.spdxeclipse.project.SpdxProject;
import org.spdx.spdxeclipse.properties.SpdxProjectProperties;

/**
 * Wizard page for project level information
 * @author Gary O'Neall
 *
 */
public class SpdxProjectWizardPage extends WizardPage implements IWizardPage, Listener {

	static final String[] PREFIXES = new String[] {SpdxRdfConstants.CREATOR_PREFIX_PERSON, 
		SpdxRdfConstants.CREATOR_PREFIX_ORGANIZATION, SpdxRdfConstants.CREATOR_PREFIX_TOOL};

	private Text tbPackageName;
	private Text tbPackageVersion;
	private Text tbPackageCopyright;
	private Combo comboOriginatorPrefixes;

	private Text tbPackageOriginator;

	private Combo comboSupplierPrefixes;

	private Text tbPackageSupplier;

	private Text tbSourceInfo;

	private Text tbArchiveFileName;

	private Text tbArchiveSha1;

	private Text tbPackageDownloadUrl;

	private Text tbPackageShortDescription;

	private Text tbPackageDescription;

	private IProject project;

	public SpdxProjectWizardPage(SpdxProject spdxProject) {
		super("Project Information");
		this.setDescription("Project level information for SPDX");
		this.setPageComplete(false);
		this.setTitle("SPDX Project");
		this.project = spdxProject.getProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(final Composite parent) {
		Composite pkgGroup = new Composite(parent, SWT.None);
		this.setControl(pkgGroup);
		pkgGroup.setLayout(new GridLayout(4, false));
		GridData gridDataGroup = new GridData();
		gridDataGroup.grabExcessHorizontalSpace = true;
		gridDataGroup.horizontalAlignment = GridData.FILL;
		gridDataGroup.grabExcessVerticalSpace = true;
		gridDataGroup.verticalAlignment = GridData.FILL;
		pkgGroup.setLayoutData(gridDataGroup);
		
		Label lblPkgName = new Label(pkgGroup, SWT.None);
		lblPkgName.setText("Name*: ");
		setBold(lblPkgName);
		this.tbPackageName = new Text(pkgGroup, SWT.BORDER);
		this.tbPackageName.setToolTipText("Name of the package (required)");
		GridData gdPackageName = new GridData(GridData.FILL, GridData.CENTER, true, false);
		this.tbPackageName.setLayoutData(gdPackageName);
		this.tbPackageName.setText(SpdxProjectPreferences.getDefaultSpdxProjectName(project));
		this.tbPackageName.addListener(SWT.FocusOut, this);
		
		Label lblPkgVersion = new Label(pkgGroup, SWT.None);
		lblPkgVersion.setText("Version: ");
		this.tbPackageVersion = new Text(pkgGroup, SWT.BORDER);
		this.tbPackageVersion.setToolTipText("Package version (optional)");
		GridData gdPackageVersion = new GridData();
		gdPackageVersion.grabExcessHorizontalSpace = false;
		gdPackageVersion.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		gdPackageVersion.widthHint = 150;
		this.tbPackageVersion.setLayoutData(gdPackageVersion);

		this.tbPackageVersion.setText(SpdxProjectPreferences.getDefaultSpdxProjectVersion(project));
		this.tbPackageVersion.addListener(SWT.FocusOut, this);
		
		Label lblCopyright = new Label(pkgGroup, SWT.None);
		lblCopyright.setText("Copyright*: ");
		this.tbPackageCopyright = new Text(pkgGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tbPackageCopyright.setToolTipText("Copyright text for package (required)");
		setBold(lblCopyright);
		GridData gdPackageCopyright = new GridData();
		gdPackageCopyright.grabExcessHorizontalSpace = true;
		gdPackageCopyright.horizontalAlignment = SWT.FILL;
		gdPackageCopyright.horizontalSpan = 3;
		gdPackageCopyright.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		gdPackageCopyright.heightHint = 50;		
		tbPackageCopyright.setLayoutData(gdPackageCopyright);
		this.tbPackageCopyright.setText(SpdxProjectPreferences.getDefaultSpdxPackageCopyright(project));

		this.tbPackageCopyright.addListener(SWT.FocusOut, this);
		
		Label lblOriginator = new Label(pkgGroup, SWT.None);
		lblOriginator.setText("Originator: ");
		createOriginatorComposite(pkgGroup);
		
		Label lblSupplier = new Label(pkgGroup, SWT.None);
		lblSupplier.setText("Supplier: ");
		createSupplierComposite(pkgGroup);

		Label lblSourceInfo = new Label(pkgGroup, SWT.None);
		lblSourceInfo.setText("Source: ");
		this.tbSourceInfo = new Text(pkgGroup, SWT.BORDER);
		this.tbSourceInfo.setToolTipText("Optional information on the source of the package");
		GridData gdSourceInfo = new GridData();
		gdSourceInfo.grabExcessHorizontalSpace = true;
		gdSourceInfo.horizontalAlignment = GridData.FILL;
		gdSourceInfo.horizontalSpan = 3;
		this.tbSourceInfo.setLayoutData(gdSourceInfo);
		this.tbSourceInfo.addListener(SWT.FocusOut, this);		

		Label lblArchiveFileName = new Label(pkgGroup, SWT.None);
		lblArchiveFileName.setText("Archive File: ");
		this.tbArchiveFileName = new Text(pkgGroup, SWT.BORDER);
		this.tbArchiveFileName.setToolTipText("Archive file name for the package as it is distributed");
		GridData gdArchiveFileName = new GridData();
		gdArchiveFileName.grabExcessHorizontalSpace = true;
		gdArchiveFileName.horizontalAlignment = GridData.FILL;
		gdArchiveFileName.horizontalSpan = 3;
		this.tbArchiveFileName.setLayoutData(gdArchiveFileName);
		this.tbArchiveFileName.addListener(SWT.FocusOut, this);
		
		Label lblArchiveSha1 = new Label(pkgGroup, SWT.None);	
		lblArchiveSha1.setText("Archive SHA1: ");
		this.tbArchiveSha1 = new Text(pkgGroup, SWT.BORDER);
		this.tbArchiveSha1.setToolTipText("SHA1 checksum for the archive file (optional)");
		GridData gdArchiveSha1 = new GridData();
		gdArchiveSha1.grabExcessHorizontalSpace =  true;
		gdArchiveSha1.horizontalSpan = 3;
		gdArchiveSha1.horizontalAlignment = GridData.FILL;
		this.tbArchiveSha1.setLayoutData(gdArchiveSha1);
		this.tbArchiveSha1.addListener(SWT.FocusOut, new Listener() {

			@Override
			public void handleEvent(Event event) {
				String validationMessage = validateSha1(tbArchiveSha1.getText());
				if (validationMessage != null) {
					MessageDialog.openError(parent.getShell(), "Invalid SHA1", validationMessage);
				}
			}
			
		});
		this.tbArchiveSha1.addListener(SWT.FocusOut, this);
		
		Label lblDownloadUrl = new Label(pkgGroup, SWT.None);
		lblDownloadUrl.setText("Download URL*: ");
		setBold(lblDownloadUrl);
		this.tbPackageDownloadUrl = new Text(pkgGroup, SWT.BORDER);
		this.tbPackageDownloadUrl.setToolTipText("Download URL for the package");
		GridData gdPkgDownload = new GridData();
		gdPkgDownload.grabExcessHorizontalSpace = true;
		gdPkgDownload.horizontalAlignment = GridData.FILL;
		gdPkgDownload.horizontalSpan = 3;
		this.tbPackageDownloadUrl.setLayoutData(gdPkgDownload);
		this.tbPackageDownloadUrl.setText(SpdxProjectPreferences.getDefaultSpdxPackageDownloadUrl(project));
		this.tbPackageDownloadUrl.addListener(SWT.FocusOut, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (tbPackageDownloadUrl.getText().trim().isEmpty()) {
					return;		// ignore if blank
				}
				String validationMessage = validateUrl(tbPackageDownloadUrl.getText());
				if (validationMessage != null) {
					MessageDialog.openError(parent.getShell(), "Invalid Download URL", validationMessage);
				}
			}
			
		});
		this.tbPackageDownloadUrl.addListener(SWT.FocusOut, this);
		
		Label lblShortDesc = new Label(pkgGroup, SWT.None);
		lblShortDesc.setText("Short Description: ");
		this.tbPackageShortDescription = new Text(pkgGroup, SWT.BORDER);
		this.tbPackageShortDescription.setToolTipText("Short description");
		GridData gdPkgShortDesc = new GridData();
		gdPkgShortDesc.grabExcessHorizontalSpace = true;
		gdPkgShortDesc.horizontalAlignment = GridData.FILL;
		gdPkgShortDesc.horizontalSpan = 3;
		this.tbPackageShortDescription.setLayoutData(gdPkgShortDesc);
		this.tbPackageShortDescription.setText("");

		this.tbPackageShortDescription.addListener(SWT.FocusOut, this);
		
		Label lblDescription = new Label(pkgGroup, SWT.None);
		lblDescription.setText("Descrption: ");
		this.tbPackageDescription = new Text(pkgGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		this.tbPackageDescription.setToolTipText("Full description of package");
		GridData gdPkgDescription = new GridData();
		gdPkgDescription.grabExcessHorizontalSpace = true;
		gdPkgDescription.grabExcessVerticalSpace = true;
		gdPkgDescription.verticalAlignment = GridData.FILL;
		gdPkgDescription.horizontalAlignment = GridData.FILL;
		gdPkgDescription.horizontalSpan = 3;
		gdPkgDescription.heightHint = 90;
		this.tbPackageDescription.setLayoutData(gdPkgDescription);	
		this.tbPackageDescription.addListener(SWT.FocusOut, this);	
		
		Label lblFooter = new Label(pkgGroup, SWT.None);
		lblFooter.setText("* Required Fields");
		setBold(lblFooter);
		setPageComplete(validatePage());
	}
	
	/**
	 * Sets the font to bold for an existing lable
	 * @param lblPkgName
	 */
	private void setBold(Label lbl) {
		FontData[] fd = lbl.getFont().getFontData();
		for (int i = 0; i < fd.length; i++) {
			fd[i].setStyle(SWT.BOLD);
		}
		final Font newFont = new Font(lbl.getFont().getDevice(), fd);
		lbl.setFont(newFont);
		lbl.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				newFont.dispose();
			}
			
		});
	}

	private void createSupplierComposite(Composite parent) {
		Composite supplierGroup = new Composite(parent, SWT.None);
		GridLayout supplierLayout = new GridLayout();
		supplierLayout.numColumns = 2;
		supplierGroup.setLayout(supplierLayout);
		GridData supplierGroupGd = new GridData();
		supplierGroupGd.horizontalAlignment = GridData.FILL;
		supplierGroupGd.grabExcessHorizontalSpace = true;
		supplierGroupGd.horizontalSpan = 3;
		supplierGroupGd.verticalAlignment = GridData.CENTER;
		supplierGroup.setLayoutData(supplierGroupGd);
		this.comboSupplierPrefixes = new Combo(supplierGroup, SWT.BORDER | SWT.READ_ONLY);
		this.comboSupplierPrefixes.setItems(PREFIXES);
		this.comboSupplierPrefixes.setText(PREFIXES[0]);
		this.tbPackageSupplier = new Text(supplierGroup, SWT.BORDER);
		this.tbPackageSupplier.setToolTipText("Person or organzation supplying this package");
		GridData gdPkgSupplier = new GridData();
		gdPkgSupplier.horizontalAlignment = GridData.FILL;
		gdPkgSupplier.grabExcessHorizontalSpace = true;
		gdPkgSupplier.minimumWidth = 190;
		this.tbPackageSupplier.setLayoutData(gdPkgSupplier);
		this.tbPackageSupplier.addListener(SWT.FocusOut, this);
	}
	
	private void createOriginatorComposite(Composite parent) {
		Composite originatorGroup = new Composite(parent, SWT.None);
		GridLayout originatorLayout = new GridLayout();
		originatorLayout.numColumns = 2;
		originatorGroup.setLayout(originatorLayout);
		GridData originatorGroupGd = new GridData();
		originatorGroupGd.horizontalAlignment = GridData.FILL;
		originatorGroupGd.grabExcessHorizontalSpace = true;
		originatorGroupGd.horizontalSpan = 3;
		originatorGroupGd.verticalAlignment = GridData.CENTER;
		originatorGroup.setLayoutData(originatorGroupGd);
		this.comboOriginatorPrefixes = new Combo(originatorGroup, SWT.BORDER | SWT.READ_ONLY);
		this.comboOriginatorPrefixes.setItems(PREFIXES);
		this.comboOriginatorPrefixes.setText(PREFIXES[0]);
		this.tbPackageOriginator = new Text(originatorGroup, SWT.BORDER);
		this.tbPackageOriginator.setToolTipText("Person or organzation originating this package");
		GridData gdPkgOriginator = new GridData();
		gdPkgOriginator.horizontalAlignment = GridData.FILL;
		gdPkgOriginator.grabExcessHorizontalSpace = true;
		gdPkgOriginator.minimumWidth = 190;
		this.tbPackageOriginator.setLayoutData(gdPkgOriginator);
		this.tbPackageOriginator.addListener(SWT.FocusOut, this);
	}
	
	private boolean validatePage() {
		// name - required
		if (this.tbPackageName.getText().trim().isEmpty()) {
			return false;
		}
		// version - currently, any string is valid.  No validation required
		// copyright - required
		if (this.tbPackageCopyright.getText().trim().isEmpty()) {
			return false;
		}
		// originator - currently, any string is valid.  No validation required
		// supplier - currently, any string is valid.  No validation required
		// source - currently, any string is valid.  No validation required
		// archive - currently, any string is valid.  No validation required
		// sha1 - required, 40 characters
		if (validateSha1(this.tbArchiveSha1.getText())!= null) {
			return false;
		}		
		// download URL
		if (validateUrl(this.tbPackageDownloadUrl.getText()) != null) {
			return false;
		}
		// short desc - currently, any string is valid.  No validation required
		// description - currently, any string is valid.  No validation required
		return true;
	}
	
	/**
	 * Validates a URL string returning a message if not valid, return null if valid
	 * @param text
	 * @return
	 */
	private String validateUrl(String urlText) {
		@SuppressWarnings("unused")
		URL url;
		if (urlText == null) {
			return "No URL specified";
		}
		if (urlText.trim().isEmpty()) {
			return "No URL specified";
		}
		try {
			url = new URL(tbPackageDownloadUrl.getText());
			return null;
		} catch (Exception ex) {
			return "Invalid URL: "+ex.getMessage();
		}
	}

	/**
	 * Validates a sha1 string. Returns a message if invalid, otherwise returns null if valid
	 * @param sha1
	 * @return
	 */
	String validateSha1(String sha1) {
		if (sha1.trim().isEmpty()) {
			return null;	// optional field
		}
		char[] chars = sha1.toCharArray();
		if (chars.length != 40) {
			return "Incorrect number of digits for a SHA1 (should be 40)";
		}
		for (int i = 0; i < chars.length; i++) {
			if (!((chars[i] >= '0' && chars[i] <= '9') || 
					(chars[i] >= 'a' && chars[i] <='f') ||
					(chars[i] >= 'A' && chars[i] <= 'F'))) {
				return "Invalid charector for SHA1: '"+chars[i]+ "'";
			}
		}
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		if (validatePage()) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}

	/**
	 * Update the SPDX properties in spdxProject from the text fields and updates
	 * the project properties
	 */
	public boolean updateSpdx(SpdxProject spdxProject) {
		// name - required
		if (!this.tbPackageName.getText().trim().isEmpty()) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setDeclaredName(this.tbPackageName.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the declared name for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the declared name for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		// version
		if (!this.tbPackageVersion.getText().trim().isEmpty()) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setVersionInfo(this.tbPackageVersion.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the version for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the version for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		// copyright
		if (!this.tbPackageCopyright.getText().trim().isEmpty()) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setDeclaredCopyright(this.tbPackageCopyright.getText().trim());
				SpdxProjectProperties.setDefaultFileCopyright(project, this.tbPackageCopyright.getText());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the copyright for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the copyright for the SPDX Project: "+e.getMessage());
				return false;
			} catch (CoreException e) {
				Activator.getDefault().logError("Error setting the default file copyright for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the default file copyright for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		// originator
		if (!this.tbPackageOriginator.getText().trim().isEmpty()) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setOriginator(this.comboOriginatorPrefixes.getText() + this.tbPackageOriginator.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the originator for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the originator for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		// supplier
		if (!this.tbPackageSupplier.getText().trim().isEmpty()) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setSupplier(this.comboSupplierPrefixes.getText() + this.tbPackageSupplier.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the supplier for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the supplier for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		// source
		if (!this.tbSourceInfo.getText().trim().isEmpty()) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setSourceInfo(this.tbSourceInfo.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the source info for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the source info for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		// archive
		if (!this.tbArchiveFileName.getText().trim().isEmpty()) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setFileName(this.tbArchiveFileName.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the archive file name for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the archive file name for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		// sha1
		if (!this.tbArchiveSha1.getText().trim().isEmpty() && validateSha1(this.tbArchiveSha1.getText())== null) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setSha1(this.tbArchiveSha1.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the archive file SHA1 for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the archive file SHA1 for the SPDX Project: "+e.getMessage());
				return false;
			}
		}		
		// download URL
		if (validateUrl(this.tbPackageDownloadUrl.getText()) == null) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setDownloadUrl(this.tbPackageDownloadUrl.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the downloadURL for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the downloadURL for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		// short desc
		if (!this.tbPackageShortDescription.getText().trim().isEmpty()) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setShortDescription(this.tbPackageShortDescription.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the short description for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the short description for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		// description
		if (!this.tbPackageDescription.getText().trim().isEmpty()) {
			try {
				spdxProject.getSpdxDoc().getSpdxPackage().setDescription(this.tbPackageDescription.getText().trim());
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error setting the description for the SPDX Project", e);
				MessageDialog.openError(getShell(), "Error", 
						"Error setting the description for the SPDX Project: "+e.getMessage());
				return false;
			}
		}
		return true;	// whew, made it!
	}
}
