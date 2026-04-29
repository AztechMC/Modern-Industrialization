package aztech.modern_industrialization.definition;

public sealed interface GeneratedBlockModel {
    record TrivialCube() implements GeneratedBlockModel {}

    record TrivialColumn() implements GeneratedBlockModel {}

    record Explosive() implements GeneratedBlockModel {}

    record Tank() implements GeneratedBlockModel {}

    record NoTemplateModel() implements GeneratedBlockModel {}

    record None() implements GeneratedBlockModel {}
}
