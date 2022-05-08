# TODO
## Soon
Pressing issues that need to be fixed soon (usually because they affect performance, are painful bugs or are [regressions](https://en.wikipedia.org/wiki/Software_regression))
1. Investigate and fix [colored terracotta not being colored with OptiFine & Patrix 128](https://discord.com/channels/493715188843937793/520716613574590494/962760253378822276) (likely small task)
1. Fix config loading issues (need to unit/integration test)
   - Need to stop old configs being loaded and overwriting the correct in-memory config
   - Happens when config is updated multiple times in fast succession
   - Possibly related: [Smoothable syncing issue](https://discord.com/channels/493715188843937793/520716613574590494/957546807104253953) ([logs](https://discord.com/channels/493715188843937793/520716613574590494/958303722868441129))
   - Fix by adding GUID to config file & discarding loading configs that have a guid different to our most recently saved?
1. Fix fluids rendering their sides against extended fluids (1.18.1+)
   - Do I need to rewrite the whole fluid renderer to make this work? (if so medium task, otherwise small task)

## Later
Less pressing issues (usually large work that's important but should be sidelined for bug fixes)
1. Move [DEFAULT_SMOOTHABLES](https://github.com/Cadiboo/NoCubes/blob/55b624f27fec70986d02cf5c34377f9ea98ca20c/src/main/java/io/github/cadiboo/nocubes/config/NoCubesConfig.java#L476) to a tag for use by mod & modpack devs (small/medium task)
1. Make collisions generate for an entire area, not single blocks (large task)
   - Big performance gain
   - Needs to also fix mobs not spawning
   - Needs to also fix mobs not pathfinding
   - NB: Required for proper working of 'extra smooth' SDF meshers because they can generate a mesh that falls mostly inside air blocks (which vanilla won't check for collisions)
1. Make meshes connect to solid blocks (very large task)
   - [To fix Framed Blocks occlusion issues](https://discord.com/channels/313125603924639766/540691915373412393/972656116829925416)
   - [To make houses sit flat on the ground](https://discord.com/channels/493715188843937793/493715189338734595/959669519725494323)
   - [To fix torches/vines on walls & leaves connecting with logs](https://discord.com/channels/493715188843937793/493715189338734595/955388320723120178)
   - Leaves should be cubic where they connect to logs
   - I should initially get rid of the code that makes snow conform to the mesh and make it use the new solids system but it won't look good (it'll make snow cubic) so I'll likely have to re-implement it but better
1. Properly implement 2x smoothness meshes from 1.12.2
1. Fix 1.16.5 OptiFine extended fluids not rendering (only in production) (low value)
1. Make items get pushed up out of terrain if they fall inside it like vanilla
1. Improve lighting

# Much later
Unimportant features that would be nice to have but aren't really worth the dev time (may get to these once *everything* else is done)
1. Make destruction particles only come out of the smoothed block's bounding box instead of the original block's bounding box (medium task)
1. Add auto-updater (medium task)
   - Copy 1.12.2's code
   - Add some tests and error handling (what happens when file can't download, is in use, windows/mac)
