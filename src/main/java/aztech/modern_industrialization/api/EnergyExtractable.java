package aztech.modern_industrialization.api;

import alexiil.mc.lib.attributes.Simulation;

public interface EnergyExtractable {
    /**
     * Attempt to extract an energy packet.
     * @param simulation If simulating, don't change anything.
     * @return How much energy was extracted, or 0 if no energy could be extracted.
     */
    long attemptPacketExtraction(Simulation simulation);
}
