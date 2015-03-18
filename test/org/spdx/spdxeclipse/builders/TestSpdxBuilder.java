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
package org.spdx.spdxeclipse.builders;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.project.SpdxProject;
import org.spdx.spdxeclipse.project.SpdxProjectException;
import org.spdx.spdxeclipse.project.SpdxProjectFactory;
import org.spdx.spdxeclipse.properties.InvalidExcludedFilePattern;
import org.spdx.spdxeclipse.properties.SpdxFileProperties;
import org.spdx.spdxeclipse.properties.SpdxProjectProperties;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * @author Gary O'Neall
 *
 */
public class TestSpdxBuilder {
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

	private static final String SOURCE_DIR = "src";
	
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
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
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

	public void createSpdxProject() throws IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		spdxProject = SpdxProjectFactory.getSpdxProject(TEST_PROJECT_NAME, new NullProgressMonitor());
		this.spdxSaveFile = projectResource.getFile(TEST_SPDX_FILE_NAME);
		spdxProject.create(spdxSaveFile, nullMonitor);
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
	
	private void modifyFile(IFile fileResource) throws IOException, CoreException, InterruptedException {
		PipedInputStream in = new PipedInputStream();
		final String fileContentString = "Appended content for: "+fileResource.getFullPath().toString();
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
		fileResource.appendContents(in, true, true, nullMonitor);
		writerThread.join();	// just to make sure it finishes
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
	}
	
