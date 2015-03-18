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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.spdxeclipse.project.SpdxProject;

/**
 * Implements a group to describe a complex license using AND's and OR's
 * 
 * If an SPDX project is provided, new licenses may be added
 * 
 * @author Gary O'Neall
 *
 */
public class LicenseSelectionGroup implements Listener {

	private String[] availableLicenses;
	private CCombo comboLicense;
	private Text tbLicense;
	/**
	 * Listeners for changes in the resultant text box events
	 */
	private ArrayList<Listener> listeners = new ArrayList<Listener>();
	private String defaultLicense;
	private String groupLabel;
	private SpdxProject project = null;
	private Group licenseGroup;

	public LicenseSelectionGroup(Composite parent, int style, String groupLabel, 
			SpdxProject project, String defaultLicense) {
		this.project = project;
		this.groupLabel = groupLabel;
		final SpdxProject tProject = project;
		// this takes a while on first instance
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) {
					availableLicenses = tProject.getAvailableLicenseNames();
				}
				
			});
		} catch (InvocationTargetException e) {
			availableLicenses = tProject.getAvailableLicenseNames();
		} catch (InterruptedException e) {
			availableLicenses = tProject.getAvailableLicenseNames();
		}
		Arrays.sort(availableLicenses);
		this.defaultLicense = defaultLicense;		
		createControl(parent, style);
	}
	
	public LicenseSelectionGroup(Composite parent, int style,
			String groupLabel, String[] availableLicenseNames,
			String defaultLicense) {
		this.project = null;
		this.groupLabel = groupLabel;
		this.availableLicenses = availableLicenseNames;
		this.defaultLicense = defaultLicense;		
		createControl(parent, style);
	}
	
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	private void createControl(Composite parent, int style) {
		licenseGroup = new Group(parent, SWT.NONE);
		licenseGroup.setLayout(new GridLayout(2, false));
		licenseGroup.setText(groupLabel);
		licenseGroup.setToolTipText("Choose a license from the dropdown and use the AND, OR and X buttons to construct a license");
		GridData gdDeclaredGroup = new GridData();
		gdDeclaredGroup.horizontalAlignment = SWT.FILL;
		gdDeclaredGroup.grabExcessHorizontalSpace = true;
		gdDeclaredGroup.verticalAlignment = SWT.FILL;
		gdDeclaredGroup.heightHint = 110;
		gdDeclaredGroup.horizontalSpan = 2;
		licenseGroup.setLayoutData(gdDeclaredGroup);

		comboLicense = createLicenseComposite(licenseGroup, availableLicenses);	
		
		Composite declaredLicensePlusMinus = new Composite(licenseGroup, SWT.NONE);
		GridLayout layoutPlusMinus = new GridLayout(4, true);;
		if (this.project == null) {
			layoutPlusMinus.numColumns = 3;
		}
		declaredLicensePlusMinus.setLayout(layoutPlusMinus);
		GridData gdDeclLicPlusMin = new GridData();
		gdDeclLicPlusMin.horizontalAlignment = SWT.CENTER;
		gdDeclLicPlusMin.grabExcessHorizontalSpace = false;
		Button btDeclaredAnd = new Button(declaredLicensePlusMinus, SWT.PUSH | SWT.CENTER);
		btDeclaredAnd.setText("AND");
		btDeclaredAnd.setToolTipText("Add the license for declared licenses");
		btDeclaredAnd.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (comboLicense.getText() != null && !comboLicense.getText().isEmpty()) {
					String licString = tbLicense.getText();
					licString = appendLicenseString(licString, comboLicense.getText(), "AND");
					tbLicense.setText(licString);
				}
			}
			
		});
		Button btDeclareOr = new Button(declaredLicensePlusMinus, SWT.PUSH | SWT.CENTER);
		btDeclareOr.setText("OR");
		btDeclareOr.setToolTipText("Add license as a choice of licenses");
		btDeclareOr.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (comboLicense.getText() != null && !comboLicense.getText().isEmpty()) {
					String licString = tbLicense.getText();
					licString = appendLicenseString(licString, comboLicense.getText(), "OR");
					tbLicense.setText(licString);
				}
			}
			
		});
		
		if (this.project != null) {	// the new button is only functional when a project is provided
			Button btNewLicense = new Button(declaredLicensePlusMinus, SWT.CENTER | SWT.PUSH);
			btNewLicense.setText("NEW");
			btNewLicense.setToolTipText("Create a new custom license");
			btNewLicense.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					CreateLicenseDialog dialog = new CreateLicenseDialog(licenseGroup.getShell(), project);
					if (dialog.open() == Dialog.OK) {
						SPDXNonStandardLicense license = dialog.getLicense();
						addAvailableLicense(license.getId());
						comboLicense.setText(license.getId());
					}
				}
				
			});
		}

		Button btClearDeclaredLicense = new Button(declaredLicensePlusMinus, SWT.CENTER | SWT.PUSH);
		btClearDeclaredLicense.setText("X");
		btClearDeclaredLicense.setToolTipText("Clear declared license");
		btClearDeclaredLicense.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				tbLicense.setText("");
			}
			
		});	
		this.tbLicense = new Text(licenseGroup, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		this.tbLicense.setToolTipText("License as declared by the originator");
		GridData gdPkgDeclaredLicense = new GridData();
		gdPkgDeclaredLicense.grabExcessHorizontalSpace = true;
		gdPkgDeclaredLicense.horizontalAlignment = SWT.FILL;
		gdPkgDeclaredLicense.grabExcessVerticalSpace = true;
		gdPkgDeclaredLicense.verticalAlignment = SWT.FILL;
		gdPkgDeclaredLicense.horizontalSpan = 2;
		this.tbLicense.setLayoutData(gdPkgDeclaredLicense);
		if (defaultLicense != null) {
			this.tbLicense.setText(defaultLicense);	
		};
		this.tbLicense.addListener(SWT.Modify, this);
	}
	
	private void addAvailableLicense(String licenseId) {
		this.availableLicenses = Arrays.copyOf(this.availableLicenses, this.availableLicenses.length + 1);
		this.availableLicenses[this.availableLicenses.length - 1] = licenseId;
		Arrays.sort(this.availableLicenses);
		this.comboLicense.setItems(this.availableLicenses);
	}
	
	public static CCombo createLicenseComposite(final Composite parent, final String[] availableLicenses) {
		final CCombo cComboLicenseNames = new CCombo(parent, SWT.BORDER);
		cComboLicenseNames.setToolTipText("\"Enter the name of the license or select an existing license from the drop down list\"");
		GridData gdCombo = new GridData();
		gdCombo.grabExcessHorizontalSpace = true;
		gdCombo.horizontalAlignment = SWT.FILL;
		cComboLicenseNames.setLayoutData(gdCombo);
		for (int i = 0; i < availableLicenses.length; i++) {
			cComboLicenseNames.add(availableLicenses[i]);
		}
		cComboLicenseNames.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent e) {
				//Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String initialSelection = new String(new char[] {e.character});
				FilteredItemsSelectionDialog licensePicker = new LicensePicker(parent.getShell(),cComboLicenseNames.getItems(),false, initialSelection);
				licensePicker.open();
				
				String selectedLicense = null;
				if (licensePicker.getResult() != null && licensePicker.getResult().length > 0) {
					selectedLicense = licensePicker.getResult()[0].toString();
				}
				if(selectedLicense!=null){
					cComboLicenseNames.setText(selectedLicense);
				}else{
					cComboLicenseNames.setText("");
				}
			}
			
		});
		return cComboLicenseNames;
	}
	
	/**
	 * Append a new license to an existing license string
	 * @param originalString Original license string
	 * @param license The license name to be appended
	 * @param junction The junction term to be used between licenses - "AND" or "OR"
	 * @return
	 */
	public static String appendLicenseString(String originalString, String license, String junction) {
		if (originalString == null || originalString.trim().isEmpty()) {
			return license;	// nothing to append to
		} else {
			String retval = originalString;
			if (retval.charAt(0)!= '(') {
				retval = "(" + retval;
			}
			if (originalString.endsWith(")")) {
				retval = retval.substring(0, retval.length()-1);
			}
			retval = retval + " " + junction + " " + license + ")";
			return retval;
		}
	}

	@Override
	public void handleEvent(Event event) {
		for (int i = 0;i < listeners.size(); i++) {
			listeners.get(i).handleEvent(event);
		}
	}

	public String getSelectedLicense() {
		if (this.tbLicense == null) {
			return null;
		}
		return this.tbLicense.getText();
	}

	public void setLicense(String license) {
		this.tbLicense.setText(license);
	}
}
