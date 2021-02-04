package aztech.modern_industrialization.util;

import aztech.modern_industrialization.mixin.ResourceImplAccessor;
import net.minecraft.resource.Resource;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceUtil {
    public static byte[] getBytes(Resource resource) throws IOException {
        InputStream is = resource.getInputStream();
        byte[] textureBytes = IOUtils.toByteArray(is);
        ((ResourceImplAccessor) resource).setInputStream(new ByteArrayInputStream(textureBytes));
        return textureBytes;
    }
}
