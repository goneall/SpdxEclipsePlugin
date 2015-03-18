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
package org.spdx.spdxeclipse.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.spdx.compare.LicenseCompareHelper;
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXConjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDisjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SPDXLicenseSet;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxNoAssertionLicense;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxVerificationHelper;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.builders.SpdxBuilder;
import org.spdx.spdxeclipse.nature.SpdxProjectNature;
import org.spdx.spdxeclipse.properties.PropertyConstants;
import org.spdx.spdxeclipse.properties.SpdxProjectProperties;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Contains the SPDX information for an Eclipse project.  
 * This is the primary class to access the SPDX information for an Eclipse project.
 * This class should not be instantiated directly, the SpdxProjectFactory class should be used
 * to instantiate any SpdxProject class.  This will ensure only one SpdxProject class is created
 * per project.
 * 
 * The SPDX data is persisted in the configured SPDX file for the project.
 * 
 * @author Gary O'Neall
 *
 */
public class SpdxProject {
	
	static final String DEFAULT_FILE_TYPE = "RDF/XML-ABBREV";
	
	SPDXDocument spdxDoc = null;
	String projectName = null;
	IProject project = null;
	boolean dirty = false;
	/**
	 * Map of fileName, SPDXFile for all files in the SPDX document
	 */
	HashMap<String, SPDXFile> spdxFiles = new HashMap<String, SPDXFile>();
	
	/**
	 * Hashmap to map the SPDX license ID to friendly license names used in SPDX.
	 * For standard licenses, the ID and friendly name will be the same.
	 * For custom license text, a new extractedLicenseInfo will be added and
	 * the friendly name will be a user assigned name
	 */
	HashMap<String, String> availableLicenses = new HashMap<String, String>();
	/**
	 * All licenses found within the files
	 */
	HashSet<SPDXLicenseInfo> licensesFromFiles = new HashSet<SPDXLicenseInfo>();

	/**
	 * If any changes take place making this files out of sync with the SPDX
	 * document, this variable will be set to true
	 */
	private boolean fileRescanRequired = false;
	/**
	 * Array of Regex patterns of file names to exclude from the verification code
	 */
	Pattern[] excludedFilesPatterns = new Pattern[0];
	/**
	 * List of file directories to include in the scan
	 */
	String[] includedFileDirectories = new String[0];
	
	class BackgroundBuildJob extends WorkspaceJob {
		
		private int kind;

		public BackgroundBuildJob(int kind) {
			super("SPDX Build");
			this.kind = kind;
		}
		
		public int getKind() {
			return kind;
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor)
				throws CoreException {
			project.build(kind, SpdxBuilder.ID, new HashMap<String, String>(), monitor);
			return Status.OK_STATUS;
		}
		
		@Override
		public boolean belongsTo(Object id) {
			return id == SpdxProjectFactory.SPDX_BUILD_JOB_FAMILY;
		}
	}
	/**
	 * Background job to run builds when properties change
	 */
	BackgroundBuildJob buildJob = null;
	
