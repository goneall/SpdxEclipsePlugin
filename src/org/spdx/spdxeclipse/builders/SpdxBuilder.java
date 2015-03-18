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

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.project.SpdxProject;
import org.spdx.spdxeclipse.project.SpdxProjectException;
import org.spdx.spdxeclipse.project.SpdxProjectFactory;

/**
 * Project builder to maintain the SPDX file for projects with SPDX enabled
 * @author Gary O'Neall
 *
 */
public class SpdxBuilder extends IncrementalProjectBuilder {
	
	public class SpdxDeltaVisitor implements IResourceDeltaVisitor {
		
		SpdxProject project;
		IProgressMonitor monitor;

		public SpdxDeltaVisitor(SpdxProject project, IProgressMonitor monitor) {
			this.project = project;
			this.monitor = monitor;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (monitor.isCanceled()) {
				project.setFileRescanRequired(true);
				return false;
			}
			if (delta.getResource().getType() == IResource.FILE) {
				IFile file = (IFile)(delta.getResource());

				try {
					if (project.fileShouldBeIncluded(file)) {
						if (delta.getKind() == IResourceDelta.ADDED) {
							project.addFile(file, monitor);
						} else if (delta.getKind() == IResourceDelta.CHANGED ||
								delta.getKind() == IResourceDelta.REPLACED) {
							project.updateFile(file, monitor);
						} else if (delta.getKind() == IResourceDelta.REMOVED) {
							project.removeFile(file, monitor);
						}
					}
				} catch (SpdxProjectException ex) {
					error(file, ex.getMessage(), ex);
				}
			}
			return true;
		}
		
	}
	
	public static final String ID = Activator.PLUGIN_ID + "."  + "spdxBuilder";
	public static final String SPDX_MARKER_ID = Activator.PLUGIN_ID + "."  + "spdxMarker";
	public static final String SPDX_PROBLEM_MARKER_ID = Activator.PLUGIN_ID + "."  + "spdxProblem";

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {
		SpdxProject spdxProject = null;
		try {
			spdxProject = SpdxProjectFactory.getSpdxProject(getProject().getName(), monitor);
		} catch (IOException e) {
			error(getProject(), "IO error building SPDX project", e);
			return null;
		} catch (InvalidSPDXAnalysisException e) {
			error(getProject(), "SPDX Analysis error building SPDX project", e);
			return null;
		} catch (SpdxProjectException e) {
			error(getProject(), "SPDX project error building SPDX project", e);
			return null;
		} 
		if (monitor.isCanceled()) {
			return null;
		}
		if (!spdxProject.isCreated()) {
//			error(getProject(), "Can not build "+getProject().getName()+".  SPDX project has not been created.", 
//					new SpdxProjectException("Attempting to build SPDX project when no project exists"));
			return null;
		}
		if (kind == IncrementalProjectBuilder.FULL_BUILD || 
				kind == IncrementalProjectBuilder.CLEAN_BUILD ||
				spdxProject.isFileRescanRequired()) {
			fullBuild(spdxProject, monitor);
		} else {
			IResourceDelta delta = this.getDelta(getProject());
			if (delta == null) {
				fullBuild(spdxProject, monitor);
			} else {
				incrementalBuild(spdxProject, delta, monitor);
			}
		}
		try {
			spdxProject.save(monitor);
		} catch (SpdxProjectException e) {
			error(getProject(), "Error saving SPDX document", e);
		}
		return null;
	}

	private void incrementalBuild(SpdxProject spdxProject, IResourceDelta delta, IProgressMonitor monitor) {
		if (spdxProject.isCreated()) {
			try {
				delta.accept(new SpdxDeltaVisitor(spdxProject, monitor));
			} catch (CoreException e) {
				error(delta.getResource(), "Error performing incremental build", e);
			}
		}
	}

	private void fullBuild(SpdxProject spdxProject, IProgressMonitor monitor) {
		try {
			if (spdxProject.isCreated()) {
				spdxProject.refresh(monitor);
			}
		} catch (SpdxProjectException e) {
			error(getProject(), "SPDX project error building SPDX project", e);
		} catch (CoreException e) {
			error(getProject(), "Error building SPDX project", e);
		}
	}

	private void error(IResource resource, String msg, Exception e) {
		Activator.getDefault().logError(msg, e);
		try {
			IMarker errorMarker = resource.createMarker(SPDX_PROBLEM_MARKER_ID);
			errorMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			errorMarker.setAttribute(IMarker.MESSAGE, msg);
			errorMarker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
		} catch (CoreException e1) {
			Activator.getDefault().logError("Unable to set problem marker for build error.", e1);
		}
	}

	@Override
    protected void startupOnInitialize() {
        super.startupOnInitialize();
     }
	
	@Override
     protected void clean(IProgressMonitor monitor) throws CoreException {
        super.clean(monitor);
        IMarker[] markers = getProject().findMarkers(SPDX_PROBLEM_MARKER_ID, true, IResource.DEPTH_INFINITE);
        if (markers != null) {
        	for (int i = 0; i < markers.length; i++) {
        		markers[i].delete();
        	}
        }
        SpdxProject spdxProject = null;
		try {
			spdxProject = SpdxProjectFactory.getSpdxProject(getProject().getName(), monitor);
		} catch (IOException e) {
			error(getProject(), "IO error building SPDX project", e);
			return;
		} catch (InvalidSPDXAnalysisException e) {
			error(getProject(), "SPDX Analysis error building SPDX project", e);
			return;
		} catch (SpdxProjectException e) {
			error(getProject(), "SPDX project error building SPDX project", e);
			return;
		} 
		try {
			if (spdxProject.isCreated()) {
				spdxProject.refresh(monitor);
			}
		} catch (SpdxProjectException e) {
			error(getProject(), "SPDX project error cleaining SPDX project", e);
			return;
		}
     }
}
