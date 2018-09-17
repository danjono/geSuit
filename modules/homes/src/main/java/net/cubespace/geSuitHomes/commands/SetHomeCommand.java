package net.cubespace.geSuitHomes.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitHomes.managers.HomesManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetHomeCommand extends CommandManager<HomesManager> {

	public SetHomeCommand(HomesManager manager) {
		super(manager);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(args.length==0){
			manager.setHome(sender, "home");
		}else{
			// \u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00df corresponds to German characters äöüßÄÖÜ
			Pattern invalidChar = Pattern.compile("[^a-zA-Z0-9_\\u00E4\\u00F6\\u00FC\\u00C4\\u00D6\\u00DC\\u00df-]");
			Matcher reMatch = invalidChar.matcher(args[0]);

			StringBuffer bufstr = new StringBuffer();
			boolean flag = false;
            int count = 0;

			while (flag = reMatch.find()) {
				String rep = reMatch.group();
				reMatch.appendReplacement(bufstr, ChatColor.YELLOW + rep.replace("$","\\$") + ChatColor.RED);
                count++;
			}
			reMatch.appendTail(bufstr);

			if (count>0) {
                String result = (count) + " invalid character" + (count>1?"s":"") + " in new home name: " + bufstr.toString();
				sender.sendMessage(ChatColor.RED + result);
				return true;
			}

			manager.setHome(sender, args[0]);
		}
		return true;
	}

}
