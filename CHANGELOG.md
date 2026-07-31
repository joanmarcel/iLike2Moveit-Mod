# Changelog

All notable changes to this mod are documented here. The same text feeds the GitHub release, the
Modrinth page and the Discord announcement, so they cannot drift apart.

## v0.1.0 — first public release

The companion mod for the iLike2MoveIt resource pack, out in the open for the first time.

**Villager**

- The trade item follows the animated hands instead of staying pinned to the chest.
- Woman variant layer, drawn between the biome outfit and the profession one.

**Fox**

- Exposes the fox's real state — sitting, sleeping, stalking — to the pack's animations. Vanilla's own
  animation variables report the wrong thing for foxes, so without this the pack cannot tell what the
  fox is doing.
- Sleeping "Zzz" particles.
- An item carried in the mouth follows the animated snout.

**Wolf**

- Tracks whether you have really been away, so the pack can play a reunion greeting when you return.
  Greetings are staggered across wolves, so a pack of them does not greet you in unison.

**Cat**

- Exposes vanilla's lie-down progress so the resting animation matches what the cat is actually doing.

**Chicken and Pig**

- Fixes the warm biome variants, which render deformed with Fresh Animations + VanillaBackport because
  the model and the texture disagree on size.

**Held items**

- Harmonises the apparent size of any held item and seats it consistently, so a diamond, an ingot and a
  3D-modelled item all sit correctly in the same place.

**Quality of life**

- Forces EMF's `asmMaths=false` in memory only. Without it the villager's animation block exceeds a JVM
  limit and the mob silently stops animating. Your `entity_model_features.json` is left untouched.

**Known limitations**

- Client-side only, by design. Nothing here runs on a server.
- The mod does nothing without the resource pack installed.
