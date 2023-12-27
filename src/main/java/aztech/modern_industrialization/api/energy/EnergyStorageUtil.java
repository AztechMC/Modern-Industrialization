package aztech.modern_industrialization.api.energy;

import aztech.modern_industrialization.MI;

public class EnergyStorageUtil {
    public static long move(ILongEnergyStorage source, ILongEnergyStorage target, long maxAmount) {
        long simulatedExtract = source.extract(maxAmount, true);
        long simulatedInsert = target.receive(simulatedExtract, true);

        long extractedAmount = source.extract(simulatedInsert, false);
        long insertedAmount = target.receive(extractedAmount, false);

        if (insertedAmount < extractedAmount) {
            // Try to give the remainder back
            long leftover = source.receive(extractedAmount - insertedAmount, false);
            if (leftover > 0) {
                MI.LOGGER.error("Energy storage {} did not accept {} leftover energy from {}! Voiding it.", source, leftover, target);
            }
        }

        return insertedAmount;
    }
}
