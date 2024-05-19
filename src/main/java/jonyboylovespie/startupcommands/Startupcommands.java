package jonyboylovespie.startupcommands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Startupcommands implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("startup-commands");

	public static String[] getCommands() {
		String[] commands = null;
		File serverDir = FabricLoader.getInstance().getGameDir().toFile();
		String file = new File(serverDir, "startup-commands.json").getPath();
		try (FileReader reader = new FileReader(file)) {
			Gson gson = new Gson();
			commands = gson.fromJson(reader, String[].class);
		} catch (Exception e) {
			LOGGER.info("Error reading file: " + e.getMessage());
		}
		return commands;
	}

	public void writeFile(String[] commands) {
		File serverDir = FabricLoader.getInstance().getGameDir().toFile();
		String file = new File(serverDir, "startup-commands.json").getPath();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(commands);
		try (FileWriter fileWriter = new FileWriter(file)) {
			fileWriter.write(json);
		} catch (IOException e) {
			LOGGER.info("Error writing JSON to file: " + e.getMessage());
		}
	}

	public static void executeCommands(MinecraftServer server) {
		String[] commands = getCommands();
		if (commands != null) {
			for (String command : commands){
				try {
					Thread.sleep(1000);
					ParseResults<ServerCommandSource> parseResults = server.getCommandSource().getDispatcher().parse(command, server.getCommandSource());
					server.getCommandManager().execute(parseResults, command);
					LOGGER.info("Command " + command + " executed successfully");
				}
				catch (Exception e){

				}
			}
		}
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("startupcommands")
				.requires(command -> command.hasPermissionLevel(2))
					.executes(context -> {
						String[] commands = getCommands();
						if (commands != null) {
							if (commands.length == 0) {
								context.getSource().sendFeedback(() -> Text.literal("No startup commands found"), false);
							}
							for (int i = 0; i < commands.length; i++) {
								String command = commands[i];
								int number = i + 1;
								context.getSource().sendFeedback(() -> Text.literal(number + ". " + command), false);
							}
						}
						else {
							context.getSource().sendFeedback(() -> Text.literal("No startup commands found"), false);
						}
						return 1;
					})));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("addstartupcommand")
				.requires(command -> command.hasPermissionLevel(2))
					.then(argument("command (ex: \"time set day\")", StringArgumentType.string())
						.executes(context -> {
							String[] commands = getCommands();
							String[] updatedCommands;
							String command = StringArgumentType.getString(context, "command (ex: \"time set day\")");
							if (commands != null) {
								updatedCommands = new String[commands.length + 1];
								System.arraycopy(commands, 0, updatedCommands, 0, commands.length);
								updatedCommands[commands.length] = command;
							}
							else {
								updatedCommands = new String[1];
								updatedCommands[0] = StringArgumentType.getString(context, "command (ex: \"time set day\")");
							}
							writeFile(updatedCommands);
							context.getSource().sendFeedback(() -> Text.literal("Added command " + command), false);
							return 1;
						}))));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("removestartupcommand")
				.requires(command -> command.hasPermissionLevel(2))
				.then(argument("command number", StringArgumentType.string())
						.executes(context -> {
							String[] commands = getCommands();
							int number = Integer.parseInt(StringArgumentType.getString(context, "command number")) - 1;
							if (number <= commands.length) {
								String command = commands[number];
								String[] updatedCommands = new String[commands.length - 1];
								System.arraycopy(commands, 0, updatedCommands, 0, number);
								System.arraycopy(commands, number + 1, updatedCommands, number, commands.length - number - 1);
								writeFile(updatedCommands);
								context.getSource().sendFeedback(() -> Text.literal("Removed command " + command), false);
							}
							else {
								context.getSource().sendFeedback(() -> Text.literal("Command does not exist"), false);
							}
							return 1;
						}))));
	}
}