package net.minecraft.block;

import net.minecraft.item.*;
import java.util.*;

class Block$5 extends ThreadLocal<List<ItemStack>> {
    @Override
    protected List<ItemStack> initialValue() {
        return new ArrayList<ItemStack>();
    }
}