---
navigation:
  title: "Fluid Pipe Speed"
  icon: "minecraft:water_bucket"
  position: 203
  parent: modern_industrialization:midgame.md
---

# Fluid Pipe Speed

You might be wondering how you can possibly transfer all these multiple thousands of millibuckets of steam every tick? The answer is: with regular fluid pipes!

Each fluid pipe has an internal buffer of exactly one bucket (= 1000 millibuckets). When you connect multiple pipes directly, they become linked and share their internal storage.

Regular and large tanks from Modern Industrialization will also link with the pipe network if they are connected to it using a bidirectional connection. This means I/O in the menu, or the double arrows on the pipe itself.

Tanks have a much larger internal buffer than pipes, so they can store a lot more fluid.

How much fluid is transferred every tick is then bounded by the total capacity of the network. For example, 25 fluid pipes linked together can transfer up to 25000 mb/t of fluid - assuming you can produce that much fluid of course!

Adding a single steel tank (16 buckets of storage) with an I/O connection will bring the total network rate to 41000 mb/t.

Note that you might be limited by the transfer rates of the tanks/machines the pipes are connected to.

