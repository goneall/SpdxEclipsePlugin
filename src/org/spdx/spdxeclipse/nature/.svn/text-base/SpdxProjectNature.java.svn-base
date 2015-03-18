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
package org.spdx.spdxeclipse.nature;

import java.util.ArrayList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.builders.SpdxBuilder;

/**
 * Project Nature for SPDX builder
 * @author Gary O'Neall
 *
 */
public class SpdxProjectNature implements IProjectNature {

	public static final String ID = "spdxNature";
	public static final String ID_FULLY_QUALIFIED = Activator.PLUGIN_ID + "." + ID;
	private IProject project;

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	@Override
	public void configure() throws CoreException {
		// Add builder
		IProjectDescription desc = this.getProject().getDescription();
		ICommand[] commands = desc.getBuildSpec();
		boolean builderFound = false;
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].getBuilderName().equals(SpdxBuilder.ID)) {
				builderFound = true;
				break;
			}
		}
		if (!builderFound) {
			ICommand builderCommand = desc.newCommand();
			builderCommand.setBuilderName(SpdxBuilder.ID);
			ICommand[] newCommands = new ICommand[commands.length + 1];
		    System.arraycopy(commands, 0, newCommands, 1, commands.length);
		      newCommands[0] = builderCommand;
		      desc.setBuildSpec(newCommands);
		      project.setDescription(desc, new NullProgressMonitor());
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	@Override
	public void deconfigure() throws CoreException {
		// Remove Builder
		IProjectDescription desc = this.getProject().getDescription();
		ICommand[] commands = desc.getBuildSpec();
		
		ArrayList<ICommand> newCommands = new ArrayList<ICommand>();
		for (int i = 0; i < commands.length; i++) {
			if (!commands[i].getBuilderName().equals(SpdxBuilder.ID)) {
				newCommands.add(commands[i]);
			}
		}
		if (newCommands.size() < commands.length) {
			desc.setBuildSpec(newCommands.toArray(new ICommand[newCommands.size()]));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	@Override
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
