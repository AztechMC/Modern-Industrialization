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
package aztech.modern_industrialization.blocks.structure;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class StructureMultiblockControllerBlockEntity extends FastBlockEntity {
    private String inputId;
    private String inputCasing;

    private ResourceLocation id;
    private MachineCasing casing;
    private BoundingBox bounds;
    private boolean showBounds;

    public StructureMultiblockControllerBlockEntity(BlockPos pos, BlockState state) {
        super(MIRegistries.STRUCTURE_MULTIBLOCK_CONTROLLER_BE.get(), pos, state);
    }

    @Nullable
    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
        id = ResourceLocation.tryParse(inputId);
    }

    @Nullable
    public String getInputCasing() {
        return inputCasing;
    }

    public void setInputCasing(String inputCasing) {
        this.inputCasing = inputCasing;
        casing = formatCasing(inputCasing);
    }

    @Nullable
    public BoundingBox getBounds() {
        return bounds;
    }

    public void setBounds(BoundingBox bounds) {
        this.bounds = bounds;
    }

    @Nullable
    public ResourceLocation getId() {
        return id;
    }
    
    public boolean shouldShowBounds() {
        return showBounds;
    }
    
    public void setShowBounds(boolean showBounds) {
        this.showBounds = showBounds;
    }

    @Nullable
    public static MachineCasing formatCasing(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        ResourceLocation casingId = null;
        if (input.contains(":")) {
            casingId = ResourceLocation.tryParse(input);
        } else if (ResourceLocation.isValidPath(input)) {
            casingId = MI.id(input);
        }
        if (casingId == null) {
            return null;
        }
        if (MachineCasings.registeredCasings.containsKey(casingId)) {
            return MachineCasings.get(casingId);
        }
        return null;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (id != null) {
            tag.putString("structure_id", inputId);
        }
        if (inputCasing != null) {
            tag.putString("casing", inputCasing);
        }
        if (bounds != null) {
            CompoundTag boundsTag = new CompoundTag();
            boundsTag.put("min", NbtUtils.writeBlockPos(new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ())));
            boundsTag.put("max", NbtUtils.writeBlockPos(new BlockPos(bounds.maxX(), bounds.maxY(), bounds.maxZ())));
            tag.put("bounds", boundsTag);
        }
        tag.putBoolean("show_bounds", showBounds);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.setInputId(tag.getString("structure_id"));
        this.setInputCasing(tag.getString("casing"));
        if (tag.contains("bounds", Tag.TAG_COMPOUND)) {
            CompoundTag boundsTag = tag.getCompound("bounds");
            BlockPos min = NbtUtils.readBlockPos(boundsTag, "min").orElseThrow();
            BlockPos max = NbtUtils.readBlockPos(boundsTag, "max").orElseThrow();
            bounds = BoundingBox.fromCorners(min, max);
        }
        showBounds = tag.getBoolean("show_bounds");
    }
}
