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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxeclipse.Activator;
import org.spdx.spdxeclipse.project.SpdxProject;

/**
 * Wizard page for SPDX reviewer information
 * @author Gary O'Neall
 *
 */
public class SpdxReviewerWizardPage extends WizardPage implements IWizardPage {
	
	static DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
	
	static final String[] REVIEWER_PREFIXES = {SpdxRdfConstants.CREATOR_PREFIX_PERSON,
			SpdxRdfConstants.CREATOR_PREFIX_ORGANIZATION, SpdxRdfConstants.CREATOR_PREFIX_TOOL};
	
	static final String[] TABLE_TITLES = {"Reviewer Name", "Comment"};
	static final int[] COLUMN_WIDTH = {200, 300};
	
	static final int REVIEWER_NAME_COL = 0;
	static final int REVIEWER_COMMENT_COL = 1;
	
	Text tbReviewerName;
	Text tbReviewerComment;
	private Combo comboReviewerPrefixes;
	private Table tblReviewers;
	private Button btAdd;
	private Button btDelete;

	public SpdxReviewerWizardPage(SpdxProject spdxProject) {
		super("SPDX Reviewers");
		this.setDescription("Reviewer information for SPDX");
		this.setPageComplete(false);
		this.setTitle("SPDX Reviewers");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite reviewerGroup = new Composite(parent, SWT.NONE);
		this.setControl(reviewerGroup);
		reviewerGroup.setLayout(new GridLayout(3, false));
		GridData gridDataGroup = new GridData();
		gridDataGroup.grabExcessHorizontalSpace = true;
		gridDataGroup.horizontalAlignment = GridData.FILL;
		gridDataGroup.grabExcessVerticalSpace = true;
		gridDataGroup.verticalAlignment = GridData.FILL;
		reviewerGroup.setLayoutData(gridDataGroup);
		
		Label lblReviewerName = new Label(reviewerGroup, SWT.NONE);
		lblReviewerName.setText("Reviewer: ");
		
		this.comboReviewerPrefixes = new Combo(reviewerGroup, SWT.BORDER | SWT.READ_ONLY);
		this.comboReviewerPrefixes.setItems(REVIEWER_PREFIXES);
		this.comboReviewerPrefixes.setText(REVIEWER_PREFIXES[0]);
		
		this.tbReviewerName = new Text(reviewerGroup, SWT.BORDER);
		this.tbReviewerName.setToolTipText("SPDX Reviewer name");
		GridData gdReviewer = new GridData();
		gdReviewer.grabExcessHorizontalSpace = true;
		gdReviewer.horizontalAlignment = SWT.FILL;
		tbReviewerName.setLayoutData(gdReviewer);
		tbReviewerName.addListener(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				btAdd.setEnabled(!tbReviewerName.getText().trim().isEmpty());
			}
			
		});
		
		Label lblReviewerComment = new Label(reviewerGroup, SWT.NONE);
		lblReviewerComment.setText("Comment: ");
		
		this.tbReviewerComment = new Text(reviewerGroup, SWT.BORDER);
		this.tbReviewerComment.setToolTipText("Optional comment for this reviewer");
		GridData gdReviewerComment = new GridData();
		gdReviewerComment.grabExcessHorizontalSpace = true;
		gdReviewerComment.horizontalAlignment = SWT.FILL;
		gdReviewerComment.horizontalSpan = 2;
		this.tbReviewerComment.setLayoutData(gdReviewerComment);
		
		GridData gdReviewerTable = new GridData();
		gdReviewerTable.grabExcessHorizontalSpace = true;
		gdReviewerTable.grabExcessVerticalSpace = true;
		gdReviewerTable.horizontalAlignment = SWT.FILL;
		gdReviewerTable.verticalAlignment = SWT.FILL;
		gdReviewerTable.horizontalSpan = 3;
		createReviewerTable(reviewerGroup, gdReviewerTable);
		
		GridData gdButtons = new GridData();
		gdButtons.horizontalAlignment = SWT.CENTER;
		gdButtons.horizontalSpan = 3;
		createButtonGroup(reviewerGroup, gdButtons);
		
		setPageComplete(true);	// all optional information
	}
	private void createButtonGroup(Composite parent, GridData layoutData) {
		Group buttonGroup = new Group(parent, SWT.NONE);
		buttonGroup.setLayout(new GridLayout(2, false));
		buttonGroup.setLayoutData(layoutData);
		
		btAdd = new Button(buttonGroup, SWT.PUSH);
		btAdd.setText("Add/Update");
		btAdd.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (tbReviewerName.getText().trim().isEmpty()) {
					return;
				}
				String reviewer = comboReviewerPrefixes.getText()+tbReviewerName.getText().trim();
				// check for duplicates
				TableItem[] existingItems = tblReviewers.getItems();
				boolean dup = false;
				int i;
				for (i = 0; i < existingItems.length; i++) {
					if (existingItems[i].getText(REVIEWER_NAME_COL).equals(reviewer)) {
						dup = true;
						break;
					}
				}
				if (dup) {
					existingItems[i].setText(REVIEWER_COMMENT_COL, tbReviewerComment.getText().trim());
				} else {
					TableItem newRow = new TableItem(tblReviewers, SWT.NONE);
					newRow.setText(REVIEWER_NAME_COL, reviewer);
					newRow.setText(REVIEWER_COMMENT_COL, tbReviewerComment.getText().trim());
				}
				btDelete.setEnabled(true);
				tbReviewerName.setText("");
				tbReviewerComment.setText("");
				btAdd.setEnabled(false);
			}
			
		});
		btAdd.setEnabled(false);
		btDelete = new Button(buttonGroup, SWT.PUSH);
		btDelete.setText("Remove");
		
		btDelete.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				int[] toDelete = tblReviewers.getSelectionIndices();
				Arrays.sort(toDelete);
				
				for (int i = toDelete.length-1; i >= 0; i--) {
					tblReviewers.remove(toDelete[i]);
				}
				if (tblReviewers.getItemCount() < 1) {
					btDelete.setEnabled(false);
				}
			}
			
		});
		btDelete.setEnabled(false);
	}
	
	private void createReviewerTable(Composite parent,
			GridData layoutData) {
		this.tblReviewers = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		this.tblReviewers.setLayoutData(layoutData);
		for (int i = 0; i < TABLE_TITLES.length; i++) {
			TableColumn col = new TableColumn(this.tblReviewers, SWT.NONE);
			col.setText(TABLE_TITLES[i]);
			if (COLUMN_WIDTH[i] > 0) {
				col.setWidth(COLUMN_WIDTH[i]);
			}
		}
		this.tblReviewers.setHeaderVisible(true);
		this.tblReviewers.setLinesVisible(true);
	}
	public boolean updateSpdx(SpdxProject spdxProject) {
		
		TableItem[] reviewerItems = tblReviewers.getItems();
		if (reviewerItems == null || reviewerItems.length == 0) {
			return true;
		}
		String today = format.format(new Date());
		SPDXReview[] reviewers = new SPDXReview[reviewerItems.length];
		for (int i = 0; i < reviewerItems.length; i++) {
			reviewers[i] = new SPDXReview(reviewerItems[i].getText(REVIEWER_NAME_COL), 
					today, reviewerItems[i].getText(REVIEWER_COMMENT_COL));
		}

		try {
			spdxProject.getSpdxDoc().setReviewers(reviewers);
		} catch (InvalidSPDXAnalysisException e) {
			Activator.getDefault().logError("Unable to set SPDX reviewers", e);
			MessageDialog.openError(getShell(), "Error", "Unable to set SPDX reviewers: "+e.getMessage());
			return false;
		}
		return true;
	}

}
