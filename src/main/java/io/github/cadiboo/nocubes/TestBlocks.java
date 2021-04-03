package io.github.cadiboo.nocubes;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class TestBlocks {
	static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NoCubes.MOD_ID);

	public static final RegistryObject<Block> DEBUG = BLOCKS.register("debug", () -> new Block(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_CYAN).strength(0.3F)));
	public static final RegistryObject<Block> DEBUG2 = BLOCKS.register("debug2", () -> new Block(AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_CYAN).strength(0.3F)));

}
