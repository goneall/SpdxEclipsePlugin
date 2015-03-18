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

import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.spdx.spdxeclipse.Activator;

/**
 * Selection dialog for license names
 * @author Gary O'Neall
 *
 */
public class LicensePicker extends FilteredItemsSelectionDialog {
	
	private class LicenseSelectionHistory extends SelectionHistory {

		@Override
		protected Object restoreItemFromMemento(IMemento memento) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void storeItemToMemento(Object item, IMemento memento) {
			// TODO Auto-generated method stub
			
		}
		
	}

	private String[] licenseNames;
	private static final String DIALOG_SETTINGS = "FilteredResourceLicensePickerSettings";

	public LicensePicker(Shell shell, String[] licenseNames, boolean multi) {
		super(shell, multi);
		this.licenseNames = licenseNames;
		this.setTitle("Select License");
		this.setSelectionHistory(new LicenseSelectionHistory());
	}

	public LicensePicker(Shell shell, String[] licenseNames, boolean multi, String initialInput) {
		this(shell, licenseNames, multi);
		this.setInitialPattern(initialInput);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createExtendedContentArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getDialogSettings()
	 */
	@Override
	protected IDialogSettings getDialogSettings() {
    	IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);
    	if (settings == null) {
    		settings = Activator.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
    	}
    	return settings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
	 */
	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFilter()
	 */
	@Override
	protected ItemsFilter createFilter() {
		return new ItemsFilter() {

			@Override
			public boolean matchItem(Object item) {
				return matches(item.toString());
			}

			@Override
			public boolean isConsistentItem(Object item) {
				return true;
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemsComparator()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected Comparator getItemsComparator() {
		return new Comparator() {

			@Override
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContentProvider(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractContentProvider, org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
			throws CoreException {
		progressMonitor.beginTask("Adding License Names", this.licenseNames.length);
		for (int i = 0; i < licenseNames.length; i++) {
			contentProvider.add(this.licenseNames[i], itemsFilter);
			progressMonitor.worked(1);
		}
		progressMonitor.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getElementName(java.lang.Object)
	 */
	@Override
	public String getElementName(Object item) {
		return item.toString();
	}

}
