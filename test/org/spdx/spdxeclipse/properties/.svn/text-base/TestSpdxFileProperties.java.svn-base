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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Gary O'Neall
 *
 */
public class TestSpdxFileProperties {
	static final String TEST_PROJECT_NAME = "TestProject";

	private IProject projectResource = null;

	static final String TEST_FILE_NAME = "TestFile.txt";
	
	private IFile file = null;
	
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
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		this.projectResource = root.getProject(TEST_PROJECT_NAME);
		if (!this.projectResource.exists()) {
			this.projectResource.create(new NullProgressMonitor());
		}
		if (!this.projectResource.isOpen()) {
			this.projectResource.open(new NullProgressMonitor());
		}
		if (this.file == null) {
			this.file = this.projectResource.getFile(TEST_FILE_NAME);
			if (!this.file.exists()) {
				InputStream source = new ByteArrayInputStream("Source Text".getBytes());
				this.file.create(source, true, new NullProgressMonitor());
			}
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (this.file != null) {
			this.file.delete(true, new NullProgressMonitor());
			this.file = null;
		}
		if (this.projectResource != null) {
			if (this.projectResource.isOpen()) {
				this.projectResource.close(new NullProgressMonitor());
			}
			this.projectResource.delete(true, new NullProgressMonitor());
			this.projectResource = null;
		}
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxFileProperties#setConcludedLicense(org.eclipse.core.resources.IFile, java.lang.String)}.
	 * @throws CoreException 
	 */
	@Test
	public void testSetConcludedLicense() throws CoreException {
		String s = "Apache-2.0";
		SpdxFileProperties.setConcludedLicense(file, s);
		assertEquals(s, SpdxFileProperties.getConcludedLicense(file));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxFileProperties#setCopyright(org.eclipse.core.resources.IFile, java.lang.String)}.
	 */
	@Test
	public void testSetCopyright() throws CoreException {
		String s = "Test Copyright";
		SpdxFileProperties.setCopyright(file, s);
		assertEquals(s, SpdxFileProperties.getCopyright(file));

	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxFileProperties#setNotice(org.eclipse.core.resources.IFile, java.lang.String)}.
	 */
	@Test
	public void testSetNotice() throws CoreException {
		String s = "Test Notice";
		SpdxFileProperties.setNotice(file, s);
		assertEquals(s, SpdxFileProperties.getNotice(file));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxFileProperties#setComment(org.eclipse.core.resources.IFile, java.lang.String)}.
	 */
	@Test
	public void testSetComment() throws CoreException {
		String s = "Test Comment";
		SpdxFileProperties.setComment(file, s);
		assertEquals(s, SpdxFileProperties.getComment(file));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxFileProperties#setContributors(org.eclipse.core.resources.IFile, java.lang.String[])}.
	 */
	@Test
	public void testSetContributors() throws CoreException {
		String[] contribs = new String[] {"newdefaultfileContributors", "another"};
		SpdxFileProperties.setContributors(file, contribs);
		String[] result = SpdxFileProperties.getContributors(file);
		assertEquals(2, result.length);
		assertEquals(contribs[0], result[0]);
		assertEquals(contribs[1], result[1]);
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxFileProperties#setProjectName(org.eclipse.core.resources.IFile, java.lang.String)}.
	 */
	@Test
	public void testSetProjectName() throws CoreException {
		String s = "ProjectName";
		SpdxFileProperties.setProjectName(file, s);
		assertEquals(s, SpdxFileProperties.getProjectName(file));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxFileProperties#setProjectUrl(org.eclipse.core.resources.IFile, java.lang.String)}.
	 */
	@Test
	public void testSetProjectUrl() throws CoreException {
		String s = "http://projecturl";
		SpdxFileProperties.setProjectUrl(file, s);
		assertEquals(s, SpdxFileProperties.getProjectUrl(file));

	}

}
