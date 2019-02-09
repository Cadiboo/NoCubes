package io.github.cadiboo.nocubes.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * @author Cadiboo
 */
public class ModRenderGlobal extends DelegatingRenderGlobal {

	private static final int BLOCK_UPDATE_EXTEND = 2;
	private static final MethodHandle RenderGlobal_markBlocksForUpdate;
	static {
		try {
			RenderGlobal_markBlocksForUpdate = MethodHandles.publicLookup().unreflect(
					ObfuscationReflectionHelper.findMethod(RenderGlobal.class, "func_184385_a",
							void.class,
							int.class, int.class, int.class, int.class, int.class, int.class, boolean.class
					)
			);
		} catch (IllegalAccessException e) {
			final CrashReport crashReport = new CrashReport("Unable to find method RenderGlobal.markBlocksForUpdate. Method does not exist!", e);
			crashReport.makeCategory("Finding Method");
			throw new ReportedException(crashReport);
		}
	}

	public ModRenderGlobal(final RenderGlobal delegateIn) {
		super(delegateIn);
	}

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		int k1 = pos.getX();
		int l1 = pos.getY();
		int i2 = pos.getZ();
		this.markBlocksForUpdate(k1 - BLOCK_UPDATE_EXTEND, l1 - BLOCK_UPDATE_EXTEND, i2 - BLOCK_UPDATE_EXTEND, k1 + BLOCK_UPDATE_EXTEND, l1 + BLOCK_UPDATE_EXTEND, i2 + BLOCK_UPDATE_EXTEND, (flags & 8) != 0);
	}

	private void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately) {
		try {
			RenderGlobal_markBlocksForUpdate.invokeExact(getDelegate(), minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);
		} catch (ReportedException e) {
			throw e;
		} catch (Throwable throwable) {
			final CrashReport crashReport = new CrashReport("Exception invoking method RenderGlobal.markBlocksForUpdate", throwable);
			crashReport.makeCategory("Reflectively Invoking Method");
			throw new ReportedException(crashReport);
		}
	}

}
