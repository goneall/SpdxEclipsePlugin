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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.spdx.spdxeclipse.Activator;

/**
 * Class to manage access to the project level preferences for SPDX.  This class
 * is also responsible for converting any PROJECT_NAME template strings to the actual
 * project name.
 * 
 * @author Gary O'Neall
 *
 */
public class SpdxProjectPreferences {
	
	public static IEclipsePreferences[] getPreferenceNodes(IProject project) {
		IScopeContext instanceScope = new InstanceScope();
		IEclipsePreferences instanceNode = instanceScope.getNode(Activator.PLUGIN_ID);
		IScopeContext configurationScope = new ConfigurationScope();
		IEclipsePreferences configurationNode = configurationScope.getNode(Activator.PLUGIN_ID);
		IScopeContext defaultScope = new DefaultScope();
		IEclipsePreferences defaultNode = defaultScope.getNode(Activator.PLUGIN_ID);
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(Activator.PLUGIN_ID);
		return new IEclipsePreferences[] {projectNode, instanceNode, configurationNode, defaultNode};
	}
	
	public static String getStringValue(IProject project, String keyName, String defaultValue) {
		IPreferencesService preferenceService = Platform.getPreferencesService();
		return preferenceService.get(keyName, defaultValue, getPreferenceNodes(project))
					.replace(PreferenceConstants.PROJECT_NAME, project.getName());
	}
	
	public static String getDefaultSpdxFileName(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_FILE_NAME, project.getName()+".spdx");
	}

	public static String getDefaultSpdxDocUrl(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_URL, project.getName());
	}

	public static String getDefaultSpdxProjectName(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_PROJECT_NAME, project.getName());
	}

	public static String getDefaultSpdxProjectVersion(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_PROJECT_VERSION, project.getName());
	}

	public static String getDefaultSpdxPackageCopyright(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_PACKAGE_COPYRIGHT, project.getName());
	}

	public static String getDefaultSpdxPackageDownloadUrl(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_PACKAGE_DOWNLOAD_URL, project.getName());
	}

	public static String getDefaultFileLicense(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_FILE_LICENSE, project.getName());
	}

	public static String getDefaultConcludedLicense(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_CONCLUDED_LICENSE, project.getName());
	}

	public static String getDefaultDeclaredLicense(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_DECLARED_LICENSE, project.getName());
	}

	public static String getDefaultCreator(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_CREATOR, project.getName());
	}

	public static String getDefaultSpdxFileCopyright(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_FILE_COPYRIGHT, project.getName());
	}

	public static String getDefaultSpdxFileNotice(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_FILE_NOTICE, project.getName());
	}

	public static String getDefaultSpdxFileDefaultContributors(IProject project) {
		return getStringValue(project, PreferenceConstants.PREF_DEFAULT_SPDX_FILE_CONTRIBUTORS, project.getName());
	}

}
