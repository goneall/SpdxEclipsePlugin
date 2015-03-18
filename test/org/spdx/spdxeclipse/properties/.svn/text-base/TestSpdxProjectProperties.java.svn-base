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
public class TestSpdxProjectProperties {
	
	static final String TEST_PROJECT_NAME = "TestProject";

	private IProject projectResource = null;

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

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (this.projectResource != null) {
			if (this.projectResource.isOpen()) {
				this.projectResource.close(new NullProgressMonitor());
			}
			this.projectResource.delete(true, new NullProgressMonitor());
			this.projectResource = null;
		}
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxProjectProperties#setSpdxFileName(org.eclipse.core.resources.IProject, java.lang.String)}.
	 * @throws CoreException 
	 */
	@Test
	public void testSetSpdxFileName() throws CoreException {
		String newFileName = "newSpdxFileName.spdx";
		SpdxProjectProperties.setSpdxFileName(projectResource, newFileName);
		assertEquals(newFileName, SpdxProjectProperties.getSpdxFileName(projectResource));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxProjectProperties#setSpdxInitialized(org.eclipse.core.resources.IProject, boolean)}.
	 * @throws CoreException 
	 */
	@Test
	public void testSetSpdxInitialized() throws CoreException {
		assertTrue(!SpdxProjectProperties.isSpdxInitialized(projectResource));
		SpdxProjectProperties.setSpdxInitialized(projectResource, true);
		assertTrue(SpdxProjectProperties.isSpdxInitialized(projectResource));
		SpdxProjectProperties.setSpdxInitialized(projectResource, false);
		assertTrue(!SpdxProjectProperties.isSpdxInitialized(projectResource));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxProjectProperties#setSpdxDocUrl(org.eclipse.core.resources.IProject, java.lang.String)}.
	 * @throws CoreException 
	 */
	@Test
	public void testSetSpdxDocUrl() throws CoreException {
		String s = "newSpdxDocUrl";
		SpdxProjectProperties.setSpdxDocUrl(projectResource, s);
		assertEquals(s, SpdxProjectProperties.getSpdxDocUrl(projectResource));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxProjectProperties#setDefaultFileLicense(org.eclipse.core.resources.IProject, java.lang.String)}.
	 */
	@Test
	public void testSetDefaultFileLicense() throws CoreException {
		String s = "newdefaultfilelicense";
		SpdxProjectProperties.setDefaultFileLicense(projectResource, s);
		assertEquals(s, SpdxProjectProperties.getDefaultFileLicense(projectResource));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxProjectProperties#setDefaultFileCopyright(org.eclipse.core.resources.IProject, java.lang.String)}.
	 */
	@Test
	public void testSetDefaultFileCopyright() throws CoreException {
		String s = "newdefaultCopyright";
		SpdxProjectProperties.setDefaultFileCopyright(projectResource, s);
		assertEquals(s, SpdxProjectProperties.getDefaultFileCopyright(projectResource));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxProjectProperties#setDefaultFileNotice(org.eclipse.core.resources.IProject, java.lang.String)}.
	 */
	@Test
	public void testSetDefaultFileNotice() throws CoreException {
		String s = "newdefaultfileNotice";
		SpdxProjectProperties.setDefaultFileNotice(projectResource, s);
		assertEquals(s, SpdxProjectProperties.getDefaultFileNotice(projectResource));
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxProjectProperties#setDefaulFileContributors(org.eclipse.core.resources.IProject, java.lang.String[])}.
	 */
	@Test
	public void testSetDefaulFileContributors() throws CoreException {
		String[] contribs = new String[] {"newdefaultfileContributors", "another"};
		SpdxProjectProperties.setDefaulFileContributors(projectResource, contribs);
		String[] result = SpdxProjectProperties.getDefaultFileContributors(projectResource);
		assertEquals(2, result.length);
		assertEquals(contribs[0], result[0]);
		assertEquals(contribs[1], result[1]);
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxProjectProperties#setIncludedResourceDirectories(org.eclipse.core.resources.IProject, java.lang.String[])}.
	 */
	@Test
	public void testSetIncludedResourceDirectories() throws CoreException {
		String[] dirs = new String[] {"dir1", "dir2"};
		SpdxProjectProperties.setIncludedResourceDirectories(projectResource, dirs);
		String[] result = SpdxProjectProperties.getIncludedResourceDirectories(projectResource);
		assertEquals(2, result.length);
		assertEquals(dirs[0], result[0]);
		assertEquals(dirs[1], result[1]);	
	}

	/**
	 * Test method for {@link org.spdx.spdxeclipse.properties.SpdxProjectProperties#setExcludedFilePatterns(org.eclipse.core.resources.IProject, java.lang.String[])}.
	 * @throws InvalidExcludedFilePattern 
	 */
	@Test
	public void testSetExcludedFilePatterns() throws CoreException, InvalidExcludedFilePattern {
		String[] patterns = new String[] {"p1*", "p2*"};
		SpdxProjectProperties.setExcludedFilePatterns(projectResource, patterns);
		String[] result = SpdxProjectProperties.getExcludedFilePatterns(projectResource);
		assertEquals(2, result.length);
		assertEquals(patterns[0], result[0]);
		assertEquals(patterns[1], result[1]);		
	}
}
