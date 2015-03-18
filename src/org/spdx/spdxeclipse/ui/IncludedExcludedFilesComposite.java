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

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Composite used for the included and excluded file groups for SPDX
 * @author Gary O'Neall
 *
 */
public class IncludedExcludedFilesComposite extends Composite {
	
	private IProject project;
	private ResourceTreeSelectionGroup includedFilesGroup;
	private Button btAddExcluded;
	private Button btRemoveExcluded;
	private List excludedFiles;
	private HashMap<String, Pattern> excludedFilePatterns = new HashMap<String, Pattern>();


	public IncludedExcludedFilesComposite(Composite cparent, int style, IProject project, String[] includedDirectories, String[] excludedFilesPattern) {
		super(cparent, style);
		this.project = project;

		this.setLayout(new GridLayout(1, true));
		GridData gridDataGroup = new GridData();
		gridDataGroup.grabExcessHorizontalSpace = true;
		gridDataGroup.horizontalAlignment = SWT.FILL;
		gridDataGroup.grabExcessVerticalSpace = true;
		gridDataGroup.verticalAlignment = SWT.FILL;
		this.setLayoutData(gridDataGroup);
		
		Label lblResourceInclude = new Label(this, SWT.None);
		lblResourceInclude.setText("Select the resource to include:");
		
		createIncludeFilesComposite(includedDirectories);
		
		Label lblExcludedFilesPattern = new Label(this, SWT.None);
		lblExcludedFilesPattern.setText("Enter file patterns to exclude:");
		
		createExcludedFilesComposite(excludedFilesPattern);
		ViewerFilter filter = new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IResource) {
					return !isExcluded(((IResource)element).getFullPath());
				} else {
					return true;
				}
			}
			
		};
		this.includedFilesGroup.addViewerFilter(filter);
	}
	
	public void addIncludeFileTreeListener(IResourceTreeSelectionChangeListener listener) {
		this.includedFilesGroup.addChangeListener(listener);
	}
	
	public void removeIncludeFileTreeListener(IResourceTreeSelectionChangeListener listener) {
		this.includedFilesGroup.removeChangeListener(listener);
	}
	
	private void createIncludeFilesComposite(String[] includedDirectories) {
		this.includedFilesGroup = new ResourceTreeSelectionGroup(this, project, includedDirectories);
		this.includedFilesGroup.refresh();	// for the change in the tree viewer filter
	}

	private void createExcludedFilesComposite(String[] excludedFilesPattern) {
		Composite excludeFilesComposite = new Composite(this, SWT.BORDER);
		GridLayout layout = new GridLayout(4, false);
		excludeFilesComposite.setLayout(layout);
		GridData gdComposite = new GridData();
		gdComposite.grabExcessHorizontalSpace = true;
		gdComposite.horizontalAlignment = SWT.FILL;
		gdComposite.verticalAlignment = SWT.FILL;
		gdComposite.heightHint = 200;
		excludeFilesComposite.setLayoutData(gdComposite);
		Label lblFileRegex = new Label(excludeFilesComposite, SWT.NONE);
		lblFileRegex.setText("File Name Regex: ");
		lblFileRegex.setToolTipText("Regular expression for a file name which, if matched, is excluded");
		final Text fileRegex = new Text(excludeFilesComposite, SWT.NONE);
		fileRegex.setToolTipText("Regular expression for a file name which, if matched, is excluded");
		GridData gdFileRegex = new GridData();
		gdFileRegex.horizontalAlignment = SWT.FILL;
		gdFileRegex.grabExcessHorizontalSpace = true;
		fileRegex.setLayoutData(gdFileRegex);
		fileRegex.addListener(SWT.FocusOut, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (fileRegex.getText().isEmpty()) {
					btAddExcluded.setEnabled(false);
				} else {
					btAddExcluded.setEnabled(true);
				}
			}
			
		});
		
		btAddExcluded = new Button(excludeFilesComposite, SWT.PUSH);
		btAddExcluded.setText("Add");
		btAddExcluded.setEnabled(false);
		btAddExcluded.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				try {
					addExcluded(fileRegex.getText());
					excludedFiles.add(fileRegex.getText());
					fileRegex.setText("");
					btAddExcluded.setEnabled(false);
				} catch(PatternSyntaxException ex) {
					MessageDialog.openError(getShell(), "Invalid Pattern", "Invalid pattern: "+ex.getMessage());
					return;
				}
			}
		});
		
		btRemoveExcluded = new Button(excludeFilesComposite, SWT.PUSH);
		btRemoveExcluded.setText("Remove");
		btRemoveExcluded.setEnabled(false);
		btRemoveExcluded.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				String[] toBeRemoved = excludedFiles.getSelection();
				for (int i = 0; i < toBeRemoved.length; i++) {
					removeExcluded(toBeRemoved[i]);
					excludedFiles.remove(toBeRemoved[i]);
				}
				btRemoveExcluded.setEnabled(false);
			}
			
		});
		
		excludedFiles = new List(excludeFilesComposite, SWT.NONE);
		excludedFiles.setToolTipText("Regular expression for a file name which, if matched, is excluded");
		GridData gdExcludedFiles = new GridData();
		gdExcludedFiles.verticalAlignment = SWT.FILL;
		gdExcludedFiles.horizontalAlignment = SWT.FILL;
		gdExcludedFiles.grabExcessHorizontalSpace = true;
		gdExcludedFiles.grabExcessVerticalSpace = true;
		gdExcludedFiles.horizontalSpan = 4;
		excludedFiles.setLayoutData(gdExcludedFiles);
		for (int i = 0; i < excludedFilesPattern.length; i++) {
			excludedFiles.add(excludedFilesPattern[i]);
			addExcluded(excludedFilesPattern[i]);
		}
		excludedFiles.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (excludedFiles.getSelectionCount() > 0) {
					btRemoveExcluded.setEnabled(true);
				} else {
					btRemoveExcluded.setEnabled(false);
				}
			}
			
		});
	}


	/**
	 * Returns true if the resource path provided is excluded based on the 
	 * excluded file strings
	 * @param path
	 * @return
	 */
	protected boolean isExcluded(IPath path) {
		int numSegments = path.segmentCount();
		for (int i = 0; i < numSegments; i++) {
			if (isSegmentExcluded(path.segment(i))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isSegmentExcluded(String segment) {
		Iterator<Pattern> iter = this.excludedFilePatterns.values().iterator();
		while (iter.hasNext()) {
			if (iter.next().matcher(segment).matches()) {
				return true;
			}
		}
		return false;
	}

	protected void removeExcluded(String fileRegex) {
		this.excludedFilePatterns.remove(fileRegex);
		this.includedFilesGroup.refresh();
	}
	
	private void addExcluded(String fileRegex) throws PatternSyntaxException {
		Pattern fileRegexPattern = null;
		fileRegexPattern = Pattern.compile(fileRegex);
		this.excludedFilePatterns.put(fileRegex, fileRegexPattern);
		this.includedFilesGroup.refresh();	// for the change in the tree viewer filter
	}

	public String[] getIncludedResourcePaths() {
		return this.includedFilesGroup.getSelectedResourcePaths();
	}

	public String[] getExcludeFilePatterns() {
		return excludedFilePatterns.keySet().toArray(new String[excludedFilePatterns.size()]);
	}

	public boolean hasSelectedResourcePaths() {
		return this.includedFilesGroup.hasSelectedResourcePaths();
	}

	public void setExcludedPatterns(String[] excludedFilesPattern) {
		// remove the old
		String[] currentExcluded = this.getExcludeFilePatterns();
		for (int i = 0; i < currentExcluded.length; i++) {
			removeExcluded(currentExcluded[i]);
			excludedFiles.remove(currentExcluded[i]);
		}
		// add the new
		for (int i = 0; i < excludedFilesPattern.length; i++) {
			excludedFiles.add(excludedFilesPattern[i]);
			addExcluded(excludedFilesPattern[i]);
		}
		if (excludedFilesPattern.length > 0) {
			this.btRemoveExcluded.setEnabled(true);
		} else {
			this.btRemoveExcluded.setEnabled(false);
		}
	}

	public void setIncludedDirectories(String[] includedDirectories) {
		this.includedFilesGroup.setIncludedDirectories(includedDirectories);
	}
	
}
