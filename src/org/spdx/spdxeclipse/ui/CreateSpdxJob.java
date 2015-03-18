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
package org.spdx.spdxeclipse.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.project.SpdxProject;
import org.spdx.spdxeclipse.project.SpdxProjectException;
import org.spdx.rdfparser.SPDXFile;

/**
 * @author Source Auditor
 *
 */
public class CreateSpdxJob extends WorkspaceJob {

	ArrayList<SPDXFile> files = new ArrayList<SPDXFile>();
	private SpdxProject project;
	private IFile spdxResource;

	public CreateSpdxJob(SpdxProject project, IFile spdxResource) {
		super("Creating SPDX");
		this.project = project;
		this.spdxResource = spdxResource;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		try {
			project.create(spdxResource, monitor);
			project.refresh(spdxResource, monitor);

			project.save(monitor);
			monitor.done();
			ArrayList<String> verify = project.getSpdxDoc().verify();
			if (verify != null && verify.size() > 0) {
				StringBuilder msg = new StringBuilder();
				msg.append(verify.get(0));
				for (int i = 1; i < verify.size(); i++) {
					msg.append('\n');
					msg.append(verify.get(i));
				}
				return new Status(Status.ERROR, Activator.PLUGIN_ID, 
						msg.toString());
			} else {
				return Status.OK_STATUS;
			}
		} catch (SpdxProjectException e) {
			return new Status(Status.ERROR, Activator.PLUGIN_ID, Status.OK, "Error creating SPDX project: "+e.getMessage(), e);
		}
	}
}
