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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.spdx.rdfparser.SpdxRdfConstants;

/**
 * Field editor for a single creator
 * @author Gary O'Neall
 *
 */
public class CreatorFieldEditor extends FieldEditor {

	private Composite parent;
	private static String[] CREATOR_PREFIXES = {SpdxRdfConstants.CREATOR_PREFIX_PERSON,
		SpdxRdfConstants.CREATOR_PREFIX_ORGANIZATION, SpdxRdfConstants.CREATOR_PREFIX_TOOL};
	private Text tbCreator;
	private Combo comboCreatorPrefixes;

	public CreatorFieldEditor(String preference, String title,
			Composite parent) {
		super(preference, title, parent);
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
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numColumns;
		this.parent.setLayoutData(gd);
		
		Label lblTitle = getLabelControl(parent);
		GridData lblData = new GridData();
		lblData.horizontalSpan = numColumns;
		lblTitle.setLayoutData(lblData);
		
		createCreatorComposite(parent);
	}

	private void createCreatorComposite(Composite parent) {
		Composite creatorComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		creatorComposite.setLayoutData(gd);
		creatorComposite.setLayout(new GridLayout(2, false));
		
		this.comboCreatorPrefixes = new Combo(creatorComposite, SWT.BORDER | SWT.READ_ONLY);
		this.comboCreatorPrefixes.setItems(CREATOR_PREFIXES);
		this.comboCreatorPrefixes.setText(CREATOR_PREFIXES[0]);
		this.tbCreator = new Text(creatorComposite, SWT.BORDER);
		this.tbCreator.setToolTipText("Creator for the SPDX document (can be a person, organzation, or a tool)");
		GridData gdCreator = new GridData();
		gdCreator.grabExcessHorizontalSpace = true;
		gdCreator.horizontalAlignment = SWT.FILL;
		tbCreator.setLayoutData(gdCreator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		String unparsed = getPreferenceStore().getString(getPreferenceName());
		parsePropertySetText(unparsed);
	}

	private void parsePropertySetText(String unparsed) {
		for (int i = 0; i < CREATOR_PREFIXES.length; i++) {
			if (unparsed.startsWith(CREATOR_PREFIXES[i])) {
				comboCreatorPrefixes.setText(CREATOR_PREFIXES[i]);
				tbCreator.setText(unparsed.substring(CREATOR_PREFIXES[i].length()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		String unparsed = getPreferenceStore().getDefaultString(getPreferenceName());
		parsePropertySetText(unparsed);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	@Override
	protected void doStore() {
		if(!tbCreator.getText().trim().isEmpty()) {
			getPreferenceStore().setValue(getPreferenceName(), 
					this.comboCreatorPrefixes.getText() + this.tbCreator.getText());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	@Override
	public int getNumberOfControls() {
		return 1;
	}

}
