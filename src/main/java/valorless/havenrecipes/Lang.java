package valorless.havenrecipes;

import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.utils.Utils;
import valorless.havenrecipes.hooks.PlaceholderAPIHook;
import valorless.valorlessutils.ValorlessUtils.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.PlaceholderAPI;

public class Lang {
	public static Config lang;
	public static Placeholders placeholders;
	
	public static void SetPlaceholders(Placeholders p) {
		placeholders = p;
	}
	
	public static String Parse(String text, OfflinePlayer... player) {
		if(!Utils.IsStringNullOrEmpty(text)) {
			text = text.replace("%prefix%", lang.GetString("prefix"));
			if(placeholders != null) {
				text = text.replace("%player%", placeholders.player.getName());
			}
			text = text.replace("&", "§");
			text = text.replace("\\n", "\n");
		}
		if(player.length != 0) {
			text = (ParsePlaceholders(text, player[0]));
		}
		return hex(text);
	}
	
	public static String Get(String key, OfflinePlayer... player) {
		if(lang.Get(key) == null) {
			Log.Error(Main.plugin, String.format("Messages.yml is missing the key '%s'!", key));
			return "§4error";
		}
		if(player.length != 0) {
			return Parse(lang.GetString(key), player[0]);
		}else {
			OfflinePlayer[] offp = Bukkit.getOfflinePlayers();
			// Choose random player as placeholder to parse strings, without a defined player.
			return Parse(lang.GetString(key), offp[0]);
		}
	}
	
	public static String ParsePlaceholders(String text, OfflinePlayer player) {
		if(PlaceholderAPIHook.isHooked()) {
			return PlaceholderAPI.setPlaceholders(player, text);
		}else {
			return text;
		}
	}
	
	public static String hex(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');
           
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }
           
            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
