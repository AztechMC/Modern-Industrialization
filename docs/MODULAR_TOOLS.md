# Modular Tools

A tool consists of:

A casing (the base ModularToolItem)
An energy storage component
An energy converter component
A head
Some number of upgrade modules

## Tiers

The following material tiers exist

- Steel (motor/pump, redstone battery)
- Aluninium (large motor/pump, silicon battery)
- Stainless Steel (advanced motor/pump, sodium battery)
- Titanium (large advanced motor/pump, cadmium battery)

## Casings

Casings determine:

- the max tier of electric energy storage component that can be installed
- the max tier of energy converter component that can be installed
- the max tier of head that can be installed
- the number of upgrade modules that can be installed

Casings:

- Steel: redstone battery, basic converter, steel heads, 2 stacks
- Aluminium: silicon battery, large converter, aluminium heads, 3 stacks
- Stainless Steel: sodium battery, advanced converter, stainless heads, 4 stacks
- Titanium: cadmium battery, large advanced converter, titanium heads, 5 stacks

## Energy

A tool may be fitted with a battery and motor, allowing it to store and use EU directly, or with a tank and pump, allowing it to store liquid fuels and burn them to generate energy, with a granularity of 1 mB

Base energy consumption is 32 EU

Converters have a max throughput

- Motor: 1024 EU
- Large Motor: 2048 EU
- Advanced Motor: 4096 EU
- Large Advanced Motor: 8192
- Pump: 1024 EU
- Large Pump: 2048 EU
- Advanced Pump: 4096 EU
- Large Advanced Pump: 8192 EU

## Heads

Heads determine the base mining speed and attack damage

There are two kinds - drill heads and rotary blades

## Modules

- Area = 9x, max 2

- Fire Aspect = 1.5x, max 2
- Looting = 2x, max 3
- Knockback = 1.25x, max 2
- Sweeping Edge = 1.5x, max 3
- Sharpness/Smite/Bane of Arthropods = 1.25x, max 5

- Efficiency = 1.25x, max 5

- Fortune = 2x, max 3
- Silk Touch = 2x, max 1

### Module Identification

Modules are crafted by choosing the placement of fine copper wires around a circuit board; each recipe has 8 slots for 4 wires, for a total of 8C4 = 70 possible modules. See the list below for current assignments (the format has 1 = wire and 0 = no wire, and starting in the top left corner going clockwise)

- 11110000 = area
- 11101000 = fire aspect
- 11100100 = looting
- 11100010 = knockback
- 11100001 = sweeping edge - temporarily disabled
- 11011000 = sharpness
- 11010100 = smite
- 11010010 = bane of arthropods
- 11010001 = efficiency
- 11001100 = fortune
- 11001010 = silk touch
- 11001001
- 11000110
- 11000101
- 11000011
- 10111000
- 10110100
- 10110010
- 10110001
- 10101100
- 10101010
- 10101001
- 10100110
- 10100101
- 10100011
- 10011100
- 10011010
- 10011001
- 10010110
- 10010101
- 10010011
- 10001110
- 10001101
- 10001011
- 10000111
- 01111000
- 01110100
- 01110010
- 01110001
- 01101100
- 01101010
- 01101001
- 01100110
- 01100101
- 01100011
- 01011100
- 01011010
- 01011001
- 01010110
- 01010101
- 01010011
- 01001110
- 01001101
- 01001011
- 01000111
- 00111100
- 00111010
- 00111001
- 00110110
- 00110101
- 00110011
- 00101110
- 00101101
- 00101011
- 00100111
- 00011110
- 00011101
- 00011011
- 00010111
- 00001111
