/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.compat.dashloader;

import aztech.modern_industrialization.pipes.impl.PipeMeshCache;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;

public class DashPipeMeshCache {

    // wierd activej issues
    @Serialize(order = 0)
    public List<List<List<List<DashMesh>>>> connectionMeshes;
    @Serialize(order = 1)
    public List<List<DashMesh>> centerMeshes;

    @SuppressWarnings("unused")
    public DashPipeMeshCache(@Deserialize("connectionMeshes") List<List<List<List<DashMesh>>>> connectionMeshes,
            @Deserialize("centerMeshes") List<List<DashMesh>> centerMeshes) {
        this.connectionMeshes = connectionMeshes;
        this.centerMeshes = centerMeshes;
    }

    public DashPipeMeshCache(PipeMeshCache pipeMeshCache) {
        final Mesh[][][][] originalConnectionMeshes = pipeMeshCache.getConnectionMeshes();
        DashMesh[][][][] connectionMeshes = new DashMesh[originalConnectionMeshes.length][3][6][8];
        // why why why why why why why why why why why why why why why why why why why
        for (int i = 0; i < originalConnectionMeshes.length; i++) {
            final Mesh[][][] originalConnectionMesh = originalConnectionMeshes[i];
            for (int i1 = 0; i1 < originalConnectionMesh.length; i1++) {
                final Mesh[][] originalConnectionMesh1 = originalConnectionMesh[i1];
                for (int i2 = 0; i2 < originalConnectionMesh1.length; i2++) {
                    final Mesh[] meshes = originalConnectionMesh1[i2];
                    for (int i3 = 0; i3 < meshes.length; i3++) {
                        connectionMeshes[i][i1][i2][i3] = new DashMesh(meshes[i3]);
                    }
                }
            }
        }

        this.connectionMeshes = Arrays.stream(connectionMeshes).map(dashMeshes -> {
            return Arrays.stream(dashMeshes).map(dashMeshes1 -> {
                return Arrays.stream(dashMeshes1).map(dashMeshes2 -> {
                    final List<DashMesh> objects = new ArrayList<>();
                    for (int i1 = 0; i1 < dashMeshes2.length; i1++) {
                        objects.add(i1, dashMeshes2[i1]);
                    }
                    return objects;
                }).toList();
            }).toList();
        }).toList();

        DashMesh[][] centerMeshes = new DashMesh[3][1 << 6];
        final Mesh[][] originalCenterMeshes = pipeMeshCache.getCenterMeshes();
        for (int i = 0; i < originalCenterMeshes.length; i++) {
            final Mesh[] originalCenterMesh = originalCenterMeshes[i];
            for (int i1 = 0; i1 < originalCenterMesh.length; i1++) {
                centerMeshes[i][i1] = new DashMesh(originalCenterMesh[i1]);
            }
        }

        this.centerMeshes = Arrays.stream(centerMeshes).map(dashMeshes -> {
            final List<DashMesh> objects = new ArrayList<>();
            for (int i1 = 0; i1 < dashMeshes.length; i1++) {
                objects.add(i1, dashMeshes[i1]);
            }
            return objects;
        }).toList();
    }

    public PipeMeshCache toUndash() {
        final Mesh[][][][] connectionMeshesOut = new Mesh[connectionMeshes.size()][3][6][8];
        for (int i = 0; i < connectionMeshes.size(); i++) {
            final List<List<List<DashMesh>>> originalConnectionMesh = connectionMeshes.get(i);
            for (int i1 = 0; i1 < originalConnectionMesh.size(); i1++) {
                final List<List<DashMesh>> originalConnectionMesh1 = originalConnectionMesh.get(i1);
                for (int i2 = 0; i2 < originalConnectionMesh1.size(); i2++) {
                    final List<DashMesh> meshes = originalConnectionMesh1.get(i2);
                    for (int i3 = 0; i3 < meshes.size(); i3++) {
                        connectionMeshesOut[i][i1][i2][i3] = meshes.get(i3).toUndash();
                    }
                }
            }
        }
        final Mesh[][] centerMeshesOut = new Mesh[3][1 << 6];
        for (int i = 0; i < centerMeshes.size(); i++) {
            final List<DashMesh> centerMesh = centerMeshes.get(i);
            for (int i1 = 0; i1 < centerMesh.size(); i1++) {
                centerMeshesOut[i][i1] = centerMesh.get(i1).toUndash();
            }
        }
        return new PipeMeshCache(connectionMeshesOut, centerMeshesOut,
                RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());
    }

}
