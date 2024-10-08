package io.quarkiverse.backstage.v1alpha1;

import io.quarkiverse.backstage.model.builder.VisitableBuilder;

public class ParameterBuilder extends ParameterFluent<ParameterBuilder>
        implements VisitableBuilder<Parameter, ParameterBuilder> {
    public ParameterBuilder() {
        this(new Parameter());
    }

    public ParameterBuilder(ParameterFluent<?> fluent) {
        this(fluent, new Parameter());
    }

    public ParameterBuilder(ParameterFluent<?> fluent, Parameter instance) {
        this.fluent = fluent;
        fluent.copyInstance(instance);
    }

    public ParameterBuilder(Parameter instance) {
        this.fluent = this;
        this.copyInstance(instance);
    }

    ParameterFluent<?> fluent;

    public Parameter build() {
        Parameter buildable = new Parameter(fluent.getName(), fluent.getDescription(), fluent.getType(),
                fluent.getDefaultValue());
        return buildable;
    }

}