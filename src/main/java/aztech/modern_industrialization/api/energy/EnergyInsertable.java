package aztech.modern_industrialization.api.energy;

public interface EnergyInsertable {
    /**
     * Attempt to partially insert energy into the block.
     * @param amount How much energy to insert.
     * @return The leftover energy.
     */
    long insertEnergy(long amount);

    /**
     * Return whether the machine can accept energy from a given pipe tier.
     * @param tier The tier of the connexion.
     * @return whether true if the machine can accept energy from that pipe tier, or false otherwise.
     */
    boolean canInsert(CableTier tier);
}
