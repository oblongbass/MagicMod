package com.magic.data;

import net.minecraft.nbt.NbtCompound;
import java.util.Optional;

public class PlayerData {
    public boolean hasReceivedGuideBook = false;
    
    public void writeNbt(NbtCompound nbt) {
        nbt.putBoolean("hasReceivedGuideBook", hasReceivedGuideBook);
    }
    
    public void readNbt(NbtCompound nbt) {
        hasReceivedGuideBook = nbt.getBoolean("hasReceivedGuideBook").orElse(false);
    }
} 