package org.mule.runtime.module.tooling.api.metadata;

import static java.util.Optional.ofNullable;

import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.meta.Typed;
import org.mule.runtime.api.meta.model.ComponentModel;
import org.mule.runtime.api.meta.model.HasOutputModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MetadataTypesContainer {

    private Map<String, MetadataType> input = new HashMap<>();
    private MetadataType output;
    private MetadataType outputAttributes;

    public MetadataTypesContainer(ComponentModel model) {
        if (model instanceof HasOutputModel) {
            this.output = ((HasOutputModel) model).getOutput().getType();
            this.outputAttributes = ((HasOutputModel) model).getOutputAttributes().getType();
        }
        model.getAllParameterModels().stream()
                .filter(Typed::hasDynamicType)
                .forEach(p -> input.put(p.getName(), p.getType()));
    }

    public Map<String, MetadataType> getInputMetadata() {
        return new HashMap<>(input);
    }

    public Optional<MetadataType> getOutputMetadata() {
        return ofNullable(output);
    }

    public Optional<MetadataType> getOutputAttributesMetadata() {
        return ofNullable(outputAttributes);
    }

}
