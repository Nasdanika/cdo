/*
 * Copyright (c) 2016 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.tests.bugzilla;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.tests.AbstractCDOTest;
import org.eclipse.emf.cdo.tests.model1.Company;
import org.eclipse.emf.cdo.transaction.CDOAutoLocker;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CDOUtil;
import org.eclipse.emf.cdo.view.CDOView;

import org.eclipse.emf.ecore.EObject;

/**
 * Bug 485489: CDOAutoLocker: Some locks can be left after view has been closed
 *
 * @author Eike Stepper
 */
public class Bugzilla_485489_Test extends AbstractCDOTest
{
  public void testTransactionCommitAfterAutoLocker() throws Exception
  {
    Company company = getModel1Factory().createCompany();
    company.setName("Company1");
    company.setStreet("Street1");

    CDOSession session = openSession();
    CDOTransaction transaction = session.openTransaction();
    CDOResource resource = transaction.createResource(getResourcePath("/test1"));
    resource.getContents().add(company);

    transaction.commit();
    assertWriteLock(false, company);

    transaction.addTransactionHandler(new CDOAutoLocker());

    company.setName("Company2"); // Acquire write lock.
    company.setStreet("Street2"); // Increase write lock count to 2.
    assertWriteLock(true, company);

    transaction.commit();
    assertWriteLock(false, company);
  }

  public void testViewCloseAfterAutoLocker() throws Exception
  {
    Company company = getModel1Factory().createCompany();
    company.setName("Company1");
    company.setStreet("Street1");

    CDOSession session = openSession();
    CDOTransaction transaction = session.openTransaction();
    CDOResource resource = transaction.createResource(getResourcePath("/test1"));
    resource.getContents().add(company);

    transaction.commit();
    assertWriteLock(false, company);

    transaction.addTransactionHandler(new CDOAutoLocker());

    company.setName("Company2"); // Acquire write lock.
    company.setStreet("Street2"); // Increase write lock count to 2.
    assertWriteLock(true, company);

    transaction.close();
    assertWriteLock(false, company);
  }

  private void assertWriteLock(boolean expected, EObject object) throws InterruptedException
  {
    CDOObject cdoObject = CDOUtil.getCDOObject(object);
    CDOView view = cdoObject.cdoView();
    CDOTransaction transaction = view.getSession().openTransaction(view.getBranch());

    try
    {
      CDOObject txObject = transaction.getObject(cdoObject);

      boolean actual = !txObject.cdoWriteLock().tryLock(500);
      assertEquals(expected, actual);
    }
    finally
    {
      transaction.close();
    }
  }
}
