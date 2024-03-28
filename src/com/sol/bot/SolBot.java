package com.sol.bot;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

public class SolBot
{
	public static final String HELP_MSG = "```\n" + "!learn <charName> <spellId>\n" + "!unlearn <charName> <spellId>\n"
			+ "!help skills\n" + "!skill <charName> <skillId> <value>\n"
			+ "!item <charName> <itemId1> <itemId2> <itemId3> ...\n" + "!money <charName> <goldAmount>\n"
			+ "!revive <charName>\n" + "!log\n" + "```\n";
	public static final String HELP_MSG_SKILLS = "alchemy (171), blacksmithing (164), cooking (185), enchanting (333), engineering (202), "
			+ "first aid (129), fishing (356), herbalism (182), inscription (773), jewelcrafting (755), leatherworking (165), mining (186), "
			+ "riding (762), skinning (393), tailoring (197)";

	public static void main(String[] args) throws Exception
	{
		if (args.length < 3)
		{
			System.out.println("Usage: " + SolBot.class.getName() + " <tokenFile> <namedPipe> <worldLog>");
			System.exit(1);
		}

		Path tokenFile = Paths.get(args[0]);

		if (!Files.isReadable(tokenFile))
		{
			System.err.println("Cannot read " + tokenFile);
			System.exit(1);
		}

		Path worldLog = Paths.get(args[2]);

		if (!Files.isReadable(worldLog))
		{
			System.err.println("Cannot read " + worldLog);
			System.exit(1);
		}

		String token = Files.readAllLines(tokenFile).get(0);
		Path namedPipe = Paths.get(args[1]);

		while (true)
		{
			DiscordClient client = DiscordClient.create(token);
			GatewayDiscordClient gateway = client.login().block();

			gateway.on(MessageCreateEvent.class).subscribe(event ->
			{
				parseMessage(event.getMessage(), namedPipe, worldLog);
			});

			gateway.onDisconnect().block();
			Thread.sleep(5000);
		}
	}

	public static void parseMessage(Message message, Path namedPipe, Path worldLog)
	{
		String msg = message.getContent();

		if (msg == null)
		{
			return;
		}

		if (!Files.isWritable(namedPipe))
		{
			return;
		}

		try
		{
			String cmd = null;

			if (msg.equals("!help"))
			{
				MessageChannel channel = message.getChannel().block();
				channel.createMessage(HELP_MSG).block();
				return;
			}
			else if (msg.equals("!help skills"))
			{
				MessageChannel channel = message.getChannel().block();
				channel.createMessage(HELP_MSG_SKILLS).block();
				return;
			}
			else if (msg.equals("!log"))
			{
				MessageChannel channel = message.getChannel().block();
				channel.createMessage(readLog(worldLog)).block();
				return;
			}
			else if (msg.startsWith("!learn"))
			{
				Pattern pattern = Pattern.compile("^!learn (\\w+) (\\d+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".player learn " + matcher.group(1) + " " + matcher.group(2) + "\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"), StandardOpenOption.WRITE);
				}
			}
			else if (msg.startsWith("!unlearn"))
			{
				Pattern pattern = Pattern.compile("^!unlearn (\\w+) (\\d+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".player unlearn " + matcher.group(1) + " " + matcher.group(2) + "\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"), StandardOpenOption.WRITE);
				}
			}
			else if (msg.startsWith("!item"))
			{
				Pattern pattern = Pattern.compile("^!item (\\w+)(( [\\d:]+)+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".send items " + matcher.group(1) + " \"Items\" \"Items\" " + matcher.group(2) + "\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"), StandardOpenOption.WRITE);
				}
			}
			else if (msg.startsWith("!money"))
			{
				Pattern pattern = Pattern.compile("^!money (\\w+) (\\d+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".send money " + matcher.group(1) + " \"Gold\" \"Gold\" " + matcher.group(2) + "0000\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"), StandardOpenOption.WRITE);
				}
			}
			else if (msg.startsWith("!revive"))
			{
				Pattern pattern = Pattern.compile("^!revive (\\w+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".revive " + matcher.group(1) + "\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"), StandardOpenOption.WRITE);
				}
			}
			else if (msg.startsWith("!skill"))
			{
				Pattern pattern = Pattern.compile("^!skill (\\w+) (\\d+) (\\d+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					int skillValue = Integer.parseInt(matcher.group(3));
					int maxSkill = ((int) Math.ceil(skillValue / 75.0)) * 75;
					cmd = ".player setskill " + matcher.group(1) + " " + matcher.group(2) + " " + skillValue + " "
							+ maxSkill + "\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"), StandardOpenOption.WRITE);
				}
			}

			if (cmd != null)
			{
				MessageChannel channel = message.getChannel().block();
				channel.createMessage("Command executed: " + cmd).block();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String readLog(Path worldLog)
	{
		if (!Files.isReadable(worldLog))
		{
			return "Error: cannot read log\n";
		}

		LinkedList<String> a = new LinkedList<String>();
		String msg = "```\n";

		try (RandomAccessFile f = new RandomAccessFile(worldLog.toFile(), "r"))
		{
			if (f.length() > 5120)
			{
				f.seek(f.length() - 5120);
			}

			String line = f.readLine();

			while (line != null)
			{
				a.add(line);
				line = f.readLine();
			}
		}
		catch (Exception e)
		{
			return "Error: " + e.getMessage() + "\n";
		}

		if (a.size() == 0)
		{
			return "Error: log empty\n";
		}

		while (a.size() > 10)
		{
			a.remove(0);
		}

		for (String s : a)
		{
			msg += s + "\n";
		}

		return msg + "```\n";
	}
}
