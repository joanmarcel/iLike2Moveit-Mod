# Changelog

All notable changes to this mod are documented here. The same text feeds the GitHub release, the
Modrinth page and the Discord announcement, so they cannot drift apart.

## v0.2.1-beta.2 — reliable rendering across both loaders

This update fixes a production Fabric rendering failure and improves model and attachment isolation. Use it with iLike2MoveIt RP v1.1.2-beta.4 and choose the JAR matching your loader.

**Availability:** This release is available for NeoForge first. The Fabric build of this version is not published yet.

**Fixed**

- EMF render hooks now resolve the Minecraft method name used by production Fabric as well as NeoForge, preventing model transformation failures.
- Dedicated baby models and render-pass attachment capture keep adult models, collars and held items from inheriting another entity's render state.
- Build verification checks the exact generated JAR, configured mixins and Fabric refmap.

**Installation**

- Minecraft 1.21.1; install only one JAR for your loader.
- VanillaBackport 1.1.7.10 or newer is required wherever Core is installed, including servers. Install its matching dependencies; Fabric also requires Fabric API.
- Tiny Takeover baby presentations remain selectable alongside Classic. Sheep offers Classic and Alternate presentations.
- Replace the previous Core JAR before installing this version.

## v0.2.0-beta.1 — server-backed behavior and persistent breeds

The bridge now runs on the server as well as the client. A versioned handshake keeps every
server-authoritative animation state safely disabled when a client joins a server without the mod.

**New**

- **Pig — collective resting.** Adult pigs can lie down in nearby groups of four or more. The server
  owns the state, limits resting pigs to half of each local group, and synchronizes the full
  lie-down/hold/get-up cycle to clients.
- **Persistent pig and cow breeds.** Biome identity is assigned once at spawn, survives movement and
  reloads, and follows VanillaBackport inheritance when animals breed. Named and cosmetic variants
  remain visual overrides rather than rewriting that identity.
- **Cow presentation.** Climate and persistent biome breeds route through dedicated EMF layers;
  birch cows and the named bull presentation render with the matching model and atlas.
- **Rabbit signals.** The pack can read the vanilla client jump clock, vertical velocity and actual
  horizontal travel instead of reconstructing them from unreliable ground-state guesses.
- **Villager vehicles.** Villagers and wandering traders expose whether they are riding a boat,
  separately from the generic riding state used by minecarts.

**Fixed**

- Resting cats and wolves keep their body heading stable while their heads remain free to track.
- Wandering-trader potion and milk items follow the animated locator only during their real use
  window, without stale bottles floating after the effect changes.
- Publishable source comments, build diagnostics and test failures are consistently in English.

**Installation**

- Choose exactly one client jar: `iLike2MoveIt-0.2.0-beta.1.jar` for NeoForge or
  `iLike2MoveIt-fabric-0.2.0-beta.1.jar` for Fabric.
- Install the matching jar on the server to enable collective pig resting and persistent custom
  breeds. Clients can still join servers without it; server-backed states then remain disabled.

## v0.1.2-beta.1 — now on Fabric

The mod runs on **Fabric** as well as NeoForge. Same features on both: pick the file that matches your
loader.

**New**

- **Fabric support.** Requires Fabric Loader 0.19.3 or newer and Fabric API. Everything the mod already
  did on NeoForge works here too: the animation signals the resource pack needs for the wolf, the cat
  and the fox, the warm chicken and warm pig variants, and the villager's trade item following its
  animated hands instead of staying pinned to the chest.
- Two files ship with every release from now on. `iLike2MoveIt-<version>.jar` is the NeoForge build and
  `iLike2MoveIt-fabric-<version>.jar` is the Fabric one. **Install only the one for your loader.**

**Notes**

- The resource pack is unchanged and works with either loader.
- On Fabric the mod is client-side only, as it already was on NeoForge. It does nothing on a server.

## v0.1.1-beta.2 — the mod sets `asmMaths` for you

**You must delete the previous jar before installing this one.** The mod's internal id changed, so if
both files sit in `mods/` the game loads them as two separate mods and applies the same patches twice.
Remove the old `iLike2MoveIt` jar first, then drop this one in.

**New**

- The mod now sets EMF's `asmMaths` option to `false` on its own, in memory. Until now you had to edit
  `config/entity_model_features.json` by hand, and if you forgot, the villager silently stopped
  animating with nothing in the log pointing at the cause. Your config file is never rewritten, and
  uninstalling the mod leaves your settings exactly as they were.

**Changed**

- Internal id and package renamed. This is what makes the step above necessary. Doing it now, while the
  mod is new, avoids having to break anyone's setup later.
- The licence declared inside the jar now matches the one on this page: **CC BY-NC-SA 4.0**.

Everything else behaves exactly as in the previous version.

## v0.1.0-beta.1 — first public release

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