	private void setDefaultProjectProperties() throws CoreException, InvalidExcludedFilePattern {
		SpdxProjectProperties.setDefaultFileCopyright(projectResource, FILE_COPYRIGHT_STRING);
		SpdxProjectProperties.setDefaultFileLicense(projectResource, FILE_DEFAULT_LICENSE);
		SpdxProjectProperties.setExcludedFilePatterns(projectResource, DEFAULT_EXCLUDED_FILE_PATTERNS);
		SpdxProjectProperties.setIncludedResourceDirectories(projectResource, DEFAULT_INCLUDED_DIRECTORIES);
	}

	
	@Test
	public void testFullBuild() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InvalidExcludedFilePattern, InterruptedException {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			@SuppressWarnings("unused")
			SPDXFile[] filesBeforeBuild = this.spdxProject.getSpdxDoc().getFileReferences();
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
			this.projectResource.build(IncrementalProjectBuilder.FULL_BUILD, SpdxBuilder.ID, new HashMap<String, String>(), nullMonitor);
			SPDXFile[] filesAfterBuild = this.spdxProject.getSpdxDoc().getFileReferences();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, filesAfterBuild.length);
			IMarker[] markers = this.projectResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			assertEquals(0, markers.length);
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.builders.SpdxBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testIncrementalBuildNewFile() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InvalidExcludedFilePattern, InterruptedException {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.projectResource.build(IncrementalProjectBuilder.FULL_BUILD, SpdxBuilder.ID, new HashMap<String, String>(), nullMonitor);
			SPDXFile[] fullBuildFiles = this.spdxProject.getSpdxDoc().getFileReferences();
			String originalVerificationCode = this.spdxProject.getSpdxDoc().getSpdxPackage().getVerificationCode().getValue();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, fullBuildFiles.length);
			IFile newFile = this.projectResource.getFolder(SOURCE_DIR).getFile("newFile.c");
			this.createResourceFile(newFile);
			// the above should trigger the incremental build
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
			SPDXFile[] incBuildFiles = this.spdxProject.getSpdxDoc().getFileReferences();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES+1, incBuildFiles.length);
			SPDXFile addedSpdxFile = null;
			String newFilePath = newFile.getFullPath().toString();
			for (int i = 0; i < incBuildFiles.length; i++) {
				String fullBuildName = "/" + this.projectResource.getName() + "/" + incBuildFiles[i].getName();
				if (fullBuildName.equals(newFilePath)) {
					addedSpdxFile = incBuildFiles[i];
				}
			}
			if (addedSpdxFile == null) {
				fail("File was not added to SPDX file");
			}
			IMarker[] markers = this.projectResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			assertEquals(0, markers.length);
			String newVerificationCode = this.spdxProject.getSpdxDoc().getSpdxPackage().getVerificationCode().getValue();
			assertTrue(!newVerificationCode.equals(originalVerificationCode));
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	/**
	 * Test method for {@link org.spdx.spdxeclipse.builders.SpdxBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testIncrementalBuildUpdatedFile() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InvalidExcludedFilePattern, InterruptedException {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.createFiles();
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);

			SPDXFile[] fullBuildFiles = this.spdxProject.getSpdxDoc().getFileReferences();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, fullBuildFiles.length);
			String modifiedFilePath = FILE_PATHS[0];
			SPDXFile modifiedSpdxFile = null;
			for (int i = 0; i < fullBuildFiles.length; i++) {
				if (fullBuildFiles[i].getName().equals(modifiedFilePath)) {
					modifiedSpdxFile = fullBuildFiles[i];
				}
			}
			String originalVerificationCode = this.spdxProject.getSpdxDoc().getSpdxPackage().getVerificationCode().getValue();
			String originalSha1 = modifiedSpdxFile.getSha1();
			
			IFile modifiedFile = getFile(this.projectResource, FILE_PATHS[0]);
			modifyFile(modifiedFile);
			// the above should trigger the incremental build
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);

			IMarker[] markers = this.projectResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			assertEquals(0, markers.length);
			String newVerificationCode = this.spdxProject.getSpdxDoc().getSpdxPackage().getVerificationCode().getValue();
			assertTrue(!newVerificationCode.equals(originalVerificationCode));
			String newSha1 = modifiedSpdxFile.getSha1();
			assertTrue(!newSha1.equals(originalSha1));	
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	private IFile getFile(IContainer container, String path) throws CoreException, IOException, InterruptedException {
		String[] parts = path.split("/");
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
			return subFolder.getFile(parts[parts.length-1]);
		} else {
			return this.projectResource.getFile(parts[0]);
		}			
	}

	@Test
	public void testFileSavedOnBuild() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InvalidExcludedFilePattern, InterruptedException {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			this.projectResource.build(IncrementalProjectBuilder.FULL_BUILD, SpdxBuilder.ID, new HashMap<String, String>(), nullMonitor);
			SPDXFile[] fullBuildFiles = this.spdxProject.getSpdxDoc().getFileReferences();
			String originalVerificationCode = this.spdxProject.getSpdxDoc().getSpdxPackage().getVerificationCode().getValue();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, fullBuildFiles.length);
			IFile newFile = this.projectResource.getFolder(SOURCE_DIR).getFile("newFile.c");
			this.createResourceFile(newFile);
			// the above should trigger the incremental build
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
			SPDXFile[] incBuildFiles = this.spdxProject.getSpdxDoc().getFileReferences();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES+1, incBuildFiles.length);
			SPDXFile addedSpdxFile = null;
			String newFilePath = newFile.getFullPath().toString();
			for (int i = 0; i < incBuildFiles.length; i++) {
				String fullBuildName = "/" + this.projectResource.getName() + "/" + incBuildFiles[i].getName();
				if (fullBuildName.equals(newFilePath)) {
					addedSpdxFile = incBuildFiles[i];
				}
			}
			if (addedSpdxFile == null) {
				fail("File was not added to SPDX file");
			}
			IMarker[] markers = this.projectResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			assertEquals(0, markers.length);
			String newVerificationCode = this.spdxProject.getSpdxDoc().getSpdxPackage().getVerificationCode().getValue();
			assertTrue(!newVerificationCode.equals(originalVerificationCode));
			
			// check the SPDX document was saved
			IFile spdxDocumentFile = this.projectResource.getFile(TEST_SPDX_FILE_NAME);
//			String spdxDocumentFileName = spdxDocumentFile.getFullPath().toFile().getAbsolutePath();
			SPDXDocument savedDoc = null;
			
			InputStream input = null;
			try {
				input = spdxDocumentFile.getContents();
				savedDoc = SPDXDocumentFactory.createSpdxDocument(input, "http://dummy.url", "RDF/XML-ABBREV");
			}	finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						Activator.getDefault().logError("IO Error closing input spdx file", e);
					}
				}
			}
			assertEquals(newVerificationCode, savedDoc.getSpdxPackage().getVerificationCode().getValue());
			SPDXFile[] savedDocFiles = savedDoc.getFileReferences();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES+1, savedDocFiles.length);
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.builders.SpdxBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@SuppressWarnings("unused")
	@Test
	public void testClean() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InvalidExcludedFilePattern, InterruptedException {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			SPDXFile[] filesBeforeClean = this.spdxProject.getSpdxDoc().getFileReferences();
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
			this.projectResource.build(IncrementalProjectBuilder.CLEAN_BUILD, SpdxBuilder.ID, new HashMap<String, String>(), nullMonitor);
			SPDXFile[] filesAfterBuild = this.spdxProject.getSpdxDoc().getFileReferences();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, filesAfterBuild.length);
			IMarker[] markers = this.projectResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			assertEquals(0, markers.length);
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}
	
	@Test
	public void testSetClearMarkers() throws CoreException, IOException, InvalidSPDXAnalysisException, SpdxProjectException, InvalidLicenseStringException, InvalidExcludedFilePattern, InterruptedException {
		this.openResourceProject();
		try {
			this.createSpdxProject();
			this.fillSpdxProjectInfo();
			this.setDefaultProjectProperties();
			this.createFiles();
			this.spdxProject.setIncludedFileDirectories(DEFAULT_INCLUDED_DIRECTORIES);
			this.spdxProject.setExcludedFilePatterns(SKIPPED_FILES_PATTERNS);
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
			SPDXFile[] fullBuildFiles = this.spdxProject.getSpdxDoc().getFileReferences();
			assertEquals(NUM_NON_SKIPPED_SOURCE_FILES, fullBuildFiles.length);
			IMarker[] markers = this.projectResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			assertEquals(0, markers.length);
			IFile newFile = this.projectResource.getFolder(SOURCE_DIR).getFile("newFile.c");
			this.createResourceFile(newFile);
			markers = this.projectResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			assertEquals(0, markers.length);
			// force an error
			try {
				SpdxFileProperties.setConcludedLicense(newFile, "(badlicense");
			} catch (Exception e) {
				// ignore
			}
			modifyFile(newFile);
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
			markers = this.projectResource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			assertEquals(1, markers.length);
			assertEquals(markers[0].getResource(), newFile);
			// fix the error and clean build
			SpdxFileProperties.setProjectUrl(newFile, "http://goodurl.com");
			projectResource.build(IncrementalProjectBuilder.CLEAN_BUILD, nullMonitor);
			//TODO: force project level error - currently not possible
		} finally {
			this.projectResource.close(nullMonitor);
		}
	}

}
