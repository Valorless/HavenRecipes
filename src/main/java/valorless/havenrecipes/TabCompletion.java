package valorless.havenrecipes;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

public class TabCompletion implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			List<String> subCommands = new ArrayList<>();
			if (sender.hasPermission("havenrecipe.export")) {
				subCommands.add("export");
			}
			if (sender.hasPermission("havenrecipe.reload")) {
				subCommands.add("reload");
			}

			StringUtil.copyPartialMatches(args[0], subCommands, completions);
		}
		return completions;
	}
}