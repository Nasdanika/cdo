/*
 * Copyright (c) 2004-2013 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.releng.projectconfig.util;

import org.eclipse.emf.cdo.releng.predicates.NaturePredicate;
import org.eclipse.emf.cdo.releng.predicates.Predicate;
import org.eclipse.emf.cdo.releng.predicates.PredicatesFactory;
import org.eclipse.emf.cdo.releng.preferences.PreferenceNode;
import org.eclipse.emf.cdo.releng.preferences.PreferencesFactory;
import org.eclipse.emf.cdo.releng.preferences.Property;
import org.eclipse.emf.cdo.releng.preferences.util.PreferencesUtil;
import org.eclipse.emf.cdo.releng.projectconfig.PreferenceFilter;
import org.eclipse.emf.cdo.releng.projectconfig.PreferenceProfile;
import org.eclipse.emf.cdo.releng.projectconfig.Project;
import org.eclipse.emf.cdo.releng.projectconfig.ProjectConfigFactory;
import org.eclipse.emf.cdo.releng.projectconfig.PropertyFilter;
import org.eclipse.emf.cdo.releng.projectconfig.WorkspaceConfiguration;
import org.eclipse.emf.cdo.releng.projectconfig.impl.ProjectConfigPlugin;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Eike Stepper
 */
public final class ProjectConfigUtil
{
  private static final IWorkspaceRoot WORKSPACE_ROOT = ResourcesPlugin.getWorkspace().getRoot();

  public static final String PROJECT_CONF_NODE_NAME = "org.eclipse.emf.cdo.releng.projectconfig";

  public static final String PROJECT_CONF_PROJECT_KEY = "project";

  public static final String PROJECT_CONFIG_SCHEME = "configuration";

  public static final URI PROJECT_CONFIG_URI = URI.createURI(PROJECT_CONFIG_SCHEME + ":/");

  public static final WorkspaceConfiguration getWorkspaceConfiguration()
  {
    return getWorkspaceConfiguration(null);
  }

  private static List<PropertyFilter> getDefaultPropertyFilters()
  {
    List<PropertyFilter> result = new ArrayList<PropertyFilter>();

    PropertyFilter encodingPropertyFilter = ProjectConfigFactory.eINSTANCE.createPropertyFilter();
    encodingPropertyFilter.setOmissions(Pattern.compile("org\\.eclipse\\.core\\.resources/encoding/.*(?<!<project>)"));
    result.add(encodingPropertyFilter);

    PropertyFilter jdtCompilerCompliancePropertyFilter = ProjectConfigFactory.eINSTANCE.createPropertyFilter();
    jdtCompilerCompliancePropertyFilter.setOmissions(Pattern.compile("org\\.eclipse\\.jdt\\.core/("
        + "org\\.eclipse\\.jdt\\.core\\.compiler\\.codegen\\.targetPlatform" + "|"
        + "org.eclipse\\.jdt\\.core\\.compiler\\.compliance" + "|" + "org\\.eclipse\\.jdt\\.core\\.compiler\\.source"
        + ")"));
    NaturePredicate naturePredicate = PredicatesFactory.eINSTANCE.createNaturePredicate();
    naturePredicate.setNature("org.eclipse.pde.PluginNature");
    jdtCompilerCompliancePropertyFilter.getPredicates().add(naturePredicate);
    result.add(jdtCompilerCompliancePropertyFilter);

    return result;
  }

