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
package org.spdx.spdxeclipse.properties;

import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.preferences.SpdxProjectPreferences;
import org.spdx.spdxeclipse.project.SpdxProjectException;
import org.spdx.spdxeclipse.project.SpdxProjectFactory;

/**
 * Class used to manage the project properties associated with SPDX
 * 
 * If a property has not been set, the property will be retrieved from the 
 * Project Preferences and the property will be initialized to that preference
 * 
 * @author Gary O'Neall
 *
 */
public class SpdxProjectProperties {
	
	private static final String LIST_SEPARATOR = ";";	// used to separate lists.  Must be regex friendly (not spec. char) and must not appear in a file name path

	public static String getSpdxFileName(IProject project) throws CoreException {
		String spdxFileName = null;
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_NAME);
		try {
			spdxFileName = project.getPersistentProperty(qPropName);
		} catch (CoreException e) {
			// nothing
		}
		if (spdxFileName == null) {
			// not initialized
			spdxFileName = SpdxProjectPreferences.getDefaultSpdxFileName(project);
			project.setPersistentProperty(qPropName, spdxFileName);
		}
		return spdxFileName;
	}
	
	public static void setSpdxFileName(IProject project, String spdxFileName) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_NAME);
		String oldValue = project.getPersistentProperty(qPropName);
		notifySpdxProject(project, PropertyConstants.PROP_SPDX_FILE_NAME, oldValue, spdxFileName);
		project.setPersistentProperty(qPropName, spdxFileName);
	}
	
	public static boolean isSpdxInitialized(IProject project) throws CoreException {
		String initialized = null;
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_INITIALIZED);
		try {
			initialized = project.getPersistentProperty(qPropName);
		} catch (CoreException e) {
			// nothing
		}
		if (initialized == null) {
			initialized = convertBooleanToString(false);
			project.setPersistentProperty(qPropName, initialized);
		}
		return convertStringToBoolean(initialized);	
	}
	

	public static void setSpdxInitialized(IProject project, boolean initialized) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_INITIALIZED);
		project.setPersistentProperty(qPropName, convertBooleanToString(initialized));
	}
	
	private static String convertBooleanToString(boolean b) {
		if (b) {
			return "TRUE";
		} else {
			return "FALSE";
		}
	}


	private static boolean convertStringToBoolean(String s) {
		return s.toUpperCase().equals("TRUE");	
	}

	public static String getSpdxDocUrl(IProject project) throws CoreException {
		String spdxDocUrl = null;
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DOC_URL);
		try {
			spdxDocUrl = project.getPersistentProperty(qPropName);
		} catch (CoreException e) {
			// nothing
		}
		if (spdxDocUrl == null) {
			// not initialized
			spdxDocUrl = SpdxProjectPreferences.getDefaultSpdxDocUrl(project);
			project.setPersistentProperty(qPropName, spdxDocUrl);
		}
		return spdxDocUrl;
	}
	
	public static void setSpdxDocUrl(IProject project, String docUrl) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DOC_URL);
		String oldValue = project.getPersistentProperty(qPropName);
		notifySpdxProject(project, PropertyConstants.PROP_SPDX_DOC_URL, oldValue, docUrl);
		project.setPersistentProperty(qPropName, docUrl);
	}
	
	public static String getDefaultFileLicense(IProject project) throws CoreException {
		String defaultFileLicense = null;
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DEFAULT_FILE_LICENSE);
		try {
			defaultFileLicense = project.getPersistentProperty(qPropName);
		} catch (CoreException e) {
			// nothing
		}
		if (defaultFileLicense == null) {
			// not initialized
			defaultFileLicense = SpdxProjectPreferences.getDefaultFileLicense(project);
			project.setPersistentProperty(qPropName, defaultFileLicense);
		}
		return defaultFileLicense;
	}
	
	public static String getDefaultFileCopyright(IProject project) throws CoreException {
		String defaultFileCopyright = null;
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DEFAULT_FILE_COPYRIGHT);
		try {
			defaultFileCopyright = project.getPersistentProperty(qPropName);
		} catch (CoreException e) {
			//nothing
		}
		if (defaultFileCopyright == null) {
			// not initialized
			defaultFileCopyright = SpdxProjectPreferences.getDefaultSpdxFileCopyright(project);
			project.setPersistentProperty(qPropName, defaultFileCopyright);
		}
		return defaultFileCopyright;
	}

	public static void setDefaultFileLicense(IProject project, String fileLicense) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DEFAULT_FILE_LICENSE);
		String oldValue = project.getPersistentProperty(qPropName);
		notifySpdxProject(project, PropertyConstants.PROP_SPDX_DEFAULT_FILE_LICENSE, oldValue, fileLicense);
		project.setPersistentProperty(qPropName, fileLicense);
	}

	public static void setDefaultFileCopyright(IProject project, String copyrightText) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DEFAULT_FILE_COPYRIGHT);
		String oldValue = project.getPersistentProperty(qPropName);
		notifySpdxProject(project, PropertyConstants.PROP_SPDX_DEFAULT_FILE_COPYRIGHT, oldValue, copyrightText);
		project.setPersistentProperty(qPropName, copyrightText);
	}
	
	public static void setDefaultFileNotice(IProject project, String noticeText) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DEFAULT_FILE_NOTICE);
		String oldValue = project.getPersistentProperty(qPropName);
		notifySpdxProject(project, PropertyConstants.PROP_SPDX_DEFAULT_FILE_NOTICE, oldValue, noticeText);
		project.setPersistentProperty(qPropName, noticeText);
	}
	
	public static String getDefaultFileNotice(IProject project) throws CoreException {
		String defaultFileNotice = null;
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DEFAULT_FILE_NOTICE);
		try {
			defaultFileNotice = project.getPersistentProperty(qPropName);
		} catch (CoreException e) {
			//nothing
		}
		if (defaultFileNotice == null) {
			// not initialized
			defaultFileNotice = SpdxProjectPreferences.getDefaultSpdxFileNotice(project);
			project.setPersistentProperty(qPropName, defaultFileNotice);
		}
		return defaultFileNotice;
	}
	
	public static String[] getDefaultFileContributors(IProject project) throws CoreException {
		String defaultContributors = null;
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DEFAULT_FILE_CONTRIBUTORS);
		try {
			defaultContributors = project.getPersistentProperty(qPropName);
		} catch (CoreException e) {
			//nothing
		}
		if (defaultContributors == null) {
			// not initialized
			defaultContributors = SpdxProjectPreferences.getDefaultSpdxFileDefaultContributors(project);
			project.setPersistentProperty(qPropName, defaultContributors);
		}
		return defaultContributors.split(LIST_SEPARATOR);
	}
	
	public static void setDefaulFileContributors(IProject project, String[] contributors) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_DEFAULT_FILE_CONTRIBUTORS);
		String oldValue = project.getPersistentProperty(qPropName);
		StringBuilder propValue = new StringBuilder();
		if (contributors.length > 0) {
			propValue.append(contributors[0]);
		}
		for (int i = 1; i < contributors.length; i++) {
			propValue.append(LIST_SEPARATOR);
			propValue.append(contributors[i]);
		}
		notifySpdxProject(project, PropertyConstants.PROP_SPDX_DEFAULT_FILE_CONTRIBUTORS, oldValue, propValue.toString());
		project.setPersistentProperty(qPropName, propValue.toString());
	}
	
	public static void setIncludedResourceDirectories(IProject project, String[] includedDirectories) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_INCLUDED_RESOURCE_DIRECTORIES);
		String oldValue = project.getPersistentProperty(qPropName);
		StringBuilder propValue = new StringBuilder();
		if (includedDirectories.length > 0) {
			propValue.append(includedDirectories[0]);
		}
		for (int i = 1; i < includedDirectories.length; i++) {
			propValue.append(LIST_SEPARATOR);
			propValue.append(includedDirectories[i]);
		}
		notifySpdxProject(project, PropertyConstants.PROP_INCLUDED_RESOURCE_DIRECTORIES, oldValue, propValue.toString());
		project.setPersistentProperty(qPropName, propValue.toString());
	}
	
	public static String[] getIncludedResourceDirectories(IProject project) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_INCLUDED_RESOURCE_DIRECTORIES);
		String propValue = project.getPersistentProperty(qPropName);
		if (propValue == null) {
			return new String[0];
		}
		else {
			return propValue.split(LIST_SEPARATOR);
		}
	}
	
	public static void setExcludedFilePatterns(IProject project, String[] excludedFilePatterns) throws CoreException, InvalidExcludedFilePattern {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_EXCLUDED_FILE_PATTERNS);
		String oldValue = project.getPersistentProperty(qPropName);
		StringBuilder propValue = new StringBuilder();
		if (excludedFilePatterns.length > 0) {
			propValue.append(excludedFilePatterns[0]);
		}
		for (int i = 1; i < excludedFilePatterns.length; i++) {
			// validate the regular expression
			try {
				Pattern.compile(excludedFilePatterns[i]);
			} catch (Exception ex) {
				throw(new InvalidExcludedFilePattern("Invalid regular expression for excluded file patterns: "+excludedFilePatterns[i]));
			}
			propValue.append(LIST_SEPARATOR);
			propValue.append(excludedFilePatterns[i]);
		}
		notifySpdxProject(project, PropertyConstants.PROP_EXCLUDED_FILE_PATTERNS, oldValue, propValue.toString());
		project.setPersistentProperty(qPropName, propValue.toString());
	}
	
	public static String[] getExcludedFilePatterns(IProject project) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_EXCLUDED_FILE_PATTERNS);
		String propValue = project.getPersistentProperty(qPropName);
		if (propValue == null || propValue.isEmpty()) {
			return new String[0];
		}
		else {
			return propValue.split(LIST_SEPARATOR);
		}
	}
	
	/**
	 * Notifies the SPDX project of the property change if the SPDX project has
	 * been initialized for the project
	 * @param project
	 * @param property
	 * @param oldValue
	 * @param newValue
	 * @throws CoreException 
	 */
	private static void notifySpdxProject(IProject project, String property, String oldValue, String newValue) throws CoreException {		
		try {
			String projectName = project.getName();
			if (isSpdxInitialized(project) && SpdxProjectFactory.isSpdxProjectOpen(projectName)) {
				SpdxProjectFactory.getSpdxProject(projectName, new NullProgressMonitor()).propertyChange(project, property, oldValue, newValue);
			}
		} catch (IOException e) {
			Activator.getDefault().logError("IO Error getting spdx project", e);
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "IO Error getting spdx project", e);
			throw new CoreException(status);
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Invalid SPDX Analysis error getting spdx project", e);
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Invalid SPDX Analysis error getting spdx project", e);
			throw new CoreException(status);
		} catch (SpdxProjectException e) {
			Activator.getDefault().logError("Error updating the new property value in the SPDX project", e);
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error updating the new property value in the SPDX project", e);
			throw new CoreException(status);
		}
	}

	public static void addExcludedFilePattern(IProject project,
			String excludedPattern) throws CoreException {
		QualifiedName qPropName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_EXCLUDED_FILE_PATTERNS);
		String oldValue = project.getPersistentProperty(qPropName);
		StringBuilder propValue = new StringBuilder(oldValue);
		if (oldValue.length() > 0) {
			propValue.append(LIST_SEPARATOR);
		}
		propValue.append(excludedPattern);
		project.setPersistentProperty(qPropName, propValue.toString());
		notifySpdxProject(project, PropertyConstants.PROP_EXCLUDED_FILE_PATTERNS, oldValue, propValue.toString());
	}

}