	/**
	 * This should only be called from the SpdxProjectFactory class
	 * @param projectName
	 * @throws SpdxProjectException 
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	protected SpdxProject(String projectName) throws IOException, InvalidSPDXAnalysisException, SpdxProjectException {

		this.projectName = projectName;
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		// initialize the available licenses from SPDX licenses
		String[] stdLicenseIds = SPDXLicenseInfoFactory.getStandardLicenseIds();
		for (int i = 0; i < stdLicenseIds.length; i++) {
			this.availableLicenses.put(stdLicenseIds[i], stdLicenseIds[i]);
		}
		this.availableLicenses.put(SPDXLicenseInfoFactory.NOASSERTION_LICENSE_NAME, 
				SPDXLicenseInfoFactory.NOASSERTION_LICENSE_NAME);
		this.availableLicenses.put(SPDXLicenseInfoFactory.NONE_LICENSE_NAME, 
				SPDXLicenseInfoFactory.NONE_LICENSE_NAME);
		if (isCreated()) {
			String fileName;
			try {
				fileName = SpdxProjectProperties.getSpdxFileName(project);
			} catch (CoreException e) {
				Activator.getDefault().logError("Error getting SPDX file name property", e);
				throw(new SpdxProjectException("Error getting SPDX file name property", e));
			}
			IFile spdxResource = project.getFile(fileName);
			restoreSpdxDoc(spdxResource);
		} else {
			createDefaultSpdxDoc();
			addNature();
		}
	}

	private void addNature() throws SpdxProjectException {
	   try {
	      IProjectDescription description = project.getDescription();
	      String[] natures = description.getNatureIds();
	      boolean natureExists = false;
	      for (int i = 0; i < natures.length; i++) {
	    	  if (natures[i].equals(SpdxProjectNature.ID_FULLY_QUALIFIED)) {
	    		  natureExists = true;
	    		  break;
	    	  }
	      }
	      if (!natureExists) {
		      String[] newNatures = new String[natures.length + 1];
		      System.arraycopy(natures, 0, newNatures, 0, natures.length);
		      newNatures[natures.length] = SpdxProjectNature.ID_FULLY_QUALIFIED;
		      description.setNatureIds(newNatures);
		      project.setDescription(description, new NullProgressMonitor());
	      }

	   } catch (CoreException e) {
		   Activator.getDefault().logError("Error adding project nature", e);			   
		   throw(new SpdxProjectException("Error adding project nature.  See log for details.",e));
	   }
	}

	/**
	 * Creates an empty default SPDX document for the project using default information from
	 * the project properties and preferences
	 * @throws SpdxProjectException 
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	private void createDefaultSpdxDoc() throws IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		Model model = ModelFactory.createDefaultModel();
		this.spdxDoc = new SPDXDocument(model);
		this.spdxDoc.createSpdxAnalysis(getDefaultSpdxDocUrl());
		this.spdxDoc.createSpdxPackage();
	}

	private String getDefaultSpdxDocUrl() throws SpdxProjectException {
		try {
			return SpdxProjectProperties.getSpdxDocUrl(project);
		} catch (CoreException e) {
			throw(new SpdxProjectException("Unable to get project properties for the SPDX Document URL default.  See log for details.", e));
		}
	}

	private void restoreSpdxDoc(IFile spdxFile) throws SpdxProjectException {
		InputStream input = null;
		try {
			input = spdxFile.getContents();
			this.spdxDoc = SPDXDocumentFactory.createSpdxDocument(input, this.getDefaultSpdxDocUrl(), DEFAULT_FILE_TYPE);
		} catch (CoreException e) {
			Activator.getDefault().logError("Unable to get project SPDX file", e);
			throw(new SpdxProjectException("Unable to get project SPDX file", e));
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Error parsing SPDX document", e);
			throw(new SpdxProjectException("Error parsing SPDX document", e));
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					Activator.getDefault().logError("IO Error closing input spdx file", e);
				}
			}
		}
		ArrayList<String> verify = this.spdxDoc.verify();
		if (verify != null && verify.size() > 0) {
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append(verify.get(0));
			for (int i = 1; i < verify.size(); i++) {
				errorMsg.append('\n');
				errorMsg.append(verify.get(i));
			}
			Activator.getDefault().logError(errorMsg.toString(), new Exception("VerificationFailed"));
		}
		// add the extracted licenses to the available license list
		SPDXNonStandardLicense[] extractedLicenseInfos = null;
		try {
			extractedLicenseInfos = this.spdxDoc.getExtractedLicenseInfos();
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Error obtaining extracted license infos from files",e);
			throw(new SpdxProjectException("Unable to get extracted licensing infos from SPDX document.  See log for details.", e));
		}
		for (int i = 0; i < extractedLicenseInfos.length; i++) {
			String licenseId = extractedLicenseInfos[i].getId();
			String licenseName = extractedLicenseInfos[i].getLicenseName();
			if (licenseName == null || licenseName.trim().isEmpty()) {
				licenseName = licenseId;
			}
			this.availableLicenses.put(licenseId, licenseName);
			this.licensesFromFiles.add(extractedLicenseInfos[i]);
		}
		this.refreshExcluedFilePatterns();
		this.refreshIncludedFileDirectories();
		// add the files
		this.spdxFiles.clear();
		SPDXFile[] spdxFiles;
		try {
			spdxFiles = this.spdxDoc.getFileReferences();
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Error obtaining file information from SPDX document",e);
			throw(new SpdxProjectException("Unable to get file information from the SPDX document.  See log for details.", e));
		}
		for (int i = 0; i < spdxFiles.length; i++) {
			this.spdxFiles.put(spdxFiles[i].getName(), spdxFiles[i]);
		}
	}

	/**
	 * This should only be called from the SpdxProjectFactory class
	 * @throws SpdxProjectException 
	 */
	protected void close(IProgressMonitor monitor) throws SpdxProjectException {
		if (dirty) {
			this.save(monitor);
		}
	}

	/**
	 * @return true if the SPDX project information has already been initialized
	 */
	public boolean isCreated() {
		try {
			return SpdxProjectProperties.isSpdxInitialized(this.project);
		} catch (CoreException e) {
			return false;
		}
	}
	
