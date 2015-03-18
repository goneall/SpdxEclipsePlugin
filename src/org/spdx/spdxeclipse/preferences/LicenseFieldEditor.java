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
package org.spdx.spdxeclipse.preferences;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxeclipse.ui.LicenseSelectionGroup;

/**
 * Field editor for SPDX licenses
 * @author Gary O'Neall
 *
 */
public class LicenseFieldEditor extends FieldEditor {
	
	String[] availableLicenses;
	// The top-level control for the field editor.
	private Composite parent;
	private LicenseSelectionGroup licenseSelectionGroup;
	
	public LicenseFieldEditor(String preference, String label, Composite parent) {
		super(preference, label, parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = ((GridData)parent.getLayoutData());
		gd.horizontalSpan = numColumns;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		this.parent = parent;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numColumns;
		this.parent.setLayoutData(gd);
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) {
					availableLicenses = SPDXLicenseInfoFactory.getStandardLicenseIds();
				}
				
			});
		} catch (InvocationTargetException e) {
			availableLicenses = SPDXLicenseInfoFactory.getStandardLicenseIds();
		} catch (InterruptedException e) {
			availableLicenses = SPDXLicenseInfoFactory.getStandardLicenseIds();
		}
		Arrays.sort(availableLicenses);
		licenseSelectionGroup = new LicenseSelectionGroup(parent, SWT.BORDER,
				this.getLabelText(), availableLicenses, SpdxRdfConstants.NOASSERTION_VALUE);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		String license = getPreferenceStore().getString(getPreferenceName());
		licenseSelectionGroup.setLicense(license);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		String license = getPreferenceStore().getDefaultString(getPreferenceName());
		licenseSelectionGroup.setLicense(license);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	@Override
	protected void doStore() {
		String val = licenseSelectionGroup.getSelectedLicense();
		if (val != null) {
			getPreferenceStore().setValue(getPreferenceName(), val);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	@Override
	public int getNumberOfControls() {
		return 1;
	}

}
