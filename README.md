# iLike2MoveIt — Mod

Companion mod for the **[iLike2MoveIt resource pack](https://github.com/joanmarcel/iLike2Moveit-RP)**.
It unlocks the parts of that pack that cannot be done with a resource pack alone, because they need
code running in the game.

A resource pack can replace a model and animate it, but it cannot tell the animation *what the mob is
actually doing*, and it cannot move a held item off the spot vanilla nails it to. That is what this mod
is for.

**Client-side only.** It touches nothing on the server: you can use it on a vanilla server, or on any
server you already play on, without anyone else installing anything.

<!-- Screenshots go here. Add a couple before promoting the Modrinth page:
     a villager holding a trade item mid-gesture, and a sleeping fox with the Zzz particles. -->

## What it unlocks

| | What the mod does |
|---|---|
| **Villager** | The trade item follows the animated hands instead of floating pinned to the chest. Adds the woman variant layer, drawn between the biome outfit and the profession one. |
| **Fox** | Exposes the fox's real state (sitting, sleeping, stalking) to the pack's animations — vanilla's own animation variables report the wrong thing for foxes. Adds the sleeping "Zzz" particles, and makes an item carried in the mouth follow the animated snout. |
| **Wolf** | Tracks whether you have actually been away, so the pack can play a proper reunion greeting when you come back — and staggers it across several wolves so a pack of them does not greet you in unison. |
| **Cat** | Exposes vanilla's lie-down progress so the resting animation matches what the cat is really doing. |
| **Chicken and Pig** | Fixes the warm biome variants, which render deformed with Fresh Animations + VanillaBackport because the model and the texture disagree on size. |
| **Held items** | Harmonises the apparent size of any held item and seats it consistently, so a diamond, an ingot and a 3D-modelled item all sit correctly in the same spot. |

It also forces one EMF setting (`asmMaths=false`) **in memory only**, so you do not have to edit it by
hand. Without it the villager's animation block exceeds a JVM limit and the mob silently stops
animating. Your `entity_model_features.json` is left untouched, and uninstalling the mod gives you your
configuration back exactly as it was.

## Requirements

The mod does nothing on its own — it is the other half of the resource pack.

| | Tested version |
|---|---|
| Minecraft Java | **1.21.1** |
| Loader | **NeoForge 21.1.240** or later, Java 21 |
| **EMF** (Entity Model Features) | **3.2.4** |
| **ETF** (Entity Texture Features) | **7.1** |
| **VanillaBackport** | **1.1.7.10** (plus Platform **1.3.3**) |
| The resource pack | [iLike2MoveIt](https://github.com/joanmarcel/iLike2Moveit-RP) |

## Installation

1. Install NeoForge for 1.21.1.
2. Drop EMF, ETF, VanillaBackport and Platform into `mods/`.
3. Drop this mod's `.jar` into `mods/` as well.
4. Install the [resource pack](https://github.com/joanmarcel/iLike2Moveit-RP) and enable it, following
   the load order described there.

## Reporting a bug

Open an issue. The template asks for the versions of the mod, NeoForge, EMF, ETF and VanillaBackport,
whether the resource pack is installed, and your `latest.log`. Those are the details that make a render
bug reproducible — without them almost any report is a guess.

If something looks wrong but you are not sure which half is at fault, report it here anyway. The mod and
the pack are two pieces of the same thing.

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

## License

**CC BY-NC-SA 4.0.** Share it, adapt it, build on it — with attribution, non-commercially, and under the
same licence. The same terms as the resource pack.

`TEMPLATE_LICENSE.txt` covers the Gradle wrapper and the NeoForge MDK scaffolding, which are MIT and
belong to the NeoForged project.
