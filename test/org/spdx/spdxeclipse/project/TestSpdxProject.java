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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXConjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDisjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SPDXStandardLicense;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxeclipse.properties.InvalidExcludedFilePattern;
import org.spdx.spdxeclipse.properties.SpdxFileProperties;
import org.spdx.spdxeclipse.properties.SpdxProjectProperties;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestSpdxProject {
	
	static DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);

	static final String TEST_PROJECT_NAME = "TestProject";

	static final String TEST_SPDX_FILE_NAME = "spdxFile.spdx";

	static final String TEST_DOC_COMMENT = "Test Document Comment";
	
	static final String DEFAULT_DATA_LICENSE = "CC0-1.0";

	static final String COPYRIGHT_STRING = "Copyright 2012, Source Auditor Inc.";

	private static final String SPDX_PACKAGE_NAME = "Package Name";

	private static final String SPDX_PACKAGE_VERSION = "1.0.0";

	private static final String SPDX_PACKAGE_COPYRIGHT = "Copyright 2013, Source Auditor Inc.";

	private static final String SPDX_PACKAGE_ORIGINATOR = "Organization: Test Originator";

	private static final String SPDX_PACKAGE_SUPPLIER = "Person: Test Supplier";

	private static final String SPDX_PACKAGE_SOURCE_INFO = "Test Source Info";

	private static final String SPDX_PACKAGE_ARCHIVE_FILE_NAME = "ArchiveFile.tgz";

	private static final String SPDX_PACKAGE_SHA1 = "2fd4e1c67a2d28fced849ee1bb76e7381b93eb12";

	private static final String SPDX_PACKAGE_DOWNLOAD_URL = "http://donwloadurl.org";

	private static final String SPDX_PACKAGE_SHORT_DESC = "Test short description";

	private static final String SPDX_PACKAGE_DESCRIPTION = "Test long description";

	private static final String SPDX_PACKAGE_DECLARED_LICENSE = "Apache-2.0";

	private static final String SPDX_PACKAGE_CONCLUDED_LICENSE = "Apache-1.1";

	private static final String CREATOR_COMMENT = "Test Creator Comment";
	
	private static final String LICENSE_LIST_VERSION = "1.19";

	private static final String[] SPDX_CREATORS = new String[] {"Tool: One creator", "Organization: two creators"};

	private static final String[] SPDX_REVIEWERS = new String[] {"Person: Reviewer1", "Person: Reviewer2"};

	private static final String[] SPDX_REVIEWER_COMMENTS = new String[] {"Reviwer Comment1", "Reviewer comment2"};

	private static final String SPDX_PACKAGE_VERIFICATION_CODE_SHA1 = "3fd4e1c67a2d28fced849ee1bb76e7381b93eb12";

	private static final String SOURCE_DIR = "src";
	
	@SuppressWarnings("unused")
	private static final String BIN_DIR = "bin";

	private static final String RESOURCE_FILENAME1 = "FileName1";

	private static final String FILE_COPYRIGHT_STRING = "Copyright (c) 2015, Source Auditor Inc.";

	private static final String FILE_DEFAULT_LICENSE = "Apache-1.1";

	private static final String[] DEFAULT_EXCLUDED_FILE_PATTERNS = new String[] {".*\\.skip", ".*\\.project", ".*\\.spdx"};

	private static final String[] DEFAULT_INCLUDED_DIRECTORIES = new String[] {"src"};

	private static final String[] FILE_PATHS = new String[] {
		"src/srcfiles1.c", "src/srcfiles2.c", "src/srcfiles3.skip", 	// 2 source files
		"src/subdir/srcfiles4.php", "src/subdir/srcfiles5.php", 		// 2 source files
		"bin/binfile.a", "bin/binfile2.a", "bin/subdir/binefile3.a",
		"attrootfile1.c", "attrootfile2.c", "attrootfile3.skip"
	};
	
	private static final int NUM_NON_SKIPPED_SOURCE_FILES = 4;
	private static final int NUM_SOURCE_FILES = 5;
	@SuppressWarnings("unused")
	private static final int NUM_BIN_FILES = 3;
	private static final int NUM_NON_SKIPPED_NON_PHP = 2;
	private static final int NUM_NON_SKIPPED_SOURCE_IN_SUBDIR = 2;
	private static final int NUM_NON_SKIPPED_FILES_IN_ALL = 9;
	private static final int NUM_TOTAL_FILES = 11;

	private static final Pattern[] SKIPPED_FILES_PATTERNS = new Pattern[DEFAULT_EXCLUDED_FILE_PATTERNS.length];
	static {
		for (int i = 0;i < DEFAULT_EXCLUDED_FILE_PATTERNS.length; i++) {
			SKIPPED_FILES_PATTERNS[i] = Pattern.compile(DEFAULT_EXCLUDED_FILE_PATTERNS[i]);
		}
	}

	
	private IProject projectResource = null;
	private SpdxProject spdxProject = null;
	private IProgressMonitor nullMonitor = new NullProgressMonitor();
	private IFile spdxSaveFile = null;
	
	@Before
	public void setUp() throws Exception {
	
	}

	@After
	public void tearDown() throws Exception {
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);	// sync up with the build process
		Job.getJobManager().join(SpdxProjectFactory.SPDX_BUILD_JOB_FAMILY, null);
		if (this.spdxProject != null) {
			this.spdxProject.waitForBuildJob();
		}
		SpdxProjectFactory.closeAllSpdxProjects();
		if (this.projectResource != null && this.projectResource.isOpen()) {
			this.projectResource.close(nullMonitor);
			this.projectResource = null;
		}
		this.spdxProject = null;
		// delete all projects that were created
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (int i = 0; i < projects.length; i++) {
			projects[i].delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
	}
	
	private void openResourceProject() throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		this.projectResource = root.getProject(TEST_PROJECT_NAME);
		if (!this.projectResource.exists()) {
			this.projectResource.create(nullMonitor);
		}
		if (!this.projectResource.isOpen()) {
			this.projectResource.open(nullMonitor);
		}
	}

	@Test
	public void testSpdxProject() throws IOException, InvalidSPDXAnalysisException, SpdxProjectException, CoreException {
		openResourceProject();
		try {
			spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			assertEquals(TEST_PROJECT_NAME, spdxProject.getProject().getName());
		} finally {
			if (spdxProject != null) {
				spdxProject.close(nullMonitor);
			}
		}
	}

	@Test
	public void testCreate() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		openResourceProject();
		try {
			spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			this.spdxSaveFile = projectResource.getFile(TEST_SPDX_FILE_NAME);
			assertFalse(spdxProject.isCreated());
			spdxProject.create(spdxSaveFile, nullMonitor);
			spdxProject.isCreated();
			assertTrue(spdxProject.isCreated());
		} finally {
			projectResource.close(nullMonitor);
		}
	}
	
	public void createSpdxProject() throws IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
		this.spdxSaveFile = projectResource.getFile(TEST_SPDX_FILE_NAME);
		spdxProject.create(spdxSaveFile, nullMonitor);
	}

	@Test
	public void testSaveAs() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		openResourceProject();
		try {
			SpdxProject spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			this.spdxSaveFile = projectResource.getFile(TEST_SPDX_FILE_NAME);
			spdxProject.create(spdxSaveFile, nullMonitor);
			spdxProject.getSpdxDoc().setDocumentComment(TEST_DOC_COMMENT);
			spdxProject.saveAs(spdxSaveFile, nullMonitor);
			SPDXDocument docUnderTest = SPDXDocumentFactory.creatSpdxDocument(spdxSaveFile.getRawLocation().toOSString());
			assertEquals(TEST_DOC_COMMENT, docUnderTest.getDocumentComment());
		} finally {
			projectResource.close(nullMonitor);
		}
	}

	@Test
	public void testGetAvailableLicenseNames() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		openResourceProject();
		try {
			this.spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			String[] licenseNames = spdxProject.getAvailableLicenseNames();
			String[] licenseIds = SPDXLicenseInfoFactory.getStandardLicenseIds();
			if (licenseNames.length < licenseIds.length) {
				fail("Not enough license names");
			}
		} finally {
			projectResource.close(nullMonitor);
		}
	}

	@Test
	public void testGetSpdxDoc() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		this.openResourceProject();
		try {
			SpdxProject spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			SPDXDocument doc = spdxProject.getSpdxDoc();
			String dataLicense = doc.getDataLicense().getName();
			assertEquals(DEFAULT_DATA_LICENSE, dataLicense);
		} finally {
			projectResource.close(nullMonitor);
		}
	}

	@Test
	public void testGetProject() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		this.openResourceProject();
		try {
			SpdxProject spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			assertEquals(this.projectResource.getName(), spdxProject.getProject().getName());
		} finally {
			projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testOpenCloseOpen() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		// test creating a project, saving info across an open and close
		this.openResourceProject();
		try {
			this.spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			IFile spdxFile = projectResource.getFile(TEST_SPDX_FILE_NAME);
			spdxProject.create(spdxFile, nullMonitor);
			spdxProject.getSpdxDoc().setDocumentComment(TEST_DOC_COMMENT);
			spdxProject.getSpdxDoc().getSpdxPackage().setDeclaredCopyright(COPYRIGHT_STRING);
			spdxProject.save(nullMonitor);
			SpdxProjectFactory.closeAllSpdxProjects();
			projectResource.close(nullMonitor);
			projectResource = null;
			openResourceProject();
			SpdxProject reOpened = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			assertEquals(TEST_DOC_COMMENT, reOpened.getSpdxDoc().getDocumentComment());
			assertEquals(COPYRIGHT_STRING, reOpened.getSpdxDoc().getSpdxPackage().getDeclaredCopyright());
		} finally {
			if (projectResource != null) {
				projectResource.close(nullMonitor);
			}
		}
	}
	
	@Test
	public void testValidSpdxFromProperties() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			SpdxPackageVerificationCode verificationCode = new SpdxPackageVerificationCode(SPDX_PACKAGE_VERIFICATION_CODE_SHA1, new String[] {});
			this.spdxProject.getSpdxDoc().getSpdxPackage().setVerificationCode(verificationCode);
			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 2) {	// 2 expected errors from not having any files
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	
	private void fillSpdxProjectInfo() throws InvalidSPDXAnalysisException, InvalidLicenseStringException {
		// package level information
		spdxProject.getSpdxDoc().getSpdxPackage().setDeclaredName(SPDX_PACKAGE_NAME);
		spdxProject.getSpdxDoc().getSpdxPackage().setVersionInfo(SPDX_PACKAGE_VERSION);
		spdxProject.getSpdxDoc().getSpdxPackage().setDeclaredCopyright(SPDX_PACKAGE_COPYRIGHT);
		spdxProject.getSpdxDoc().getSpdxPackage().setOriginator(SPDX_PACKAGE_ORIGINATOR);
		spdxProject.getSpdxDoc().getSpdxPackage().setSupplier(SPDX_PACKAGE_SUPPLIER);
		spdxProject.getSpdxDoc().getSpdxPackage().setSourceInfo(SPDX_PACKAGE_SOURCE_INFO);
		spdxProject.getSpdxDoc().getSpdxPackage().setFileName(SPDX_PACKAGE_ARCHIVE_FILE_NAME);
		spdxProject.getSpdxDoc().getSpdxPackage().setSha1(SPDX_PACKAGE_SHA1);
		spdxProject.getSpdxDoc().getSpdxPackage().setDownloadUrl(SPDX_PACKAGE_DOWNLOAD_URL);
		spdxProject.getSpdxDoc().getSpdxPackage().setShortDescription(SPDX_PACKAGE_SHORT_DESC);
		spdxProject.getSpdxDoc().getSpdxPackage().setDescription(SPDX_PACKAGE_DESCRIPTION);
		
		//License information
		spdxProject.getSpdxDoc().getSpdxPackage().setDeclaredLicense(
				SPDXLicenseInfoFactory.parseSPDXLicenseString(SPDX_PACKAGE_DECLARED_LICENSE));
		spdxProject.getSpdxDoc().getSpdxPackage().setConcludedLicenses(
				SPDXLicenseInfoFactory.parseSPDXLicenseString(SPDX_PACKAGE_CONCLUDED_LICENSE));
		
		//Creator information
		String today = format.format(new Date());
		SPDXCreatorInformation creatorInfo = new SPDXCreatorInformation(
				SPDX_CREATORS, today, CREATOR_COMMENT, LICENSE_LIST_VERSION);
		spdxProject.getSpdxDoc().setCreationInfo(creatorInfo);
		
		// reviewers
		SPDXReview[] reviewers = new SPDXReview[SPDX_REVIEWERS.length];
		for (int i = 0; i < SPDX_REVIEWERS.length; i++) {
			reviewers[i] = new SPDXReview(SPDX_REVIEWERS[i], 
					today, SPDX_REVIEWER_COMMENTS[i]);
		}
		spdxProject.getSpdxDoc().setReviewers(reviewers);
	}
	
	private IFile createResourceFile(IFolder folder, String fileName) throws IOException, CoreException, InterruptedException {
		IFile fileResource = folder.getFile(fileName);
		createResourceFile(fileResource);
		return fileResource;
	}

	private void createResourceFile(IFile fileResource) throws IOException, CoreException, InterruptedException {
		PipedInputStream in = new PipedInputStream();
		final String fileContentString = "File Conents for: "+fileResource.getFullPath().toString();
		final byte[] fileContents = new byte[fileContentString.length()];
		for (int i = 0; i < fileContents.length; i++) {
			fileContents[i] = (byte)fileContentString.charAt(i);
		}

		final PipedOutputStream out = new PipedOutputStream(in);
		// need to write in a separate thread
		Runnable writer = new Runnable() {

			@Override
			public void run() {
				try {
					out.write(fileContents);
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		};
		Thread writerThread = new Thread(writer);
		writerThread.start();
		fileResource.create(in, true, nullMonitor);
		writerThread.join();	// just to make sure it finishes
	}

	@Test
	public void testAddFileWithDefaults() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InterruptedException, InvalidExcludedFilePattern {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.spdxProject.waitForBuildJob();
			IFolder srcFolder = this.projectResource.getFolder(SOURCE_DIR);
			srcFolder.create(true, true, nullMonitor);
			this.createResourceFile(srcFolder, RESOURCE_FILENAME1);
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);	// sync up with the build process
			this.spdxProject.setIncludedFileDirectories(new String[] {SOURCE_DIR});
			this.spdxProject.setExcludedFilePatterns(new Pattern[] {});
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	private void setDefaultProjectProperties() throws CoreException, InvalidExcludedFilePattern, InterruptedException {
		SpdxProjectProperties.setDefaultFileCopyright(projectResource, FILE_COPYRIGHT_STRING);
		SpdxProjectProperties.setDefaultFileLicense(projectResource, FILE_DEFAULT_LICENSE);
		SpdxProjectProperties.setExcludedFilePatterns(projectResource, DEFAULT_EXCLUDED_FILE_PATTERNS);
		SpdxProjectProperties.setIncludedResourceDirectories(projectResource, DEFAULT_INCLUDED_DIRECTORIES);
		this.spdxProject.waitForBuildJob();
	}

	@Test
	public void testUpdateFilesFromProject() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InterruptedException, InvalidExcludedFilePattern {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
			SPDXFile[] files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, files.length);
			for (int i = 0; i < files.length; i++) {
				assertEquals(FILE_DEFAULT_LICENSE, files[i].getConcludedLicenses().toString());
				SPDXLicenseInfo[] seenLicenses = files[i].getSeenLicenses();
				assertEquals(1, seenLicenses.length);
				assertEquals(FILE_DEFAULT_LICENSE, seenLicenses[0].toString());
				assertEquals(FILE_COPYRIGHT_STRING, files[i].getCopyright());
				String fileName = files[i].getName();
				IFile file = this.projectResource.getFile(fileName);
				assertTrue(file.exists());
				assertEquals("SOURCE", files[i].getType());
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}

	private void createFiles() throws CoreException, IOException, InterruptedException {
		for (int i = 0; i < FILE_PATHS.length; i++) {
			String[] parts = FILE_PATHS[i].split("/");
			if (parts.length > 1) {
				IFolder subFolder = this.projectResource.getFolder(parts[0]);
				if (!subFolder.exists()) {
					subFolder.create(true, true, nullMonitor);
				}
				for (int j = 1; j < parts.length-1; j++) {
					subFolder = subFolder.getFolder(parts[j]);
					if (!subFolder.exists()) {
						subFolder.create(true, true, nullMonitor);
					}
				}
				this.createResourceFile(subFolder, parts[parts.length-1]);
			} else {
				IFile file = this.projectResource.getFile(parts[0]);
				createResourceFile(file);
			}			
		}
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);	// sync up with the build process
	}

	@Test
	public void testPropertyChangeFileConcludedLicense() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InterruptedException, InvalidLicenseStringException, InvalidExcludedFilePattern {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			IFile fileToUpdate = this.projectResource.getFile(FILE_PATHS[0]);
			String UPDATE_CONCLUDED_LICENSE_STRING = "APSL-1.2";
			SpdxFileProperties.setConcludedLicense(fileToUpdate, UPDATE_CONCLUDED_LICENSE_STRING);
			SPDXFile[] files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			SPDXFile foundFile = null;
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().equals(FILE_PATHS[0])) {
					foundFile = files[i];
					break;
				}
			}
			if (foundFile == null) {
				fail("Updated file not found");
			}
			assertEquals(UPDATE_CONCLUDED_LICENSE_STRING, foundFile.getConcludedLicenses().toString());
			
			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testPropertyChangeIncludedResourceDirectories() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InterruptedException, InvalidExcludedFilePattern {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			SPDXFile[] files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, files.length);
			String[] includedSourceDirectories = new String[] {"src/subdir/"};
			SpdxProjectProperties.setIncludedResourceDirectories(projectResource, includedSourceDirectories);
			this.spdxProject.waitForBuildJob();
			files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			assertEquals(NUM_NON_SKIPPED_SOURCE_IN_SUBDIR, files.length);
			// now try all of the subdirectories
			includedSourceDirectories = new String[] {"/"};
			SpdxProjectProperties.setIncludedResourceDirectories(projectResource, includedSourceDirectories);
			this.spdxProject.waitForBuildJob();
			files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			assertEquals(NUM_NON_SKIPPED_FILES_IN_ALL, files.length);

			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testPropertyChangeFileArtifactOfUrl() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InterruptedException, InvalidExcludedFilePattern {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			IFile fileToUpdate = this.projectResource.getFile(FILE_PATHS[0]);
			String ARTIFACT_OF_URL = "http://apache.org";
			SpdxFileProperties.setProjectUrl(fileToUpdate, ARTIFACT_OF_URL);
			SPDXFile[] files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			SPDXFile foundFile = null;
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().equals(FILE_PATHS[0])) {
					foundFile = files[i];
					break;
				}
			}
			if (foundFile == null) {
				fail("Updated file not found");
			}
			assertEquals(ARTIFACT_OF_URL, foundFile.getArtifactOf()[0].getHomePage());
			
			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testPropertyChangeFileArtifactOfProject() throws CoreException, SpdxProjectException, InvalidLicenseStringException, InvalidSPDXAnalysisException, IOException, InterruptedException, InvalidExcludedFilePattern {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			IFile fileToUpdate = this.projectResource.getFile(FILE_PATHS[0]);
			String ARTIFACT_OF_PROJECT = "PROJECT";
			SpdxFileProperties.setProjectName(fileToUpdate, ARTIFACT_OF_PROJECT);
			SPDXFile[] files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			SPDXFile foundFile = null;
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().equals(FILE_PATHS[0])) {
					foundFile = files[i];
					break;
				}
			}
			if (foundFile == null) {
				fail("Updated file not found");
			}
			assertEquals(ARTIFACT_OF_PROJECT, foundFile.getArtifactOf()[0].getName());
			
			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testPropertyChangeFileCopyright() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InterruptedException, InvalidExcludedFilePattern {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			IFile fileToUpdate = this.projectResource.getFile(FILE_PATHS[0]);
			String NEW_FILE_COPYRIGHT = "Copyright (c) SomeoneNew";
			SpdxFileProperties.setCopyright(fileToUpdate, NEW_FILE_COPYRIGHT);
			SPDXFile[] files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			SPDXFile foundFile = null;
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().equals(FILE_PATHS[0])) {
					foundFile = files[i];
					break;
				}
			}
			if (foundFile == null) {
				fail("Updated file not found");
			}
			assertEquals(NEW_FILE_COPYRIGHT, foundFile.getCopyright());
			
			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testPropertyChangeExcludedFilesPattern() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InterruptedException, InvalidExcludedFilePattern {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			SPDXFile[] files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, files.length);
			String[] excludePhpAndSkip = new String[] {".*\\.skip", ".*\\.project", ".*\\.spdx", ".*\\.php"};
			SpdxProjectProperties.setExcludedFilePatterns(projectResource, excludePhpAndSkip);
			this.spdxProject.waitForBuildJob();
			files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			assertEquals(NUM_NON_SKIPPED_NON_PHP, files.length);
			// now try all of the files
			String[] noSkipped = new String[] {};
			SpdxProjectProperties.setExcludedFilePatterns(projectResource, noSkipped);
			SpdxProjectProperties.setIncludedResourceDirectories(projectResource, new String[] {"/"});
			this.spdxProject.waitForBuildJob();
			files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			assertEquals(NUM_TOTAL_FILES+2, files.length);	// add 2 for the .spdx file and .project file

			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testPropertyChangeSpdxFileName() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InterruptedException, InvalidExcludedFilePattern {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			String newFileName = "NewFile.spdx";
			this.spdxProject.save(nullMonitor);
			SpdxProjectProperties.setSpdxFileName(projectResource, newFileName);
			IFile newFile = this.projectResource.getFile(newFileName);
			IFile oldFile = this.projectResource.getFile(TEST_SPDX_FILE_NAME);
			assertTrue(newFile.exists());
			assertTrue(!oldFile.exists());
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testaddLicense() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		openResourceProject();
		try {
			spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			
			String licenseIdNoName = "LicenseRef-Test";
			String licenseIdWithName = "LicenseRef-Test2";
			String licenseName = "LicenseName";
			SPDXNonStandardLicense licenseNoName = new SPDXNonStandardLicense(licenseIdNoName, "License Text");
			SPDXNonStandardLicense licenseWithName = new SPDXNonStandardLicense(licenseIdWithName,"License Text2", licenseName, new String[0], "");

			String[] availableLicensesBefore = spdxProject.getAvailableLicenseNames();
			spdxProject.addLicense(licenseNoName);
			spdxProject.addLicense(licenseWithName);
			String[] result = spdxProject.getAvailableLicenseNames();
			assertEquals(availableLicensesBefore.length + 2, result.length);
			boolean foundLicenseNoName = false;
			boolean foundLicenseWithName = false;
			for (int i = 0; i < result.length; i++) {
				if (result[i].equals(licenseName)) {
					foundLicenseWithName = true;
				}
				if (result[i].equals(licenseIdNoName)) {
					foundLicenseNoName = true;
				}				
			}
			assertTrue(foundLicenseNoName);
			assertTrue(foundLicenseWithName);
		} finally {
			if (spdxProject != null) {
				spdxProject.close(nullMonitor);
			}
		}
	}
	
	@Test
	public void testverifyNewLicenseId() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		openResourceProject();
		try {
			spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			
			String licenseIdNoName = "LicenseRef-Test";
			SPDXNonStandardLicense licenseNoName = new SPDXNonStandardLicense(licenseIdNoName, "License Text");

			// valid license ID
			assertTrue(spdxProject.verifyNewLicenseId("LicenseRef-X"));
			assertTrue(spdxProject.verifyNewLicenseId("LicenseRef-12"));
			// invalid string
			assertTrue(!spdxProject.verifyNewLicenseId("NotValidPrefix-X"));
			assertTrue(!spdxProject.verifyNewLicenseId("LicenseRef-!NotValid^char"));
			// license already added
			assertTrue(spdxProject.verifyNewLicenseId(licenseIdNoName));
			spdxProject.addLicense(licenseNoName);
			assertFalse(spdxProject.verifyNewLicenseId(licenseIdNoName));
		} finally {
			if (spdxProject != null) {
				spdxProject.close(nullMonitor);
			}
		}
	}
	
	@Test
	public void testgetNextAvailableLicenseId() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		openResourceProject();
		try {
			spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
			String nextLicId = spdxProject.getNextAvailableLicenseId();
			assertEquals("LicenseRef-1", nextLicId);
			nextLicId = spdxProject.getNextAvailableLicenseId();
			assertEquals("LicenseRef-2", nextLicId);
		} finally {
			if (spdxProject != null) {
				spdxProject.close(nullMonitor);
			}
		}
	}
	
	
	@Test 
	public void testsetExcludedFilePatterns() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidExcludedFilePattern, InvalidLicenseStringException, InterruptedException {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(new String[] {SOURCE_DIR});
			this.spdxProject.setExcludedFilePatterns(new Pattern[] {});
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			ArrayList<String> verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
			assertEquals(NUM_SOURCE_FILES, this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles().length);
			
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.refresh(nullMonitor);
			verifyInfo = this.spdxProject.getSpdxDoc().verify();
			if (verifyInfo.size() > 0) {	
				StringBuilder sb = new StringBuilder();
				sb.append(verifyInfo.get(0));
				for (int i = 1; i < verifyInfo.size(); i++) {
					sb.append(", ");
					sb.append(verifyInfo.get(i));
				}
				fail(sb.toString());
			}
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles().length);
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test 
	public void testbackgroundBuild() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidExcludedFilePattern, InvalidLicenseStringException, InterruptedException {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);

			assertTrue(!this.spdxProject.isFileRescanRequired());
			this.spdxProject.setFileRescanRequired(true);
			WorkspaceJob job = this.spdxProject.backgroundFullBuild(IncrementalProjectBuilder.FULL_BUILD);
			job.join();
			IStatus result = job.getResult();
			assertEquals(IStatus.OK, result.getSeverity());
			assertTrue(!this.spdxProject.isFileRescanRequired());
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test 
	public void testisFileRescanRequired() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidExcludedFilePattern, InvalidLicenseStringException, InterruptedException {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);

			assertTrue(!this.spdxProject.isFileRescanRequired());
			this.spdxProject.setFileRescanRequired(true);
			assertTrue(this.spdxProject.isFileRescanRequired());
			this.spdxProject.setFileRescanRequired(false);
			assertTrue(!this.spdxProject.isFileRescanRequired());
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test 
	public void testremoveFile() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidExcludedFilePattern, InvalidLicenseStringException, InterruptedException {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);

			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles().length);
			IFile fileToBeRemoved = this.projectResource.getFile(FILE_PATHS[1]);
			this.spdxProject.removeFile(fileToBeRemoved, nullMonitor);
			SPDXFile[] spdxFiles = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES-1, spdxFiles.length);
			for (int i = 0; i < spdxFiles.length; i++) {
				assertTrue(!spdxFiles[i].getName().equals(FILE_PATHS[1]));
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test 
	public void testupdateFile()throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidExcludedFilePattern, InvalidLicenseStringException, InterruptedException  {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			// set the file properties before update
			String testFileName = FILE_PATHS[0];
			IFile testFile = this.projectResource.getFile(testFileName);
			String beforeComment = "Before Comment";
			String afterComment = "After Comment";
			String beforeConcludedLicense = "AFL-1.1";
			String afterConcludedLicense = "(Aladdin OR APSL-2.0)";
			String[] beforeContributors = new String[] {"Before1", "Before2"};
			String[] afterContributors = new String[] {"After1", "After2"};
			String beforeCopyright = "Before copyright";
			String afterCopyright = "After copyright";
			String beforeNotice = "Before notice";
			String afterNotice = "After notice";
			String beforeProjectName = "before project name";
			String afterProjectName = "after project name";
			String beforeProjectUrl = "http://before.url.com";
			String afterProjectUrl = "http://after.url.com";
			SpdxFileProperties.setComment(testFile, beforeComment);
			SpdxFileProperties.setConcludedLicense(testFile, beforeConcludedLicense);
			SpdxFileProperties.setContributors(testFile, beforeContributors);
			SpdxFileProperties.setCopyright(testFile, beforeCopyright);
			SpdxFileProperties.setNotice(testFile, beforeNotice);
			SpdxFileProperties.setProjectName(testFile, beforeProjectName);
			SpdxFileProperties.setProjectUrl(testFile, beforeProjectUrl);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);

			SPDXFile[] files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			SPDXFile foundFile = null;
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().equals(testFileName)) {
					foundFile = files[i];
					break;
				}
			}
			if (foundFile == null) {
				fail("Could not find SPDX file");
			}
			assertEquals(beforeComment, foundFile.getComment());
			assertEquals(beforeConcludedLicense, foundFile.getConcludedLicenses().toString());
			assertArraysEqual(beforeContributors, foundFile.getContributors());
			assertEquals(beforeCopyright, foundFile.getCopyright());
			assertEquals(beforeNotice, foundFile.getNoticeText());
			assertEquals(1, foundFile.getArtifactOf().length);
			assertEquals(beforeProjectName, foundFile.getArtifactOf()[0].getName());
			assertEquals(beforeProjectUrl, foundFile.getArtifactOf()[0].getHomePage());

			SpdxFileProperties.setComment(testFile, afterComment);
			SpdxFileProperties.setConcludedLicense(testFile, afterConcludedLicense);
			SpdxFileProperties.setContributors(testFile, afterContributors);
			SpdxFileProperties.setCopyright(testFile, afterCopyright);
			SpdxFileProperties.setNotice(testFile, afterNotice);
			SpdxFileProperties.setProjectName(testFile, afterProjectName);
			SpdxFileProperties.setProjectUrl(testFile, afterProjectUrl);
			spdxProject.updateFile(testFile, nullMonitor);
			
			files = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			foundFile = null;
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().equals(testFileName)) {
					foundFile = files[i];
					break;
				}
			}
			if (foundFile == null) {
				fail("Could not find SPDX file");
			}
			assertEquals(afterComment, foundFile.getComment());
			assertEquals(afterConcludedLicense, foundFile.getConcludedLicenses().toString());
			assertArraysEqual(afterContributors, foundFile.getContributors());
			assertEquals(afterCopyright, foundFile.getCopyright());
			assertEquals(afterNotice, foundFile.getNoticeText());
			assertEquals(1, foundFile.getArtifactOf().length);
			assertEquals(afterProjectName, foundFile.getArtifactOf()[0].getName());
			assertEquals(afterProjectUrl, foundFile.getArtifactOf()[0].getHomePage());
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	private void assertArraysEqual(Object[] a1,
			Object[] a2) {
		assertEquals(a1.length, a2.length);
		for (int i = 0; i < a1.length; i++) {
			boolean found = false;
			for (int j = 0; j < a2.length; j++) {
				if (a1[i].equals(a2[j])) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}

	@Test 
	public void testupdateVerificationCodeFromFiles()throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidExcludedFilePattern, InvalidLicenseStringException, InterruptedException  {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			
			SpdxPackageVerificationCode origvc = this.spdxProject.getSpdxDoc().getSpdxPackage().getVerificationCode();
			this.spdxProject.updateVerificationCodeFromFiles();
			// should not change
			SpdxPackageVerificationCode result = this.spdxProject.getSpdxDoc().getSpdxPackage().getVerificationCode();
			assertEquals(origvc.getValue(), result.getValue());
			
			IFile deletedFile = this.projectResource.getFile(FILE_PATHS[0]);
			this.spdxProject.removeFile(deletedFile, nullMonitor);
			this.spdxProject.updateVerificationCodeFromFiles();
			result = this.spdxProject.getSpdxDoc().getSpdxPackage().getVerificationCode();
			if (result.getValue().equals(origvc.getValue())) {
				fail("Verification code not updated");
			}
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test 
	public void testaddFile() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidExcludedFilePattern, InvalidLicenseStringException, InterruptedException  {
		openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.updateSpdxFilesFromProjectResources(TEST_SPDX_FILE_NAME, this.projectResource, nullMonitor);
			
			String newFilePath = "src/newfilepath.c";
			IFile newFile = this.projectResource.getFile(newFilePath);
			createResourceFile(newFile);
			SPDXFile[] origFiles = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			this.spdxProject.addFile(newFile, nullMonitor);
			SPDXFile[] result = this.spdxProject.getSpdxDoc().getSpdxPackage().getFiles();
			assertEquals(origFiles.length + 1, result.length);
			boolean found = false;
			for (int i = 0; i < result.length; i++) {
				if (result[i].getName().equals(newFilePath)) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test 
	public void testfileIncluded() throws IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, CoreException, InvalidExcludedFilePattern {
		openResourceProject();
		try {
			this.createSpdxProject();
			
			// only check included directories
			this.spdxProject.setExcludedFilePatterns(new Pattern[] {});
			this.spdxProject.setIncludedFileDirectories(new String[] {"a", "b"});
			IFile testFile = this.projectResource.getFile("a/test.c");
			assertTrue(this.spdxProject.fileShouldBeIncluded(testFile));
			testFile = this.projectResource.getFile("b/anotherfile");
			assertTrue(this.spdxProject.fileShouldBeIncluded(testFile));
			testFile = this.projectResource.getFile("c/yetanother.c");
			assertTrue(!this.spdxProject.fileShouldBeIncluded(testFile));
			testFile = this.projectResource.getFile("fileatroot.java");
			assertTrue(!this.spdxProject.fileShouldBeIncluded(testFile));
			
			// check patterns
			Pattern[] skippedPatterns = new Pattern[] {Pattern.compile(".*\\.skip")};
			this.spdxProject.setExcludedFilePatterns(skippedPatterns);
			testFile = this.projectResource.getFile("b/fileWithoutSkip.c");
			assertTrue(this.spdxProject.fileShouldBeIncluded(testFile));
			testFile = this.projectResource.getFile("b/fileWIthSkip.skip");
			assertTrue(!this.spdxProject.fileShouldBeIncluded(testFile));
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testConvertLocalLicense() throws IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, CoreException, InvalidExcludedFilePattern {
		openResourceProject();
		try {
			this.createSpdxProject();
			String licText1 = "This is license text for number 1.";
			String licText2 = "license text 2";
			String licText3 = "License text 3";
			String licId1 = this.spdxProject.getNextAvailableLicenseId();
			SPDXNonStandardLicense license1 = new SPDXNonStandardLicense(licId1, licText1);
			this.spdxProject.addLicense(license1);
			String licId2 = this.spdxProject.getNextAvailableLicenseId();
			SPDXNonStandardLicense license2 = new SPDXNonStandardLicense(licId2, licText2);
			this.spdxProject.addLicense(license2);
			
			Model model = ModelFactory.createDefaultModel();
			SPDXDocument doc = SPDXDocumentFactory.createSpdxDocument(model);
			doc.createSpdxAnalysis("http://temp.uri");
			doc.createSpdxPackage();
			SPDXNonStandardLicense docLicense1 = doc.addNewExtractedLicenseInfo(licText1);
			SPDXNonStandardLicense docLicense2 = doc.addNewExtractedLicenseInfo(licText2);
			SPDXNonStandardLicense docLicense3 = doc.addNewExtractedLicenseInfo(licText3);
			
			// existing licenses
			SPDXLicenseInfo result = this.spdxProject.convertToLocalLicense(doc, docLicense1);
			assertEquals(result, license1);
			result = this.spdxProject.convertToLocalLicense(doc, docLicense2);
			assertEquals(result, license2);
			result = this.spdxProject.convertToLocalLicense(doc, docLicense3);
			assertTrue(result instanceof SPDXNonStandardLicense);
			assertEquals(licText3, ((SPDXNonStandardLicense)result).getText());
			
			// SPDX Standard License
			String stdLicId = "AFL-2.0";
			SPDXLicenseInfo standardLicense = SPDXLicenseInfoFactory.parseSPDXLicenseString(stdLicId);
			result = this.spdxProject.convertToLocalLicense(doc, standardLicense);
			assertTrue(result instanceof SPDXStandardLicense);
			assertEquals(standardLicense, result);
			
			// Conjunctive license
			SPDXLicenseInfo[] licenseSet = new SPDXLicenseInfo[] {
					standardLicense, docLicense1, docLicense2
			};
			
			SPDXConjunctiveLicenseSet cLicense = new SPDXConjunctiveLicenseSet(licenseSet);
			result = this.spdxProject.convertToLocalLicense(doc, cLicense);
			assertTrue(result instanceof SPDXConjunctiveLicenseSet);
			assertEquals(cLicense, result);
			
			// Disjunctive license
			SPDXDisjunctiveLicenseSet dLicense = new SPDXDisjunctiveLicenseSet(licenseSet);
			result = this.spdxProject.convertToLocalLicense(doc, dLicense);
			assertTrue(result instanceof SPDXDisjunctiveLicenseSet);
			assertEquals(dLicense, result);
			
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testGetNonStdLicenseWithText() throws IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, CoreException, InvalidExcludedFilePattern {
		openResourceProject();
		try {
			this.createSpdxProject();
			String licText1 = "This is license text for number 1.";
			String licText2 = "license text 2";
			String licText3 = "this is license text 3";
			SPDXNonStandardLicense result = this.spdxProject.getNonStdLicenseWithText(licText1);
			assertTrue(result == null);
			String licId1 = this.spdxProject.getNextAvailableLicenseId();
			SPDXNonStandardLicense license1 = new SPDXNonStandardLicense(licId1, licText1);
			this.spdxProject.addLicense(license1);
			result = this.spdxProject.getNonStdLicenseWithText(licText1);
			assertEquals(license1, result);
			result = this.spdxProject.getNonStdLicenseWithText(licText2);
			assertTrue(result == null);
			String licId2 = this.spdxProject.getNextAvailableLicenseId();
			SPDXNonStandardLicense license2 = new SPDXNonStandardLicense(licId2, licText2);
			this.spdxProject.addLicense(license2);
			result = this.spdxProject.getNonStdLicenseWithText(licText2);
			assertEquals(license2, result);
			result = this.spdxProject.getNonStdLicenseWithText(licText1);
			assertEquals(license1, result);
			result = this.spdxProject.getNonStdLicenseWithText(licText3);
			assertTrue(result == null);
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
}
