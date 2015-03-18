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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;

/**
 * Common UI code
 * @author Gary O'Neall
 *
 */
public class UIHelper {

	/**
	 * This class should not be instantiated - it only contains static methods
	 */
	protected UIHelper() {
		
	}
	
	public static String chooseProject() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		int countOpenProjects = 0;
		for (int i = 0; i < allProjects.length; i++) {
			if (allProjects[i].isOpen()) {
				countOpenProjects ++;
			}
		}
		String[] openProjectNames = new String[countOpenProjects];
		int j = 0;
		for (int i = 0; i < allProjects.length; i++) {
			if (allProjects[i].isAccessible()) {
				openProjectNames[j++] = allProjects[i].getName();
			}
		}
		ProjectChooserDialog chooser = new ProjectChooserDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), openProjectNames);
		int status = chooser.open();
		String retval = null;
		if (status == Dialog.OK) {
			retval = chooser.getChoosenProject();
		}
		chooser.close();
		return retval;
	}

}
