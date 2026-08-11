# iLike2MoveIt — Core

The companion mod for the **[iLike2MoveIt resource pack](https://github.com/joanmarcel/iLike2Moveit-RP)**.
The pack brings vanilla mobs to life with CEM/EMF animation; this mod is the piece that **hands the
animation engine the information the pack cannot read from the game on its own**.

EMF animates from variables. But there are states vanilla does not expose in any useful way — has this
wolf just found you again? is the fox really asleep or merely standing still? how far along is the cat
in lying down on you? The mod derives visual-only signals on the client and owns behavior that affects
movement or persistent identity on the server, then hands the synchronized result to EMF. Without this
bridge those animations would have nothing reliable to fire from.

For the complete 0.2 feature set, install the matching jar on both client and server. A versioned
handshake fails closed: clients can still join servers without the mod, but server-backed behavior and
custom persistent breeds stay disabled there.

<!-- Screenshots go here. Add a couple before promoting the Modrinth page:
     a villager holding a trade item mid-gesture, and a sleeping fox with the Zzz particles. -->

## What it does

- **Wolf — reunion.** Recognises when your wolf finds you again after being apart, and fires its
  greeting. State is kept per UUID, so each wolf remembers its own. Greetings are staggered, so a pack
  of them does not greet you in unison.
- **Cat — resting.** Hands over the real progress of the cat lying down on you, so the transition reads
  as a movement instead of a jump.
- **Fox — its own states.** Sitting, sleeping and stalking (crouched), worked out separately because
  vanilla's flags do not apply to foxes.
- **Chicken and pig — warm variant.** Repairs the layer missing from VanillaBackport's renderer on
  1.21.1, so the warm biome variants render correctly.
- **Pig — collective resting.** Nearby adult pigs can lie down in groups, with density limits and the
  complete transition synchronized by the server.
- **Pig and cow — persistent breeds.** Biome identity is assigned once at spawn, saved, inherited and
  rendered through the matching CEM model instead of changing when the animal crosses a biome border.
- **Rabbit — jump signals.** Exposes the real vanilla jump clock and movement values so airborne
  animations do not have to guess from ground state.
- **Villager — boat state.** Distinguishes boats from other vehicles so the pack can choose the right
  passenger pose.
- **Villager — trade item.** The item it offers you stops being pinned to its chest and follows the
  model's animated hands.
- **Held items.** Harmonises the apparent size of any held item and seats it consistently, so a diamond,
  an ingot and a 3D-modelled item all sit correctly in the same place.

It also forces one EMF setting (`asmMaths=false`) **in memory only**, so you do not have to edit it by
hand. Without it the villager's animation block exceeds a JVM limit and the mob silently stops
animating. Your `entity_model_features.json` is left untouched, and uninstalling the mod gives your
configuration back exactly as it was.

## Requirements

- Minecraft **1.21.1** and Java 21
- NeoForge **21.1.0+**, or Fabric Loader **0.19.3+** with Fabric API
- **Entity Model Features (EMF)** 3.2.4+
- **Entity Texture Features (ETF)** 7.1+
- **VanillaBackport** 1.1.7.10+ (plus Platform 1.3.3+)
- The **[iLike2MoveIt resource pack](https://github.com/joanmarcel/iLike2Moveit-RP)**, enabled

Install only the jar for your loader. Put the same loader-specific jar on the server when you want
collective pig resting and persistent custom breed assignment; a client-only installation keeps all
visual signals and safely disables those server-backed features.

## Where this is going

This is the beginning. The plan is to keep widening it: more mobs with states of their own, more
gestures, and better compatibility with what people already run. Bug reports and suggestions are
welcome.

## Reporting a bug

Open an issue. The template asks for the versions of the mod, NeoForge, EMF, ETF and VanillaBackport,
whether the resource pack is installed, and your `latest.log`. Those are the details that make a render
bug reproducible — without them almost any report is a guess.

If something looks wrong but you are not sure which half is at fault, report it here anyway. The mod
and the pack are two pieces of the same thing.

## Contributing

Pull requests are welcome. The template asks you to confirm the contribution is your own work and that
you license it under the same terms as the rest of the project.

The build resolves its dependencies from Modrinth's Maven, so it works on a clean clone:

```bash
./gradlew clean build
```

Note that `./gradlew build` passing does **not** prove a change to the mixins works — Mixin resolves at
runtime, and a wrongly prefixed `@Unique` compiles without a single warning. Run the client and check
the log.

## Community

Discord: **https://discord.gg/45YFupj5Q7** — for questions, screenshots and anything that is not a bug
report.

## Thanks

- To **Traben**, for **Entity Model Features** and **Entity Texture Features** — none of this would be
  possible without them.
- To **lukidonu**, for
  [**Villagers Refreshed**](https://modrinth.com/resourcepack/villagers-refreshed).
- To **Fresh Animations**, as a reference and a source of inspiration.

## License

**CC BY-NC-SA 4.0.** Share it, adapt it, build on it — with attribution, non-commercially, and under the
same licence. The same terms as the resource pack.

`TEMPLATE_LICENSE.txt` covers the Gradle wrapper and the NeoForge MDK scaffolding, which are MIT and
belong to the NeoForged project.
