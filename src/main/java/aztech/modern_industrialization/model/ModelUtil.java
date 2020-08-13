package aztech.modern_industrialization.model;

import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.Vector3f;

/**
 * Model utilities.
 */
public class ModelUtil {
    private static final Transformation BLOCK_GUI = new Transformation(
            new Vector3f(30, 225, 0),
            new Vector3f(0, 0, 0),
            new Vector3f(0.625f, 0.625f, 0.625f)
    );
    private static final Transformation BLOCK_GROUND = new Transformation(
            new Vector3f(0, 0, 0),
            new Vector3f(0, 3.0f / 16, 0),
            new Vector3f(0.25f, 0.25f, 0.25f)
    );
    private static final Transformation BLOCK_FIXED = new Transformation(
            new Vector3f(0, 0, 0),
            new Vector3f(0, 0, 0),
            new Vector3f(0.5f, 0.5f, 0.5f)
    );
    private static final Transformation BLOCK_THIRDPERSON = new Transformation(
            new Vector3f(75, 45, 0),
            new Vector3f(0, 2.5f / 16, 0),
            new Vector3f(0.375f, 0.375f, 0.375f)
    );
    private static final Transformation BLOCK_FIRSTPERSON_RIGHTHAND = new Transformation(
            new Vector3f(0, 45, 0),
            new Vector3f(0, 0, 0),
            new Vector3f(0.40f, 0.40f, 0.40f)
    );
    private static final Transformation BLOCK_FIRSTPERSON_LEFTHAND = new Transformation(
            new Vector3f(0, 225, 0),
            new Vector3f(0, 0, 0),
            new Vector3f(0.40f, 0.40f, 0.40f)
    );
    /**
     * Default block transformation, hardcoded with values from minecraft:block/block model.
     */
    // Should we load this from a JSON file?
    public static final ModelTransformation BLOCK_TRANSFORMATION = new ModelTransformation(
            BLOCK_THIRDPERSON,
            BLOCK_THIRDPERSON,
            BLOCK_FIRSTPERSON_LEFTHAND,
            BLOCK_FIRSTPERSON_RIGHTHAND,
            Transformation.IDENTITY,
            BLOCK_GUI,
            BLOCK_GROUND,
            BLOCK_FIXED
    );
}
