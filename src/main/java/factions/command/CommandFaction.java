package factions.command;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import factions.factions.Faction;
import factions.factions.FactionManager;
import factions.factions.FactionMember;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandFaction extends CommandBase
{
	private final FactionManager manager;

	public CommandFaction(FactionManager container)
	{
		this.manager = container;
	}

	@Override
	public String getName()
	{
		return "faction";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "//TODO add usage";
	}

	@Override
	public List<String> getAliases()
	{
		return Lists.newArrayList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length == 0)
			throw new CommandException(this.getUsage(sender));
		if (args.length == 1)
		{
			if ("list".equals(args[0]))
				for (Faction faction : this.manager.getFactions())
					sender.sendMessage(new TextComponentString(faction.getName()));
			if ("disband".equals(args[0]))
			{
				Entity entity = sender.getCommandSenderEntity();
				if (entity instanceof EntityPlayer)
				{
					Faction faction = this.manager.disbandFaction(((EntityPlayer) entity).getGameProfile().getId());
					sender.sendMessage(new TextComponentString("Succesfully disbanded \"" + faction.getName() + "\"."));
					FactionMember owner = faction.getOwnerInfo();
					for (UUID uuid : faction.getMembers().keySet())
					{

					}

				} else
					throw new CommandException("You must be a player to disband a faction");
			}

		} else if (args.length == 2)
		{
			if ("create".equals(args[0]))
			{
				Entity entity = sender.getCommandSenderEntity();
				if (entity instanceof EntityPlayer)
				{
					this.manager.createFaction(((EntityPlayer) entity).getGameProfile().getId(), args[1]);
				} else
					throw new CommandException("You must be a player to create a faction");
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		List<String> completions = Lists.newArrayList();
		if (args.length == 1)
		{
			if ("create".startsWith(args[0]))
				completions.add("create");
			if ("list".startsWith(args[0]))
				completions.add("list");
			if ("disband".startsWith(args[0]))
				completions.add("disband");
		}
		return completions;
	}

}
