package factions.factions;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class FactionMember implements INBTSerializable<NBTTagCompound>
{
	private final UUID uuid;
	private FactionRankLevel rank;
	
	public FactionMember(UUID player)
	{
		this.uuid = player;
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound compound = new NBTTagCompound();
		compound.setUniqueId("uuid", this.uuid);
		return compound;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound compound)
	{
		// TODO put UUID check here
	}
	
	public FactionRankLevel getRank()
	{
		return this.rank;
	}
	
	protected void setRank(FactionRankLevel rank)
	{
		this.rank = rank;
	}
	
	public UUID getUUID()
	{
		return this.uuid;
	}
	
	public static FactionMember fromNBT(Faction faction, NBTTagCompound compound)
	{
		UUID uuid = compound.getUniqueId("uuid");
		return new FactionMember(uuid);
	}
}