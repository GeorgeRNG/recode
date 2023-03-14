package io.github.homchom.recode.mod.mixin.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.homchom.recode.mod.config.Config;
import io.github.homchom.recode.sys.sidedchat.ChatRule;
import io.github.homchom.recode.sys.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

import static net.minecraft.client.gui.GuiComponent.fill;

@Mixin(ChatComponent.class)
public abstract class MSideChatHUD {
    @Shadow
    @Final
    private static Logger LOGGER;

    private final List<GuiMessage.Line> sideVisibleMessages = Lists.newArrayList();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private int chatScrollbarPos;

    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Shadow
    private boolean newMessageSinceScroll;

    private int oldSideChatWidth = -1;
    private int sideScrolledLines;

    @Shadow
    private static double getTimeFactor(int age) {
        return 0;
    }

    @Shadow
    protected abstract boolean isChatFocused();

    @Shadow
    protected abstract boolean isChatHidden();

    @Shadow
    public abstract double getScale();

    @Shadow
    public abstract int getLinesPerPage();

    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract void rescaleChat();

    @Shadow
    public abstract int getLineHeight();

    @Shadow
    public abstract int getTagIconLeft(GuiMessage.Line line);

    @Shadow
    public abstract void drawTagIcon(PoseStack poseStack, int x, int y, GuiMessageTag.Icon icon);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(PoseStack poseStack, int i, int j, int k, CallbackInfo ci) {
        List<GuiMessage.Line> tempMain = new ArrayList<>();
        List<GuiMessage.Line> tempSide = new ArrayList<>();
        if (Config.getBoolean("stackDuplicateMsgs")) {
            tempMain.addAll(trimmedMessages);
            tempSide.addAll(sideVisibleMessages);
            trimmedMessages.clear();
            trimmedMessages.addAll(stackMsgs(tempMain));
            sideVisibleMessages.clear();
            sideVisibleMessages.addAll(stackMsgs(tempSide));
        }

        int renderedLines = renderChat(poseStack, 0, trimmedMessages, 0, getWidth(),
                chatScrollbarPos);
        renderChat(poseStack, 0, sideVisibleMessages, getSideChatStartX(),
            getSideChatWidth(), sideScrolledLines);
        renderOthers(poseStack, renderedLines);
        ci.cancel();

        if (Config.getBoolean("stackDuplicateMsgs")) {
            trimmedMessages.clear();
            trimmedMessages.addAll(tempMain);
            sideVisibleMessages.clear();
            sideVisibleMessages.addAll(tempSide);
        }
    }

    /**
     * Renders a chat box, drawn into its own function so I don't repeat code for side chat Most
     * params are just stuff the code needs and I don't have the confidence to change
     *
     * @param displayX      X to display at
     * @param width         Width of the chat to display
     * @param scrolledLines The amount of lines scrolled on this chat
     * @return The amount of lines actually rendered. Other parts of rendering need to know this
     */
    @SuppressWarnings("deprecation")
    private int renderChat(PoseStack matrices, int tickDelta, List<GuiMessage.Line> visibleMessages, int displayX, int width, int scrolledLines) {
        // reset chat (re-allign all text) whenever calculated size for side chat changes
        int newSideChatWidth = getSideChatWidth();
        if (newSideChatWidth != oldSideChatWidth) {
            oldSideChatWidth = newSideChatWidth;
            rescaleChat();
        }

        // will apologise - most code is taken from deobfuscated minecraft jar
        // have attempted to make it as readable as possible but some lines idk man no clue
        int visibleLineCount = this.getLinesPerPage();
        int visibleMessagesSize = visibleMessages.size();
        int renderedLines = 0;
        if (visibleMessagesSize > 0) {
            boolean chatFocused = this.isChatFocused();

            float d = (float) this.getScale();
            int k = Mth.ceil((double) width / d);
            matrices.pushPose();
            matrices.translate(4.0F, 8.0F, 0.0F);
            matrices.scale(d, d, 1f);
            double opacity = this.minecraft.options.chatOpacity().get();
            double backgroundOpacity = this.minecraft.options.textBackgroundOpacity().get();
            double lineSpacing = this.minecraft.options.chatLineSpacing().get();
            double lineSpacing2 = -8.0D * (lineSpacing + 1.0D) + 4.0D * lineSpacing;
            int lineHeight = this.getLineHeight();

            for (int i = 0; i + scrolledLines < visibleMessages.size() && i < visibleLineCount; ++i) {
                GuiMessage.Line chatHudLine = visibleMessages.get(i + scrolledLines);
                if (chatHudLine != null) {
                    int ticksSinceCreation = tickDelta - chatHudLine.addedTime();
                    if (ticksSinceCreation < 200 || chatFocused) {
                        double o = chatFocused ? 1.0D : getTimeFactor(ticksSinceCreation);
                        int aa = (int) (255.0D * o * opacity);
                        int ab = (int) (255.0D * o * backgroundOpacity);
                        ++renderedLines;
                        if (aa > 3) {
                            int s = -i * lineHeight;
                            int t = (int)(s + lineSpacing2);
                            matrices.pushPose();
                            matrices.translate(displayX, 0, 50.0D);
                            fill(matrices, -2, s - lineHeight, k + 4, s, ab << 24);
                            GuiMessageTag guiMessageTag = chatHudLine.tag();
                            if (guiMessageTag != null) {
                                int w = guiMessageTag.indicatorColor() | aa << 24;
                                fill(matrices, -4, s - lineHeight, -2, s, w);
                                if (chatFocused && chatHudLine.endOfEntry() && guiMessageTag.icon() != null) {
                                    int x = this.getTagIconLeft(chatHudLine);
                                    Objects.requireNonNull(this.minecraft.font);
                                    int y = t + 9;
                                    this.drawTagIcon(matrices, x, y, guiMessageTag.icon());
                                }
                            }
                            RenderSystem.enableBlend();
                            matrices.translate(0.0D, 0.0D, 50.0D);
                            this.minecraft.font.drawShadow(matrices, chatHudLine.content(), 0.0F, t, 16777215 + (aa << 24));
                            RenderSystem.disableDepthTest();
                            RenderSystem.disableBlend();
                            matrices.popPose();
                        }
                    }
                }
            }
            // in case you're wondering, the splitting of the text by width is done as its received, not upon rendering

            matrices.popPose();
        }
        return renderedLines;
    }

