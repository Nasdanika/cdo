/*
 * Copyright (c) 2011, 2013 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Victor Roldan Betancort - initial API and implementation
 *    Eike Stepper - maintenance
 */
package org.eclipse.emf.cdo.explorer.ui.application;

import org.eclipse.emf.cdo.internal.ui.views.CDORemoteSessionsView;
import org.eclipse.emf.cdo.internal.ui.views.CDOSessionsView;
import org.eclipse.emf.cdo.internal.ui.views.CDOWatchListView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;

/**
 * @author Victor Roldan Betancort
 */
public class CDOExplorerPerspective implements IPerspectiveFactory
{
  public static final String ID = "org.eclipse.emf.cdo.explorer.CDOExplorerPerspective"; //$NON-NLS-1$

  private IPageLayout pageLayout;

  public CDOExplorerPerspective()
  {
  }

  public IPageLayout getPageLayout()
  {
    return pageLayout;
  }

  public void createInitialLayout(IPageLayout pageLayout)
  {
    this.pageLayout = pageLayout;
    addViews();
    addPerspectiveShortcuts();
    addViewShortcuts();
  }

  protected void addViews()
  {
    IFolderLayout sessionsPane = pageLayout.createFolder("sessionsPane", IPageLayout.LEFT, 0.30f, //$NON-NLS-1$
        pageLayout.getEditorArea());
    sessionsPane.addView(CDOSessionsView.ID);

    IFolderLayout propertiesPane = pageLayout.createFolder("propertiesPane", IPageLayout.BOTTOM, 0.70f, //$NON-NLS-1$
        pageLayout.getEditorArea());
    propertiesPane.addView(IPageLayout.ID_PROP_SHEET);
    propertiesPane.addView(CDOWatchListView.ID);
    propertiesPane.addView(CDORemoteSessionsView.ID);

    IFolderLayout outlinePane = pageLayout.createFolder("outlinePane", IPageLayout.RIGHT, 0.70f, //$NON-NLS-1$
        pageLayout.getEditorArea());
    outlinePane.addView(IPageLayout.ID_OUTLINE);
  }

  protected void addViewShortcuts()
  {
    pageLayout.addShowViewShortcut(CDOSessionsView.ID);
    pageLayout.addShowViewShortcut(CDOWatchListView.ID);
    pageLayout.addShowViewShortcut(CDORemoteSessionsView.ID);
    pageLayout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
    pageLayout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
  }

  protected void addPerspectiveShortcuts()
  {
    pageLayout.addPerspectiveShortcut(ID);
  }

  static public boolean isCurrent()
  {
    return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective().getId()
        .equals(CDOExplorerPerspective.ID);
  }
}