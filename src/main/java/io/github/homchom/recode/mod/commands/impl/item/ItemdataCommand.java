package io.github.homchom.recode.mod.commands.impl.item;

import com.mojang.brigadier.CommandDispatcher;
import io.github.homchom.recode.mod.commands.Command;
import io.github.homchom.recode.mod.commands.arguments.ArgBuilder;
import io.github.homchom.recode.sys.player.chat.ChatType;
import io.github.homchom.recode.sys.player.chat.*;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.world.item.ItemStack;

public class ItemdataCommand extends Command {

    @Override
    public void register(Minecraft mc, CommandDispatcher<FabricClientCommandSource> cd) {
        cd.register(ArgBuilder.literal("itemdata")
                .executes(ctx -> {
                    ItemStack item = mc.player.getMainHandItem();
                    CompoundTag nbt = item.getTag();
                    if (nbt != null) {
                        ChatUtil.sendMessage(String.format("§5----------§dItem Data for %s§5----------", item.getHoverName().getString()));
                        mc.player.displayClientMessage(new TextComponent(nbt.toString()), false);


                        String unformatted = nbt.toString();

                        TextComponent msg1 = new TextComponent("§5Click here to copy a ");
                        TextComponent msg3 = new TextComponent("§d§lUnformatted");
                        TextComponent msg4 = new TextComponent("§5 or ");
                        TextComponent msg5 = new TextComponent("§d§l/dfgive");
                        TextComponent msg6 = new TextComponent("§5 version!");

                        msg3.withStyle((style) -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/copytxt " + unformatted)));
                        msg5.withStyle((style) -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/copytxt " + "/dfgive " + Registry.ITEM.getKey(item.getItem()) + unformatted + " 1")));

                        this.sendMessage(mc, msg1.append(msg3).append(msg4).append(msg5).append(msg6));

                    } else {
                        ChatUtil.sendMessage("No NBT data found!", ChatType.FAIL);
                    }
                    return 1;
                })
        );
    }

    @Override
    public String getDescription() {
        return "[blue]/itemdata[reset]\n"
                + "\n"
                + "Shows the item NBT of the item you are holding.";
    }

    @Override
    public String getName() {
        return "/itemdata";
    }
}
