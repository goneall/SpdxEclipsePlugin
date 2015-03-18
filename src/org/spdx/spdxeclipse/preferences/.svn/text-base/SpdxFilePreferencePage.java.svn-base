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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.spdx.spdxeclipse.Activator;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxFilePreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public SpdxFilePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Software Product Data Exchange (SPDX) file preferences");
	}
	public void createFieldEditors() {
		addField(new LicenseFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_LICENSE,
				"Default SPDX File License: ", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_COPYRIGHT,
				"Default SPDX File Copyright: ", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_NOTICE,
				"Default SPDX File Notice: ", getFieldEditorParent()));
		addField(new StringListFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_CONTRIBUTORS,
				"Default SPDX File Contributors: ", getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {

	}
}
