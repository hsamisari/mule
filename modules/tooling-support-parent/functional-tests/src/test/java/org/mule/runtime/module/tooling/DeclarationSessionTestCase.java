/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.tooling;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mule.runtime.app.declaration.api.fluent.ElementDeclarer.newArtifact;
import static org.mule.tck.junit4.matcher.MetadataKeyMatcher.metadataKeyWithId;
import static org.mule.test.infrastructure.maven.MavenTestUtils.getMavenLocalRepository;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.internal.utils.MetadataTypeWriter;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataKeysContainer;
import org.mule.runtime.api.metadata.resolving.MetadataResult;
import org.mule.runtime.api.value.ValueResult;
import org.mule.runtime.app.declaration.api.ComponentElementDeclaration;
import org.mule.runtime.app.declaration.api.SourceElementDeclaration;
import org.mule.runtime.app.declaration.api.fluent.ArtifactDeclarer;
import org.mule.runtime.module.tooling.api.artifact.DeclarationSession;
import org.mule.runtime.module.tooling.api.metadata.MetadataTypesContainer;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.infrastructure.deployment.AbstractFakeMuleServerTestCase;

import java.util.List;
import java.util.Set;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class DeclarationSessionTestCase extends AbstractFakeMuleServerTestCase implements TestExtensionAware {

  private static final String EXTENSION_GROUP_ID = "org.mule.tooling";
  private static final String EXTENSION_ARTIFACT_ID = "tooling-support-test-extension";
  private static final String EXTENSION_VERSION = "1.0.0-SNAPSHOT";
  private static final String EXTENSION_CLASSIFIER = "mule-plugin";
  private static final String EXTENSION_TYPE = "jar";

  private static final String CONFIG_NAME = "dummyConfig";
  private static final String CLIENT_NAME = "client";
  private static final String PROVIDED_PARAMETER_NAME = "providedParameter";

  private static final String CONFIG_LESS_CONNECTION_METADATA_RESOLVER = "ConfigLessConnectionLessMetadataResolver";
  private static final String CONFIG_LESS_METADATA_RESOLVER = "ConfigLessMetadataResolver";
  private static final String MULTI_LEVEL_PARTIAL_TYPE_KEYS_OUTPUT_RESOLVER = "MultiLevelPartialTypeKeysOutputResolver";

  private DeclarationSession session;

  @ClassRule
  public static SystemProperty artifactsLocation =
      new SystemProperty("mule.test.maven.artifacts.dir", DeclarationSession.class.getResource("/").getPath());

  @Rule
  public SystemProperty repositoryLocation =
      new SystemProperty("muleRuntimeConfig.maven.repositoryLocation", getMavenLocalRepository().getAbsolutePath());

  @Override
  public void setUp() throws Exception {
    ArtifactDeclarer artifactDeclarer = newArtifact();
    artifactDeclarer.withGlobalElement(configurationDeclaration(CONFIG_NAME, connectionDeclaration(CLIENT_NAME)));
    super.setUp();
    this.session = this.muleServer
        .toolingService()
        .newDeclarationSessionBuilder()
        .addDependency(EXTENSION_GROUP_ID,
                       EXTENSION_ARTIFACT_ID,
                       EXTENSION_VERSION,
                       EXTENSION_CLASSIFIER,
                       EXTENSION_TYPE)
        .setArtifactDeclaration(artifactDeclarer.getDeclaration())
        .build();
    this.muleServer.start();
  }

  @Test
  public void testConnection() {
    ConnectionValidationResult connectionValidationResult = session.testConnection(CONFIG_NAME);
    assertThat(connectionValidationResult.isValid(), equalTo(true));
  }

  private String expectedParameter(String actingParameter) {
    return "WITH-ACTING-PARAMETER-" + actingParameter;
  }

  @Test
  public void configLessConnectionLessOnOperation() {
    ComponentElementDeclaration elementDeclaration = configLessConnectionLessOPDeclaration(CONFIG_NAME);
    getResultAndValidate(elementDeclaration, PROVIDED_PARAMETER_NAME, "ConfigLessConnectionLessNoActingParameter");
  }

  @Test
  public void configLessConnectionLessOnOperationMetadataKeys() {
    MetadataResult<MetadataKeysContainer> metadataKeys =
        session.getMetadataKeys(configLessConnectionLessOPDeclaration(CONFIG_NAME));
    assertThat(metadataKeys.isSuccess(), is(true));

    Set<MetadataKey> keys = metadataKeys.get().getKeysByCategory().get(CONFIG_LESS_CONNECTION_METADATA_RESOLVER);
    assertThat(keys, IsCollectionWithSize.hasSize(1));
    assertThat(keys.stream().findFirst().map(metadataKey -> metadataKey.getId())
        .orElseThrow(() -> new AssertionError("MetadataKey not resolved")), is(CONFIG_LESS_CONNECTION_METADATA_RESOLVER));
  }

  @Test
  public void configLessOPMetadataKeys() {
    ComponentElementDeclaration elementDeclaration = configLessOPDeclaration(CONFIG_NAME);
    MetadataResult<MetadataKeysContainer> metadataKeys = session.getMetadataKeys(elementDeclaration);
    assertThat(metadataKeys.isSuccess(), is(true));

    Set<MetadataKey> keys = metadataKeys.get().getKeysByCategory().get(CONFIG_LESS_METADATA_RESOLVER);
    assertThat(keys, IsCollectionWithSize.hasSize(1));
    assertThat(keys.stream().findFirst().map(metadataKey -> metadataKey.getId())
        .orElseThrow(() -> new AssertionError("MetadataKey not resolved")), is(CLIENT_NAME));
  }

  @Test
  public void multiLevelOPMetadataKeysPartialEmptyFirstLevel() {
    multiLevelComponentMetadataKeysPartialEmptyFirstLevel(multiLevelOPDeclaration(CONFIG_NAME, null, null));
  }

  @Test
  public void multiLevelSourceMetadataKeysPartialEmptyFirstLevel() {
    multiLevelComponentMetadataKeysPartialEmptyFirstLevel(sourceDeclaration(CONFIG_NAME, null, null, null));
  }

  private void multiLevelComponentMetadataKeysPartialEmptyFirstLevel(ComponentElementDeclaration elementDeclaration) {
    MetadataResult<MetadataKeysContainer> metadataKeys = session.getMetadataKeys(elementDeclaration);
    assertThat(metadataKeys.isSuccess(), is(true));

    Set<MetadataKey> keys = metadataKeys.get().getKeysByCategory().get(MULTI_LEVEL_PARTIAL_TYPE_KEYS_OUTPUT_RESOLVER);
    assertThat(keys, IsCollectionWithSize.hasSize(0));
  }

  @Test
  public void multiLevelOPMetadataKeyPartialWithFirstLevel() {
    multiLevelComponentMetadataKeyPartialWithFirstLevel(multiLevelOPDeclaration(CONFIG_NAME, "America", null));
  }

  @Test
  public void multiLevelShowInDslGroupOPMetadataKeyPartialWithFirstLevel() {
    multiLevelComponentMetadataKeyPartialWithFirstLevel(multiLevelShowInDslGroupOPDeclaration(CONFIG_NAME, "America", null));
  }

  @Test
  public void multiLevelSourceMetadataKeyPartialWithFirstLevel() {
    multiLevelComponentMetadataKeyPartialWithFirstLevel(sourceDeclaration(CONFIG_NAME, "America", null));
  }

  private void multiLevelComponentMetadataKeyPartialWithFirstLevel(ComponentElementDeclaration elementDeclaration) {
    MetadataResult<MetadataKeysContainer> metadataKeys = session.getMetadataKeys(elementDeclaration);
    assertThat(metadataKeys.isSuccess(), is(true));

    Set<MetadataKey> continents = metadataKeys.get().getKeysByCategory().get(MULTI_LEVEL_PARTIAL_TYPE_KEYS_OUTPUT_RESOLVER);
    assertThat(continents, IsCollectionWithSize.hasSize(1));
    final MetadataKey continent =
        continents.stream().findFirst().orElseThrow(() -> new AssertionError("MetadataKey not resolved"));
    assertThat(continent, metadataKeyWithId("AMERICA").withDisplayName("AMERICA").withPartName("continent"));

    Set<MetadataKey> countries = continent.getChilds();
    assertThat(countries, IsCollectionWithSize.hasSize(2));
    assertThat(countries, hasItem(metadataKeyWithId("USA").withDisplayName("United States").withPartName("country")));
    assertThat(countries, hasItem(metadataKeyWithId("ARGENTINA").withDisplayName("ARGENTINA").withPartName("country")));
  }

  @Test
  public void allMetadataSource() {
    SourceElementDeclaration sourceElementDeclaration = sourceDeclaration(CONFIG_NAME, null, "America", "USA", "SFO");
    MetadataResult<MetadataTypesContainer> containerTypeMetadataResult = session.getMetadataTypes(sourceElementDeclaration);
    assertThat(containerTypeMetadataResult.isSuccess(), is(true));

    assertThat(containerTypeMetadataResult.get().getOutputMetadata().isPresent(), is(true));
    assertThat(new MetadataTypeWriter().toString(containerTypeMetadataResult.get().getOutputMetadata().get()),
            equalTo("%type _:Java = @default(\"value\" : \"America|USA|SFO\") String"));

    assertThat(containerTypeMetadataResult.get().getOutputAttributesMetadata().isPresent(), is(true));
    assertThat(new MetadataTypeWriter().toString(containerTypeMetadataResult.get().getOutputAttributesMetadata().get()),
            equalTo("%type _:Java = @typeId(\"value\" : \"org.mule.tooling.extensions.metadata.api.source.StringAttributes\") {\n"
                    +
                    "  \"value\"? : @default(\"value\" : \"America|USA|SFO\") String\n" +
                    "}"));

  }

  @Test
  public void outputMetadataSource() {
    SourceElementDeclaration sourceElementDeclaration = sourceDeclaration(CONFIG_NAME, null, "America", "USA", "SFO");
    MetadataResult<MetadataType> outputTypeMetadataResult = session.outputMetadata(sourceElementDeclaration);
    assertThat(outputTypeMetadataResult.isSuccess(), is(true));
    assertThat(new MetadataTypeWriter().toString(outputTypeMetadataResult.get()),
               equalTo("%type _:Java = @default(\"value\" : \"America|USA|SFO\") String"));
  }

  @Test
  public void outputAttributesMetadataSource() {
    SourceElementDeclaration sourceElementDeclaration = sourceDeclaration(CONFIG_NAME, null, "America", "USA", "SFO");
    MetadataResult<MetadataType> outputTypeMetadataResult = session.outputAttributesMetadata(sourceElementDeclaration);
    assertThat(outputTypeMetadataResult.isSuccess(), is(true));
    assertThat(new MetadataTypeWriter().toString(outputTypeMetadataResult.get()),
               equalTo("%type _:Java = @typeId(\"value\" : \"org.mule.tooling.extensions.metadata.api.source.StringAttributes\") {\n"
                   +
                   "  \"value\"? : @default(\"value\" : \"America|USA|SFO\") String\n" +
                   "}"));
  }

  @Test
  public void inputSourceCallbackMetadataSource() {
    SourceElementDeclaration sourceElementDeclaration = sourceDeclaration(CONFIG_NAME, null, "America", "USA", "SFO");
    MetadataResult<MetadataType> outputTypeMetadataResult = session.inputMetadata(sourceElementDeclaration, "onSuccessParameter");
    assertThat(outputTypeMetadataResult.isSuccess(), is(true));
    assertThat(new MetadataTypeWriter().toString(outputTypeMetadataResult.get()),
               equalTo("%type _:Java = @default(\"value\" : \"America|USA|SFO\") String"));
  }

  @Test
  public void configLessOnOperation() {
    ComponentElementDeclaration elementDeclaration = configLessOPDeclaration(CONFIG_NAME);
    getResultAndValidate(elementDeclaration, PROVIDED_PARAMETER_NAME, CLIENT_NAME);
  }

  @Test
  public void actingParameterOnOperation() {
    final String actingParameter = "actingParameter";
    ComponentElementDeclaration elementDeclaration = actingParameterOPDeclaration(CONFIG_NAME, actingParameter);
    getResultAndValidate(elementDeclaration, PROVIDED_PARAMETER_NAME, expectedParameter(actingParameter));
  }

  @Test
  public void actingParameterGroup() {
    final String stringValue = "stringValue";
    final int intValue = 0;
    final List<String> listValue = asList("one", "two", "three");
    ComponentElementDeclaration elementDeclaration =
        actingParameterGroupOPDeclaration(CONFIG_NAME, stringValue, intValue, listValue);
    getResultAndValidate(elementDeclaration, PROVIDED_PARAMETER_NAME, "stringValue-0-one-two-three");
  }

  @Test
  public void complexActingParameter() {
    final String stringValue = "stringValue";
    ComponentElementDeclaration elementDeclaration =
        complexActingParameterOPDeclaration(CONFIG_NAME, stringValue);
    getResultAndValidate(elementDeclaration, PROVIDED_PARAMETER_NAME, stringValue);
  }

  private void getResultAndValidate(ComponentElementDeclaration elementDeclaration, String parameterName, String expectedValue) {
    ValueResult providerResult = session.getValues(elementDeclaration, parameterName);

    assertThat(providerResult.isSuccess(), equalTo(true));
    assertThat(providerResult.getValues(), hasSize(1));
    assertThat(providerResult.getValues().iterator().next().getId(), is(expectedValue));
  }

}
