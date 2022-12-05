package io.github.homchom.recode.mixin.game;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.homchom.recode.LegacyRecode;
import io.github.homchom.recode.mod.features.switcher.StateSwitcherScreen;
import io.github.homchom.recode.sys.networking.LegacyState;
import io.github.homchom.recode.sys.player.DFInfo;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public class MKeyboardHandler {
    private static final int stateSwitcherKey = InputConstants.KEY_F5;

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void handleDebugKeys(int i, CallbackInfoReturnable<Boolean> cir) {
        if(i == stateSwitcherKey && DFInfo.isOnDF() && DFInfo.currentState.getMode() != LegacyState.Mode.SPAWN) {
            cir.cancel();
            LegacyRecode.MC.setScreen(new StateSwitcherScreen(stateSwitcherKey));
            cir.setReturnValue(true);
        }
    }
}
