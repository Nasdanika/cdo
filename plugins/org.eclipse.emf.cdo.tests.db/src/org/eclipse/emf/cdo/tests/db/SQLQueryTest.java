/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kai Schlamp - initial API and implementation
 *    Eike Stepper - maintenance
 *    Kai Schlamp - Bug 284812: [DB] Query non CDO object fails
 *    Stefan Winkler - Bug 284812: [DB] Query non CDO object fails
 *    Erdal Karaca - added test case for cdoObjectResultAsMap query parameter
 */
package org.eclipse.emf.cdo.tests.db;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.server.internal.db.SQLQueryHandler;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.tests.AbstractCDOTest;
import org.eclipse.emf.cdo.tests.model1.Customer;
import org.eclipse.emf.cdo.tests.model1.Order;
import org.eclipse.emf.cdo.tests.model1.OrderDetail;
import org.eclipse.emf.cdo.tests.model1.Product1;
import org.eclipse.emf.cdo.tests.model1.SalesOrder;
import org.eclipse.emf.cdo.tests.model1.VAT;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.view.CDOQuery;

import org.eclipse.net4j.util.WrappedException;
import org.eclipse.net4j.util.collection.CloseableIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Test different aspects of SQL querying using the CDO query api.
 * 
 * @author Kai Schlamp
 */
public class SQLQueryTest extends AbstractCDOTest
{
  private static final int NUM_OF_PRODUCTS = 20;

  private static final int NUM_OF_CUSTOMERS = 5;

  private static final int NUM_OF_PRODUCTS_CUSTOMER = NUM_OF_PRODUCTS / NUM_OF_CUSTOMERS;

  private static final int NUM_OF_SALES_ORDERS = 5;

  public void testSimpleQueries() throws Exception
  {
    msg("Opening session");
    CDOSession session = openSession();

    createTestSet(session);

    msg("Opening transaction for querying");
    CDOTransaction transaction = session.openTransaction();

    // {
    // msg("Query for products");
    // CDOQuery query = transaction.createQuery("sql", "SELECT CDO_ID FROM PRODUCT1");
    // final List<Product1> products = query.getResult(Product1.class);
    // assertEquals(NUM_OF_PRODUCTS, products.size());
    // }

    {
      msg("Query for products with a specific name");
      CDOQuery query = transaction.createQuery("sql", "SELECT CDO_ID FROM MODEL1_PRODUCT1 WHERE name=:name");
      query.setParameter("name", "" + 1);
      final List<Product1> products = query.getResult(Product1.class);
      assertEquals(1, products.size());
    }

    {
      msg("Query for Customers");
      CDOQuery query = transaction.createQuery("sql", "SELECT CDO_ID FROM MODEL1_CUSTOMER");
      final List<Customer> customers = query.getResult(Customer.class);
      assertEquals(NUM_OF_CUSTOMERS, customers.size());
    }

    {
      msg("Query for products with VAT15");
      CDOQuery query = transaction.createQuery("sql", "SELECT CDO_ID FROM MODEL1_PRODUCT1 WHERE VAT =:vat");
      query.setParameter("vat", VAT.VAT15.getValue());
      final List<Product1> products = query.getResult(Product1.class);
      assertEquals(10, products.size());
      for (Product1 p : products)
      {
        assertEquals(p.getVat(), VAT.VAT15);
      }
    }

    transaction.commit();
  }

  @CleanRepositoriesBefore
  public void testFunctions() throws Exception
  {
    msg("Opening session");
    CDOSession session = openSession();

    createTestSet(session);

    msg("Opening transaction for querying");
    CDOTransaction transaction = session.openTransaction();

    {
      msg("Count products");
      CDOQuery query = transaction.createQuery("sql", "SELECT COUNT(*) from MODEL1_PRODUCT1");
      query.setParameter(SQLQueryHandler.CDO_OBJECT_QUERY, false);

      // we need to handle objects, because different DBs produce either
      // Long or Integer results
      final List<Object> counts = query.getResult(Object.class);
      assertEquals(counts.size(), 1);

      Object result = counts.get(0);
      int intResult;
      if (result instanceof Integer)
      {
        intResult = ((Integer)result).intValue();
      }
      else
      {
        assertEquals(true, result instanceof Long);
        intResult = ((Long)result).intValue();
      }

      assertEquals(NUM_OF_PRODUCTS, intResult);
    }

    transaction.commit();
  }

