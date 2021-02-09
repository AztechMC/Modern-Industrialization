package aztech.modern_industrialization.machinesv2.components;

import aztech.modern_industrialization.util.Simulation;

public class EnergyComponent {
    private long storedEu;

    public long consumeEu(long max, Simulation simulation) {
        long ext = Math.min(max, storedEu);
        if (simulation.isActing()) {
            storedEu -= ext;
        }
        return ext;
    }
}
