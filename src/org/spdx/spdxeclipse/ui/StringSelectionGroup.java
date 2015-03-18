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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;


/**
 * SWT Widget for displaying and selecting Strings
 * @author Gary O'Neall
 *
 */
public class StringSelectionGroup {
	
	List selectionList;
	private Button btAdd;
	private Button btRemove;
	private Text text;
	private String title;
	private String[] initialSelection;
	private Group selectionGroup;

	public StringSelectionGroup(Composite parent, int style, String title,
			String[] initialSelection, int numColumns) {
		this.title = title;
		this.initialSelection = initialSelection;
		createControl(parent, style, numColumns);
	}

	private void createControl(Composite parent, int style, int numColumns) {
		this.selectionGroup = new Group(parent, style);
		GridLayout layout = new GridLayout();
		this.selectionGroup.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = numColumns;
		this.selectionGroup.setLayoutData(gd);
		this.selectionGroup.setText(title);
		
		selectionList = new List(selectionGroup, SWT.BORDER | SWT.V_SCROLL);
		GridData gdList = new GridData(GridData.FILL_HORIZONTAL);
		gdList.heightHint = 50;
		selectionList.setLayoutData(gdList);
		if (this.initialSelection != null) {
			for (int i = 0; i < this.initialSelection.length; i++) {
				selectionList.add(this.initialSelection[i]);
			}
		}
		
		selectionList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				btRemove.setEnabled(selectionList.getSelectionIndex() >= 0);
			}
		});
		
		addRemoveComposite(selectionGroup);
	}
	
	private void addRemoveComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gd);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		btAdd = new Button(composite, SWT.None);
		btAdd.setText("Add");
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {	
				String selectedText = text.getText();
				if (selectedText != null && !selectedText.isEmpty()) {
					selectionList.add(selectedText);
				}
				text.setText("");
			}
		});
		
		text = new Text(composite, SWT.BORDER);
		GridData txtGd = new GridData(GridData.FILL_HORIZONTAL);
		txtGd.verticalAlignment = GridData.BEGINNING;
		text.setLayoutData(txtGd);
		
		btRemove = new Button(composite, SWT.None);
		btRemove.setText("Remove");
		btRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {	
				selectionList.remove(selectionList.getSelectionIndex());
				btRemove.setEnabled(false);
			}
		});
		btRemove.setEnabled(false);
	}

	public String[] getList() {
		return this.selectionList.getItems();
	}

	public void setList(String[] newSelectedList) {
		this.selectionList.removeAll();
		if (newSelectedList != null) {
			for (int i = 0; i < newSelectedList.length; i++) {
				this.selectionList.add(newSelectedList[i]);
			}
		}
	}
}
