package me.reasonless.codeutilities.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.realmsclient.gui.ChatFormatting;

import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import me.reasonless.codeutilities.commands.help.HelpCommand;
import me.reasonless.codeutilities.commands.image.ImageCommand;
import me.reasonless.codeutilities.commands.image.ImageLoadCommand;
import me.reasonless.codeutilities.commands.nbs.NBSCommand;
import me.reasonless.codeutilities.commands.nbs.NBSLoadCommand;
import me.reasonless.codeutilities.commands.nbs.NBSPlayerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class Commands implements ClientCommandPlugin {
	MinecraftClient mc = MinecraftClient.getInstance();
	@Override
	public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {

		dispatcher.register(ArgumentBuilders.literal("nbs")
			.then(ArgumentBuilders.literal("player")
					.executes(ctx -> {
						try {
							return NBSPlayerCommand.execute(mc, ctx);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						return 0;
					}))
			.then(ArgumentBuilders.literal("load")
				.then(ArgumentBuilders.argument("location", StringArgumentType.greedyString())
						.executes(ctx -> {
							try {
								return NBSLoadCommand.execute(mc, ctx);
							} catch (Exception e) {
								mc.player.sendMessage(new LiteralText(ChatFormatting.DARK_RED + " - " + ChatFormatting.RED + "There was an error loading this nbs file."));
								mc.player.sendMessage(new LiteralText(ChatFormatting.DARK_RED + " - " + ChatFormatting.RED + "Maybe the nbs is made using an older Noteblock Studio Version."));
								mc.player.sendMessage(new LiteralText(ChatFormatting.GOLD + " - " + ChatFormatting.YELLOW + "The NBS function uses NBS File Format v4"));
								e.printStackTrace();
							}
							return 1;
						})))
			.executes(ctx -> NBSCommand.execute(mc, ctx)));

		dispatcher.register(ArgumentBuilders.literal("image")
				.then(ArgumentBuilders.literal("load")
						.then(ArgumentBuilders.argument("location", StringArgumentType.greedyString())
								.executes(ctx -> {
									try {
										return ImageLoadCommand.execute(mc, ctx);
									} catch (Exception e) {
										mc.player.sendMessage(new LiteralText(ChatFormatting.DARK_RED + " - " + ChatFormatting.RED + "There was an error loading this image."));
										e.printStackTrace();
									}
									return 1;
								})))
				.executes(ctx -> ImageCommand.execute(mc, ctx)));

		dispatcher.register(ArgumentBuilders.literal("webview")
			.executes(ctx -> WebViewCommand.execute(mc, ctx)));
		dispatcher.register(ArgumentBuilders.literal("codeutilities")
				.then(ArgumentBuilders.literal("music")
						.executes(ctx -> {
							return NBSCommand.execute(mc, ctx);
						}))
				.then(ArgumentBuilders.literal("image")
						.executes(ctx -> {
							return ImageCommand.execute(mc, ctx);
						}))
		.executes(ctx -> HelpCommand.execute(mc, ctx)));
	}
	

}