  @CleanRepositoriesBefore
  public void testComplexQuerySalesOrderJoinCustomerProduct() throws Exception
  {
    msg("Opening session");
    CDOSession session = openSession();

    createTestSet(session);

    msg("Opening transaction for querying");
    CDOTransaction transaction = session.openTransaction();

    {
      msg("Query for customers");
      CDOQuery customerQuery = transaction.createQuery("sql", "SELECT CDO_ID FROM MODEL1_CUSTOMER ORDER BY NAME");
      final List<Customer> customers = customerQuery.getResult(Customer.class);
      assertEquals(NUM_OF_CUSTOMERS, customers.size());

      msg("Query for products");
      CDOQuery productQuery = transaction.createQuery("sql", "SELECT CDO_ID FROM MODEL1_PRODUCT1");
      final List<Product1> products = productQuery.getResult(Product1.class);
      assertEquals(NUM_OF_PRODUCTS, products.size());
    }

    transaction.commit();
  }

  public void testPaging() throws Exception
  {
    msg("Opening session");
    CDOSession session = openSession();

    createTestSet(session);

    msg("Opening transaction for querying");
    CDOTransaction transaction = session.openTransaction();

    {
      msg("Query for products in pages");
      int pageSize = 5;
      int numOfPages = NUM_OF_PRODUCTS / pageSize;
      final List<Product1> allProducts = new ArrayList<Product1>();
      for (int page = 0; page < numOfPages; page++)
      {
        CDOQuery productQuery = transaction.createQuery("sql", "SELECT CDO_ID FROM MODEL1_PRODUCT1");
        productQuery.setMaxResults(pageSize);
        productQuery.setParameter(SQLQueryHandler.FIRST_RESULT, page * pageSize);
        final List<Product1> queriedProducts = productQuery.getResult(Product1.class);
        assertEquals(true, queriedProducts.size() <= pageSize);
        // a product should not have been read yet
        for (Product1 newProduct : queriedProducts)
        {
          assertEquals(true, !allProducts.contains(newProduct));
        }

        allProducts.addAll(queriedProducts);
      }

      assertEquals(NUM_OF_PRODUCTS, allProducts.size());
    }

    transaction.commit();
  }

  public void testIterator() throws Exception
  {
    msg("Opening session");
    CDOSession session = openSession();

    createTestSet(session);

    msg("Opening transaction for querying");
    CDOTransaction transaction = session.openTransaction();

    {
      msg("Query for products");
      CDOQuery productQuery = transaction.createQuery("sql", "SELECT CDO_ID FROM MODEL1_PRODUCT1");
      final CloseableIterator<Product1> iterator = productQuery.getResultAsync(Product1.class);
      int counter = 0;
      while (iterator.hasNext())
      {
        final Product1 product = iterator.next();
        // meaningless but do something
        assertEquals(true, product != null);
        counter++;
        if (counter == NUM_OF_PRODUCTS / 2)
        {
          iterator.close();
        }
      }
    }

    transaction.commit();
  }

  public void _testNonCdoObjectQueries() throws Exception
  {
    msg("Opening session");
    CDOSession session = openSession();

    createTestSet(session);

    msg("Opening transaction for querying");
    CDOTransaction transaction = session.openTransaction();

    {
      msg("Query for customer street strings.");
      CDOQuery query = transaction.createQuery("sql", "SELECT STREET FROM MODEL1_CUSTOMER");
      query.setParameter("cdoObjectQuery", false);
      List<String> streets = new ArrayList<String>(query.getResult(String.class));
      for (int i = 0; i < 5; i++)
      {
        assertEquals(true, streets.contains("Street " + i));
      }
    }
  }

  public void _testNonCdoObjectQueries_Null() throws Exception
  {
    msg("Opening session");
    CDOSession session = openSession();

    createTestSet(session);

    msg("Opening transaction for querying");
    CDOTransaction transaction = session.openTransaction();

    {
      msg("Query for customer city strings.");
      CDOQuery query = transaction.createQuery("sql", "SELECT CITY FROM MODEL1_CUSTOMER");
      query.setParameter("cdoObjectQuery", false);
      List<String> cities = new ArrayList<String>(query.getResult(String.class));

      assertEquals(true, cities.contains(null));
      for (int i = 1; i < 5; i++)
      {
        assertEquals(true, cities.contains("City " + i));
      }
    }
  }

