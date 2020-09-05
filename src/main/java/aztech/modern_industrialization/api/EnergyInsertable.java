package aztech.modern_industrialization.api;

import alexiil.mc.lib.attributes.Simulation;

public interface EnergyInsertable {
    /**
     * Attempt to fully insert an energy packet into the block, could be called multiple times each tick.
     * @param packet How much energy to insert.
     * @param simulation If simulating, don't change anything.
     * @return True if the packet could be entirely accepted, false otherwise.
     */
    boolean attemptPacketInsertion(long packet, Simulation simulation);
}
