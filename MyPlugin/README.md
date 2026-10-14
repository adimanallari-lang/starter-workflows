# FurniturePlugin — Batch 1 (Core + Christmas)

Cross-play custom furniture for Paper 26.3, usable by both Java and Bedrock
(via Geyser + Floodgate) players with no client mods and no manual
resource-pack downloads.

## What's really in this batch

- Full plugin source (Maven project) — commands, GUI (categories, search,
  pagination), placement/rotation/removal, Java resource-pack auto-delivery.
- 15 fully modeled, fully textured **Christmas** furniture pieces (see list
  below). Every other category from your spec (Everyday, Modern & Luxury,
  Halloween, Easter, Birthday, Medieval, Japanese, Garden, Restaurant,
  School, Café) is wired into the catalog loader, GUI, and config file
  *structure* already, just with `items: []` — ready for the next batches.
- A Java resource pack (`java-resourcepack.zip`, bundled into the jar) with
  real multi-cuboid 3D models and procedurally patterned textures (wood
  grain, knit, brick, speckled snow, etc. — not flat recolors).
- A Bedrock resource pack (`respack-bedrock.mcpack`) with matching
  attachable geometry + icons, plus a Geyser custom-item mapping file.

## Honest status — please read before deploying to your friends

1. **Not compiled in this environment.** This sandbox has no access to
   `repo.papermc.io` or `repo.maven.apache.org` (verified by actually
   running `mvn package` here — see `BUILD_LOG.txt`), so I could not produce
   a tested `.jar`. The source is complete and passed a structural syntax
   check (balanced braces/parens across every file), but it has **not**
   been type-checked against the real Paper API or run on a live server.
   Build it yourself with `./build.sh` (just needs normal internet access)
   and test on a non-production world first.
2. **Paper 26.3 is new.** Minecraft 26.3 "Wilderness Bound" released
   2026-09-15. Multiple independent trackers describe Paper's 26.3 support
   as still experimental as of this writing. Expect possible plugin-API
   changes in future Paper builds.
3. **Bedrock furniture uses a different rendering path than Java's fancy
   "floating 3D model" look you might expect.** Bedrock Edition has no
   equivalent to Java's 1.19.4+ item/block *display entities*, and Geyser
   does not translate them by default (confirmed from Geyser's own issue
   tracker; the only fixes are small, separately-installed, explicitly
   experimental Geyser extensions). To guarantee furniture actually
   **renders reliably for Bedrock players** rather than being invisible,
   every piece is a real, vanilla-supported entity instead:
   - Floor/tabletop furniture = an invisible `ArmorStand` holding the
     custom-model item in its hand.
   - Wall furniture = an `ItemFrame` holding the custom-model item.

   Both entity types are things Geyser has translated reliably for years.
   Java players get a genuinely detailed 3D model (via the item's custom
   model data); Bedrock players get the same item rendered through a custom
   attachable geometry defined in the Bedrock pack. The geometry is a
   faithful but independently-modeled conversion of the same cuboids — I
   could not visually verify alignment/scale on a live Bedrock client, so
   budget time to nudge offsets after your first real test.
4. **Persistence is real, not a stub.** Placed furniture are ordinary
   Bukkit entities tagged with a PersistentDataContainer key. Minecraft's
   own chunk save system persists them exactly like any player-placed
   armor stand/item frame — no custom save/load code, and nothing to break
   on restart.

## Build it

**Option A — GitHub Actions (no local setup, easiest):** push this folder to
a GitHub repo. `.github/workflows/build.yml` compiles it on GitHub's own
runners (which have normal internet access, unlike the sandbox this was
written in) and attaches `FurniturePlugin.jar` as a downloadable workflow
artifact. This is the fastest way to get a real, compiled jar.

**Option B — locally, on any machine with internet:**
```bash
./build.sh
# or directly:
mvn -f pom.xml package
```

