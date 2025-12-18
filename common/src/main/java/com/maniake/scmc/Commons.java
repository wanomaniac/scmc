package com.maniake.scmc;

import com.maniake.scmc.config.ModMenuConfig;
import com.maniake.scmc.config.ModMenuConfigManager;
import com.maniake.scmc.interfaces.IComponent;
import com.maniake.scmc.interfaces.IModMetadata;
import com.maniake.scmc.interfaces.IPlatformHelper;
import com.maniake.scmc.interfaces.IServerEvents;
import com.maniake.scmc.services.ServiceKey;
import com.maniake.scmc.services.ServicesManager;
import com.maniake.scmc.utils.Network;
import com.maniake.scmc.utils.servercmd;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import com.maniake.scmc.db.Player;

import java.util.HashMap;
import java.util.Map;

public class Commons {
    public static final ServiceKey<IPlatformHelper> PLATFORM_KEY =
            ServiceKey.of(IPlatformHelper.class);
    public static final ServiceKey<IModMetadata> ModVersionKey =
            ServiceKey.of(IModMetadata.class);
    public static final ServiceKey<IComponent> ComponentClassKey =
            ServiceKey.of(IComponent.class);
    public static final Map<String, Player> PLAYERS = new HashMap<>();

    public static IPlatformHelper PLATFORM;;
    public static IModMetadata modVersion;
    private static boolean registerd = false;
    public static MinecraftServer serverD = null;
    public static Network scmcNetwork = new Network(27752);
    public static Network.ServerCommands commands = new Network.ServerCommands();

    public static void init() {
//        AbstComponent = ServicesManager.get(ComponentClassKey);
        modVersion = ServicesManager.get(ModVersionKey);

        ModMenuConfigManager.prepareConfigFile();
        if (!ModMenuConfigManager.fileExistant()) {

            Constants.LOG.info(Component.translatable("scmc.console.welcome", Component.translatable("scmc.welcome.header"), modVersion.getVersionName()).getString());
            ModMenuConfigManager.initializeConfig();
        } else {
            ModMenuConfigManager.initializeConfig();
            Constants.LOG.info(Component.translatable("scmc.console.initalize", Component.translatable("scmc.header"), modVersion.getVersionName()).getString());
        }

        scmcNetwork.setPort(ModMenuConfig.CPORT.getValue());
        PLATFORM = ServicesManager.get(PLATFORM_KEY);
    }

    public static void initServer(MinecraftServer server){
        if(!registerd) {
            registerd = true;
            serverD = server;
            Thread main = new Thread(() -> {
                Thread connection = new Thread(() -> {
                    try {
                        Constants.LOG.info("Initalizing - Connection");
                        commands.addCommand("getall", servercmd::getsvrmds);
                        commands.addCommand("hello", servercmd::helloworld);
                        commands.addCommand("download", servercmd::downloadFile);
                        commands.addCommand("getmod", servercmd::getModName);
                        commands.addCommand("addpmods", servercmd::verifyPlayerMods);
                        commands.addCommand("addploc", servercmd::setPlayerLocale);
                        commands.addCommand("getversion", servercmd::getVersion);
                        commands.addCommand("getprotversion", servercmd::GetProtocalVersion);
                        commands.addCommand("getfabricversion", servercmd::GetFabricVersion);
                        scmcNetwork.init(commands);

                        while (true) {
                            if (!scmcNetwork.isOnline && scmcNetwork.crashed) {
                                Constants.LOG.error("NETWORkING HAS CRASHED!");
                                Exception test = new Exception("Critical exception while running connection thread - " + scmcNetwork.howC + "\n \nIf this crash is related to your port, fix your permissions or configuration to a proper setting!");
                                if(ModMenuConfig.ENABLESCMCCRASHES.getValue()){
                                    SCMCCrashReport.setCrash(test, server);
                                } else {
                                    Constants.LOG.error(SCMCCrashReport.getCrash(test, false));
                                    Constants.LOG.info("SCMC will not stop the server but it's recommanded to take note!");
                                }
                                break;
                            } else if (scmcNetwork.isOnline && !scmcNetwork.crashed) {
                                break;
                            }
                        }

                        if(scmcNetwork.isOnline) Constants.LOG.info("Connection - Ready");
                    } catch (Exception error) {
                        Constants.LOG.error("Exception occured while initalizing connection thread \n {}", error.getMessage());
                        try {
                            throw new Exception();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                connection.setUncaughtExceptionHandler((thread, error) -> {
                    if(!scmcNetwork.isOnline && ModMenuConfig.ENABLESCMCCRASHES.getValue())
                        SCMCCrashReport.setCrash((Exception) error, server);
                    else {
                        if(!ModMenuConfig.ENABLESCMCCRASHES.getValue() && !scmcNetwork.isOnline){
                            Constants.LOG.error("SCMC connection thread has errored & it could not recover! \n This is the error report \n");
                            Constants.LOG.error(SCMCCrashReport.getCrash((Exception) error, false));
                        }
                    }
                });

                connection.setName("Main Thread - Connection - SCMC");
                connection.setPriority(1);

                Thread events = new Thread(() -> {
                    Constants.LOG.info("Initalizing - Events");
                    IServerEvents event = ServicesManager.get(ServiceKey.of(IServerEvents.class));
                    event.Register();
                    Constants.LOG.info("Events - Ready");
                });

                events.setUncaughtExceptionHandler((thread, error) -> {
                    if(ModMenuConfig.ENABLESCMCCRASHES.getValue()){
                        SCMCCrashReport.setCrash((Exception) error, server);
                    } else {
                        Constants.LOG.error("An error occured to the events register! This is the following error report: \n");
                        Constants.LOG.error(SCMCCrashReport.getCrash((Exception) error, false));
                    }
                });

                events.setName("Main Thread - Events - SCMC");
                connection.setPriority(2);

                connection.start();
                events.start();

                try {
                    connection.join();
                    events.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            main.setName("Main Thread - SCMC");

            main.start();
        }
    }

    public static void stopServer(MinecraftServer server){
        scmcNetwork.stop();
        Constants.LOG.info("Goodbye");
    }

}