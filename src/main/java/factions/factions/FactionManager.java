package factions.factions;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import factions.Factions;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;

public class FactionManager
{
	private final Map<UUID, Faction> uuid_to_faction = Maps.newHashMap();
	private final Map<UUID, FactionNotification> uuid_to_notification = Maps.newHashMap();
	private final Map<UUID, FactionMember> uuid_to_factionmember = Maps.newHashMap();
	private final MinecraftServer server;
	public boolean needs_saving = false;

	public FactionManager(MinecraftServer server)
	{
		this.server = server;
	}

	public MinecraftServer getServer()
	{
		return this.server;
	}

	public FactionMember getFactionMember(UUID uuid)
	{
		for (Faction faction : this.uuid_to_faction.values())
		{
			if (faction.getOwnerInfo().getUUID().equals(uuid))
				return faction.getOwnerInfo();
			if (faction.getMembers().containsKey(uuid))
				return faction.getMembers().get(uuid);
		}
		return null;
	}

	public void createFaction(UUID owner, String name) throws CommandException
	{
		for (Faction faction : this.uuid_to_faction.values())
		{
			if (faction.getName().toLowerCase(Locale.ENGLISH).equals(name.toLowerCase(Locale.ENGLISH)))
				throw new CommandException("There is already a faction with that name.");
			if (faction.getOwnerInfo().getUUID().equals(owner))
				throw new CommandException("You already own a faction. If you wish to create a new one, disband \""
						+ faction.getName() + "\".");
		}
		Faction faction = new Faction(this, owner, name);
		this.uuid_to_faction.put(faction.getUUID(), faction);
		this.needs_saving = true;
	}

	public Faction disbandFaction(UUID executor) throws CommandException
	{
		for (Faction faction : this.uuid_to_faction.values())
		{
			if (faction.getMembers().containsValue(executor))
				throw new CommandException("You are not the owner of \"" + faction.getName() + "\".");
			if (faction.getOwnerInfo().getUUID().equals(executor))
			{
				this.uuid_to_faction.remove(faction.getUUID());
				this.needs_saving = true;
				return faction;
			}
		}
		throw new CommandException("You are not part of a faction.");
	}

	public void removePlayerFromFaction(UUID executor, String player) throws CommandException
	{
		UUID uuid = EntityPlayer.getOfflineUUID(player);
		if (uuid == null)
			throw new CommandException("\"" + player + "\" does not exist.");

	}

	public Collection<Faction> getFactions()
	{
		return this.uuid_to_faction.values();
	}

	public void loadFactionsFromFile()
	{
		this.uuid_to_faction.clear();
		this.uuid_to_notification.clear();
		this.uuid_to_factionmember.clear();
		File factions = new File(DimensionManager.getCurrentSaveRootDirectory(), "factions.dat");
		if (factions.exists())
		{
			Factions.LOGGER.info("Loading Factions from directory [" + factions.getAbsolutePath() + "].");
			NBTTagCompound compound;
			try
			{
				compound = CompressedStreamTools.read(factions);
			} catch (IOException exception)
			{
				compound = new NBTTagCompound();
				exception.printStackTrace();
			}
			NBTTagList list = compound.getTagList("factions", 10);
			for (int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound compound1 = list.getCompoundTagAt(i);
				UUID uuid = compound1.getUniqueId("uuid");
				Faction faction = new Faction(this, uuid);
				faction.deserializeNBT(compound1.getCompoundTag("faction"));
				this.uuid_to_faction.put(uuid, faction);
			}
			for (Faction faction : this.uuid_to_faction.values())
			{
				for (FactionMember member : faction.getMembers().values())
				{
					if (this.uuid_to_factionmember.containsKey(member.getUUID()))
					{
					}
				}
			}
			NBTTagList notifications = compound.getTagList("notifications", 10);
			for (int i = 0; i < notifications.tagCount(); i++)
			{
				NBTTagCompound compound1 = notifications.getCompoundTagAt(i);
				UUID uuid = compound1.getUniqueId("uuid");
				FactionNotification notification = new FactionNotification(uuid);
				notification.deserializeNBT(compound1.getCompoundTag("notification"));
				this.uuid_to_notification.put(uuid, notification);
			}
		}
		this.needs_saving = true;
		this.sanityCheck();
	}

	public void sanityCheck()
	{
		Factions.LOGGER.info("Starting Sanity Check...");
	}

	public void saveFactionsToFile(boolean force)
	{
		if (force || this.needs_saving)
		{
			File factions = new File(DimensionManager.getCurrentSaveRootDirectory(), "factions.dat");
			Factions.LOGGER.info("Loading Factions to directory [" + factions.getAbsolutePath() + "].");
			if (!factions.exists())
			{
				try
				{
					factions.createNewFile();
				} catch (Exception exception)
				{
					exception.printStackTrace();
				}
			}
			try
			{
				NBTTagCompound compound = new NBTTagCompound();
				NBTTagList list = new NBTTagList();
				for (Faction faction : this.uuid_to_faction.values())
				{
					NBTTagCompound compound1 = new NBTTagCompound();
					compound1.setUniqueId("uuid", faction.getUUID());
					compound1.setTag("faction", faction.serializeNBT());
					list.appendTag(compound1);
				}
				compound.setTag("factions", list);
				CompressedStreamTools.write(compound, factions);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		this.needs_saving = false;
	}
}
