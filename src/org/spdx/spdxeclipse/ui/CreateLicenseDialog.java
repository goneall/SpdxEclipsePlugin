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

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.spdxeclipse.project.SpdxProject;
import org.spdx.spdxeclipse.project.SpdxProjectException;

/**
 * Dialog to create new licenses for a specific SPDX project
 * 
 * The license is added to the project
 * 
 * @author Gary O'Neall
 *
 */
public class CreateLicenseDialog extends Dialog {

	Text txtLicenseId;
	Text txtLicenseText;
	Text txtLicenseName;
	Text txtLicenseComment;
	StringSelectionGroup grpSourceUrls;
	private SpdxProject project;
	private SPDXNonStandardLicense license = null;
	
	public CreateLicenseDialog(Shell parentShell, SpdxProject project) {
		super(parentShell);
		this.project = project;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		GridLayout layout = (GridLayout)(composite.getLayout());
		layout.numColumns = 4;
		Label lblLicenseId = new Label(composite, SWT.NONE);
		lblLicenseId.setText("Id: ");
		this.txtLicenseId = new Text(composite, SWT.BORDER);
		GridData gdLicId = new GridData(GridData.FILL_HORIZONTAL);
		gdLicId.widthHint = 40;
		this.txtLicenseId.setLayoutData(gdLicId);
		this.txtLicenseId.setToolTipText("License ID.  Must be of the form LicenseRef-XX where XX is an alpha numeric string");
		this.txtLicenseId.setText(project.getNextAvailableLicenseId());
		this.txtLicenseId.addVerifyListener(new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent e) {
				// from what I can tell, I have to simulate the results of any insert or delete before verifying
				String newLicId;
				String originalicId  = txtLicenseId.getText();
				if (e.character == 0x7f) {	// delete
					if (originalicId.length() > e.start) {
						newLicId = originalicId.substring(0, e.start) + originalicId.substring(e.start+1, originalicId.length());
					} else {
						newLicId = originalicId;
					}
				} else if (e.character == 0x8) {	// backspace
					if (originalicId.length() > e.start) {
						newLicId = originalicId.substring(0, e.start) + originalicId.substring(e.start+1, originalicId.length());
					} else {
						newLicId = originalicId;
					}
				} else {
					newLicId = originalicId.substring(0, e.start) + e.text + originalicId.substring(e.start, originalicId.length());
				}
				if (!project.verifyNewLicenseId(newLicId)) {
					e.doit = false;
				}
			}
			
		});

		
		Label lblLicenseName = new Label(composite, SWT.NONE);
		lblLicenseName.setText("Name: ");
		this.txtLicenseName = new Text(composite, SWT.BORDER);
		GridData gdLicName = new GridData(GridData.FILL_HORIZONTAL);
		gdLicName.grabExcessHorizontalSpace = true;
		this.txtLicenseName.setLayoutData(gdLicName);
		this.txtLicenseName.setToolTipText("Optional name for the license");
		
		Label lblLicenseComment = new Label(composite, SWT.NONE);
		lblLicenseComment.setText("Comment: ");
		this.txtLicenseComment = new Text(composite, SWT.BORDER);
		GridData gdLicComment = new GridData(GridData.FILL_HORIZONTAL);
		gdLicComment.horizontalSpan = 3;
		gdLicComment.grabExcessHorizontalSpace = true;
		this.txtLicenseComment.setLayoutData(gdLicComment);
		this.txtLicenseComment.setToolTipText("Optional comment for the license");
		
		Label lblLicenseText = new Label(composite, SWT.NONE);
		lblLicenseText.setText("Text: ");
		this.txtLicenseText = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		GridData gdLicText = new GridData(GridData.FILL_BOTH);
		gdLicText.horizontalSpan = 3;
		gdLicText.grabExcessHorizontalSpace = true;
		gdLicText.grabExcessVerticalSpace = true;
		this.txtLicenseText.setLayoutData(gdLicText);
		this.txtLicenseText.setToolTipText("Text for the license (required)");
		this.txtLicenseText.addListener(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				getButton(IDialogConstants.OK_ID).setEnabled(
						!txtLicenseText.getText().trim().isEmpty());
			}
			
		});
		
		this.grpSourceUrls = new StringSelectionGroup(composite, SWT.BORDER, "Source URLs",
				new String[0], 4);
		
		return composite;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Create New License");
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
	    Button okButton = this.getButton(IDialogConstants.OK_ID);
	    okButton.setEnabled(false);	// disable the OK button until text has been added
		return ctrl;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}
	@Override
	protected void okPressed() {
		license = new SPDXNonStandardLicense(txtLicenseId.getText().trim(),
				txtLicenseText.getText(), txtLicenseName.getText().trim(),
				grpSourceUrls.getList(), txtLicenseComment.getText().trim());
		ArrayList<String> errors = license.verify();
		if (errors != null && errors.size() > 0) {
			StringBuilder errorStr = new StringBuilder("Invalid license:");
			for (int i = 0; i < errors.size(); i++) {
				errorStr.append("\n");
				errorStr.append(errors.get(i));
			}
			MessageDialog.openError(this.getShell(), "Invalid License", errorStr.toString());
			this.setReturnCode(Dialog.CANCEL);
		}
		try {
			project.addLicense(license);
		} catch(SpdxProjectException ex) {
			MessageDialog.openError(this.getShell(), "Error", "Error adding license to SPDX project: "+ex.getMessage());
			this.setReturnCode(Dialog.CANCEL);
		}
		super.okPressed();
	}

	public SPDXNonStandardLicense getLicense() {
		return license;
	}
}
