package com.sol.bot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

public class SolBot
{
	public static final String HELP_MSG = "!learn <charName> <spellId>\n" + "!unlearn <charName> <spellId>\n"
			+ "!item <charName> <itemId1> <itemId2> <itemId3> ...\n" + "!money <charName> <goldAmount>\n"
			+ "!revive <charName>\n";

	public static void main(String[] args) throws Exception
	{
		Path tokenFile = Paths.get(args[0]);

		if (!Files.isReadable(tokenFile))
		{
			System.err.println("Cannot read " + tokenFile);
			System.exit(1);
		}

		String token = Files.readAllLines(tokenFile).get(0);

		Path namedPipe = Paths.get(args[1]);

		if (!Files.isWritable(namedPipe))
		{
			System.err.println("Cannot write " + namedPipe);
			System.exit(1);
		}

		DiscordClient client = DiscordClient.create(token);
		GatewayDiscordClient gateway = client.login().block();

		gateway.on(MessageCreateEvent.class).subscribe(event ->
		{
			parseMessage(event.getMessage(), namedPipe);
		});

		gateway.onDisconnect().block();
	}

	public static void parseMessage(Message message, Path namedPipe)
	{
		String msg = message.getContent();

		if (msg == null)
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
			else if (msg.startsWith("!learn"))
			{
				Pattern pattern = Pattern.compile("^!learn (\\w+) (\\d+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".player learn " + matcher.group(1) + " " + matcher.group(2) + "\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"));
				}
			}
			else if (msg.startsWith("!unlearn"))
			{
				Pattern pattern = Pattern.compile("^!unlearn (\\w+) (\\d+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".player unlearn " + matcher.group(1) + " " + matcher.group(2) + "\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"));
				}
			}
			else if (msg.startsWith("!item"))
			{
				Pattern pattern = Pattern.compile("^!item (\\w+)(( [\\d:]+)+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".send items " + matcher.group(1) + " \"Items\" \"Items\" " + matcher.group(2) + "\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"));
				}
			}
			else if (msg.startsWith("!money"))
			{
				Pattern pattern = Pattern.compile("^!money (\\w+) (\\d+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".send money " + matcher.group(1) + " \"Gold\" \"Gold\" " + matcher.group(2) + "0000\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"));
				}
			}
			else if (msg.startsWith("!revive"))
			{
				Pattern pattern = Pattern.compile("^!revive (\\w+)$");
				Matcher matcher = pattern.matcher(msg);

				if (matcher.matches())
				{
					cmd = ".revive " + matcher.group(1) + "\n";
					Files.write(namedPipe, cmd.getBytes("UTF-8"));
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
}