  public static WorkspaceConfiguration getWorkspaceConfiguration(PreferenceNode cachedProjectsPreferenceNode)
  {
    ResourceSet resourceSet = new ResourceSetImpl();
    Resource resource = resourceSet.createResource(URI.createURI("*.projectconfig"));
    resource.setURI(PROJECT_CONFIG_URI);

    PreferenceNode rootPreferenceNode = PreferencesUtil.getRootPreferenceNode();

    WorkspaceConfiguration workspaceConfiguration = ProjectConfigFactory.eINSTANCE.createWorkspaceConfiguration();
    PreferenceNode instancePreferenceNode = rootPreferenceNode.getNode("instance");
    workspaceConfiguration.setInstancePreferenceNode(instancePreferenceNode);
    workspaceConfiguration.setDefaultPreferenceNode(rootPreferenceNode.getNode("default"));

    EList<PropertyFilter> propertyFilters = workspaceConfiguration.getPropertyFilters();
    PreferenceNode instanceProjectConfNode = instancePreferenceNode.getNode(PROJECT_CONF_NODE_NAME);
    if (instanceProjectConfNode != null)
    {
      for (Property property : instanceProjectConfNode.getProperties())
      {
        String name = property.getName();
        String value = property.getValue();
        if (!"".equals(name) || !"".equals(value))
        {
          PropertyFilter propertyFilter = ProjectConfigFactory.eINSTANCE.createPropertyFilter();
          propertyFilter.setOmissions(Pattern.compile(name));
          if (value != null)
          {
            XMLResourceImpl projectResource = new XMLResourceImpl(PROJECT_CONFIG_URI);
            InputStream in = new URIConverter.ReadableInputStream(value);
            try
            {
              projectResource.load(in, null);
            }
            catch (IOException ex)
            {
              // Ignore.
            }

            EList<EObject> contents = projectResource.getContents();
            if (!contents.isEmpty())
            {
              @SuppressWarnings("unchecked")
              Collection<? extends Predicate> predicates = (Collection<? extends Predicate>)contents;
              propertyFilter.getPredicates().addAll(predicates);
            }
          }

          propertyFilters.add(propertyFilter);
        }
      }
    }
    else
    {
      propertyFilters.addAll(getDefaultPropertyFilters());
    }

    PreferenceNode projectsPreferenceNode = rootPreferenceNode.getNode("project");
    EList<Project> projects = workspaceConfiguration.getProjects();

    for (IProject iProject : WORKSPACE_ROOT.getProjects())
    {
      String name = iProject.getName();
      PreferenceNode projectPreferenceNode = projectsPreferenceNode.getNode(name);
      PreferenceNode cachedProjectPreferenceNode = cachedProjectsPreferenceNode == null ? null
          : cachedProjectsPreferenceNode.getNode(name);
      if (projectPreferenceNode != null
          && (cachedProjectsPreferenceNode == null || cachedProjectPreferenceNode != null))
      {
        Project project = ProjectConfigFactory.eINSTANCE.createProject();

        PreferenceNode projectConfNode = (cachedProjectPreferenceNode == null ? projectPreferenceNode
            : cachedProjectPreferenceNode).getNode(PROJECT_CONF_NODE_NAME);
        if (projectConfNode != null)
        {
          Property projectProperty = projectConfNode.getProperty(PROJECT_CONF_PROJECT_KEY);
          if (projectProperty != null)
          {
            String value = projectProperty.getValue();
            if (value != null)
            {
              XMLResourceImpl projectResource = new XMLResourceImpl(PROJECT_CONFIG_URI);
              InputStream in = new URIConverter.ReadableInputStream(value);
              try
              {
                projectResource.load(in, null);
              }
              catch (IOException ex)
              {
                // Ignore.
              }

              EList<EObject> contents = projectResource.getContents();
              if (!contents.isEmpty())
              {
                project = (Project)contents.get(0);
              }
            }
          }
        }

        project.setPreferenceNode(projectPreferenceNode);

        projects.add(project);
      }
    }

    EList<EObject> contents = resource.getContents();
    contents.add(workspaceConfiguration);
    contents.add(rootPreferenceNode);

    for (Project project : projects)
    {
      for (PreferenceProfile preferenceProfile : project.getPreferenceProfiles())
      {
        for (PreferenceFilter preferenceFilter : preferenceProfile.getPreferenceFilters())
        {
          preferenceFilter.getPreferenceNode();
        }

        for (PreferenceProfile requiredPreferenceProfile : preferenceProfile.getPrerequisites())
        {
          // Resolve proxies.
          requiredPreferenceProfile.eClass();
        }
      }
      EList<PreferenceProfile> profileReferences = project.getPreferenceProfileReferences();
      ArrayList<PreferenceProfile> copy = new ArrayList<PreferenceProfile>(profileReferences);
      profileReferences.clear();
      profileReferences.addAll(copy);
    }

    workspaceConfiguration.updatePreferenceProfileReferences();

    return workspaceConfiguration;
  }

  public static final void saveWorkspaceConfiguration(WorkspaceConfiguration workspaceConfiguration)
      throws BackingStoreException
  {
    saveWorkspaceConfiguration(workspaceConfiguration, false);
  }

  public static final PreferenceNode cacheWorkspaceConfiguration(WorkspaceConfiguration workspaceConfiguration)
  {
    try
    {
      return saveWorkspaceConfiguration(workspaceConfiguration, true);
    }
    catch (BackingStoreException ex)
    {
      // Can't occur when only caching.
      return null;
    }
  }

