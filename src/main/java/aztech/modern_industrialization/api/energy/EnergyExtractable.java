package aztech.modern_industrialization.api.energy;

public interface EnergyExtractable {
    /**
     * Attempt to extract an energy packet.
     * 
     * @param maxAmount The max amount of EU to extract.
     * @return How much energy was extracted, or 0 if no energy could be extracted.
     */
    long extractEnergy(long maxAmount);

    /**
     * Return whether the machine can send energy to a given pipe tier.
     * 
     * @param tier The tier of the connexion.
     * @return whether true if the machine can send energy to that pipe tier, or
     *         false otherwise.
     */
    boolean canExtract(CableTier tier);
}
