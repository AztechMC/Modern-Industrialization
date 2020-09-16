package aztech.modern_industrialization.api;

public interface EnergyExtractable {
    /**
     * Attempt to extract an energy packet.
     * @param maxAmount The max amount of EU to extract.
     * @return How much energy was extracted, or 0 if no energy could be extracted.
     */
    long extractEnergy(long maxAmount);
}
