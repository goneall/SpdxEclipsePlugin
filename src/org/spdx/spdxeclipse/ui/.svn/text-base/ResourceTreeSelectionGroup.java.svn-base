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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.spdx.spdxeclipse.Activator;

/**
 * UI Group containing a TreeView for resources and a
 * List containing list of directory patterns which have
 * been selected
 * 
 * @author Gary O'Neall
 *
 */
public class ResourceTreeSelectionGroup implements ITreeViewerListener, ICheckStateListener, ISelectionChangedListener, Listener {
	
	private ArrayList<IResourceTreeSelectionChangeListener> listeners = new ArrayList<IResourceTreeSelectionChangeListener>();
	/**
	 * Simple type class to hold a project - used for the input to the TeeViewer
	 * @author Gary O'Neall
	 *
	 */
	public class ProjectResourceInput {
		public ProjectResourceInput(IProject project) {
			this.project = project;
		}

		public IProject project;
	}
	static final int HEIGHT = 200;	// height of the tree and list controls
	static final Comparator<IResource> RESOURCE_COMPARATOR = new Comparator<IResource>() {

		@Override
		public int compare(IResource o1, IResource o2) {
			if (o1 instanceof IContainer && !(o2 instanceof IContainer)) {			
					return -1;	// folders are sorted first
			} else if (!(o1 instanceof IContainer) && o2 instanceof IContainer) {
				return 1;
			} else {
				return o1.getLocation().toString().compareTo(o2.getLocation().toString());
			}
		}
	};
	
