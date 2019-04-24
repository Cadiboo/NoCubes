//package io.github.cadiboo.nocubes.collision;
//
///**
// * @author Cadiboo
// */
//
//import java.util.Random;
//import java.util.function.IntFunction;
//import java.util.stream.IntStream;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.Block.Properties;
//import net.minecraft.block.BlockHorizontal;
//import net.minecraft.block.BlockSlab;
//import net.minecraft.block.IBucketPickupHandler;
//import net.minecraft.block.ILiquidContainer;
//import net.minecraft.block.state.BlockFaceShape;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.fluid.Fluid;
//import net.minecraft.fluid.IFluidState;
//import net.minecraft.init.Blocks;
//import net.minecraft.init.Fluids;
//import net.minecraft.item.BlockItemUseContext;
//import net.minecraft.pathfinding.PathType;
//import net.minecraft.state.BooleanProperty;
//import net.minecraft.state.DirectionProperty;
//import net.minecraft.state.EnumProperty;
//import net.minecraft.state.IProperty;
//import net.minecraft.state.StateContainer.Builder;
//import net.minecraft.state.properties.BlockStateProperties;
//import net.minecraft.state.properties.Half;
//import net.minecraft.state.properties.StairsShape;
//import net.minecraft.util.BlockRenderLayer;
//import net.minecraft.util.EnumFacing;
//import net.minecraft.util.EnumHand;
//import net.minecraft.util.Mirror;
//import net.minecraft.util.Rotation;
//import net.minecraft.util.EnumFacing.Axis;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.shapes.VoxelShape;
//import net.minecraft.util.math.shapes.VoxelShapes;
//import net.minecraft.world.Explosion;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.IWorldReader;
//import net.minecraft.world.IWorldReaderBase;
//import net.minecraft.world.World;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//
//import javax.annotation.ParametersAreNonnullByDefault;
//
//@ParametersAreNonnullByDefault
//public class BS extends Block implements IBucketPickupHandler, ILiquidContainer {
//	public static final DirectionProperty FACING;
//	public static final EnumProperty<Half> HALF;
//	public static final EnumProperty<StairsShape> SHAPE;
//	public static final BooleanProperty WATERLOGGED;
//	protected static final VoxelShape AABB_SLAB_TOP;
//	protected static final VoxelShape AABB_SLAB_BOTTOM;
//	protected static final VoxelShape field_196512_A;
//	protected static final VoxelShape field_196513_B;
//	protected static final VoxelShape field_196514_C;
//	protected static final VoxelShape field_196515_D;
//	protected static final VoxelShape field_196516_E;
//	protected static final VoxelShape field_196517_F;
//	protected static final VoxelShape field_196518_G;
//	protected static final VoxelShape field_196519_H;
//	protected static final VoxelShape[] field_196520_I;
//	protected static final VoxelShape[] field_196521_J;
//	private static final int[] field_196522_K;
//	private final Block modelBlock;
//	private final IBlockState modelState;
//
//	private static VoxelShape[] func_199779_a(VoxelShape p_199779_0_, VoxelShape p_199779_1_, VoxelShape p_199779_2_, VoxelShape p_199779_3_, VoxelShape p_199779_4_) {
//		return (VoxelShape[])IntStream.range(0, 16).mapToObj((p_199780_5_) -> {
//			return func_199781_a(p_199780_5_, p_199779_0_, p_199779_1_, p_199779_2_, p_199779_3_, p_199779_4_);
//		}).toArray((p_199778_0_) -> {
//			return new VoxelShape[p_199778_0_];
//		});
//	}
//
//	private static VoxelShape func_199781_a(int p_199781_0_, VoxelShape p_199781_1_, VoxelShape p_199781_2_, VoxelShape p_199781_3_, VoxelShape p_199781_4_, VoxelShape p_199781_5_) {
//		VoxelShape lvt_6_1_ = p_199781_1_;
//		if ((p_199781_0_ & 1) != 0) {
//			lvt_6_1_ = VoxelShapes.or(p_199781_1_, p_199781_2_);
//		}
//
//		if ((p_199781_0_ & 2) != 0) {
//			lvt_6_1_ = VoxelShapes.or(lvt_6_1_, p_199781_3_);
//		}
//
//		if ((p_199781_0_ & 4) != 0) {
//			lvt_6_1_ = VoxelShapes.or(lvt_6_1_, p_199781_4_);
//		}
//
//		if ((p_199781_0_ & 8) != 0) {
//			lvt_6_1_ = VoxelShapes.or(lvt_6_1_, p_199781_5_);
//		}
//
//		return lvt_6_1_;
//	}
//
//	protected BS(IBlockState p_i48321_1_, Properties p_i48321_2_) {
//		super(p_i48321_2_);
//		this.setDefaultState((IBlockState)((IBlockState)((IBlockState)((IBlockState)((IBlockState)this.stateContainer.getBaseState()).with(FACING, EnumFacing.NORTH)).with(HALF, Half.BOTTOM)).with(SHAPE, StairsShape.STRAIGHT)).with(WATERLOGGED, false));
//		this.modelBlock = p_i48321_1_.getBlock();
//		this.modelState = p_i48321_1_;
//	}
//
//	public int getOpacity(IBlockState p_200011_1_, IBlockReader p_200011_2_, BlockPos p_200011_3_) {
//		return p_200011_2_.getMaxLightLevel();
//	}
//
//	public VoxelShape getShape(IBlockState p_196244_1_, IBlockReader p_196244_2_, BlockPos p_196244_3_) {
//		return (p_196244_1_.get(HALF) == Half.TOP ? field_196520_I : field_196521_J)[field_196522_K[this.func_196511_x(p_196244_1_)]];
//	}
//
//	private int func_196511_x(IBlockState p_196511_1_) {
//		return ((StairsShape)p_196511_1_.get(SHAPE)).ordinal() * 4 + ((EnumFacing)p_196511_1_.get(FACING)).getHorizontalIndex();
//	}
//
//	public BlockFaceShape getBlockFaceShape(IBlockReader p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_) {
//		if (p_193383_4_.getAxis() == Axis.Y) {
//			return p_193383_4_ == EnumFacing.UP == (p_193383_2_.get(HALF) == Half.TOP) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
//		} else {
//			StairsShape lvt_5_1_ = (StairsShape)p_193383_2_.get(SHAPE);
//			if (lvt_5_1_ != StairsShape.OUTER_LEFT && lvt_5_1_ != StairsShape.OUTER_RIGHT) {
//				EnumFacing lvt_6_1_ = (EnumFacing)p_193383_2_.get(FACING);
//				switch(SyntheticClass_1.field_208122_a[lvt_5_1_.ordinal()]) {
//					case 1:
//						return lvt_6_1_ == p_193383_4_ ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
//					case 2:
//						return lvt_6_1_ != p_193383_4_ && lvt_6_1_ != p_193383_4_.rotateY() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
//					case 3:
//						return lvt_6_1_ != p_193383_4_ && lvt_6_1_ != p_193383_4_.rotateYCCW() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
//					default:
//						return BlockFaceShape.UNDEFINED;
//				}
//			} else {
//				return BlockFaceShape.UNDEFINED;
//			}
//		}
//	}
//
//	public boolean isFullCube(IBlockState p_149686_1_) {
//		return false;
//	}
//
//	@OnlyIn(Dist.CLIENT)
//	public void animateTick(IBlockState p_180655_1_, World p_180655_2_, BlockPos p_180655_3_, Random p_180655_4_) {
//		this.modelBlock.animateTick(p_180655_1_, p_180655_2_, p_180655_3_, p_180655_4_);
//	}
//
//	public void onBlockClicked(IBlockState p_196270_1_, World p_196270_2_, BlockPos p_196270_3_, EntityPlayer p_196270_4_) {
//		this.modelState.onBlockClicked(p_196270_2_, p_196270_3_, p_196270_4_);
//	}
//
//	public void onPlayerDestroy(IWorld p_176206_1_, BlockPos p_176206_2_, IBlockState p_176206_3_) {
//		this.modelBlock.onPlayerDestroy(p_176206_1_, p_176206_2_, p_176206_3_);
//	}
//
//	@OnlyIn(Dist.CLIENT)
//	public int getPackedLightmapCoords(IBlockState p_185484_1_, IWorldReader p_185484_2_, BlockPos p_185484_3_) {
//		return this.modelState.getPackedLightmapCoords(p_185484_2_, p_185484_3_);
//	}
//
//	public float getExplosionResistance() {
//		return this.modelBlock.getExplosionResistance();
//	}
//
//	public BlockRenderLayer getRenderLayer() {
//		return this.modelBlock.getRenderLayer();
//	}
//
//	public int tickRate(IWorldReaderBase p_149738_1_) {
//		return this.modelBlock.tickRate(p_149738_1_);
//	}
//
//	public boolean isCollidable() {
//		return this.modelBlock.isCollidable();
//	}
//
//	public boolean isCollidable(IBlockState p_200293_1_) {
//		return this.modelBlock.isCollidable(p_200293_1_);
//	}
//
//	public void onBlockAdded(IBlockState p_196259_1_, World p_196259_2_, BlockPos p_196259_3_, IBlockState p_196259_4_) {
//		if (p_196259_1_.getBlock() != p_196259_1_.getBlock()) {
//			this.modelState.neighborChanged(p_196259_2_, p_196259_3_, Blocks.AIR, p_196259_3_);
//			this.modelBlock.onBlockAdded(this.modelState, p_196259_2_, p_196259_3_, p_196259_4_);
//		}
//	}
//
//	public void onReplaced(IBlockState p_196243_1_, World p_196243_2_, BlockPos p_196243_3_, IBlockState p_196243_4_, boolean p_196243_5_) {
//		if (p_196243_1_.getBlock() != p_196243_4_.getBlock()) {
//			this.modelState.onReplaced(p_196243_2_, p_196243_3_, p_196243_4_, p_196243_5_);
//		}
//	}
//
//	public void onEntityWalk(World p_176199_1_, BlockPos p_176199_2_, Entity p_176199_3_) {
//		this.modelBlock.onEntityWalk(p_176199_1_, p_176199_2_, p_176199_3_);
//	}
//
//	public void tick(IBlockState p_196267_1_, World p_196267_2_, BlockPos p_196267_3_, Random p_196267_4_) {
//		this.modelBlock.tick(p_196267_1_, p_196267_2_, p_196267_3_, p_196267_4_);
//	}
//
//	public boolean onBlockActivated(IBlockState p_196250_1_, World p_196250_2_, BlockPos p_196250_3_, EntityPlayer p_196250_4_, EnumHand p_196250_5_, EnumFacing p_196250_6_, float p_196250_7_, float p_196250_8_, float p_196250_9_) {
//		return this.modelState.onBlockActivated(p_196250_2_, p_196250_3_, p_196250_4_, p_196250_5_, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
//	}
//
//	public void onExplosionDestroy(World p_180652_1_, BlockPos p_180652_2_, Explosion p_180652_3_) {
//		this.modelBlock.onExplosionDestroy(p_180652_1_, p_180652_2_, p_180652_3_);
//	}
//
//	public boolean isTopSolid(IBlockState p_185481_1_) {
//		return p_185481_1_.get(HALF) == Half.TOP;
//	}
//
//	public IBlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
//		EnumFacing lvt_2_1_ = p_196258_1_.getFace();
//		IFluidState lvt_3_1_ = p_196258_1_.getWorld().getFluidState(p_196258_1_.getPos());
//		IBlockState lvt_4_1_ = this.getDefaultState().with(FACING, p_196258_1_.getPlacementHorizontalFacing()).with(HALF, lvt_2_1_ != EnumFacing.DOWN && (lvt_2_1_ == EnumFacing.UP || (double)p_196258_1_.getHitY() <= 0.5D) ? Half.BOTTOM : Half.TOP).with(WATERLOGGED, lvt_3_1_.getFluid() == Fluids.WATER);
//		return lvt_4_1_.with(SHAPE, func_208064_n(lvt_4_1_, p_196258_1_.getWorld(), p_196258_1_.getPos()));
//	}
//
//	public IBlockState updatePostPlacement(IBlockState p_196271_1_, EnumFacing p_196271_2_, IBlockState p_196271_3_, IWorld p_196271_4_, BlockPos p_196271_5_, BlockPos p_196271_6_) {
//		if (p_196271_1_.get(WATERLOGGED)) {
//			p_196271_4_.getPendingFluidTicks().scheduleTick(p_196271_5_, Fluids.WATER, Fluids.WATER.getTickRate(p_196271_4_));
//		}
//
//		return p_196271_2_.getAxis().isHorizontal() ? (IBlockState)p_196271_1_.with(SHAPE, func_208064_n(p_196271_1_, p_196271_4_, p_196271_5_)) : super.updatePostPlacement(p_196271_1_, p_196271_2_, p_196271_3_, p_196271_4_, p_196271_5_, p_196271_6_);
//	}
//
//	private static StairsShape func_208064_n(IBlockState p_208064_0_, IBlockReader p_208064_1_, BlockPos p_208064_2_) {
//		EnumFacing lvt_3_1_ = (EnumFacing)p_208064_0_.get(FACING);
//		IBlockState lvt_4_1_ = p_208064_1_.getBlockState(p_208064_2_.offset(lvt_3_1_));
//		if (isBlockStairs(lvt_4_1_) && p_208064_0_.get(HALF) == lvt_4_1_.get(HALF)) {
//			EnumFacing lvt_5_1_ = (EnumFacing)lvt_4_1_.get(FACING);
//			if (lvt_5_1_.getAxis() != ((EnumFacing)p_208064_0_.get(FACING)).getAxis() && isDifferentStairs(p_208064_0_, p_208064_1_, p_208064_2_, lvt_5_1_.getOpposite())) {
//				if (lvt_5_1_ == lvt_3_1_.rotateYCCW()) {
//					return StairsShape.OUTER_LEFT;
//				}
//
//				return StairsShape.OUTER_RIGHT;
//			}
//		}
//
//		IBlockState lvt_5_2_ = p_208064_1_.getBlockState(p_208064_2_.offset(lvt_3_1_.getOpposite()));
//		if (isBlockStairs(lvt_5_2_) && p_208064_0_.get(HALF) == lvt_5_2_.get(HALF)) {
//			EnumFacing lvt_6_1_ = (EnumFacing)lvt_5_2_.get(FACING);
//			if (lvt_6_1_.getAxis() != ((EnumFacing)p_208064_0_.get(FACING)).getAxis() && isDifferentStairs(p_208064_0_, p_208064_1_, p_208064_2_, lvt_6_1_)) {
//				if (lvt_6_1_ == lvt_3_1_.rotateYCCW()) {
//					return StairsShape.INNER_LEFT;
//				}
//
//				return StairsShape.INNER_RIGHT;
//			}
//		}
//
//		return StairsShape.STRAIGHT;
//	}
//
//	private static boolean isDifferentStairs(IBlockState p_185704_0_, IBlockReader p_185704_1_, BlockPos p_185704_2_, EnumFacing p_185704_3_) {
//		IBlockState lvt_4_1_ = p_185704_1_.getBlockState(p_185704_2_.offset(p_185704_3_));
//		return !isBlockStairs(lvt_4_1_) || lvt_4_1_.get(FACING) != p_185704_0_.get(FACING) || lvt_4_1_.get(HALF) != p_185704_0_.get(HALF);
//	}
//
//	public static boolean isBlockStairs(IBlockState p_185709_0_) {
//		return p_185709_0_.getBlock() instanceof net.minecraft.block.BlockStairs;
//	}
//
//	public IBlockState rotate(IBlockState p_185499_1_, Rotation p_185499_2_) {
//		return (IBlockState)p_185499_1_.with(FACING, p_185499_2_.rotate((EnumFacing)p_185499_1_.get(FACING)));
//	}
//
//	public IBlockState mirror(IBlockState p_185471_1_, Mirror p_185471_2_) {
//		EnumFacing lvt_3_1_ = (EnumFacing)p_185471_1_.get(FACING);
//		StairsShape lvt_4_1_ = (StairsShape)p_185471_1_.get(SHAPE);
//		switch(SyntheticClass_1.field_185869_c[p_185471_2_.ordinal()]) {
//			case 1:
//				if (lvt_3_1_.getAxis() == Axis.Z) {
//					switch(SyntheticClass_1.field_208122_a[lvt_4_1_.ordinal()]) {
//						case 2:
//							return (IBlockState)p_185471_1_.rotate(Rotation.CLOCKWISE_180).with(SHAPE, StairsShape.INNER_RIGHT);
//						case 3:
//							return (IBlockState)p_185471_1_.rotate(Rotation.CLOCKWISE_180).with(SHAPE, StairsShape.INNER_LEFT);
//						case 4:
//							return (IBlockState)p_185471_1_.rotate(Rotation.CLOCKWISE_180).with(SHAPE, StairsShape.OUTER_RIGHT);
//						case 5:
//							return (IBlockState)p_185471_1_.rotate(Rotation.CLOCKWISE_180).with(SHAPE, StairsShape.OUTER_LEFT);
//						default:
//							return p_185471_1_.rotate(Rotation.CLOCKWISE_180);
//					}
//				}
//				break;
//			case 2:
//				if (lvt_3_1_.getAxis() == Axis.X) {
//					switch(SyntheticClass_1.field_208122_a[lvt_4_1_.ordinal()]) {
//						case 1:
//							return p_185471_1_.rotate(Rotation.CLOCKWISE_180);
//						case 2:
//							return (IBlockState)p_185471_1_.rotate(Rotation.CLOCKWISE_180).with(SHAPE, StairsShape.INNER_LEFT);
//						case 3:
//							return (IBlockState)p_185471_1_.rotate(Rotation.CLOCKWISE_180).with(SHAPE, StairsShape.INNER_RIGHT);
//						case 4:
//							return (IBlockState)p_185471_1_.rotate(Rotation.CLOCKWISE_180).with(SHAPE, StairsShape.OUTER_RIGHT);
//						case 5:
//							return (IBlockState)p_185471_1_.rotate(Rotation.CLOCKWISE_180).with(SHAPE, StairsShape.OUTER_LEFT);
//					}
//				}
//		}
//
//		return super.mirror(p_185471_1_, p_185471_2_);
//	}
//
//	protected void fillStateContainer(Builder<Block, IBlockState> p_206840_1_) {
//		p_206840_1_.add(FACING, HALF, SHAPE, WATERLOGGED);
//	}
//
//	public Fluid pickupFluid(IWorld p_204508_1_, BlockPos p_204508_2_, IBlockState p_204508_3_) {
//		if ((Boolean)p_204508_3_.get(WATERLOGGED)) {
//			p_204508_1_.setBlockState(p_204508_2_, (IBlockState)p_204508_3_.with(WATERLOGGED, false), 3);
//			return Fluids.WATER;
//		} else {
//			return Fluids.EMPTY;
//		}
//	}
//
//	public IFluidState getFluidState(IBlockState p_204507_1_) {
//		return (Boolean)p_204507_1_.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(p_204507_1_);
//	}
//
//	public boolean canContainFluid(IBlockReader p_204510_1_, BlockPos p_204510_2_, IBlockState p_204510_3_, Fluid p_204510_4_) {
//		return !(Boolean)p_204510_3_.get(WATERLOGGED) && p_204510_4_ == Fluids.WATER;
//	}
//
//	public boolean receiveFluid(IWorld p_204509_1_, BlockPos p_204509_2_, IBlockState p_204509_3_, IFluidState p_204509_4_) {
//		if (!(Boolean)p_204509_3_.get(WATERLOGGED) && p_204509_4_.getFluid() == Fluids.WATER) {
//			if (!p_204509_1_.isRemote()) {
//				p_204509_1_.setBlockState(p_204509_2_, (IBlockState)p_204509_3_.with(WATERLOGGED, true), 3);
//				p_204509_1_.getPendingFluidTicks().scheduleTick(p_204509_2_, p_204509_4_.getFluid(), p_204509_4_.getFluid().getTickRate(p_204509_1_));
//			}
//
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//	public boolean allowsMovement(IBlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
//		return false;
//	}
//
//	static {
//		FACING = BlockHorizontal.HORIZONTAL_FACING;
//		HALF = BlockStateProperties.HALF;
//		SHAPE = BlockStateProperties.STAIRS_SHAPE;
//		WATERLOGGED = BlockStateProperties.WATERLOGGED;
//		AABB_SLAB_TOP = BlockSlab.TOP_SHAPE;
//		AABB_SLAB_BOTTOM = BlockSlab.BOTTOM_SHAPE;
//		field_196512_A = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 8.0D, 8.0D, 8.0D);
//		field_196513_B = Block.makeCuboidShape(0.0D, 0.0D, 8.0D, 8.0D, 8.0D, 16.0D);
//		field_196514_C = Block.makeCuboidShape(0.0D, 8.0D, 0.0D, 8.0D, 16.0D, 8.0D);
//		field_196515_D = Block.makeCuboidShape(0.0D, 8.0D, 8.0D, 8.0D, 16.0D, 16.0D);
//		field_196516_E = Block.makeCuboidShape(8.0D, 0.0D, 0.0D, 16.0D, 8.0D, 8.0D);
//		field_196517_F = Block.makeCuboidShape(8.0D, 0.0D, 8.0D, 16.0D, 8.0D, 16.0D);
//		field_196518_G = Block.makeCuboidShape(8.0D, 8.0D, 0.0D, 16.0D, 16.0D, 8.0D);
//		field_196519_H = Block.makeCuboidShape(8.0D, 8.0D, 8.0D, 16.0D, 16.0D, 16.0D);
//		field_196520_I = func_199779_a(AABB_SLAB_TOP, field_196512_A, field_196516_E, field_196513_B, field_196517_F);
//		field_196521_J = func_199779_a(AABB_SLAB_BOTTOM, field_196514_C, field_196518_G, field_196515_D, field_196519_H);
//		field_196522_K = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
//	}
//}
//
