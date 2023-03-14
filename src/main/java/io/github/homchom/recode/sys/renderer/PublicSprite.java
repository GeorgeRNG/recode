package io.github.homchom.recode.sys.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PublicSprite extends TextureAtlasSprite {

    public PublicSprite(ResourceLocation resourceLocation, @NotNull SpriteContents spriteContents, int i, int j, int k, int l) {
        super(resourceLocation, spriteContents, i, j, k, l);
    }
}
