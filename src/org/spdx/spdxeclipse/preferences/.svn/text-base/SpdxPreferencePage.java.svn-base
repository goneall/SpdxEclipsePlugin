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

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.spdx.spdxeclipse.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class SpdxPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public SpdxPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Software Product Data Exchange (SPDX) preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new StringFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_NAME, 
				"Default SPDX &File Name:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_URL, 
				"Default SPDX Document &URL:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_PROJECT_NAME, 
				"Default SPDX Project &Name:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_PROJECT_VERSION, 
				"Default SPDX Project &Version:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_PACKAGE_COPYRIGHT,
				"Default SPDX Project &Copyright: ", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_PACKAGE_DOWNLOAD_URL,
				"Default SPDX Project &Download URL: ", getFieldEditorParent()));
		addField(new CreatorFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_CREATOR,
				"Default SPDX Creator: ", getFieldEditorParent()));		
		addField(new LicenseFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_DECLARED_LICENSE,
				"Default SPDX Declared License: ", getFieldEditorParent()));
		addField(new LicenseFieldEditor(PreferenceConstants.PREF_DEFAULT_SPDX_CONCLUDED_LICENSE,
				"Default SPDX Concluded License: ", getFieldEditorParent()));			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}