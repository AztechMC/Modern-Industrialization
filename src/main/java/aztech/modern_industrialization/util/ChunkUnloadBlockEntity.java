package aztech.modern_industrialization.util;

public interface ChunkUnloadBlockEntity {
    /**
     * Will be called for every block entity implementing this interface when its chunk is unloaded.
     */
    void onChunkUnload();
}
