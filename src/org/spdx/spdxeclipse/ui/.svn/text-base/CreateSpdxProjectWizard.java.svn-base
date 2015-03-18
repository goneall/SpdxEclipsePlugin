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

import org.eclipse.jface.wizard.Wizard;
import org.spdx.spdxeclipse.project.SpdxProject;

/**
 * Wizard to gather information used in the creation of a new SPDX for a project
 * 
 * Information is stored in the projectSpdx object
 * 
 * The actual SPDX creation is not performed by the wizard.  This is to be done
 * outside of the Wizard once the Wizard successfully completes
 * 
 * @author Gary O'Neall
 *
 */
public class CreateSpdxProjectWizard extends Wizard {
	
	private SpdxProject spdxProject;
	private SpdxProjectWizardPage projectPage;
	private SpdxProjectLicenseWizardPage licensePage;
	private SpdxCreatorWizardPage creatorPage;
	private SpdxReviewerWizardPage reviewerPage;
	private IncludedFilesWizardPage includedFilesPage;
	
	public CreateSpdxProjectWizard(SpdxProject spdxProject) {
		this.spdxProject = spdxProject;
		this.projectPage = new SpdxProjectWizardPage(spdxProject);
		this.addPage(this.projectPage);
		this.licensePage = new SpdxProjectLicenseWizardPage(spdxProject);
		this.addPage(this.licensePage);
		this.creatorPage = new SpdxCreatorWizardPage(spdxProject);
		this.addPage(this.creatorPage);
		this.includedFilesPage = new IncludedFilesWizardPage(spdxProject);
		this.addPage(this.includedFilesPage);
		this.reviewerPage = new SpdxReviewerWizardPage(spdxProject);
		this.addPage(this.reviewerPage);
		this.setHelpAvailable(false);	//TODO: Add help
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if (!this.projectPage.updateSpdx(spdxProject)) {
			return false;
		}
		if (!this.licensePage.updateSpdx(spdxProject)) {
			return false;
		}
		if (!this.creatorPage.updateSpdx(spdxProject)) {
			return false;
		}
		if (!this.reviewerPage.updateSpdx(spdxProject)) {
			return false;
		}
		if (!this.includedFilesPage.updateSpdx(spdxProject)) {
			return false;
		}
		return true;
	}

}
