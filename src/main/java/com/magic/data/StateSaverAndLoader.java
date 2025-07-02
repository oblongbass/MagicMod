package com.magic.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.magic.data.PlayerData;

public class StateSaverAndLoader extends PersistentState {
    public Map<UUID, PlayerData> players = new HashMap<>();
    
    // 注意：1.21.5+ PersistentState API 变动，以下方法需根据官方文档适配
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerData.writeNbt(playerNbt);
            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);
        return nbt;
    }
    
    public static StateSaverAndLoader createFromNbt(NbtCompound nbt) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        Optional<NbtCompound> playersNbtOpt = nbt.getCompound("players");
        if (playersNbtOpt.isEmpty()) return state;
        NbtCompound playersNbt = playersNbtOpt.get();
        for (String uuidString : playersNbt.getKeys()) {
            UUID uuid = UUID.fromString(uuidString);
            PlayerData playerData = new PlayerData();
            playerData.readNbt(playersNbt.getCompound(uuidString).orElse(new NbtCompound()));
            state.players.put(uuid, playerData);
        }
        return state;
    }
    
    // PersistentStateManager.getOrCreate 相关API 1.21.5+已变动，需查阅官方文档适配
    // public static StateSaverAndLoader getServerState(MinecraftServer server) {
    //     PersistentStateManager persistentStateManager = server.getWorld(server.getOverworld().getRegistryKey()).getPersistentStateManager();
    //     
    //     StateSaverAndLoader state = persistentStateManager.getOrCreate(
    //             StateSaverAndLoader::createFromNbt,
    //             StateSaverAndLoader::new,
    //             "magic_mod_data"
    //     );
    //     
    //     state.markDirty();
    //     return state;
    // }
    
    // public static PlayerData getPlayerState(ServerPlayerEntity player) {
    //     StateSaverAndLoader serverState = getServerState(player.getServer());
    //     return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    // }
} 