Both need internet access to `repo.papermc.io` (Paper API) and
`repo.opencollab.dev` (Floodgate API) — neither is reachable from the
sandbox this was written in (confirmed by actually running the build here;
see `BUILD_LOG.txt`), which is why no jar is bundled in this zip.

## Install

1. Drop the built `target/FurniturePlugin.jar` into your server's `plugins/`.
2. Start the server once to generate `plugins/FurniturePlugin/config.yml`
   and `furniture-defs.yml`.
3. Edit `config.yml`'s `resource-pack.public-url` and `port` so the embedded
   pack server is reachable by your players (port-forward if needed).
4. Restart. Java players get prompted to accept the resource pack on join.
5. For Bedrock players, see `BEDROCK_SETUP.md`.

## Commands & permissions

- `/furniture` — opens the catalog GUI (`furnitureplugin.use`, default: all).
- `/furniture reload` — reloads `furniture-defs.yml` (`furnitureplugin.admin`).
- `/furniture give <id> [player]` — gives a specific piece (`furnitureplugin.admin`).

## In-world controls

- Right-click a block/wall while holding a furniture item: place it.
- Right-click a placed piece (not sneaking): rotate it.
- Sneak + hit a placed piece: remove it and return the item.

## About the uploaded "ChristmasPack" .mcpack

You uploaded `updated_updated_ChristmasPack_1_21_11_v3_2.mcpack`. Worth
knowing: despite the `.mcpack` extension and a Bedrock-shaped `manifest.json`,
unzipping it shows it's actually built as a **Java Edition** resource pack
(blockstate/model JSON, OptiFine CTM/CEM configs, `.tga` textures — none of
which exist on Bedrock, and it's also missing the `pack.mcmeta` Java itself
requires to load a pack at all). So it couldn't go into the Bedrock `.mcpack`
as-is — Bedrock would just ignore every file in it.

What I actually did:
- Merged its vanilla-block reskins (snowy grass/leaves, decorated bookshelf,
  snowman-peony, etc.) into the **Java** resource pack — no file collisions
  with our furniture assets, so it's pure bonus ambience for Java players.
  Full credit preserved in `CREDITS_ChristmasPack_by_Futureazoo.txt`.
- Pulled out the genuinely hand-drawn pieces (the snowman/candy-cane/
  gingerbread/present/snowflake mug icons) and built **5 new placeable
  "Holiday Mug" items** from that actual art — wired into both the Java
  model and a matching Bedrock attachable, so these are real, cross-play,
  placeable catalog items (`mug_snowman`, `mug_candycane`,
  `mug_gingerbread`, `mug_present`, `mug_snowflake` — custom-model-data
  100016–100020), not just inert files sitting in a folder.
- The rest of that pack (doors, chests, shulkers, generic foliage) is
  unrelated filler bundled alongside the Christmas content — left out of
  the furniture catalog, though it's still in the merged Java pack as
  ambient texture overrides if you want it.

## Christmas batch 1 — now 20 pieces

Decorated Christmas Tree (large), Tabletop Christmas Tree, Wrapped Present
(red/gold), Stacked Presents (gold/green), Holly Wreath, Pine Garland,
Festive String Lights, Knit Stocking, Cozy Fireplace, Festive Armchair,
Festive Dining Table, Gingerbread House, Snow Globe, Candy Cane Pole, Snowy
Lamp Post, Snowman Holiday Mug, Candy Cane Holiday Mug, Gingerbread Holiday
Mug, Present Holiday Mug, Snowflake Holiday Mug.

## What's next (future batches)

Everyday furniture is the natural next batch (chairs, sofas, beds, tables,
desks, wardrobes, shelves, TVs, computers, kitchen/bathroom fixtures, lamps,
rugs, plants), followed by Modern/Luxury, then the remaining theme packs —
same pipeline (`gen_*.py` scripts), same honesty about what's tested vs not.
