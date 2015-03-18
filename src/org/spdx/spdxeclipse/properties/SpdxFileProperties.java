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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxNoAssertionLicense;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.project.SpdxProjectException;
import org.spdx.spdxeclipse.project.SpdxProjectFactory;

/**
* Class used to manage the file properties associated with SPDX
 * 
 * If a property has not been set, the property will be retrieved from the 
 * Project property and the property will be initialized to that preference
 * @author Gary O'Neall
 *
 */
public class SpdxFileProperties {
	
	static final String LIST_SEPARATOR = ";";	// used to separate lists.  Must be regex friendly (not spec. char) and must not appear in a file name path

	public static String getConcludedLicense(IFile file) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_CONCLUDED_LICENSE);
		String license = file.getPersistentProperty(qName);
		if (license == null || license.isEmpty()) {
			license = SpdxProjectProperties.getDefaultFileLicense(file.getProject());		
			file.setPersistentProperty(qName, license);
		}
		if (license == null) {
			license = new SpdxNoAssertionLicense().toString();
		}
		return license;
	}
	
	public static void setConcludedLicense(IFile file, String concludedLicense) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_CONCLUDED_LICENSE);
		String oldValue = file.getPersistentProperty(qName);
		notifySpdxProject(file, PropertyConstants.PROP_SPDX_FILE_CONCLUDED_LICENSE, oldValue, concludedLicense);
		file.setPersistentProperty(qName, concludedLicense);
	}

	public static String getCopyright(IFile file) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_COPYRIGHT);
		String copyright = file.getPersistentProperty(qName);
		if (copyright == null || copyright.isEmpty()) {
			copyright = SpdxProjectProperties.getDefaultFileCopyright(file.getProject());	
			file.setPersistentProperty(qName, copyright);
		}
		if (copyright == null) {
			copyright = SpdxRdfConstants.NOASSERTION_VALUE;
		}
		return copyright;
	}
	
	public static void setCopyright(IFile file, String copyright) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_COPYRIGHT);
		String oldValue = file.getPersistentProperty(qName);
		notifySpdxProject(file, PropertyConstants.PROP_SPDX_FILE_COPYRIGHT, oldValue, copyright);
		file.setPersistentProperty(qName, copyright);
	}
	
	public static String getNotice(IFile file) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_NOTICE);
		String notice = file.getPersistentProperty(qName);
		if (notice == null || notice.isEmpty()) {
			notice = SpdxProjectProperties.getDefaultFileNotice(file.getProject());	
			file.setPersistentProperty(qName, notice);
		}
		if (notice == null) {
			notice = "";
		}
		return notice;
	}
	
	public static void setNotice(IFile file, String notice) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_NOTICE);
		String oldValue = file.getPersistentProperty(qName);
		notifySpdxProject(file, PropertyConstants.PROP_SPDX_FILE_NOTICE, oldValue, notice);
		file.setPersistentProperty(qName, notice);
	}
	
	public static String getComment(IFile file) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_COMMENT);
		String comment = file.getPersistentProperty(qName);
		if (comment == null) {
			return "";
		}
		return comment;
	}
	
	public static void setComment(IFile file, String comment) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_COMMENT);
		String oldValue = file.getPersistentProperty(qName);
		notifySpdxProject(file, PropertyConstants.PROP_SPDX_FILE_COMMENT, oldValue, comment);
		file.setPersistentProperty(qName, comment);
	}
	
	public static String[] getContributors(IFile file) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_CONTRIBUTORS);
		String contributors = file.getPersistentProperty(qName);
		if (contributors == null || contributors.trim().isEmpty()) {
			String[] defaultContributors = SpdxProjectProperties.getDefaultFileContributors(file.getProject());
			contributors = stringArrayToString(defaultContributors);
			file.setPersistentProperty(qName, contributors);
		}
		return stringToStringArray(contributors);
	}
	
	public static void setContributors(IFile file, String[] contributors) throws CoreException {
		String sContributors = stringArrayToString(contributors);
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_CONTRIBUTORS);
		String oldValue = file.getPersistentProperty(qName);
		notifySpdxProject(file, PropertyConstants.PROP_SPDX_FILE_CONTRIBUTORS, oldValue, sContributors);
		file.setPersistentProperty(qName, sContributors);
	}

	/**
	 * Converts an array of strings to a string by inserting delimiters
	 * @param sa
	 * @return
	 */
	private static String stringArrayToString(String[] sa) {
		if (sa== null || sa.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(sa[0].trim());
		for (int i = 1; i < sa.length; i++) {
			sb.append(LIST_SEPARATOR);
			sb.append(sa[i].trim());
		}
		return sb.toString();
	}
	
	/**
	 * Converts a delimited list to an array of strings
	 * @param s
	 * @return
	 */
	private static String[] stringToStringArray(String s) {
		if (s == null || s.trim().isEmpty()) {
			return new String[0];
		}
		String[] retval = s.split(LIST_SEPARATOR);
		for (int i = 0; i < retval.length; i++) {
			retval[i] = retval[i].trim();
		}
		return retval;
	}

	/**
	 * Artifact of project name
	 * @param file
	 * @return
	 * @throws CoreException 
	 */
	public static String getProjectName(IFile file) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_ARTIFACT_OF_PROJECT);
		String projectName = file.getPersistentProperty(qName);
		if (projectName == null) {
			projectName = "";
		}
		return projectName;
	}
	
	/**
	 * @param file
	 * @param projectName ArtifactOf Project Name
	 * @throws CoreException
	 */
	public static void setProjectName(IFile file, String projectName) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_ARTIFACT_OF_PROJECT);
		String oldValue = file.getPersistentProperty(qName);
		notifySpdxProject(file, PropertyConstants.PROP_SPDX_FILE_ARTIFACT_OF_PROJECT, oldValue, projectName);
		file.setPersistentProperty(qName, projectName);
	}

	/**
	 * ARtifact of project URL
	 * @param file
	 * @return
	 * @throws CoreException 
	 */
	public static String getProjectUrl(IFile file) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_ARTIFACT_OF_URL);
		String projectUrl = file.getPersistentProperty(qName);
		if (projectUrl == null) {
			projectUrl = "";
		}
		return projectUrl;
	}
	
	/**
	 * @param file
	 * @param url ARtifact of project URL
	 * @throws CoreException
	 */
	public static void setProjectUrl(IFile file, String url) throws CoreException {
		QualifiedName qName = new QualifiedName(Activator.PLUGIN_ID, PropertyConstants.PROP_SPDX_FILE_ARTIFACT_OF_URL);
		String oldValue = file.getPersistentProperty(qName);
		notifySpdxProject(file, PropertyConstants.PROP_SPDX_FILE_ARTIFACT_OF_URL, oldValue, url);
		file.setPersistentProperty(qName, url);
	}
	
	/**
	 * Notifies the SPDX project of the property change if the SPDX project has
	 * been initialized for the project
	 * @param file
	 * @param property
	 * @param oldValue
	 * @param newValue
	 * @throws CoreException 
	 */
	private static void notifySpdxProject(IFile file, String property, String oldValue, String newValue) throws CoreException {		
		try {
			IProject project = file.getProject();
			if (SpdxProjectProperties.isSpdxInitialized(project) && SpdxProjectFactory.isSpdxProjectOpen(project.getName())) {
				SpdxProjectFactory.getSpdxProject(project.getName(), new NullProgressMonitor()).propertyChange(file, property, oldValue, newValue);
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
}
