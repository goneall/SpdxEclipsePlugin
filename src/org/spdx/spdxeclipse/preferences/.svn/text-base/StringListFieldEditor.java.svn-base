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
package org.spdx.spdxeclipse.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.spdx.spdxeclipse.ui.StringSelectionGroup;

/**
 * Field editor for simple list of strings
 * @author Gary O'Neall
 *
 */
public class StringListFieldEditor extends FieldEditor {
	
	private static final String DELIM = ";";
	Composite parent;
	private StringSelectionGroup stringSelection;

	public StringListFieldEditor(String preferenceName,
			String label, Composite fieldEditorParent) {
		super(preferenceName, label, fieldEditorParent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = ((GridData)parent.getLayoutData());
		gd.horizontalSpan = numColumns;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		this.parent = parent;
		this.stringSelection = new StringSelectionGroup(parent, SWT.NONE, this.getLabelText(), new String[0], numColumns);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		String unparsedList = getPreferenceStore().getString(getPreferenceName());
		String[] list = parseList(unparsedList);
		this.stringSelection.setList(list);
	}

	private String[] parseList(String unparsedList) {
		if (unparsedList == null || unparsedList.trim().isEmpty()) {
			return new String[0];
		}
		String[] retval = unparsedList.split(DELIM);
		for (int i = 0; i < retval.length; i++) {
			retval[i] = retval[i].trim();
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		String unparsedList = getPreferenceStore().getDefaultString(getPreferenceName());
		String[] list = parseList(unparsedList);
		this.stringSelection.setList(list);	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	@Override
	protected void doStore() {
		StringBuilder sb = new StringBuilder();
		String[] list = this.stringSelection.getList();
		if (list != null && list.length > 0) {
			sb.append(list[0]);
			for (int i = 1; i < list.length; i++) {
				sb.append(DELIM);
				sb.append(list[i]);
			}
		}
		getPreferenceStore().setValue(getPreferenceName(), sb.toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	@Override
	public int getNumberOfControls() {
		return 2;
	}

}
