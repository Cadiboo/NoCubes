package clickme.nocubes;

import net.minecraft.block.*;

public class BlockRenderHook
{
    public static boolean shouldHookRenderer(final Block block)
    {
        return NoCubes.isBlockSoft(block) || NoCubes.isBlockLiquid(block);
    }
}
