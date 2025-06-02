---
navigation:
  title: "The Nuclear Reactor"
  icon: "modern_industrialization:uranium_fuel_rod_quad"
  position: 206
  parent: modern_industrialization:midgame.md
item_ids:
  - modern_industrialization:nuclear_reactor
  - modern_industrialization:nuclear_casing
  - modern_industrialization:nuclear_item_hatch
  - modern_industrialization:nuclear_fluid_hatch
---

# The Nuclear Reactor

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="nuclear_reactor" />
    <MultiblockShape controller="nuclear_reactor" useBigShape={true} x="-11" z="-3" />
</GameScene>

The Nuclear Reactor is a large multiblock whose purpose is to generate massive amounts of energy by consuming nuclear fuel. It can produce 100s of times more EU/t than diesel. It comes in several sizes from small to large.

<Recipe id="modern_industrialization:electric_age/machine/nuclear_reactor_asbl" />

To produce power using the Nuclear Reactor, you need to place some form of Water in a Nuclear Fluid Hatch. It will turn into Steam that can then be used to produce power, possibly by running it through a Heat Exchanger first.

The Nuclear Reactor is also the only way to produce some materials such as Plutonium.

Finally, it can produce fluids used for nuclear fusion: Deuterium and Tritium.

How it works under the hood can be a bit overwhelming, despite the following pages trying to explain it. Nevertheless, you don't need to understand the details to be able to design a powerful reactor.

We recommend that you experiment with various designs in creative mode until you find one that suits you.

Note that the Nuclear Reactor cannot explode, emit radiation, or otherwise damage the map. The only thing that it can damage is the items you put in it if the temperature gets too high (more on this later).

You should feel free to experiment, and enjoy your new life as a nuclear scientist!

The main component is the Nuclear Casing which is made with Nuclear Alloy : a mix of cadmium, beryllium and blastproof alloy.

<Recipe id="modern_industrialization:electric_age/casing/nuclear_casing_asbl" />

The upper part of the structure accepts Nuclear Item or Fluid Hatches (or simple casings). Those are the inputs and outputs of the reactor.

<Recipe id="modern_industrialization:electric_age/casing/nuclear_item_hatch_asbl" />

Each hatch has one input (either item or fluid) and two outputs. The input slots will form a grid, displayed in the reactor GUI (accessible by right-clicking on the controller).

<Recipe id="modern_industrialization:electric_age/casing/nuclear_fluid_hatch_asbl" />

Each hatch has a temperature and stores heat which can be dissipated in several ways. The heat will naturally move to an adjacent hatch or to the outside if the hatch is on the edge (the heat is then lost). The speed of this process is equal to the hatches content's heat transfer coefficient (shown in REI) times the temperature difference. The heat can also be extracted in a fluid hatch by producing steam. A temperature above an item's maximum temperature will destroy it.

The core nuclear reactor elements are neutrons. Those are produced by the nuclear fuel. There are two type of neutrons, fast and thermal : the fast carry energy while the thermal do not. Neutrons move in a straight line until they encounter an element or exit the reactor (for fast neutrons, their energy is then lost). The flow of neutrons is shown in the reactor GUI.

When a neutron encounters a non-empty hatch, two things can happen : the neutron is absorbed or scattered. A scattered neutron will randomly change direction. If the scattered neutron is fast, it has a chance of slowing down, becoming a thermal one. This transfers the energy from the neutron to the hatch in the form of heat. An absorbed neutron stops its course and also transfers its energy if it was a fast neutron.

The number of absorbed neutrons in a single hatch can be seen in the GUI. The probability for each processes is shown in REI. Those will strongly depend on the hatch's content and the neutron type (fast or thermal). Nuclear fuels absorb thermal neutrons far better.

When a neutron, either fast or thermal, is absorbed in nuclear fuel, more neutrons are generated. Those are always fast neutrons and have random direction. Their generation is accompanied by the direct release of energy in the form of additional heat in the hatch.

Above a certain threshold, the number of generated neutrons will decrease with the temperature until reaching zero. This process wastes some energy, but guarantees the stability of the reactor. The number of generated neutrons (and effective efficiency), the direct energy and the temperature threshold are in REI.

Each nuclear component has maximum number of absorptions. When reached, the item is either destroyed or transformed into a depleted version. This is particularly useful for nuclear fuel because some part of the U238 is transformed into Plutonium in the depleted version, meaning some of it can be transformed back into fuel.

The same thing happens for fluids: a bit of fluid is transformed after each neutron absorption. This can be used to mass produce useful isotopes like deuterium and tritium. In both cases, the outcome is shown in REI.

