package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SetSmoothableBase implements IMessage {

	protected /*final*/ IsSmoothable isSmoothable;
	protected /*final*/ IBlockState state;
	protected /*final*/ boolean newValue;

	public SetSmoothableBase(IsSmoothable isSmoothable, IBlockState state, boolean newValue) {
		this.isSmoothable = isSmoothable;
		this.state = state;
		this.newValue = newValue;
	}

//	public static S2CSetSmoothable decode(PacketBuffer buffer) {
//		IsSmoothable isSmoothable = parseIsSmoothable(buffer.readString(100));
//		BlockState state = BlockStateConverter.fromId(buffer.readVarInt());
//		boolean newValue = buffer.readBoolean();
//		return new S2CSetSmoothable(isSmoothable, state, newValue);
//	}

	public static void encode(SetSmoothableBase msg, PacketBuffer buffer) {
		buffer.writeString(msg.isSmoothable.name());
		buffer.writeVarInt(BlockStateConverter.toId(msg.state));
		buffer.writeBoolean(msg.newValue);
	}

	// 1.12.2 compat

	/**
	 * So this can be instantiated via reflection.
	 */
	public SetSmoothableBase() {
	}


	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		isSmoothable = parseIsSmoothable(buffer.readString(100));
		state = BlockStateConverter.fromId(buffer.readVarInt());
		newValue = buffer.readBoolean();
	}

	private static IsSmoothable parseIsSmoothable(String isSmoothableName) {
		for (IsSmoothable potential : new IsSmoothable[]{IsSmoothable.TERRAIN, IsSmoothable.LEAVES}) {
			if (!potential.name().equals(isSmoothableName))
				continue;
			return potential;
		}
		throw new NullPointerException("Unable to find a smoothable with the name " + isSmoothableName);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		encode(this, new PacketBuffer(buf));
	}
}
