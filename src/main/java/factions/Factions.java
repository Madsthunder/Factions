package factions;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factions.command.CommandFaction;
import factions.factions.FactionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

@Mod(modid = Factions.MODID, name = Factions.NAME, version = Factions.VERSION)
public class Factions
{
	public static final String MODID = "factions";
	public static final String NAME = "Factions";
	public static final String VERSION = "0.0.0";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static class Proxy
	{
		@SidedProxy(modId = MODID)
		public static Proxy I = null;

		public void init()
		{
			PermissionAPI.registerNode("factions.disband", DefaultPermissionLevel.ALL,
					"Determines if the faction member has permission to disband their current faction.");
		}

		public static class ClientProxy extends Proxy
		{

		}

		public static class ServerProxy extends Proxy
		{

		}
	}

	public static FactionManager container = null;

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		Proxy.I.init();
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		if (container != null)
			container.saveFactionsToFile(true);
		container = new FactionManager(event.getServer());
		container.loadFactionsFromFile();
		event.registerServerCommand(new CommandFaction(container));
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{
		if (container != null)
			container.saveFactionsToFile(true);
		container = null;
	}

	@NetworkCheckHandler
	public boolean check(Map<String, String> remoteVersions, Side side)
	{
		if (side == Side.CLIENT)
		{
			return true;
		} else
		{
			return true;
		}
	}

	@SubscribeEvent
	public static void onWorldSave(WorldEvent.Save event)
	{
		if (!event.getWorld().isRemote && container != null)
			container.saveFactionsToFile(false);
	}

}
