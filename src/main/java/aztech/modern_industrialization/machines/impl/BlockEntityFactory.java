package aztech.modern_industrialization.machines.impl;

@FunctionalInterface
public interface BlockEntityFactory {
    MachineBlockEntity create(MachineFactory factory);
}