	static final ViewerComparator VIEW_COMPARATOR = new ViewerComparator() {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof IContainer && e2 instanceof IResource) {			
					return -1;	// folders are sorted first
			} else if (e1 instanceof IResource && e2 instanceof IContainer) {
				return 1;
			} else {
				return super.compare(viewer, e1, e2);
			}
		}
	};

	private CheckboxTreeViewer treeViewer;
	private ProjectResourceInput root;

	private ArrayList<IContainer> expandedTreeNodes;

	private TableViewer listViewer;

	public ResourceTreeSelectionGroup(Composite parent, IProject project, String[] includedDirectories) {
		root = new ProjectResourceInput(project);
		createContents(parent);
		setInitialCheckedState(includedDirectories);
	}

	private void setInitialCheckedState(final String[] includedDirectories) {
		
		this.treeViewer.getControl().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < includedDirectories.length; i++) {
					IContainer selectedFolder = root.project.getFolder(includedDirectories[i]);
					
					if (selectedFolder != null) {
						setTreeChecked(selectedFolder, true);
						updateParents(selectedFolder);
					}
				}
			}
			
		});
	}

	private void createContents(Composite parent) {
		Composite group = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		group.setLayout(layout);
		group.setLayoutData(
				new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		
		// treeViewer
		Tree tree = new Tree(group, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = HEIGHT;
		tree.setLayoutData(gd);
		this.treeViewer = new CheckboxTreeViewer(tree);
		
		final ITreeContentProvider treeContentProvider = new ITreeContentProvider() {

			ProjectResourceInput input;
			@Override
			public void dispose() {
				// Nothing to dispose, everything is in the ResourceTreeSelectionGroup
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				if (newInput instanceof ProjectResourceInput) {
					input = (ProjectResourceInput)newInput;
				}
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof ProjectResourceInput) {
					return new Object[] {input.project};
				} else {
					return getChildren(inputElement);
				}
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof IContainer) {
					IContainer container = (IContainer)parentElement;
					return getContainerChildren(container);
				} else if (parentElement instanceof ProjectResourceInput) {
					return new Object[] {input.project};
				} else {
					return new Object[0];
				}
			}

			@Override
			public Object getParent(Object element) {
				if (element.equals(input.project)) {
					return input;
				} else if (element instanceof IResource) {
					return ((IResource)element).getParent();
				} else {
					return null;
				}
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof ProjectResourceInput) {
					return true;
				} else if (element instanceof IContainer) {
					IContainer container = (IContainer)element;
					IResource[] children;
					try {
						children = container.members();
						if (children != null) {
							for (int i = 0; i < children.length; i++) {
								if (children[i] instanceof IContainer) {
									return true;
								}
							}
							return false; 	// no containers found in the above for loop
						} else {
							return false;
						}
					} catch (CoreException e) {
						Activator.getDefault().logError("Error getting members for "+container.getName(), e);
						return false;
					}
				} else {
					return false;
				}
			}
			
		};
		
		this.treeViewer.setContentProvider(treeContentProvider);

		ILabelProvider treeLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
		this.treeViewer.setLabelProvider(treeLabelProvider);
		this.treeViewer.setComparator(VIEW_COMPARATOR);
		this.treeViewer.addTreeListener(this);
		this.treeViewer.addCheckStateListener(this);
		this.treeViewer.addSelectionChangedListener(this);
		
		this.treeViewer.setInput(root);
		this.expandedTreeNodes = new ArrayList<IContainer>();
		this.expandedTreeNodes.add(root.project);
		
		// List
		this.listViewer = new TableViewer(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
//		this.listViewer = CheckboxTableViewer.newCheckList(group, SWT.BORDER);
		GridData gdList = new GridData();
		gdList.horizontalAlignment = SWT.FILL;
		gdList.verticalAlignment = SWT.FILL;
		this.listViewer.getControl().setLayoutData(gdList);
		this.listViewer.setComparator(VIEW_COMPARATOR);
		IStructuredContentProvider listContentProvider = new IStructuredContentProvider() {
			// container that only return non-container resources
			private IContainer input;
			@Override
			public void dispose() {
				// nothing to dispose
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				if (newInput instanceof IContainer) {
					this.input = (IContainer)newInput;
				}
			}

			@Override
			public Object[] getElements(Object inputElement) {
				try {
					IResource[] members = input.members();
					int count = 0; 
					for (int i = 0; i < members.length; i++) {
						if (!(members[i] instanceof IContainer)) {
							count++;
						}
					}
					Object[] elements = new Object[count];
					int elementIndex = 0;
					for (int i = 0; i < members.length; i++) {
						if (!(members[i] instanceof IContainer)) {
							elements[elementIndex++] = members[i];
						}
					}
					return elements;
				} catch (CoreException e) {
					Activator.getDefault().logError("Unable to get members for the list group for "+input.getName(), e);
					return new Object[0];
				}
			}
			
		};
		this.listViewer.setContentProvider(listContentProvider);
		this.listViewer.setInput(root.project);
		ILabelProvider listLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
		this.listViewer.setLabelProvider(listLabelProvider);	// can we use the same label provider object?
		StructuredSelection treeSelection = new StructuredSelection(new Object[] {root.project});
		this.treeViewer.setSelection(treeSelection);
	}
	
	public void addViewerFilter(ViewerFilter filter) {
		this.treeViewer.addFilter(filter);
		this.listViewer.addFilter(filter);
	}
	
	public void removeTreeViewerFilter(ViewerFilter filter) {
		this.treeViewer.removeFilter(filter);
	}
	
	public void refresh() {
		this.treeViewer.refresh();
		this.listViewer.refresh();
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		// No action required
		
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		if (!(event.getElement() instanceof IContainer)) {
			return;	// only worry about container items
		}
		final IContainer item = (IContainer)event.getElement();
		// we need to check all of the children
		BusyIndicator.showWhile(this.treeViewer.getControl().getDisplay(),
				new Runnable() {

					@Override
					public void run() {
						if (!expandedTreeNodes.contains(item)) {
							// first time this node has been expanded
							// need to carry forward the checked state
							expandedTreeNodes.add(item);
							boolean checked = treeViewer.getChecked(item);
							try {
								IResource[] children = item.members();
								for (int i = 0; i < children.length; i++) {
									treeViewer.setChecked(children[i], checked);
								}
							} catch (CoreException e) {
								Activator.getDefault().logError("Unable to get children to check for "+item.getName(), e);
							}
						}
					}
			
		});
	}

	@Override
	public void checkStateChanged(final CheckStateChangedEvent event) {

		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {

			@Override
			public void run() {
				if (event.getCheckable().equals(treeViewer)) {
					IResource element = (IContainer)event.getElement();
					treeItemCheckStateChanged(element, event.getChecked());
				} else {
					listItemCheckStateChanged(event.getElement(), event.getChecked());
				}
				for (int i = 0; i < listeners.size(); i++) {
					listeners.get(i).checkStateChanged();
				}
			}
			
		});
	}

	protected void listItemCheckStateChanged(Object item, boolean checked) {
		// TODO Auto-generated method stub
		
	}

	protected void treeItemCheckStateChanged(IResource element, boolean checked) {
		// clear any grey for the checked item
		treeViewer.setGrayed(element, false);
		if (element instanceof IContainer) {
			setTreeChecked((IContainer)element, checked);	// set all children to the same state
		}
		// update the parents
		updateParents(element);
	}
	

	private void updateParents(IResource element) {
		IContainer parent = element.getParent();
		if (parent != null && expandedTreeNodes.contains(parent)) {
			// figure out if it should be grey or white and if it should be checked
			boolean grey = false;
			boolean foundChecked = false;
			boolean foundUnchecked = false;
			IResource[] children = getContainerChildren(parent);
			for (int i = 0; i < children.length; i++) {
				if (elementIsVisibleInTree(parent, children[i])) {
					if (treeViewer.getGrayed(children[i])) {
						grey = true;
					}
					if (treeViewer.getChecked(children[i])) {
						foundChecked = true;
						if (foundUnchecked) {
							grey = true;
						}
					} else {
						foundUnchecked = true;
						if (foundChecked) {
							grey = true;
						}
					}
				}
			}

			treeViewer.setChecked(parent, foundChecked);
			treeViewer.setGrayed(parent, grey);
			// recursively call the parent
			updateParents(parent);
			// Note: this can be optimized by passing in a hint of the grey - if this is grey, the parent must be
		}
	}

	private IResource[] getContainerChildren(IContainer container) {
		IResource[] members;
		try {
			members = container.members();
			// filter out the non-containers
			int count = 0; 
			for (int i = 0; i < members.length; i++) {
				if (members[i] instanceof IContainer) {
					count++;
				}
			}
			IResource[] children = new IResource[count];
			int childIndex = 0;
			for (int i = 0; i < members.length; i++) {
				if (members[i] instanceof IContainer) {
					children[childIndex++] = members[i];
				}
			}
			
			return children;
		} catch (CoreException e) {
			Activator.getDefault().logError("Error getting members for "+container.getName(), e);
			return new IResource[0];
		}
	}

	/**
	 * Returns true if the element is visible in the tree based on the filters
	 * @param element
	 * @return
	 */
	private boolean elementIsVisibleInTree(IContainer parent, IResource element) {
		ViewerFilter[] filters = treeViewer.getFilters();
		for (int i = 0; i < filters.length; i++) {
			if (!filters[i].select(treeViewer, parent, element)) {
				return false;
			}
		}
		return true;
	}

	private void setTreeChecked(IContainer container, boolean checked) {
		try {
			if (!this.expandedTreeNodes.contains(container)) {
				// need to expand down to this node
				expandTreeToFolder(container);
			}
			this.treeViewer.setGrayChecked(container, false);
			this.treeViewer.setChecked(container, checked);
			IResource[] children = container.members();
			for (int i = 0; i < children.length; i++) {
				treeViewer.setChecked(children[i], checked);
				treeViewer.setGrayed(children[i], false);
				if (this.expandedTreeNodes.contains(children[i])) {
					// only need to set the state if the tree has already been expanded
					if (children[i] instanceof IContainer) {
						IContainer childContainer = (IContainer)children[i];
						setTreeChecked(childContainer, checked);
					}
				}
			}
		} catch (CoreException e) {
			Activator.getDefault().logError("Unable to get container childrent to set checked state for "+container.getName(), e);
		}
	}

	/**
	 * Expand the tree down to the level of the selected tree node
	 * @param container
	 */
	private void expandTreeToFolder(IContainer container) {
		if (!this.expandedTreeNodes.contains(container.getParent())) {
			expandTreeToFolder(container.getParent());
		}
		this.treeViewer.expandToLevel(container, 1);
		this.expandedTreeNodes.add(container);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection)event.getSelection();
		if (selection.getFirstElement() != null && selection.getFirstElement() instanceof IContainer) {
			listViewer.setInput(selection.getFirstElement());
		} 
	}

	@Override
	public void handleEvent(Event event) {
		// Nothing needs to be done
	}
	
	/**
	 * @return all resource paths which include all files and subdirectories as selected
	 */
	public String[] getSelectedResourcePaths() {
		Object[] checkedElements = this.treeViewer.getCheckedElements();
		int count = 0;
		for (int i = 0; i < checkedElements.length; i++) {
			if (checkedElements[i] instanceof IContainer) {
				count++;
			}
		}
		
		IContainer[] checkedContainers = new IContainer[count];
		int cindex = 0;
		for (int i = 0; i < checkedElements.length; i++) {
			if (checkedElements[i] instanceof IContainer) {
				checkedContainers[cindex++] = (IContainer)checkedElements[i];
			}
		}
		Arrays.sort(checkedContainers, RESOURCE_COMPARATOR);
		ArrayList<String> alCheckedWhite = new ArrayList<String>();
		for (int i = 0; i < checkedContainers.length; i++) {
			String resourcePath = checkedContainers[i].getProjectRelativePath().toString();
			if (ancestorIn(resourcePath, alCheckedWhite)) {
				continue;	// go onto the next, a parent has already been checked
			} else if (treeViewer.getGrayed(checkedContainers[i])) {
				continue;	// need to keep moving down the tree until we find a completely checked or unchecked node
			} else if (treeViewer.getChecked(checkedContainers[i])) {
				alCheckedWhite.add(resourcePath);
			}
		}
		return alCheckedWhite.toArray(new String[alCheckedWhite.size()]);
	}

	/**
	 * Determines if a the resource path is a child path of one of the paths in the searchResourcePaths
	 * @param resourcePath
	 * @param searchResourcePaths
	 * @return
	 */
	private boolean ancestorIn(String resourcePath,
			ArrayList<String> searchResourcePaths) {
		for (int i = 0; i < searchResourcePaths.size(); i++) {
			if (resourcePath.startsWith(searchResourcePaths.get(i))) {
				return true;
			}
		}
		return false;
	}

	public void addChangeListener(
			IResourceTreeSelectionChangeListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeChangeListener(IResourceTreeSelectionChangeListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * @return true if there are any resource paths selected
	 */
	public boolean hasSelectedResourcePaths() {
		Iterator<IContainer> iter = this.expandedTreeNodes.iterator();
		while (iter.hasNext()) {
			if (treeViewer.getChecked(iter.next())) {
				return true;
			}
		}
		return false;
	}

	public void setIncludedDirectories(String[] includedDirectories) {
		// clear all checks first
		Object[] checkedObjects = this.treeViewer.getCheckedElements();
		for (int i = 0; i < checkedObjects.length; i++) {
			this.treeViewer.setChecked(checkedObjects[i], false);
		}
		this.setInitialCheckedState(includedDirectories);
	}
}
