package valorless.havenrecipes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.json.JsonUtils;

public class CommandListener implements CommandExecutor {
	
	public static JavaPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Log.Debug(plugin, "Sender: " + sender.getName());
    	Log.Debug(plugin, "Command: " + command.toString());
    	Log.Debug(plugin, "Label: " + label);
    	for(String a : args) {
    		Log.Debug(plugin, "Argument: " + a);
    	}
    	if(args.length >= 1) {
    		if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("havenrecipes.export")) { 
    			Main.config.Reload(); 
    			Lang.lang.Reload(); 
    			Crafting.RemoveRecipes();
				Crafting.PrepareRecipes();
    			sender.sendMessage(Main.Name + " §aReloaded");
    			return true;
    		}
    		if (sender instanceof Player) {
    	    	if(args[0].equalsIgnoreCase("export") && sender.hasPermission("havenrecipes.export")) { 
    	    		Player player = (Player)sender;
    	    		ItemStack item = player.getInventory().getItemInMainHand();
    	    		if(item == null) return false;
    	    		ExportItem(item, player);
    	    	}
            	return true;
        	}
    	}
        return false;
        //if(!Main.config.GetBool("enabled")) return false;
    }
    
    void ExportItem(ItemStack item, Player player) {
    	Log.Info(plugin, "Attempting to export item: " + item.toString());
    	    	
    	File export;
    	export = new File(plugin.getDataFolder() + "/exports/", String.format("%s-%s.yml", player.getName(), item.hashCode()));
    	
    	if(!export.exists()) {
    		export.getParentFile().mkdirs();
        }
    	
    	Path path = Paths.get(String.format("%s/exports/%s-%s.yml", plugin.getDataFolder(), player.getName(), item.hashCode()));
    	String json = JsonUtils.toJson(item);
    	List<String> lines = Arrays.asList(json.replace("'", "◊"));
    	try {
    		Files.write(path, lines, StandardCharsets.UTF_8);
        	Log.Info(plugin, String.format("Item has been exported to '/havenrecipes/exports/%s-%s.yml'", player.getName(), item.hashCode()));
        	player.sendMessage("§7[§aHaven§bRecipes§7]§r §aItem exported.");
    	}catch(IOException e){
			e.printStackTrace();
        	player.sendMessage("§7[§aHaven§bRecipes§7]§r §cExport failed.");
    	}
    	
    }
}
