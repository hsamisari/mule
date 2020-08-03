/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.tooling.api.artifact;


import org.mule.api.annotation.NoImplement;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.metadata.MetadataKeyProvider;
import org.mule.runtime.api.metadata.MetadataKeysContainer;
import org.mule.runtime.api.metadata.resolving.MetadataResult;
import org.mule.runtime.api.metadata.resolving.TypeKeysResolver;
import org.mule.runtime.api.value.ValueResult;
import org.mule.runtime.app.declaration.api.ComponentElementDeclaration;
import org.mule.runtime.module.tooling.api.metadata.MetadataTypesContainer;

/**
 * It is in charge of resolving connector's operations and retrieving metadata for all
 * components related to the same session configuration. The session configuration should be
 * defined by multiple global elements, including Configurations, Connections, etc.
 * <p/>
 * This session provides the possibility to avoid having a full artifact configuration before being able to
 * gather metadata from the connector.
 * <p/>
 *
 * @since 4.4.0
 */
@NoImplement
public interface DeclarationSession {

  /**
   * Test connectivity for the connection associated to the configuration with the provided name.
   *
   * @param configName The name of the config for which to test connection.
   * @return a {@link ConnectionValidationResult} with the result of the connectivity testing
   */
  ConnectionValidationResult testConnection(String configName);

  /**
   * Retrieve all {@link org.mule.runtime.api.value.Value} that can be configured for the given parameter.
   *
   * @param component a {@link ComponentElementDeclaration} for the Component (Operation, Source, etc) from which
   *                  the available values can be used on the parameter {@param parameterName}. In case the value
   *                  provider requires any acting parameters to be able to resolve this values, those parameters
   *                  should be populated in this declaration. Also, if the Component requires values from a Configuration,
   *                  then its name should be specified in the declaration.
   * @param parameterName the name of the parameter for which to resolve the {@link org.mule.runtime.api.value.Value}s
   * @return a {@link ValueResult} with the accepted parameter values to use
   */
  ValueResult getValues(ComponentElementDeclaration component, String parameterName);

  /**
   * Returns the list of types that can be described by the {@link TypeKeysResolver} associated to the {@link MetadataKeyProvider}
   * Component identified by the {@link Location}.
   *
   * @param component the location of the {@link MetadataKeyProvider} component to query for its available keys.
   * @return Successful {@link MetadataResult} if the keys are successfully resolved Failure {@link MetadataResult} if there is an
   *         error while resolving the keys
   */
  // todo vuela
  MetadataResult<MetadataKeysContainer> getMetadataKeys(ComponentElementDeclaration component);


  // todo unify
  MetadataResult<MetadataType> inputMetadata(ComponentElementDeclaration component, String parameterName);

  MetadataResult<MetadataType> outputMetadata(ComponentElementDeclaration component);

  MetadataResult<MetadataType> outputAttributesMetadata(ComponentElementDeclaration component);

  /**
   *
   * @param component
   * @return
   */
  MetadataResult<MetadataTypesContainer> getMetadataTypes(ComponentElementDeclaration component);

  /**
   * Stops and disposes all resources used by this {@link DeclarationSession}
   */
  void dispose();

}