	/**
	 * Initializes SpdxProject data
	 * @param spdxResource File resource to store the SPDX information
	 * @throws SpdxProjectException 
	 */
	public void create(IFile spdxResource, IProgressMonitor monitor) throws SpdxProjectException {
		if (isCreated()) {
			throw(new SpdxProjectException("Can not create project information - this project has already been created"));
		}
		PipedInputStream in = new PipedInputStream();

		try {
			final PipedOutputStream out = new PipedOutputStream(in);
			// need to write in a separate thread
			Runnable writer = new Runnable() {

				@Override
				public void run() {
					spdxDoc.getModel().write(out, "RDF/XML-ABBREV");
					try {
						out.close();
					} catch (IOException e) {
						Activator.getDefault().logError("IO Error closing SPDX pipe", e);
					}
				}
				
			};
			Thread writerThread = new Thread(writer);
			writerThread.start();
			spdxResource.create(in, true, monitor);
			writerThread.join();	// just to make sure it finishes
			SpdxProjectProperties.setSpdxFileName(project, spdxResource.getName());
			addExcluded(spdxResource.getName());
		} catch (CoreException e1) {
			throw(new SpdxProjectException("Unable to create SPDX file.", e1));
		} catch (IOException e) {
			throw(new SpdxProjectException("IO Error writing SPDX file.", e));
		} catch (InterruptedException e) {
			throw(new SpdxProjectException("Writing of the SPDX file was unexpectedly interrupted", e));
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				throw(new SpdxProjectException("Unable to close SPDX file.",e));
			}
		}
		try {
			SpdxProjectProperties.setSpdxInitialized(project, true);
		} catch (CoreException e) {
			throw(new SpdxProjectException("Unable to set the project properties to initialized.",e));
		}
	}
	
	private void addExcluded(String fileName) throws CoreException, SpdxProjectException {
		String escapedFileName = fileName.replaceAll("\\.", "\\.");
		SpdxProjectProperties.addExcludedFilePattern(project.getProject(), escapedFileName);
		this.refreshExcluedFilePatterns();
	}

	/**
	 * Save the SPDX information to the SPDX file stored in the project property
	 * @param monitor
	 * @throws SpdxProjectException
	 */
	public void save(IProgressMonitor monitor) throws SpdxProjectException {
		try {
			if (SpdxProjectProperties.isSpdxInitialized(project)) {
				IFile saveFile = project.getFile(SpdxProjectProperties.getSpdxFileName(project));
				saveAs(saveFile, monitor);
			}
		} catch (CoreException e) {
			Activator.getDefault().logError("Error getting SPDX Project Properties", e);
			throw(new SpdxProjectException("Error getting SPDX project properties"));
		}
	}
	
	/**
	 * Rebuild all SPDX information from the project resources
	 * @param monitor
	 * @throws CoreException
	 * @throws SpdxProjectException
	 */
	public void refresh(IProgressMonitor monitor) throws CoreException, SpdxProjectException {
		IFile spdxResource = project.getFile(SpdxProjectProperties.getSpdxFileName(project));
		refresh(spdxResource, monitor);
	}
	
	/**
	 * Rebuild all SPDX information from the project resources
	 * @param spdxResource Resource file containing the SPDX data
	 * @param monitor
	 * @throws SpdxProjectException
	 */
	public synchronized void refresh(IFile spdxResource, IProgressMonitor monitor) throws SpdxProjectException {
		IContainer root = project.getProject();
		refreshExcluedFilePatterns();
		refreshIncludedFileDirectories();
		SPDXDocument previousDoc = this.spdxDoc;	// save to copy over any values which we do not have properties for
		this.fileRescanRequired = true;
		spdxFiles.clear();
		licensesFromFiles.clear();
		availableLicenses.clear();
		try {
			this.createDefaultSpdxDoc();
		} catch (IOException e) {
			Activator.getDefault().logError("IO Error creating the default SPDX document during refresh", e);
			throw new SpdxProjectException("IO Error refreshing project.  See log for details.");
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("SPDX Error creating the default SPDX document during refresh", e);
			throw new SpdxProjectException("SPDX Error refreshing project.  See log for details.");
		}
		updateSpdxPackageFromProjectResources(monitor, previousDoc);
		updateSpdxFilesFromProjectResources(spdxResource.getName(), root, monitor);
	}


	/**
	 * Update all of the SPDX project level information from project properties
	 * @param monitor
	 * @param previousDoc Temporary parameter used to fill in any information not in properties
	 * @throws SpdxProjectException 
	 */
	private void updateSpdxPackageFromProjectResources(
			IProgressMonitor monitor, SPDXDocument previousDoc) throws SpdxProjectException {
		//TODO Remove the previous doc parameter and implement all as properties
		try {
			SPDXCreatorInformation previousCreator = previousDoc.getCreatorInfo();
			if (previousCreator != null) {
				SPDXCreatorInformation creator = new SPDXCreatorInformation(previousCreator.getCreators(), 
						previousCreator.getCreated(), previousCreator.getComment(), 
						previousCreator.getLicenseListVersion());
				// Note - we can not use the previousCreator directly since it will point to the old model
				this.spdxDoc.setCreationInfo(creator);
			}
			
			this.spdxDoc.setDataLicense(previousDoc.getDataLicense());
			String comment = previousDoc.getDocumentComment();
			if (comment != null && !comment.isEmpty()) {
				this.spdxDoc.setDocumentComment(comment);
			}
			
			SPDXReview[] previousReviewers = previousDoc.getReviewers();
			if (previousReviewers != null && previousReviewers.length > 0) {
				SPDXReview[] reviewers = new SPDXReview[previousReviewers.length];
				for (int i = 0; i < reviewers.length; i++) {
					reviewers[i] = new SPDXReview(previousReviewers[i].getReviewer(),
							previousReviewers[i].getReviewDate(), 
							previousReviewers[i].getComment());
				}
				this.spdxDoc.setReviewers(reviewers);
			}
			
			SPDXLicenseInfo concludedLicense = convertToLocalLicense(previousDoc, 
					previousDoc.getSpdxPackage().getConcludedLicenses());
			this.spdxDoc.getSpdxPackage().setConcludedLicenses(concludedLicense);
			String declaredCopyright = previousDoc.getSpdxPackage().getDeclaredCopyright();
			if (declaredCopyright != null && !declaredCopyright.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setDeclaredCopyright(declaredCopyright);
			}
			SPDXLicenseInfo declaredLicense = convertToLocalLicense(previousDoc, 
					previousDoc.getSpdxPackage().getDeclaredLicense());
			this.spdxDoc.getSpdxPackage().setDeclaredLicense(declaredLicense);
			String declaredName = previousDoc.getSpdxPackage().getDeclaredName();
			if (declaredName != null && !declaredName.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setDeclaredName(declaredName);
			}
			String description = previousDoc.getSpdxPackage().getDescription();	
			if (description != null && !description.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setDescription(description);			
			}
			String url = previousDoc.getSpdxPackage().getDownloadUrl();	
			if (url != null && !url.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setDownloadUrl(url);
			}
			String fileName = previousDoc.getSpdxPackage().getFileName();	
			if (fileName != null && !fileName.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setFileName(fileName);
			}
			String homePage = previousDoc.getSpdxPackage().getHomePage();	
			if (homePage != null && !homePage.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setHomePage(homePage);
			}
			String licenseComment = previousDoc.getSpdxPackage().getLicenseComment();	
			if (licenseComment != null && !licenseComment.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setLicenseComment(licenseComment);
			}
			String originator = previousDoc.getSpdxPackage().getOriginator();	
			if (originator != null && !originator.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setOriginator(originator);
			}
			String sha1 = previousDoc.getSpdxPackage().getSha1();	
			if (sha1 != null && !sha1.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setSha1(sha1);
			}
			String shortDescription = previousDoc.getSpdxPackage().getShortDescription();	
			if (shortDescription != null && !shortDescription.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setShortDescription(shortDescription);
			}
			String sourceInfo = previousDoc.getSpdxPackage().getSourceInfo();	
			if (sourceInfo != null && !sourceInfo.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setSourceInfo(sourceInfo);
			}
			String supplier = previousDoc.getSpdxPackage().getSupplier();	
			if (supplier != null && !supplier.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setSupplier(supplier);
			}
			String versionInfo = previousDoc.getSpdxPackage().getVersionInfo();	
			if (versionInfo != null && !versionInfo.isEmpty()) {
				this.spdxDoc.getSpdxPackage().setVersionInfo(versionInfo);
			}	
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("SPDX error updating project information from previous SPDX document", e);
			throw(new SpdxProjectException("SPDX error updating project information from previous SPDX document. See log for details.",e));
		}
	}

	/**
	 * Convert license from a different SPDX document into an SPDX document which
	 * works for this SPDX document.  
	 * @param originalDoc Document containing the original license
	 * @param license original license
	 * @return
	 * @throws SpdxProjectException 
	 */
	protected SPDXLicenseInfo convertToLocalLicense(SPDXDocument originalDoc,
			SPDXLicenseInfo license) throws SpdxProjectException {
		if (license == null) {
			return new SpdxNoAssertionLicense();
		}
		if (license instanceof SPDXLicenseSet) {
			SPDXLicenseInfo[] originalMembers = ((SPDXLicenseSet)license).getSPDXLicenseInfos();
			SPDXLicenseInfo[] members = new SPDXLicenseInfo[originalMembers.length];
			for (int i = 0; i < members.length; i++) {
				members[i] = convertToLocalLicense(originalDoc, originalMembers[i]);
			}
			if (license instanceof SPDXConjunctiveLicenseSet) {
				return new SPDXConjunctiveLicenseSet(members);
			} else if (license instanceof SPDXDisjunctiveLicenseSet) {
				return new SPDXDisjunctiveLicenseSet(members);
			} else {
				throw(new SpdxProjectException("Unknown license set type - can not convert "+license.toString()));
			}
		} else if (license instanceof SPDXNonStandardLicense) {
			SPDXNonStandardLicense origLicense = (SPDXNonStandardLicense)license;
			SPDXNonStandardLicense retval = this.getNonStdLicenseWithText(origLicense.getText());
			if (retval == null) {
				String licenseId = this.getNextAvailableLicenseId();
				retval = new SPDXNonStandardLicense(licenseId, origLicense.getText(),
						origLicense.getLicenseName(), origLicense.getSourceUrls(),
						origLicense.getComment());
				this.addLicense(retval);
			}
			return retval;
		} else {
			return license;	// no need to convert other license types
		}
	}

	private synchronized void refreshIncludedFileDirectories() throws SpdxProjectException {
		try {
			includedFileDirectories = SpdxProjectProperties.getIncludedResourceDirectories(project.getProject());
		} catch (CoreException e) {
			Activator.getDefault().logError("Error getting included resources properties", e);
			throw(new SpdxProjectException("Error getting included resources properties",e));
		}
	}

	/**
	 * Refresh the excluded file patterns from the project properties
	 * @throws SpdxProjectException 
	 */
	private synchronized void refreshExcluedFilePatterns() throws SpdxProjectException {
		String[] excludedFilesPatternRegexes;
		try {
			excludedFilesPatternRegexes = SpdxProjectProperties.getExcludedFilePatterns(project.getProject());
		} catch (CoreException e) {
			Activator.getDefault().logError("Error getting excluded files properties", e);
			throw(new SpdxProjectException("Error getting excluded files properties",e));
		}
		excludedFilesPatterns = new Pattern[excludedFilesPatternRegexes.length];
		for (int i = 0; i < excludedFilesPatternRegexes.length; i++) {
			excludedFilesPatterns[i] = Pattern.compile(excludedFilesPatternRegexes[i]);
		}
	}

	public synchronized void saveAs(IFile spdxResource, IProgressMonitor monitor) throws SpdxProjectException {
		if (this.fileRescanRequired) {
			refresh(spdxResource, monitor);
		}
		PipedInputStream in = new PipedInputStream();

		try {
			final PipedOutputStream out = new PipedOutputStream(in);
			// need to write in a separate thread
			Runnable writer = new Runnable() {

				@Override
				public void run() {
					spdxDoc.getModel().write(out, DEFAULT_FILE_TYPE);
					try {
						out.close();
					} catch (IOException e) {
						Activator.getDefault().logError("IO Error closing SPDX pipe", e);
					}
				}
				
			};
			Thread writerThread = new Thread(writer);
			writerThread.start();
			spdxResource.setContents(in, true, false, monitor);
			writerThread.join();	// just to make sure it finishes
			this.dirty = false;
		} catch (CoreException e1) {
			throw(new SpdxProjectException("Unable to create SPDX file.", e1));
		} catch (IOException e) {
			throw(new SpdxProjectException("IO Error writing SPDX file.", e));
		} catch (InterruptedException e) {
			throw(new SpdxProjectException("Writing of the SPDX file was unexpectedly interrupted", e));
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				throw(new SpdxProjectException("Unable to close SPDX file.",e));
			}
		}
	}

	/**
	 * @return List of defined license names available to the SPDX project.  This includes
	 * both standard license names and license text added specifically for this project
	 */
	public synchronized String[] getAvailableLicenseNames() {
		return this.availableLicenses.values().toArray(new String[this.availableLicenses.keySet().size()]);
	}

	public SPDXDocument getSpdxDoc() {
		return this.spdxDoc;
	}
	
	public IProject getProject() {
		return this.project;
	}

	/**
	 * Called when a property has been changed
	 * @param property Unqualified property name
	 * @param oldValue
	 * @param newValue
	 * @throws SpdxProjectException 
	 */
	public void propertyChange(IResource resource, String property,
			final String oldValue, final String newValue) throws SpdxProjectException {
		if (oldValue != null && oldValue.equals(newValue)) {
			return;	// value didn't really change
		}
		if (property.equals(PropertyConstants.PROP_EXCLUDED_FILE_PATTERNS)) {
			this.refreshExcluedFilePatterns();
			this.fileRescanRequired  = true;	// long running operation to get back in sync, mark for future run
			backgroundFullBuild(IncrementalProjectBuilder.FULL_BUILD);
		} else if (property.equals(PropertyConstants.PROP_INCLUDED_RESOURCE_DIRECTORIES)) {
			this.refreshIncludedFileDirectories();
			this.fileRescanRequired = true;
			backgroundFullBuild(IncrementalProjectBuilder.FULL_BUILD);
		} else if (property.equals(PropertyConstants.PROP_SPDX_DEFAULT_FILE_COPYRIGHT)) {
			// nothing to do here - leaving this in the code as a placeholder
		} else if (property.equals(PropertyConstants.PROP_SPDX_DEFAULT_FILE_LICENSE)) {
			// nothing to do here - leaving this in the code as a placeholder
		} else if (property.equals(PropertyConstants.PROP_SPDX_DOC_URL)) {
			// Not currently supported - there is no way to update the Document URI
			Activator.getDefault().logError("Unsupported property change - SPDX Document URL", new SpdxProjectException("Unsupported property change - SPDX Document URL"));
		} else if (property.equals(PropertyConstants.PROP_SPDX_FILE_NAME)) {
			// attempt to rename the file
			renameSpdxFile(oldValue, newValue);
		} else if (property.equals(PropertyConstants.PROP_SPDX_FILE_ARTIFACT_OF_URL)) {
			updateFileArtfactOfUrl(resource, oldValue, newValue);
		} else if (property.equals(PropertyConstants.PROP_SPDX_FILE_ARTIFACT_OF_PROJECT)) {
			updateFileArtfactOfProject(resource, oldValue, newValue);
		} else if (property.equals(PropertyConstants.PROP_SPDX_FILE_COPYRIGHT)) {
			updateFileCopyright(resource, oldValue, newValue);
		} else if (property.equals(PropertyConstants.PROP_SPDX_FILE_CONCLUDED_LICENSE)) {
			updateFileConcludedLicense(resource, oldValue, newValue);
		}
	}

	private void updateFileConcludedLicense(IResource resource,
			String oldValue, String newValue) throws SpdxProjectException {
		String filePath = resource.getProjectRelativePath().toString();
		SPDXFile file = this.spdxFiles.get(filePath);
		if (file == null) {
			return;
		}
		try {
			file.setConcludedLicenses(SPDXLicenseInfoFactory.parseSPDXLicenseString(newValue));
			this.dirty = true;
		} catch (InvalidLicenseStringException e) {
			Activator.getDefault().logError("Invalid license string for file update: "+newValue, e);
			throw(new SpdxProjectException("Invalid license string: "+e.getMessage()));
		}
	}

	private void updateFileCopyright(IResource resource, String oldValue,
			String newValue) {
		String filePath = resource.getProjectRelativePath().toString();
		SPDXFile file = this.spdxFiles.get(filePath);
		if (file == null) {
			return;
		}
		file.setCopyright(newValue);
		this.dirty = true;
	}

	private void updateFileArtfactOfProject(IResource resource,
			String oldProjectName, String newProjectName) {
		String filePath = resource.getProjectRelativePath().toString();
		SPDXFile file = this.spdxFiles.get(filePath);
		if (file == null) {
			return;
		}
		DOAPProject[] artifactOfs = file.getArtifactOf();
		if (artifactOfs == null || artifactOfs.length == 0) {
			// create a new ArtifactOfs for this project
			DOAPProject project = new DOAPProject(newProjectName, "");
			artifactOfs = new DOAPProject[] {project};
			file.setArtifactOf(artifactOfs);
			dirty = true;
		} else if (artifactOfs.length == 1) {
			artifactOfs[0].setName(newProjectName);
		} else {
			DOAPProject project = null;
			for (int i = 0; i < artifactOfs.length; i++) {
				if (artifactOfs[i].getName().equals(oldProjectName)) {
					project = artifactOfs[i];
				}
			}
			if (project != null) {
				project.setName(newProjectName);
			} else {
				DOAPProject[] updatedArtifactOfs = new DOAPProject[artifactOfs.length+1];
				updatedArtifactOfs[0] = new DOAPProject(newProjectName, "");
				for (int j = 0; j < artifactOfs.length; j++) {
					updatedArtifactOfs[j+1] = artifactOfs[j];
				}
				file.setArtifactOf(updatedArtifactOfs);
				this.dirty = true;
			}
		}
	}

	private void updateFileArtfactOfUrl(IResource resource, String oldUrl,
			String newUrl) {
		String filePath = resource.getProjectRelativePath().toString();
		SPDXFile file = this.spdxFiles.get(filePath);
		if (file == null) {
			return;
		}
		DOAPProject[] artifactOfs = file.getArtifactOf();
		if (artifactOfs == null || artifactOfs.length == 0) {
			// create a new ArtifactOfs for this project
			DOAPProject project = new DOAPProject("TEMPNAME", newUrl);
			artifactOfs = new DOAPProject[] {project};
			file.setArtifactOf(artifactOfs);
			this.dirty = true;
		} else if (artifactOfs.length == 1) {
			artifactOfs[0].setHomePage(newUrl);
		} else {
			DOAPProject project = null;
			for (int i = 0; i < artifactOfs.length; i++) {
				if (artifactOfs[i].getHomePage().equals(oldUrl)) {
					project = artifactOfs[i];
				}
			}
			if (project != null) {
				project.setHomePage(oldUrl);
			} else {
				DOAPProject[] updatedArtifactOfs = new DOAPProject[artifactOfs.length+1];
				updatedArtifactOfs[0] = new DOAPProject("TEMPNAME", newUrl);
				for (int j = 0; j < artifactOfs.length; j++) {
					updatedArtifactOfs[j+1] = artifactOfs[j];
				}
				file.setArtifactOf(updatedArtifactOfs);
				this.dirty = true;
			}
		}
	}

	private void renameSpdxFile(final String oldFileName, final String newFileName) throws SpdxProjectException {
		final IFile oldFile = this.project.getFile(oldFileName);
		if (oldFile.exists() && !oldFileName.equals(newFileName)) {
			final IFile newFile = this.project.getFile(newFileName);
			if (newFile.exists()) {
				throw(new SpdxProjectException("Can not rename SPDX file - new file name already exists"));
			} 
			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						try {
							oldFile.move(newFile.getFullPath(), true, monitor);
						} catch (CoreException e) {
							throw(new InvocationTargetException(e));
						}

					}
					
				});
			} catch (InvocationTargetException e) {
				Activator.getDefault().logError("Unable to rename SPDX file", e);
				throw(new SpdxProjectException("Unable to rename SPDX file"));
			} catch (InterruptedException e) {
				throw(new SpdxProjectException("Renaming of SPDX file cancelled by user"));
			}
		}

	}

	protected synchronized void updateSpdxFilesFromProjectResources(String spdxFileName, 
			IContainer root, IProgressMonitor monitor) throws SpdxProjectException {

		int numFiles = 0;
		for (int i = 0; i < includedFileDirectories.length; i++) {
			IContainer folder = null;
			if (includedFileDirectories[i].equals("/") || includedFileDirectories[i].equals("\\") || includedFileDirectories[i].isEmpty()) {
				folder = root;
			} else {
				Path path = new Path(includedFileDirectories[i]);
				folder = root.getFolder(path);
			}
			if (folder.exists()) {
				try {
					numFiles = numFiles + countFiles(folder, excludedFilesPatterns);
				} catch (CoreException e) {
					Activator.getDefault().logError("Error counting files", e);
					throw(new SpdxProjectException("Error counting files", e));
				}
			}
		}
		monitor.beginTask("Adding files to SPDX", numFiles);
		for (int i = 0; i < includedFileDirectories.length; i++) {
			IContainer folder = null;
			if (includedFileDirectories[i].equals("/") || includedFileDirectories[i].equals("\\") || includedFileDirectories[i].isEmpty()) {
				folder = root;
			} else {
				Path path = new Path(includedFileDirectories[i]);
				folder = root.getFolder(path);
			}
			if (folder.exists()) {
				try {
					collectFilesInformation(folder, monitor);
				} catch (CoreException e) {
					Activator.getDefault().logError("Error collecting file information files", e);
					throw(new SpdxProjectException("Error collecting file information files", e));
				}
			}
		}	
		try {
			getSpdxDoc().getSpdxPackage().setFiles(spdxFiles.values().toArray(new SPDXFile[spdxFiles.size()]));
			getSpdxDoc().getSpdxPackage().setLicenseInfoFromFiles(licensesFromFiles.toArray(new SPDXLicenseInfo[licensesFromFiles.size()]));
			updateVerificationCodeFromFiles();
			this.fileRescanRequired = false;
			this.dirty = true;
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxProjectException("Error adding files to SPDX document: "+e.getMessage(), e));
		} 
	}
	private void collectFilesInformation(IContainer folder, IProgressMonitor monitor) throws CoreException, SpdxProjectException {
		if (monitor.isCanceled()) {
			return;
		}
		IResource[] children = folder.members();
		for (int i = 0; i < children.length; i++) {
			if (monitor.isCanceled()) {
				return;
			}
			if (!isExcluded(children[i].getName())) {
				if (children[i] instanceof IFile) {
					IFile child = (IFile)children[i];
					SPDXFile file = SpdxHelper.convertFile(child);
					this.spdxFiles.put(child.getProjectRelativePath().toString(), file);
					SPDXLicenseInfo[] seenLicenses = file.getSeenLicenses();
					for (int j = 0; j < seenLicenses.length; j++) {
						licensesFromFiles.add(seenLicenses[j]);
					}
					monitor.worked(1);
				} else if (children[i] instanceof IFolder) {
					IFolder childFolder = (IFolder)children[i];
					collectFilesInformation(childFolder, monitor);
				}
			}
		}
	}

	private boolean isExcluded(String name) {
		for (int i = 0; i < this.excludedFilesPatterns.length; i++) {
			Matcher matcher = this.excludedFilesPatterns[i].matcher(name);
			if (matcher.matches()) {
				return true;
			}
		}
		return false;
	}
	
	
	private int countFiles(IContainer folder, Pattern[] excludedFilesPattern) throws CoreException {
		int retval = 0;
		IResource[] children = folder.members();
		for (int i = 0; i < children.length; i++) {
			if (!isExcluded(children[i].getName())) {
				if (children[i] instanceof IFile) {
					retval++;
				} else if (children[i] instanceof IFolder) {
					IFolder childFolder = (IFolder)children[i];
					retval = retval + countFiles(childFolder, excludedFilesPattern);
				}
			}
		}
		return retval;
	}
	
	private SpdxPackageVerificationCode calculatePackageVerificationCode(
			Collection<SPDXFile> spdxFiles,
			ArrayList<String> excludedFileNamesFromVerificationCode) throws NoSuchAlgorithmException {
		ArrayList<String> fileChecksums = new ArrayList<String>();
		Iterator<SPDXFile> iter = spdxFiles.iterator();
		while (iter.hasNext()) {
			SPDXFile file = iter.next();
			if (includeInVerificationCode(file.getName(), excludedFileNamesFromVerificationCode)) {
				fileChecksums.add(file.getSha1());
			}
		}
		Collections.sort(fileChecksums);
		MessageDigest verificationCodeDigest = MessageDigest.getInstance("SHA-1");
		for (int i = 0;i < fileChecksums.size(); i++) {
			byte[] hashInput = fileChecksums.get(i).getBytes(Charset.forName("UTF-8"));
			verificationCodeDigest.update(hashInput);
		}
		String value = SpdxHelper.convertChecksumToString(verificationCodeDigest.digest());
		return new SpdxPackageVerificationCode(value, excludedFileNamesFromVerificationCode.toArray(
				new String[excludedFileNamesFromVerificationCode.size()]));
	}

	private boolean includeInVerificationCode(String name, ArrayList<String> excludedFileNamesFromVerificationCode) {
		for (int i = 0; i < excludedFileNamesFromVerificationCode.size(); i++) {
			if (excludedFileNamesFromVerificationCode.get(i).equals(name)) {
				return false;
			}
		}
		return true;
	}

	public synchronized String getNextAvailableLicenseId() {
		return this.spdxDoc.getNextLicenseRef();
	}

	public synchronized boolean verifyNewLicenseId(String licenseId) {
		String idError = SpdxVerificationHelper.verifyNonStdLicenseid(licenseId);
		if (idError != null && !idError.isEmpty()) {
			return false;
		}
		return !this.availableLicenses.containsKey(licenseId);
	}
	
	public SPDXNonStandardLicense getNonStdLicenseWithText(String text) throws SpdxProjectException {
		SPDXNonStandardLicense retval = null;
		SPDXNonStandardLicense[] availableLicenses = null;
		try {
			availableLicenses = this.spdxDoc.getExtractedLicenseInfos();
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Error getting extracted licensing infos", e);
			throw(new SpdxProjectException("Unable to retrieve existing license information from SPDX document.  See log for details.",e));
		}
		for (int i = 0; i < availableLicenses.length; i++) {
			if (LicenseCompareHelper.isLicenseTextEquivalent(text, availableLicenses[i].getText())) {
				retval = availableLicenses[i];
				break;
			}
		}
		return retval;
	}

	public synchronized void addLicense(SPDXNonStandardLicense license) throws SpdxProjectException {
		try {
			this.spdxDoc.addNewExtractedLicenseInfo(license);
			this.dirty = true;
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Error adding non standard license", e);
			throw(new SpdxProjectException("Error adding SPDX license to SPDX document: "+e.getMessage(), e));

		}
		String licenseId = license.getId();
		String licenseName = license.getLicenseName();
		if (licenseName == null || licenseName.isEmpty()) {
			licenseName = licenseId;
		}
		this.availableLicenses.put(licenseId, licenseName);
	}

	/**
	 * Set the rescan required flag
	 * @param rescanRequired True if a complete refresh and rescan is required for an accurate SPDX file
	 */
	public synchronized void setFileRescanRequired(boolean rescanRequired) {
		this.fileRescanRequired = rescanRequired;
	}

	/**
	 * Returns true if the file should be included based on the included directories and skipped file patterns
	 * @param file
	 * @return
	 */
	public synchronized boolean fileShouldBeIncluded(IFile file) {
		boolean inIncludedDirectory = false;
		for (int i = 0; i < this.includedFileDirectories.length; i++) {
			IContainer folder = null;
			if (includedFileDirectories[i].equals("/") || includedFileDirectories[i].equals("\\") || includedFileDirectories[i].isEmpty()) {
				folder = project;
			} else {
				Path path = new Path(includedFileDirectories[i]);
				folder = project.getFolder(path);
			}
			if (folder.getFullPath().matchingFirstSegments(file.getFullPath()) == folder.getFullPath().segmentCount()) {
				inIncludedDirectory = true;
				break;
			}
		}
		if (inIncludedDirectory) {
			return !this.isExcluded(file.getName());
		} else {
			return false;
		}
	}

	/**
	 * Adds a new file to the SPDX analysis
	 * @param file
	 * @param monitor
	 * @throws SpdxProjectException 
	 */
	public synchronized void addFile(IFile file, IProgressMonitor monitor) throws SpdxProjectException {
		if (!isExcluded(file.getName())) {
			String relativePath = file.getProjectRelativePath().toString();
			if (this.spdxFiles.containsKey(relativePath)) {
				throw(new SpdxProjectException("Can not add file "+file.getName()+".  File already exists in analysis.  Use UpdateFile to update the file information."));
			}
			SPDXFile spdxFile = SpdxHelper.convertFile(file);
			try {
				this.spdxDoc.getSpdxPackage().addFile(spdxFile);
				this.dirty = true;
			} catch (InvalidSPDXAnalysisException e) {
				Activator.getDefault().logError("Error adding file "+file.getName(), e);
				throw(new SpdxProjectException("SPDX error adding file "+file.getName()));
			}
			this.spdxFiles.put(relativePath, spdxFile);
			updateVerificationCodeFromFiles();
			SPDXLicenseInfo[] seenLicenses = spdxFile.getSeenLicenses();
			for (int j = 0; j < seenLicenses.length; j++) {
				licensesFromFiles.add(seenLicenses[j]);
			}
		}		
	}
	
	

	protected void updateVerificationCodeFromFiles() throws SpdxProjectException {
		ArrayList<String> excludedFileNamesFromVerificationCode = new ArrayList<String>();
		String spdxFileName;
		try {
			spdxFileName = SpdxProjectProperties.getSpdxFileName(project);
		} catch (CoreException e) {
			Activator.getDefault().logError("Error getting SPDX file name property", e);
			throw(new SpdxProjectException("Unable to access SPDX file name property"));
		}
		if (this.spdxFiles.containsKey(spdxFileName)) {
			excludedFileNamesFromVerificationCode.add(spdxFileName);
		}			
		SpdxPackageVerificationCode verificationCode;
		try {
			verificationCode = this.calculatePackageVerificationCode(spdxFiles.values(), excludedFileNamesFromVerificationCode);
		} catch (NoSuchAlgorithmException e) {
			Activator.getDefault().logError("Error calculating verification code", e);
			throw(new SpdxProjectException("Unable to calculate verification code"));
		}
		try {
			this.spdxDoc.getSpdxPackage().setVerificationCode(verificationCode);
			this.dirty = true;
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("SPDX Error updating verification code", e);
			throw(new SpdxProjectException("Unable to update verification code"));
		}
	}

	/**
	 * Updates the information for a file
	 * @param file
	 * @param monitor
	 * @throws SpdxProjectException 
	 */
	public void updateFile(IFile file, IProgressMonitor monitor) throws SpdxProjectException {
		String relativePath = file.getProjectRelativePath().toString();
		if (isExcluded(relativePath)) {
			return;
		}
		SPDXFile origFile = this.spdxFiles.get(relativePath);
		if (origFile == null) {
			throw(new SpdxProjectException("Can not update file "+file.getName()+".  File does not exist."));
		}
		SPDXFile spdxFile = SpdxHelper.convertFile(file);
		origFile.setArtifactOf(spdxFile.getArtifactOf());
		origFile.setComment(spdxFile.getComment());
		origFile.setConcludedLicenses(spdxFile.getConcludedLicenses());
		origFile.setContributors(spdxFile.getContributors());
		origFile.setCopyright(spdxFile.getCopyright());
		try {
			origFile.setFileDependencies(spdxFile.getFileDependencies());
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Error updating file dependencies for "+file.getName(), e);
			throw(new SpdxProjectException("Can not update file "+file.getName()+".  Invalid file dependencies."));
		}
		origFile.setLicenseComments(spdxFile.getLicenseComments());
		origFile.setNoticeText(spdxFile.getNoticeText());
		origFile.setSeenLicenses(spdxFile.getSeenLicenses());
		origFile.setSha1(spdxFile.getSha1());
		try {
			origFile.setType(spdxFile.getType());
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Error updating file type for "+file.getName(), e);
			throw(new SpdxProjectException("Can not update file "+file.getName()+".  Invalid file type."));
		}
		updateVerificationCodeFromFiles();	
		this.dirty = true;
	}

	/**
	 * Removes a file from the SPDX analysis
	 * @param file
	 * @param monitor
	 * @throws SpdxProjectException 
	 */
	public synchronized void removeFile(IFile file, IProgressMonitor monitor) throws SpdxProjectException {
		String relativePath = file.getProjectRelativePath().toString();
		if (!this.spdxFiles.containsKey(relativePath)) {
			return;
		}
		this.spdxFiles.remove(relativePath);
		try {
			this.spdxDoc.getSpdxPackage().removeFile(relativePath);
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Error removing file "+file.getName(), e);
			throw(new SpdxProjectException("SPDX error removing file "+file.getName()));
		}
		updateVerificationCodeFromFiles();	
		this.dirty = true;
	}

	/**
	 * @return true if a file rescan is required
	 */
	public synchronized boolean isFileRescanRequired() {
		return this.fileRescanRequired;
	}
	
	/**
	 * Invokes a background build for the project
	 * @param kind
	 * @return buildJob Job created which should be scheduled or running.  Returned so that the call can join on the job completion.
	 */
	public WorkspaceJob backgroundFullBuild(final int kind) {
		if (buildJob != null) {		
			if (buildJob.getResult() == null && buildJob.getKind() == IncrementalProjectBuilder.FULL_BUILD) {
				buildJob.cancel();
			}
			try {
				buildJob.join();
			} catch (InterruptedException e) {
				// ignore
			}
		}
		this.buildJob = new BackgroundBuildJob(kind);
//		buildJob.setRule(this.project);		 - Not needed since the build function does a runInWorkspace command for the project.  Uncommenting will cause a rule conflict error
		buildJob.schedule();
		return buildJob;
	}

	/**
	 * Set the directories for files to be included in the SPDX document
	 * @param includedFileDirectories
	 */
	public synchronized void setIncludedFileDirectories(String[] includedFileDirectories) {
		this.includedFileDirectories = includedFileDirectories;
	}

	/**
	 * Set the list of Patterns to be skipped/ignored - these files will not be included in the SPDX project
	 * @param excludedFilesPatterns
	 */
	public synchronized void setExcludedFilePatterns(Pattern[] excludedFilesPatterns) {
		this.excludedFilesPatterns = excludedFilesPatterns;
	}
	
	public void waitForBuildJob() {
		if (this.buildJob != null) {
			try {
				this.buildJob.join();
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	public void disableSpdx() throws CoreException, SpdxProjectException {
		SpdxProjectProperties.setSpdxInitialized(project, false);
	}
}
