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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * Simple dialog to choose a project
 * @author Gary O'Neall
 *
 */
public class ProjectChooserDialog extends Dialog {
	
	String[] projects;
	List list = null;
	String choosenProject = null;

	protected ProjectChooserDialog(Shell parentShell, String[] projects) {
		super(parentShell);
		this.projects = projects;
	}

	public String getChoosenProject() {
		return choosenProject;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
	      list = new List(composite, SWT.BORDER | SWT.SINGLE);
	      list.setItems(this.projects);
	      list.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button okButton = getButton(IDialogConstants.OK_ID);
				if (list.getSelection() != null && list.getSelection().length > 0) {					
				      okButton.setEnabled(true);	
				      choosenProject = list.getSelection()[0];
				} else {
					okButton.setEnabled(false);
					choosenProject = null;
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				Button okButton = getButton(IDialogConstants.OK_ID);
				if (list.getSelection() != null && list.getSelection().length > 0) {					
				      okButton.setEnabled(true);	
				      choosenProject = list.getSelection()[0];
				} else {
					okButton.setEnabled(false);
					choosenProject = null;
				}
			}
	    	  
	      });
	      return composite;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Choose Project");
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
	    Button okButton = this.getButton(IDialogConstants.OK_ID);
	    okButton.setEnabled(false);	// disable the OK button until a selection is made
		return ctrl;
	}
}
