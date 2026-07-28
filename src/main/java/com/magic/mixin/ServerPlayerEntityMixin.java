package com.magic.mixin;

import com.magic.event.LibrarianInteractionHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (LibrarianInteractionHandler.isDialogueInProgressForPlayer(player)) {
            BlockPos originalPos = LibrarianInteractionHandler.getOriginalPosition(player);
            if (originalPos != null) {
                player.teleport(
                    originalPos.getX() + 0.5,
                    originalPos.getY(),
                    originalPos.getZ() + 0.5,
                    false
                );
            }
            player.setVelocity(0, player.getVelocity().y, 0);
        }
    }
}