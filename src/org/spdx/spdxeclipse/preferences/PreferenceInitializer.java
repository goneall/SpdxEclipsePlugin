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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.spdx.spdxeclipse.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_NAME, PreferenceConstants.PROJECT_NAME+".spdx");
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_URL, 
				"http://www.spdx.org/spdxdocs/"+PreferenceConstants.PROJECT_NAME);
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_PROJECT_NAME, PreferenceConstants.PROJECT_NAME);
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_PROJECT_VERSION, "0.1");
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_PACKAGE_COPYRIGHT, "Copyright (c) [year], [organization]");
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_PACKAGE_DOWNLOAD_URL, "http://www.spdx.org/packageDownloads/"+PreferenceConstants.PROJECT_NAME);
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_CONCLUDED_LICENSE, "");
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_DECLARED_LICENSE, "");
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_LICENSE, "");
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_CREATOR, "");
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_COPYRIGHT, "Copyright (c) [year], [organization]");
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_NOTICE, "");
		store.setDefault(PreferenceConstants.PREF_DEFAULT_SPDX_FILE_CONTRIBUTORS, "");
	}
}
