## What this changes

<!-- One or two lines. If it fixes an open issue, write "Fixes #N". -->

## How you tested it

`./gradlew build` passing is **not** enough for anything touching `src/main/java/mac/ilike2moveit/mixin/`.
Mixin resolves at runtime: a wrongly prefixed `@Unique`, or a `mixins.json` pointing at the wrong
package, compiles without a single warning and then fails when the game loads.

- [ ] `./gradlew clean build` passes
- [ ] I ran the client and there are no Mixin errors in `latest.log`
- [ ] I checked on screen that the affected mob still behaves correctly

<!-- Which mob, and what you saw. A screenshot or a clip helps a lot. -->

## Confirmation

- [ ] This is my own work, and I have the right to contribute it
- [ ] I license it under **CC BY-NC-SA 4.0**, the same terms as the rest of the project