    @SuppressWarnings("deprecation")
    private void renderOthers(PoseStack matrices, int renderedLines) {
        int visibleMessagesSize = this.trimmedMessages.size();
        if (visibleMessagesSize == 0) {
            return;
        }
        boolean chatFocused = this.isChatFocused();

        float chatScale = (float) this.getScale();
        int k = Mth.ceil((double) this.getWidth() / chatScale);
        matrices.pushPose();
        matrices.translate(4.0F, 8.0F, 0.0F);
        matrices.scale(chatScale, chatScale, 1.0f);
        double opacity = this.minecraft.options.chatOpacity().get();
        double backgroundOpacity = this.minecraft.options.textBackgroundOpacity().get();

        long queueSize = this.minecraft.getChatListener().queueSize();
        if (queueSize > 0L) {
            int m = (int) (128.0D * opacity);
            int w = (int) (255.0D * backgroundOpacity);
            matrices.pushPose();
            matrices.translate(0.0D, 0.0D, 50.0D);
            fill(matrices, -2, 0, k + 4, 9, w << 24);
            RenderSystem.enableBlend();
            matrices.translate(0.0D, 0.0D, 50.0D);
            this.minecraft.font.drawShadow(matrices,
                Component.translatable("chat.queue", queueSize), 0.0F, 1.0F,
                16777215 + (m << 24));
            matrices.popPose();
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
        }

        if (chatFocused) {
            int v = 9;
            int w = visibleMessagesSize * v + visibleMessagesSize;
            int x = renderedLines * v + renderedLines;
            int y = chatScrollbarPos * x / visibleMessagesSize;
            int z = x * x / w;
            if (w != x) {
                int aa = y > 0 ? 170 : 96;
                int ab = this.newMessageSinceScroll ? 13382451 : 3355562;
                int ac = k + 4;
                fill(matrices, ac, -y, ac + 2, -y - z, ab + (aa << 24));
                fill(matrices, ac, -y, ac + 1, -y - z, 13421772 + (aa << 24));
            }
        }

        matrices.popPose();
    }

    private int getSideChatStartX() {
        return (int) ((this.minecraft.getWindow().getGuiScaledWidth() - getSideChatWidth())
            / getSideChatScale()) - 2;
    }

    private int getSideChatWidth() {
        int configWidth = Config.getInteger("sidechat_width");

        // if the width in config is valid
        if (configWidth > 0) {
            return configWidth;
        } else { // else if 0 or less, auto size the side chat
            int rawWidth = Math.min(
                (this.minecraft.getWindow().getGuiScaledWidth() - getWidth() - 14),
                getWidth()
            );
            // if the calculated width <= 0 (window really small), have 1 as a failsafe value
            return rawWidth > 0 ? rawWidth : 1;
        }
    }

    // just incase i want to re-add the option to change side chat scale
    private double getSideChatScale() {
        return getScale();
    }

    @Inject(method = "clearMessages", at = @At("TAIL"))
    private void clearMessages(boolean clearHistory, CallbackInfo ci) {
        sideVisibleMessages.clear();
    }


    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", at = @At("TAIL"))
    private void addMessage(Component component, MessageSignature messageSignature, int messageId, GuiMessageTag guiMessageTag, boolean refresh, CallbackInfo ci) {
        boolean matchedARule = false;
        for (ChatRule chatRule : ChatRule.getChatRules()) {
            // compare against all rules
            if (chatRule.matches(component)) {
                // also don't add to chat if the chat side is either
                if (!matchedARule && chatRule.getChatSide() != ChatRule.ChatSide.EITHER) {
                    addToChat(chatRule.getChatSide(), component, messageId, guiMessageTag);
                    matchedARule = true;
                }

                // dont play sound if message is just being refreshed (ie, when window changes size)
                // & dont try to play a null sound (when the sound is set to 'None')
                if (!refresh && chatRule.getChatSound() != null) {
                    SoundUtil.playSound(chatRule.getChatSound());
                }
            }
        }

        // if matched rule, remove last from
        if (matchedARule) {
            int i = Mth.floor((double) this.getWidth() / this.getScale());
            int addedMessageCount = ComponentRenderUtils.wrapComponents(component, i,
                this.minecraft.font).size();
            // remove the last addedMessageCount messages from the visible messages
            // this has the effect of removing last message sent to main (to go to side instead)
            trimmedMessages.subList(0, addedMessageCount).clear();
        }
    }