  private static final PreferenceNode saveWorkspaceConfiguration(WorkspaceConfiguration workspaceConfiguration,
      boolean cache) throws BackingStoreException
  {
    PreferenceNode projectsPreferenceNode = null;

    Map<Pattern, String> propertyFilters = new LinkedHashMap<Pattern, String>();
    for (PropertyFilter propertyFilter : workspaceConfiguration.getPropertyFilters())
    {
      Pattern pattern = propertyFilter.getOmissions();
      Collection<Predicate> predicates = EcoreUtil.copyAll(propertyFilter.getPredicates());

      String predicatesValue = "";
      if (!predicates.isEmpty())
      {
        Resource resource = new XMLResourceImpl(workspaceConfiguration.eResource().getURI());
        resource.getContents().addAll(predicates);

        Map<Object, Object> options = new HashMap<Object, Object>();
        options.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
        options.put(XMLResource.OPTION_LINE_WIDTH, 10);
        options.put(XMLResource.OPTION_ENCODING, "UTF-8");
        try
        {
          StringWriter writer = new StringWriter();
          OutputStream out = new URIConverter.WriteableOutputStream(writer, "UTF-8");
          resource.save(out, options);
          predicatesValue = writer.toString();
        }
        catch (IOException ex)
        {
          ProjectConfigPlugin.INSTANCE.log(ex);
        }
      }

      propertyFilters.put(pattern, predicatesValue);
    }

    PreferenceNode instancePreferenceNode = workspaceConfiguration.getInstancePreferenceNode();
    if (cache)
    {
      PreferenceNode projectConfPreferenceNode = instancePreferenceNode.getNode(PROJECT_CONF_NODE_NAME);
      if (projectConfPreferenceNode == null)
      {
        projectConfPreferenceNode = PreferencesFactory.eINSTANCE.createPreferenceNode();
        projectConfPreferenceNode.setName(PROJECT_CONF_NODE_NAME);
        instancePreferenceNode.getChildren().add(projectConfPreferenceNode);
      }

      EList<Property> properties = projectConfPreferenceNode.getProperties();
      properties.clear();

      if (propertyFilters.isEmpty())
      {
        Property property = PreferencesFactory.eINSTANCE.createProperty();
        property.setName("");
        property.setValue("");
        properties.add(property);
      }
      else
      {
        for (Map.Entry<Pattern, String> entry : propertyFilters.entrySet())
        {
          Property property = PreferencesFactory.eINSTANCE.createProperty();
          property.setName(entry.getKey().toString());
          property.setValue(entry.getValue());
          properties.add(property);
        }
      }
    }
    else
    {
      Preferences projectConfPreferences = PreferencesUtil.getPreferences(instancePreferenceNode, true).node(
          PROJECT_CONF_NODE_NAME);
      projectConfPreferences.clear();

      if (propertyFilters.isEmpty())
      {
        projectConfPreferences.put("", "");
      }
      else
      {
        for (Map.Entry<Pattern, String> entry : propertyFilters.entrySet())
        {
          projectConfPreferences.put(entry.getKey().toString(), entry.getValue());
        }
      }

      projectConfPreferences.flush();
    }

    for (Project project : workspaceConfiguration.getProjects())
    {
      PreferenceNode projectPreferenceNode = project.getPreferenceNode();

      Preferences projectPreferences = cache ? null : PreferencesUtil.getPreferences(projectPreferenceNode, true);

      EList<PreferenceProfile> preferenceProfiles = project.getPreferenceProfiles();
      EList<PreferenceProfile> preferenceProfileReferences = project.getPreferenceProfileReferences();

      String projectPropertyValue = null;
      if (!preferenceProfileReferences.isEmpty() || !preferenceProfiles.isEmpty())
      {
        Project copy = EcoreUtil.copy(project);
        copy.setPreferenceNode(null);
        EList<PreferenceProfile> copyPreferenceProfileReferences = copy.getPreferenceProfileReferences();
        copyPreferenceProfileReferences.clear();

        for (PreferenceProfile preferenceProfileReference : preferenceProfileReferences)
        {
          if (preferenceProfileReference.getPredicates().isEmpty())
          {
            PreferenceProfile proxy = ProjectConfigFactory.eINSTANCE.createPreferenceProfile();
            ((InternalEObject)proxy).eSetProxyURI(URI.createURI(".#"
                + preferenceProfileReference.eResource().getURIFragment(preferenceProfileReference)));
            copyPreferenceProfileReferences.add(proxy);
          }
        }

        for (PreferenceProfile preferenceProfile : copy.getPreferenceProfiles())
        {
          for (PreferenceFilter preferenceFilter : preferenceProfile.getPreferenceFilters())
          {
            PreferenceNode preferenceNode = preferenceFilter.getPreferenceNode();
            if (preferenceNode != null)
            {
              PreferenceNode proxy = PreferencesFactory.eINSTANCE.createPreferenceNode();
              ((InternalEObject)proxy).eSetProxyURI(URI.createURI(".#"
                  + preferenceNode.eResource().getURIFragment(preferenceNode)));
              preferenceFilter.setPreferenceNode(proxy);
            }
          }

          EList<PreferenceProfile> requires = preferenceProfile.getPrerequisites();
          List<PreferenceProfile> requiresCopy = new ArrayList<PreferenceProfile>(requires);
          requires.clear();
          for (PreferenceProfile requiredPreferenceProfile : requiresCopy)
          {
            Resource eResource = requiredPreferenceProfile.eResource();
            if (eResource == null)
            {
              requires.add(requiredPreferenceProfile);
            }
            else
            {
              PreferenceProfile proxy = ProjectConfigFactory.eINSTANCE.createPreferenceProfile();
              ((InternalEObject)proxy).eSetProxyURI(URI.createURI(".#"
                  + eResource.getURIFragment(requiredPreferenceProfile)));
              requires.add(proxy);
            }
          }
        }

        if (!copy.getPreferenceProfiles().isEmpty() || !copy.getPreferenceProfileReferences().isEmpty())
        {
          Resource resource = new XMLResourceImpl(workspaceConfiguration.eResource().getURI());
          resource.getContents().add(copy);

          Map<Object, Object> options = new HashMap<Object, Object>();
          options.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
          options.put(XMLResource.OPTION_LINE_WIDTH, 10);
          options.put(XMLResource.OPTION_ENCODING, "UTF-8");
          try
          {
            StringWriter writer = new StringWriter();
            OutputStream out = new URIConverter.WriteableOutputStream(writer, "UTF-8");
            resource.save(out, options);
            projectPropertyValue = writer.toString();
          }
          catch (IOException ex)
          {
            ProjectConfigPlugin.INSTANCE.log(ex);
          }
        }
      }

      if (projectPropertyValue == null)
      {
        if (cache)
        {
          projectsPreferenceNode = projectPreferenceNode.getParent();
          PreferenceNode projectConfPreferenceNode = projectPreferenceNode.getNode(PROJECT_CONF_NODE_NAME);
          if (projectConfPreferenceNode != null)
          {
            projectConfPreferenceNode.getChildren().remove(projectConfPreferenceNode);
          }
        }
        else if (projectPreferences.nodeExists(PROJECT_CONF_NODE_NAME))
        {
          projectPreferences.node(PROJECT_CONF_NODE_NAME).removeNode();
          projectPreferences.flush();
        }
      }
      else
      {
        if (cache)
        {
          projectsPreferenceNode = projectPreferenceNode.getParent();

          PreferenceNode projectConfPreferenceNode = projectPreferenceNode.getNode(PROJECT_CONF_NODE_NAME);
          if (projectConfPreferenceNode == null)
          {
            projectConfPreferenceNode = PreferencesFactory.eINSTANCE.createPreferenceNode();
            projectConfPreferenceNode.setName(PROJECT_CONF_NODE_NAME);
            projectPreferenceNode.getChildren().add(projectConfPreferenceNode);
          }

          Property projectConfProperty = projectConfPreferenceNode.getProperty(PROJECT_CONF_PROJECT_KEY);
          if (projectConfProperty == null)
          {
            projectConfProperty = PreferencesFactory.eINSTANCE.createProperty();
            projectConfProperty.setName(PROJECT_CONF_PROJECT_KEY);
            projectConfPreferenceNode.getProperties().add(projectConfProperty);
          }

          projectConfProperty.setValue(projectPropertyValue);
        }
        else
        {
          Preferences projectConfPreferences = projectPreferences.node(PROJECT_CONF_NODE_NAME);
          projectConfPreferences.put(PROJECT_CONF_PROJECT_KEY, projectPropertyValue);
          projectConfPreferences.flush();
        }
      }
    }

    return projectsPreferenceNode;
  }

  public static IProject getProject(Project project)
  {
    PreferenceNode preferenceNode = project.getPreferenceNode();
    if (preferenceNode != null)
    {
      String name = preferenceNode.getName();
      if (name != null)
      {
        return WORKSPACE_ROOT.getProject(name);
      }
    }

    return null;
  }

  public static Map<PreferenceNode, Set<Property>> getUnmanagedPreferenceNodes(Project project)
  {
    return ProjectConfigValidator.collectUnmanagedPreferences(project);
  }
}
