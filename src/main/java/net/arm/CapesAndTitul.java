package net.arm;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.arm.util.ModMessages;
import net.arm.util.OpenMenuPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(CapesAndTitul.MODID)
public class CapesAndTitul {
    public static final String MODID = "capesandtitls";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CapesAndTitul(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        modEventBus.addListener(ModMessages::register);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        net.arm.server.TitleConfig.load();

        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            net.arm.client.CapesAndTitulClient.init(modEventBus, modContainer);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            net.arm.server.ServerDataManager.syncToClient(player);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("capes")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayer player) {
                        ModMessages.sendToPlayer(new OpenMenuPacket(0), player);
                    }
                    return 1;
                }));

        dispatcher.register(Commands.literal("titls")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayer player) {
                        ModMessages.sendToPlayer(new OpenMenuPacket(1), player);
                    }
                    return 1;
                }));

        dispatcher.register(Commands.literal("capesadmin")
                .requires(source -> source.getServer().getPlayerList().isOp(source.getPlayer() != null ? source.getPlayer().nameAndId() : null) || source.getEntity() == null)
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            String id = StringArgumentType.getString(context, "id");

                                            net.arm.server.ServerDataManager.unlockItem(target, id);

                                            context.getSource().sendSuccess(() -> Component.literal("Успешно выдан предмет " + id + " игроку " + target.getScoreboardName()), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("take")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            String id = StringArgumentType.getString(context, "id");

                                            net.arm.server.ServerDataManager.lockItem(target, id);

                                            context.getSource().sendSuccess(() -> Component.literal("Успешно забран предмет " + id + " у игрока " + target.getScoreboardName()), true);
                                            return 1;
                                        })))));
    }
}