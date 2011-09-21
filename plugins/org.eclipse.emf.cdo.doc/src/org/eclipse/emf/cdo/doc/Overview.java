/*
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.doc;

import org.eclipse.emf.ecore.EObject;

/**
 * Overview
 * <p>
 * CDO is a pure Java <i>model repository</i> for your EMF models and meta models. CDO can also serve as a
 * <i>persistence and distribution framework</i> for your EMF based application systems. For the sake of this overview a
 * model can be regarded as a graph of application or business objects and a meta model as a set of classifiers that
 * describe the structure of and the possible relations between these objects.
 * <p>
 * 
 * @default
 */
public class Overview
{
  /**
   * Functionality
   * <p>
   * The main functionality of CDO can be summarized as follows:
   * <ul>
   * <li><b>Persistence</b> of your models in all kinds of database backends like major relational databases or NoSQL
   * databases. CDO keeps your application code free of vendor specific data access code and eases transitions between
   * the supported backend types.
   * <p>
   * <li><b>Multi user access</b> to your models is supported through the notion of repository sessions. The physical
   * transport of sessions is pluggable and repositories can be configured to require secure authentication of users.
   * Various authorization policies can be established programmatically.
   * <p>
   * <li><b>Transactional access</b> to your models with ACID properties is provided by optimistic and/or pessimistic
   * locking on a per object granule. Transactions support multiple savepoints that changes can be rolled back to.
   * Pessimistic locks can be acquired separately for read access, write access and the option to reserve write access
   * in the future. All kinds of locks can optionally be turned into long lasting locks that survive repository
   * restarts. Transactional modification of models in multiple repositories is provided through the notion of XA
   * transactions with a two phase commit protocol.
   * <p>
   * <li><b>Transparent temporality</b> is available through audit views, a special kind of read only transactions that
   * provide you with a consistent model object graph exactly in the state it has been at a point in the past. Depending
   * on the chosen backend type the storage of the audit data can lead to considerable increase of database sizes in
   * time. Therefore it can be configured per repository.
   * <p>
   * <li><b>Parallel evolution</b> of the object graph stored in a repository through the concept of branches similar to
   * source code management systems like Subversion or Git. Comparisons or merges between any two branch points are
   * supported through sophisticated APIs, as well as the reconstruction of committed change sets or old states of
   * single objects.
   * <p>
   * <li><b>Scalability</b>, the ability to store and access models of arbitrary size, is transparently achieved by
   * loading single objects on demand and caching them <i>softly</i> in your application. That implies that objects that
   * are no longer referenced by the application are automatically garbage collected when memory contention occurs. Lazy
   * loading is accompanied by various prefetching strategies, including the monitoring of the object graph's
   * <i>usage</i> and the calculation of fetch rules that are optimal for the current usage patterns. The scalability of
   * EMF applications can be further increased by leveraging CDO constructs such as remote cross referencing or
   * optimized content adapters.
   * <p>
   * <li><b>Thread safety</b> ensures that multiple threads of your application can access and modify the object graph
   * without worrying about the synchronization details. This is possible and cheap because multiple transactions can be
   * opened from within a single session and they all share the same object data until one of them modifies the graph.
   * Possible commit conflicts can be handled in the same way as if they were conflicts between different sessions.
   * <p>
   * <li><b>Collaboration</b> on models with CDO is a snap because an application can opt in to be notified about remote
   * changes to the object graph. By default your local object graph transparently changes when it has changed remotely.
   * With configurable change subscription policies you can fine tune the characteristics of your <i>distributed shared
   * model</i> so that all users enjoy the impression to collaborate on a single instance of an object graph. The level
   * of collaboration can be further increased by plugging custom collaboration handlers into the asynchronous CDO
   * protocol.
   * <p>
   * <li><b>Data integrity</b> can be ensured by enabling optional commit checks in the repository server such as
   * referential integrity checks and containment cycle checks, as well as custom checks implemented by write access
   * handlers.
   * <p>
   * <li><b>Fault tolerance</b> on multiple levels, namely the setup of fail-over clusters of replicating repositories
   * under the control of a fail-over monitor, as well as the usage of a number of special session types such as
   * fail-over or reconnecting sessions that allow applications to hold on their copy of the object graph even though
   * the physical repository connection has broken down or changed to a different fail-over participant.
   * <p>
   * <li><b>Offline work</b> with your models is supported by two different mechanisms:
   * <ul>
   * <li>One way is to create a <b>clone</b> of a complete remote repository, including all history of all branches.
   * Such a clone is continuously synchronized with its remote master and can either act as an embedded repository to
   * make a single application tolerant against network outage or it can be set up to serve multiple clients, e.g., to
   * compensate low latency master connections and speed up read access to the object graph.
   * <p>
   * <li>An entirely different and somewhat lighter approach to offline work is to check out a single version of the
   * object graph from a particular branch point of the repository into a local CDO <b>workspace</b>. Such a workspace
   * behaves similar to a local repository without branching or history capture, in particular it supports multiple
   * concurrent transactions on the local checkout. In addition it supports most remote functionality that is known from
   * source code management systems such as update, merge, compare, revert and check in.
   * </ul>
   * </ul>
   */
  public class Functionality
  {
  }

