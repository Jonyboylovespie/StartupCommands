package jonyboylovespie.startupcommands.mixin;

import com.google.gson.Gson;
import com.mojang.brigadier.ParseResults;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static jonyboylovespie.startupcommands.Startupcommands.LOGGER;
import static jonyboylovespie.startupcommands.Startupcommands.executeCommands;

@Mixin(MinecraftServer.class)
public class StartupCommandsMixin {
	@Inject(at = @At("TAIL"), method = "loadWorld")
	private void init(CallbackInfo info) {
		MinecraftServer server = (MinecraftServer)(Object)this;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			executeCommands(server);
		});
		executor.shutdown();
	}
}