package com.firebow.mixin;

import com.firebow.event.FireBowRuntime;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hook vào MinecraftClient.tick() — chạy mỗi client tick (50ms).
 *
 * Giống BOOST-FPS ClientPlayerEntityMixin.beforeMovementPackets():
 * execute action đúng timing của tick loop, không phải giữa chừng
 * entity update → server không thấy packet out-of-sequence.
 */
@Mixin(MinecraftClient.class)
public class ClientTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient mc = (MinecraftClient)(Object) this;
        FireBowRuntime.get().onClientTick(mc);
    }
}