  /**
   * Architecture
   * <p>
   * The architecture of CDO comprises applications and repositories. Despite a number of embedding options applications
   * are usually deployed to client nodes and repositories to server nodes. They communicate through an application
   * level CDO protocol which can be driven through various kinds of physical transports, including fast intra JVM
   * connections.
   * <p>
   * CDO has been designed to take full advantage of the OSGi platform, if available at runtime, but can perfectly be
   * operated in standalone deployments or in various kinds of containers such as JEE web or application servers.
   * <p>
   * The following chapters give an overview about the architecures of applications and repositories, respectively.
   */
  public class Architecture
  {
    /**
     * Application Architecture
     * <p>
     * The architecture of a CDO application is characterized by its mandatory dependency on EMF, the Eclipse Modeling
     * Framework. Most of the time an application interacts with the object graph of the model through standard EMF APIs
     * because CDO model graph objects are {@link EObject EObjects}. While CDO's basic functionality integrates nicely
     * and transparently with EMF's extension mechansims some of the more advanced functions may require to add direct
     * dependendcies on CDO to your application code.
     * <p>
     * The following diagram illustrates the major building blocks of a CDO application:
     * <p align="center">
     * <img src="application-architecture.png"/>
     */
    public class Application
    {
      /**
       * OSGi
       */
      public class OSGi
      {
      }

      /**
       * EMF
       */
      public class EMF
      {
      }

      /**
       * CDO Client
       */
      public class CDOClient
      {
      }

      /**
       * Net4j Core
       */
      public class Net4j
      {
      }

      /**
       * Models
       */
      public class Models
      {
      }

      /**
       * Protocol
       */
      public class Protocol
      {
      }

      /**
       * Transport
       */
      public class Transport
      {
      }
    }

    /**
     * Repository Architecture
     * <p>
     * The main building block of a CDO repository is split into two layers, the generic repository layer that client
     * applications interact with and the database integration layer that providers can hook into to integrate their
     * data storage solutions with CDO. A number of such integrations already ship with CDO, as outlined in
     * {@link CDOStore}.
     * <p>
     * While technically a CDO repository depends on EMF this dependency is not of equal importance as it is in a CDO
     * application. In particular the generated application models are not required to be deployed to the server because
     * a CDO repository accesses models reflectively and the model objects are not implemented as {@link EObject
     * EObjects} on the server.
     * <p>
     * The following diagram illustrates the major building blocks of a CDO repository:
     * <p align="center">
     * <img src="repository-architecture.png"/>.
     */
    public class Repository
    {
      /**
       * OSGi
       */
      public class OSGi
      {
      }

      /**
       * CDO Server Core
       */
      public class CDOServerCore
      {
      }

      /**
       * CDO Store
       */
      public class CDOStore
      {
      }

      /**
       * OCL
       */
      public class OCL
      {
      }

      /**
       * Net4j
       */
      public class Net4j
      {
      }

      /**
       * Protocol
       */
      public class Protocol
      {
      }

      /**
       * Transport
       */
      public class Transport
      {
      }
    }
  }
}
