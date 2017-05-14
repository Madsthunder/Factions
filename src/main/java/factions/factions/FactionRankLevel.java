package factions.factions;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

public class FactionRankLevel implements INBTSerializable<NBTTagCompound>, Comparable<FactionRankLevel>
{
	private final Faction faction;
	private final int level;
	private final List<FactionMember> players = Lists.newArrayList();
	private String name;
	
	private FactionRankLevel(Faction faction, int level)
	{
		this.faction = faction;
		this.level = level;
		this.name = this.getDefaultName();
	}
	
	protected FactionRankLevel(Faction faction, String name, int level)
	{
		this.faction = faction;
		this.level = level;
		this.name = this.getDefaultName();
	}
	
	protected void addPlayers(Iterable<FactionMember> members)
	{
		for(FactionMember player : members)
		{
			this.players.add(player);
			this.faction.player_to_rank.put(player.getUUID(), this);
		}
	}
	
	protected void clear()
	{
		this.players.clear();
	}
	
	@Override
	public int compareTo(FactionRankLevel rank)
	{
		return this.level - rank.level;
	}
	
	public String getDefaultName()
	{
		return "Rank " + (this.level + 1);
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("name", this.name);
		NBTTagList players = new NBTTagList();
		for(FactionMember player : this.players)
		{
			NBTTagCompound uuid = new NBTTagCompound();
			uuid.setUniqueId("", player.getUUID());
			players.appendTag(uuid);
		}
		compound.setTag("players", players);
		return compound;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound compound)
	{
		this.name = compound.getString("name");
		this.players.clear();
		NBTTagList players = compound.getTagList("players", 10);
		for(int i = 0; i < players.tagCount(); i++)
		{
			UUID uuid = players.getCompoundTagAt(i).getUniqueId("");
			if(this.faction.memebers.containsKey(uuid))
			{
				FactionMember player = this.faction.memebers.get(uuid);
				if(this.faction.player_to_rank.containsKey(uuid))
				{
					FactionRankLevel rank = this.faction.player_to_rank.get(uuid);
					if(rank.level >= this.level)
						rank.addPlayers(Collections.singleton(player));
					else
						this.addPlayers(Collections.singleton(player));
				}
				else
					this.addPlayers(Collections.singleton(player));
			}
			else
			{
				// TODO throw an error?
			}
		}
	}
	
	public Faction getFaction()
	{
		return this.faction;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	protected static FactionRankLevel fromNBT(Faction faction, int level, NBTTagCompound compound)
	{
		FactionRankLevel rank = new FactionRankLevel(faction, level);
		rank.deserializeNBT(compound);
		return rank;
	}
}