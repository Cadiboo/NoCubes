# 1.12.2 Profiling notes
1. NoCubes takes up a significant portion of chunk rendering time (Hooks.preIteration accounts for >80%)
2. Lighting seems to account for the bulk of that (50%)
3. It is hard to know how much time is being spent generating vs rendering the chunk because of the lambdas
4. Commenting out the function that renders faces (but still generating them) makes NoCubes only take up 5~10% of the total chunk rendering time.  About half of this time is extended fluids rendering, the other half is mesh generation
5. Such a substantial reduction means that we are doing something very wrong in the rendering and the generation is fine.
6. Lighting seems to be a massive cost for NoCubes but not for the vanilla code (the vanilla code spends 4% of its time there, thanks to using ChunkCacheOF and ChunkCache which caches it)
7. Block colors seems to take lots as well
8. getRenderQuads is also negligible for vanilla, probably since I turned off connected textures after my last comments
9. 
