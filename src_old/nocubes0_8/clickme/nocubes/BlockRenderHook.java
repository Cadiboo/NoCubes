package clickme.nocubes;

import net.minecraft.block.Block;

public class BlockRenderHook {
   public static boolean shouldHookRenderer(Block block) {
      return NoCubes.isBlockSoft(block) || NoCubes.isBlockLiquid(block);
   }
}
