package aztech.modern_industrialization.definition;

public sealed interface GeneratedItemModel {
    record FlatItem() implements GeneratedItemModel {}

    record FlatHandheldItem() implements GeneratedItemModel {}

    record BlockEntityRenderer() implements GeneratedItemModel {}

    record Pipe() implements GeneratedItemModel {}

    /// Notifies the data generation code that a custom model is already provided as a json resource.
    record Custom() implements GeneratedItemModel {}

    /// No model generation. The model must be provided through some other means to the data generator to pass validation.
    record None() implements GeneratedItemModel {}
}
