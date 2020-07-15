package io.github.codeutilities.commands.impl.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.codeutilities.CodeUtilities;
import io.github.codeutilities.commands.impl.Command;
import io.github.codeutilities.util.StringUtil;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UuidCommand extends Command {

    @Override
    public void register(MinecraftClient mc, CommandDispatcher<CottonClientCommandSource> cd) {
        cd.register(ArgumentBuilders.literal("uuid")
                .then(ArgumentBuilders.argument("username", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            boolean copy = false;
                            if (ctx.getArgument("username", String.class).contains(" copy")) {
                                copy = true;
                            }
                            String username = ctx.getArgument("username", String.class).replace(" copy", "");
                            String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
                            try {
                                String UUIDJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
                                JSONObject json = new JSONObject(UUIDJson);
                                String uuid = json.getString("id");
                                String fullUUID = StringUtil.fromTrimmed(uuid);
                                CodeUtilities.chat("§eUUID of §b" + username + "§e is §d" + fullUUID + "§e!");
                                if (copy) {
                                    CodeUtilities.chat("§aThe UUID has been copied to the clipboard!");
                                    mc.keyboard.setClipboard(fullUUID);
                                } else if (CodeUtilities.isOnDF()) {
                                    mc.player.sendChatMessage("/txt " + fullUUID);
                                }
                            } catch (IOException | JSONException e) {
                                CodeUtilities.chat("§cUser §6" + username + "§c was not found.");
                                e.printStackTrace();
                            }
                            ;
                            return 1;
                        })
                        .then(ArgumentBuilders.literal("copy"))
                )
        );
    }
}