package factions.factions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class Faction implements ICapabilitySerializable<NBTTagCompound>
{
	private final FactionManager container;
	private final UUID uuid;
	private final FactionRankLevel owner = new FactionRankLevel(this, "Owner", 0);
	private final FactionRankLevel unranked = new FactionRankLevel(this, "Unranked", 1);
	protected final Map<UUID, FactionRankLevel> player_to_rank = Maps.newHashMap();
	protected final Map<UUID, FactionMember> memebers = Maps.newHashMap();
	private final List<FactionRankLevel> ranks = Lists.newArrayList();
	private final List<Faction> allies = Lists.newArrayList();
	private final List<Faction> enemies = Lists.newArrayList();
	private final CapabilityDispatcher capabilities;
	private FactionMember owner_info;
	private String name;

	protected Faction(FactionManager container, UUID owner, String name)
	{
		this(container, UUID.randomUUID());
		this.name = name;
		this.owner_info = new FactionMember(owner);
		this.owner.addPlayers(Collections.singleton(this.owner_info));
	}

	public Faction(FactionManager container, UUID uuid)
	{
		this.uuid = uuid;
		this.container = container;
		AttachCapabilitiesEvent<Faction> event = new AttachCapabilitiesEvent(Faction.class, this);
		MinecraftForge.EVENT_BUS.post(event);
		Map<ResourceLocation, ICapabilityProvider> capabilities = event.getCapabilities();
		this.capabilities = 0 < capabilities.size() ? new CapabilityDispatcher(capabilities, null) : null;
	}

	public UUID getUUID()
	{
		return this.uuid;
	}

	public String getName()
	{
		return this.name;
	}

	public FactionMember getOwnerInfo()
	{
		return this.owner_info;
	}

	public Map<UUID, FactionMember> getMembers()
	{
		return this.memebers;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return this.capabilities != null && this.capabilities.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		return this.capabilities == null ? null : this.capabilities.getCapability(capability, facing);
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("name", this.name);
		compound.setTag("owner", this.serializeOwner());
		compound.setTag("players", this.serializePlayers());
		compound.setTag("ranks", this.serializeRanks());
		compound.setTag("unranked", this.unranked.serializeNBT());
		container.needs_saving = true;
		return compound;
	}

	private NBTTagCompound serializeOwner()
	{
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("player", this.owner_info.serializeNBT());
		return compound;
	}

	private NBTTagList serializePlayers()
	{
		NBTTagList list = new NBTTagList();
		for (FactionMember player : this.memebers.values())
			list.appendTag(player.serializeNBT());
		return list;
	}

	private NBTTagList serializeRanks()
	{
		NBTTagList list = new NBTTagList();
		for (FactionRankLevel rank : this.ranks)
			if (rank != this.owner && rank != this.unranked)
				list.appendTag(rank.serializeNBT());
		return list;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound)
	{
		this.clear();
		this.name = compound.getString("name");
		this.deserializeOwner(compound.getCompoundTag("owner"));
		this.deserializePlayers(compound.getTagList("players", 10));
		this.deserializeRanks(compound.getTagList("ranks", 10));
		this.unranked.deserializeNBT(compound.getCompoundTag("unranked"));
		for (Entry<UUID, FactionRankLevel> entry : this.player_to_rank.entrySet())
			if (entry.getValue() != this.owner)
				this.memebers.get(entry.getKey()).setRank(entry.getValue());

	}

	private void clear()
	{
		this.memebers.clear();
		this.ranks.clear();
		this.owner.clear();
		this.unranked.clear();
	}

	private void deserializeOwner(NBTTagCompound compound)
	{
		this.owner_info = FactionMember.fromNBT(this, compound.getCompoundTag("player"));
		this.owner.addPlayers(Collections.singleton(this.owner_info));
		this.owner_info.setRank(this.owner);
		this.ranks.add(this.owner);
	}

	private void deserializePlayers(NBTTagList list)
	{
		for (int i = 0; i < list.tagCount(); i++)
		{
			FactionMember player = FactionMember.fromNBT(this, list.getCompoundTagAt(i));
			this.memebers.put(player.getUUID(), player);
		}
	}

	private void deserializeRanks(NBTTagList list)
	{
		for (int i = 0; i < list.tagCount(); i++)
			this.ranks.add(FactionRankLevel.fromNBT(this, this.ranks.size(), list.getCompoundTagAt(i)));
	}
}