    private void addToChat(ChatRule.ChatSide side, Component message, int chatLineId,
        GuiMessageTag guiMessageTag) {
        int i;
        switch (side) {
            case MAIN:
            default:
                i = Mth.floor((double) this.getWidth() / this.getScale());
                break;
            case SIDE:
                i = Mth.floor((double) this.getSideChatWidth() / this.getSideChatScale());
                break;
        }

        List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(message, i, this.minecraft.font);
        for(int k = 0; k < list.size(); ++k) {
            FormattedCharSequence formattedCharSequence = list.get(k);

            boolean bl3 = k == list.size() - 1;
            this.getChatLines(side).add(0, new GuiMessage.Line(chatLineId, formattedCharSequence, guiMessageTag, bl3));

        }
    }

    private List<GuiMessage.Line> getChatLines(ChatRule.ChatSide chatSide) {
        switch (chatSide) {
            case MAIN:
            default:
                return trimmedMessages;
            case SIDE:
                return sideVisibleMessages;
        }
    }

    @Inject(method = "rescaleChat", at = @At("HEAD"))
    private void rescaleChat(CallbackInfo ci) {
        sideVisibleMessages.clear();
    }

    // another copy from minecraft decompiled code
    // the main difference is switching references from main to side chat
    // and subtracting getSideChatStartX() from the adjusted x
    @Inject(method = "getClickedComponentStyleAt", at = @At("HEAD"), cancellable = true)
    private void getClickedComponentStyleAt(double x, double y, CallbackInfoReturnable<Style> cir) {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
            double scale = this.getSideChatScale();
            double adjustedX = (x - 2.0D) - getSideChatStartX();
            double adjustedY = (double) this.minecraft.getWindow().getGuiScaledHeight() - y - 40.0D;
            adjustedX = Mth.floor(adjustedX / scale);
            adjustedY = Mth.floor(
                adjustedY / (scale * (this.minecraft.options.chatLineSpacing().get() + 1.0D)));
            if (!(adjustedX < 0.0D) && !(adjustedY < 0.0D)) {
                int size = Math.min(this.getLinesPerPage(), this.sideVisibleMessages.size());
                if (adjustedX <= (double) Mth.floor(
                    (double) this.getSideChatWidth() / scale)) {
                    if (adjustedY < (double) (9 * size + size)) {
                        int line = (int) (adjustedY / 9.0D + (double) sideScrolledLines);
                        if (line >= 0 && line < this.sideVisibleMessages.size()) {
                            GuiMessage.Line chatHudLine = this.sideVisibleMessages.get(line);
                            cir.setReturnValue(this.minecraft.font.getSplitter()
                                .componentStyleAtWidth(chatHudLine.content(), (int) adjustedX));
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "resetChatScroll", at = @At("TAIL"))
    private void resetChatScroll(CallbackInfo ci) {
        sideScrolledLines = 0;
    }

    @Inject(method = "scrollChat", at = @At("TAIL"))
    private void scrollChat(int amount, CallbackInfo ci) {
        sideScrolledLines = (int) ((double) this.chatScrollbarPos + amount);
        int i = this.sideVisibleMessages.size();
        if (sideScrolledLines > i - this.getLinesPerPage()) {
            sideScrolledLines = i - this.getLinesPerPage();
        }

        if (sideScrolledLines <= 0) {
            sideScrolledLines = 0;
        }
    }

    // Message Stacker
    public List<GuiMessage.Line> stackMsgs(List<GuiMessage.Line> msgs) {
        GuiMessage.Line last = null;
        int count = 1;

        List<GuiMessage.Line> copy = new ArrayList<>();

        for (GuiMessage.Line msg : msgs) {
            if (last == null) {
                last = msg;
            } else {
                if (OrderedTextUtil.getString(last.content())
                    .equals(OrderedTextUtil.getString(msg.content()))) {
                    count++;
                } else {
                    if (count == 1) {
                        copy.add(last);
                    } else {
                        copy.add(new GuiMessage.Line(
                                last.addedTime(),
                                FormattedCharSequence.composite(
                                    last.content(),
                                    TextUtil.colorCodesToTextComponent(" §bx" + count).getVisualOrderText()),
                                last.tag(),
                                true
                            )
                        );
                    }
                    count = 1;
                    last = msg;
                }
            }
        }
        if (last != null) {
            copy.add(last);
        }
        return copy;
    }
}
