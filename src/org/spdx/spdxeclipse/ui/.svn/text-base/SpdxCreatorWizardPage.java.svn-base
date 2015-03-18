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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.preferences.SpdxProjectPreferences;
import org.spdx.spdxeclipse.project.SpdxProject;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxCreatorWizardPage extends WizardPage implements IWizardPage {
	
	static DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
	
	private IProject project;
	private Text tbCreator;
	private Text tbCreatorComment;
	private Combo comboCreatorPrefixes;
	private static String[] CREATOR_PREFIXES = {SpdxRdfConstants.CREATOR_PREFIX_PERSON,
			SpdxRdfConstants.CREATOR_PREFIX_ORGANIZATION, SpdxRdfConstants.CREATOR_PREFIX_TOOL};
	private List listCreators;
	private Button btAdd;
	private Button btDelete;

	public SpdxCreatorWizardPage(SpdxProject spdxProject) {
		super("Creator Information");
		this.project = spdxProject.getProject();
		this.setDescription("Creator information for SPDX");
		this.setPageComplete(false);
		this.setTitle("SPDX Creator");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite creatorGroup = new Composite(parent, SWT.NONE);
		this.setControl(creatorGroup);
		creatorGroup.setLayout(new GridLayout(3, false));
		GridData gridDataGroup = new GridData();
		gridDataGroup.grabExcessHorizontalSpace = true;
		gridDataGroup.horizontalAlignment = SWT.FILL;
		gridDataGroup.grabExcessVerticalSpace = true;
		gridDataGroup.verticalAlignment = SWT.FILL;
		creatorGroup.setLayoutData(gridDataGroup);
		
		Label lblCreator = new Label(creatorGroup, SWT.NONE);
		lblCreator.setText("Creator: ");
		this.comboCreatorPrefixes = new Combo(creatorGroup, SWT.BORDER | SWT.READ_ONLY);
		this.comboCreatorPrefixes.setItems(CREATOR_PREFIXES);
		this.comboCreatorPrefixes.setText(CREATOR_PREFIXES[0]);
		this.tbCreator = new Text(creatorGroup, SWT.BORDER);
		this.tbCreator.setToolTipText("Creator for the SPDX document (can be a person, organzation, or a tool)");
		GridData gdCreator = new GridData();
		gdCreator.grabExcessHorizontalSpace = true;
		gdCreator.horizontalAlignment = SWT.FILL;
		tbCreator.setLayoutData(gdCreator);
		tbCreator.addListener(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				btAdd.setEnabled(!tbCreator.getText().trim().isEmpty());
			}
			
		});
		
		this.listCreators = new List(creatorGroup, SWT.MULTI | SWT.V_SCROLL);
		GridData gdList = new GridData();
		gdList.horizontalAlignment = SWT.FILL;
		gdList.horizontalSpan = 3;
		gdList.grabExcessHorizontalSpace = true;
		gdList.verticalAlignment = SWT.FILL;
		gdList.grabExcessVerticalSpace = true;
		this.listCreators.setLayoutData(gdList);
		
		Group buttonGroup = new Group(creatorGroup, SWT.NONE);
		GridData gdButtons = new GridData();
		gdButtons.horizontalAlignment = SWT.CENTER;
		gdButtons.horizontalSpan = 3;
		buttonGroup.setLayoutData(gdButtons);
		buttonGroup.setLayout(new GridLayout(2, false));
		
		btAdd = new Button(buttonGroup, SWT.PUSH);
		btAdd.setText("Add");
		btAdd.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (tbCreator.getText().trim().isEmpty()) {
					return;
				}
				String creatorName = comboCreatorPrefixes.getText()+tbCreator.getText().trim();
				// check for duplicates
				String[] existingCreators = listCreators.getItems();
				boolean dup = false;
				for (int i = 0; i < existingCreators.length; i++) {
					if (existingCreators[i].equals(creatorName)) {
						dup = true;
						break;
					}
				}
				if (!dup) {
					listCreators.add(creatorName);
					setPageComplete(true);
					btDelete.setEnabled(true);
				}
				tbCreator.setText("");
				btAdd.setEnabled(false);
			}
			
		});
		btAdd.setEnabled(false);
		btDelete = new Button(buttonGroup, SWT.PUSH);
		btDelete.setText("Remove");
		
		btDelete.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				String[] toDelete = listCreators.getSelection();
				for (int i = 0; i < toDelete.length; i++) {
					listCreators.remove(toDelete[i]);
				}
				if (listCreators.getItemCount() < 1) {
					setPageComplete(false);
					btDelete.setEnabled(false);
				}
			}
			
		});
		btDelete.setEnabled(false);
		
		Label lblCreatorComment = new Label(creatorGroup, SWT.NONE);
		lblCreatorComment.setText("Comment: ");
		tbCreatorComment = new Text(creatorGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tbCreatorComment.setToolTipText("Optional comment from the creator of the SPDX document");
		GridData gdCreatorComment = new GridData();
		gdCreatorComment.horizontalAlignment = SWT.FILL;
		gdCreatorComment.grabExcessHorizontalSpace = true;
		gdCreatorComment.horizontalSpan = 2;
		gdCreatorComment.heightHint = 90;
		gdCreatorComment.verticalAlignment = SWT.FILL;
		tbCreatorComment.setLayoutData(gdCreatorComment);
		
		String[] initialCreators = new String[0];
		String initialCreatorsString = SpdxProjectPreferences.getDefaultCreator(project).trim();
		if (!initialCreatorsString.isEmpty()) {
			initialCreators = initialCreatorsString.split("\n");
		}
		if (initialCreators != null && initialCreators.length > 0) {
			for (int i = 0; i < initialCreators.length; i++) {
				listCreators.add(initialCreators[i]);
			}
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}

	public boolean updateSpdx(SpdxProject spdxProject) {
		if (listCreators != null && listCreators.getItemCount() > 0) {	
			String[] creators = listCreators.getItems();
			String licenseListVersion = SPDXLicenseInfoFactory.getLicenseListVersion();	
			SPDXCreatorInformation creatorInfo = new SPDXCreatorInformation(
					creators, format.format(new Date()), tbCreatorComment.getText().trim(), licenseListVersion);
			try {
				spdxProject.getSpdxDoc().setCreationInfo(creatorInfo);
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Unable to store creator information in SPDX document", e);
				MessageDialog.openError(getShell(), "Error", "Unable to store creator information in SPDX document: "+e.getMessage());
				return false;
			}
			StringBuilder sb = new StringBuilder();
			
			sb.append(creators[0]);
			for (int i = 1; i < creators.length; i++) {
				sb.append('\n');
				sb.append(creators[i]);
			}
		}
		return true;
	}
}
