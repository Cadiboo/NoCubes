# TODO
## Now
Stuff I should've already finished
1. Fabric compatibility

## Soon
Pressing issues that need to be fixed soon (usually because they affect performance, are painful bugs or are [regressions](https://en.wikipedia.org/wiki/Software_regression))
1. Fix powdered snow being walkable when collisions are enabled (don't have terrain collisions for smooth blocks that normally don't have collisions)
1. Investigate and fix [colored terracotta not being colored with OptiFine & Patrix 128](https://discord.com/channels/493715188843937793/520716613574590494/962760253378822276) (likely small task) - implement integration with the custom coloring optifine provides ("enable custom colors in optifine")
1. Fix config loading issues (need to unit/integration test)
   - Need to stop old configs being loaded and overwriting the correct in-memory config
   - Happens when config is updated multiple times in fast succession
   - Fix by adding GUID to config file & discarding loading configs that have a guid different to our most recently saved?
1. Fix leaves with snow on them not being rendered 2 sided (~~render snow 2 sided in this case~~, can't because we are actually rendering the snow block - can't check if nearby blocks are leaves for performance; need to actually fix snow and make it render as its own 'render layer', might be a big task)
1. Issues with phasing in the [Origins mod](https://www.curseforge.com/minecraft/mc-mods/origins-forge)

## Later
Less pressing issues (usually large work that's important but should be sidelined for bug fixes)
1. Investigate connected textures not working on 1.12.2 and backport new features
1. Make collisions generate for an entire area, not single blocks (large task)
   - Start off by keeping the exiting per-block gen code, replace it with the indev new code and then see wtf is going wrong with the indev stuff
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
1. Make items get pushed up out of terrain if they fall inside it like vanilla
1. Improve lighting

# Much later
Unimportant features that would be nice to have but aren't really worth the dev time (may get to these once *everything* else is done)
1. Make destruction particles only come out of the smoothed block's bounding box instead of the original block's bounding box (medium task)
1. Add auto-updater (medium task)
   - Copy 1.12.2's code
   - Add some tests and error handling (what happens when file can't download, is in use, windows/mac)