  @CleanRepositoriesBefore
  public void testNonCDOObjectQueries_Complex() throws Exception
  {
    msg("Opening session");
    CDOSession session = openSession();

    createTestSet(session);

    msg("Opening transaction for querying");
    CDOTransaction transaction = session.openTransaction();

    {
      msg("Query for customer fields");
      CDOQuery query = transaction.createQuery("sql", "SELECT street, city, name FROM model1_customer ORDER BY street");
      query.setParameter("cdoObjectQuery", false);

      List<Object[]> results = query.getResult(Object[].class);
      for (int i = 0; i < NUM_OF_CUSTOMERS; i++)
      {
        assertEquals("Street " + i, results.get(i)[0]);
        Object actual = results.get(i)[1];
        if (i == 0)
        {
          assertEquals(null, actual);
        }
        else
        {
          assertEquals("City " + i, actual);
        }

        assertEquals("" + i, results.get(i)[2]);
      }
    }
  }

  @CleanRepositoriesBefore
  public void testNonCDOObjectQueries_Complex_MAP() throws Exception
  {
    msg("Opening session");
    CDOSession session = openSession();

    createTestSet(session);

    msg("Opening transaction for querying");
    CDOTransaction transaction = session.openTransaction();

    msg("Query for customer fields");
    CDOQuery query = transaction.createQuery("sql", "SELECT street, city, name FROM model1_customer ORDER BY street");
    query.setParameter("cdoObjectQuery", false);
    query.setParameter("mapQuery", true);

    List<Map<String, Object>> results = query.getResult();
    for (int i = 0; i < NUM_OF_CUSTOMERS; i++)
    {
      assertEquals("Street " + i, results.get(i).get("STREET"));
      Object actual = results.get(i).get("CITY");
      if (i == 0)
      {
        assertEquals(null, actual);
      }
      else
      {
        assertEquals("City " + i, actual);
      }

      assertEquals("" + i, results.get(i).get("NAME"));
    }
  }

  private void createTestSet(CDOSession session)
  {
    msg("Opening transaction");
    CDOTransaction transaction = session.openTransaction();

    msg("Creating resource");
    CDOResource resource = transaction.createResource(getResourcePath("/test1"));

    fillResource(resource);

    try
    {
      msg("Committing");
      transaction.commit();
    }
    catch (CommitException ex)
    {
      throw WrappedException.wrap(ex);
    }
  }

  private void fillResource(CDOResource resource)
  {
    msg("Creating Testset");
    final List<Product1> products = new ArrayList<Product1>();
    for (int i = 0; i < NUM_OF_PRODUCTS; i++)
    {
      products.add(createProduct(i));
    }

    resource.getContents().addAll(products);

    int productCounter = 0;
    for (int i = 0; i < NUM_OF_CUSTOMERS; i++)
    {
      final Customer customer = getModel1Factory().createCustomer();

      if (i == 0)
      {
        // set first city null for null-test-case
        customer.setCity(null);
      }
      else
      {
        customer.setCity("City " + i);
      }

      customer.setName(i + "");
      customer.setStreet("Street " + i);
      resource.getContents().add(customer);

      final List<Product1> customerProducts = products.subList(productCounter, productCounter
          + NUM_OF_PRODUCTS_CUSTOMER);
      for (int k = 0; k < NUM_OF_SALES_ORDERS; k++)
      {
        resource.getContents().add(createSalesOrder(i * 10 + k, customer, customerProducts));
      }

      productCounter += NUM_OF_PRODUCTS_CUSTOMER;
    }
  }

  private SalesOrder createSalesOrder(int num, Customer customer, List<Product1> products)
  {
    SalesOrder salesOrder = getModel1Factory().createSalesOrder();
    salesOrder.setCustomer(customer);
    salesOrder.setId(num);
    createOrderDetail(salesOrder, num, products);
    return salesOrder;
  }

  private List<OrderDetail> createOrderDetail(Order order, int index, List<Product1> products)
  {
    final List<OrderDetail> orderDetails = new ArrayList<OrderDetail>();
    int count = 0;
    for (Product1 product : products)
    {
      OrderDetail orderDetail = getModel1Factory().createOrderDetail();
      orderDetail.setOrder(order);
      orderDetail.setPrice(count++ * index * 1.1f);
      orderDetail.setProduct(product);
    }

    return orderDetails;
  }

  private Product1 createProduct(int index)
  {
    Product1 product = getModel1Factory().createProduct1();
    product.setDescription("Description " + index);
    product.setName("" + index);
    if (index < 10)
    {
      product.setVat(VAT.VAT15);
    }
    else
    {
      product.setVat(VAT.VAT7);
    }

    return product;
  }
}