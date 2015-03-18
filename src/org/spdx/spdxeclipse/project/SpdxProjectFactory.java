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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.nature.SpdxProjectNature;

/**
 * Controls the lifecycle of SpdxProject objects.
 * 
 * There is at most one SpdxProject per project utilizing the Spdx builders.
 * 
 * The <code>getSpdxProject(String projectName)</code> will return the one SpdxProject for the projectName, creating it if necessary
 * 
 * <code>closeSpdxProject(String projectName)</code> will flush any data, clean up, and remove the instance for the project.  This should only be called when the workspace is shutting down.
 * @author Gary O'Neall
 *
 */
public class SpdxProjectFactory {
	
	/**
	 * Map of project names to SpdxProject
	 */
	static HashMap<String, SpdxProject> openProjects = new HashMap<String, SpdxProject>();

	/**
	 * Constant for all SPDX project build jobs
	 */
	public static final Object SPDX_BUILD_JOB_FAMILY = new Object();
	
	public static synchronized SpdxProject getSpdxProject(String projectName, IProgressMonitor monitor) throws IOException, InvalidSPDXAnalysisException, SpdxProjectException {
		SpdxProject retval = openProjects.get(projectName);
		if (retval == null) {
			monitor.beginTask("Opening SPDX Project "+projectName, IProgressMonitor.UNKNOWN);
			retval = new SpdxProject(projectName);
			openProjects.put(projectName, retval);
		}
		return retval;
	}
	
	public static synchronized void removeSpdxProject(String projectName, IProgressMonitor monitor) throws IOException, InvalidSPDXAnalysisException, SpdxProjectException, CoreException {
		SpdxProject spdxProject = getSpdxProject(projectName, monitor);
		spdxProject.disableSpdx();
		IProject project = spdxProject.getProject();
		closeSpdxProject(projectName);
		removeNature(project);
	}
	
	public static synchronized void closeSpdxProject(String projectName) {
		SpdxProject project = openProjects.get(projectName);
		if (project == null) {
			// one could argue we throw an exception, but we will just return since there is 
			// nothing to close
			return;
		}
		try {
			project.close(new NullProgressMonitor());
		} catch (SpdxProjectException e) {
			Activator.getDefault().logError("Error closing SPDX Project "+projectName, e);
		}
		openProjects.remove(projectName);
	}
	
	public static synchronized void closeAllSpdxProjects() {
		Iterator<SpdxProject> iter = openProjects.values().iterator();
		while (iter.hasNext()) {
			try {
				iter.next().close(new NullProgressMonitor());
			} catch (SpdxProjectException e) {
				Activator.getDefault().logError("Error closing SPDX Project", e);
			}
		}
		openProjects.clear();
	}

	public static boolean isSpdxProjectOpen(String projectName) {
		return openProjects.containsKey(projectName);
	}
	
	private static void removeNature(IProject project) throws SpdxProjectException {
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
		      if (natureExists) {
			      ArrayList<String> newNatures = new ArrayList<String>();
			      for (String nature : natures) {
			    	  if (!nature.equals(SpdxProjectNature.ID_FULLY_QUALIFIED)) {
			    		  newNatures.add(nature);
			    	  }
			      }
			      description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
			      project.setDescription(description, new NullProgressMonitor());
		      }

		   } catch (CoreException e) {
			   Activator.getDefault().logError("Error adding project nature", e);			   
			   throw(new SpdxProjectException("Error adding project nature.  See log for details.",e));
		   }
		}
}
