package io.github.homchom.recode.mixin;

import io.github.homchom.recode.RecodeDispatcher;
import io.github.homchom.recode.lifecycle.QuitGameEvent;
import kotlin.Unit;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MMinecraft {
    @Inject(method = "runTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;runAllTasks()V"
    ))
    public void runRecodeTasksNormally(CallbackInfo ci) {
        RecodeDispatcher.INSTANCE.expedite(); // ensure tasks are run on runTick if not elsewhere
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void quit(CallbackInfo ci) {
        QuitGameEvent.INSTANCE.run(Unit.INSTANCE);
    }
}