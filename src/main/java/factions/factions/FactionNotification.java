package factions.factions;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.INBTSerializable;

public final class FactionNotification implements INBTSerializable<NBTTagCompound>
{
	private final UUID uuid;
	private final Set<UUID> to_notify;
	private ITextComponent text;
	
	protected FactionNotification(UUID uuid)
	{
		this.uuid = uuid;
		this.to_notify = Sets.newHashSet();
	}
	
	public FactionNotification(ITextComponent text, Collection<UUID> to_notify)
	{
		this(UUID.randomUUID(), text, to_notify);
	}
	
	private FactionNotification(UUID uuid, ITextComponent text, Collection<UUID> to_notify)
	{
		this.uuid = uuid;
		this.text = text;
		this.to_notify = Sets.newHashSet(to_notify);
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("text", ITextComponent.Serializer.componentToJson(this.text));
		NBTTagList to_notify = new NBTTagList();
		for(UUID uuid : this.to_notify)
		{
			NBTTagCompound compound1 = new NBTTagCompound();
			compound1.setUniqueId("", uuid);
			to_notify.appendTag(compound1);
		}
		compound.setTag("to_notify", to_notify);
		return compound;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound compound)
	{
		this.to_notify.clear();
		this.text = ITextComponent.Serializer.jsonToComponent(compound.getString("text"));
		NBTTagList to_notify = compound.getTagList("to_notify", 10);
		for(int i = 0; i < to_notify.tagCount(); i++)
			this.to_notify.add(to_notify.getCompoundTagAt(i).getUniqueId(""));
	}
	
	public UUID getUUID()
	{
		return this.uuid;
	}
	
	public Set<UUID> getProfilesToNotify()
	{
		return Sets.newHashSet();
	}
	
	public ITextComponent getText()
	{
		return this.text.createCopy();
	}
	
	public FactionNotification getWithNewUUID()
	{
		return new FactionNotification(this.text, this.to_notify);
	}
}
