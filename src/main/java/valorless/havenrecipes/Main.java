package valorless.havenrecipes;

import valorless.havenrecipes.hooks.PlaceholderAPIHook;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
	public static JavaPlugin plugin;
	//public static ItemMerge merger;
	public static String Name = "§7[§aHaven§bRecipes§7]§r";
	public static Config config;
	Boolean uptodate = true;
	int newupdate = 9999999;
	String newVersion = null;
    
    public List<String> commands = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{ 
		add("havenrecipes"); 
		}
	};
	
	public void onLoad() {
		plugin = this;
		
		config = new Config(this, "config.yml");
		
		Lang.lang = new Config(this, "lang.yml");
		
		CommandListener.plugin = this;
	}
	
	@SuppressWarnings("unused")
	boolean ValorlessUtils() {
		Log.Debug(plugin, "Checking ValorlessUtils");
		
		int requiresBuild = 173;
		
		String ver = Bukkit.getPluginManager().getPlugin("ValorlessUtils").getDescription().getVersion();
		//Log.Debug(plugin, ver);
		String[] split = ver.split("[.]");
		int major = Integer.valueOf(split[0]);
		int minor = Integer.valueOf(split[1]);
		int hotfix = Integer.valueOf(split[2]);
		int build = Integer.valueOf(split[3]);
		
		if(build < requiresBuild) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        		public void run() {
        			Log.Error(plugin, String.format("HavenRecipes requires ValorlessUtils build %s or newer, found %s. (%s)", requiresBuild, build, ver));
        			Log.Error(plugin, "https://www.spigotmc.org/resources/valorlessutils.109586/");
        			Bukkit.getPluginManager().disablePlugin(plugin);
        		}
    		}, 10);
			return false;
		}
		else return true;
	}
	
	@Override
    public void onEnable() {
		Log.Debug(plugin, "HavenRecipes Debugging Enabled!");
		
		// Check if a correct version of ValorlessUtils is in use, otherwise don't run the rest of the code.
		if(!ValorlessUtils()) return;
		
		PlaceholderAPIHook.Hook();
		
		//Config
		config.AddValidationEntry("debug", "false");
		config.AddValidationEntry("enabled", "true");
		config.AddValidationEntry("check-updates", "true");
		Log.Debug(plugin, "Validating config.yml");
		config.Validate();
		
		//Lang
		Lang.lang.AddValidationEntry("prefix", "&7[&aHaven&bRecipes&7]&r");
		Log.Debug(plugin, "Validating lang.yml");
		Lang.lang.Validate();
				
		RegisterCommands();
		
        ConfigVersion();
		
		if(config.GetBool("check-updates") == true) {
			Log.Info(plugin, "Checking for updates..");
			new UpdateChecker(this, 114908).getVersion(version -> {

				newVersion = version;
				String update = version.replace(".", "");
				newupdate = Integer.parseInt(update);
				String current = getDescription().getVersion().replace(".", "");;
				int v = Integer.parseInt(current);
				

				//if (!getDescription().getVersion().equals(version)) {
				if (v < newupdate) {
						Log.Warning(plugin, String.format("An update has been found! (v%s, you are on v%s) \n", version, getDescription().getVersion()) + 
							"This could be bug fixes or additional features.\n" + 
							"Please update HavenRecipes at https://www.spigotmc.org/resources/114908/");
					
					uptodate = false;
				}else {
					Log.Info(plugin, "Up to date.");
				}
			});
		}
		
		// All you have to do is adding the following two lines in your onEnable method.
        // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 20905; // <-- Replace with the id of your plugin!
        @SuppressWarnings("unused")
		Metrics metrics = new Metrics(this, pluginId);
		
		Log.Debug(plugin, "Registering Crafting");
		getServer().getPluginManager().registerEvents(new Crafting(), this);
		Crafting.PrepareRecipes();
		
		Bukkit.getPluginManager().registerEvents(this, this);
    }
    
    @Override
    public void onDisable() {
    	Crafting.RemoveRecipes();
    }
    
    public void RegisterCommands() {

		for(String cmd : config.GetStringList("commands")) {
			commands.add(cmd);
		}
    	for (int i = 0; i < commands.size(); i++) {
    		Log.Debug(plugin, "Registering Command: " + commands.get(i));
    		getCommand(commands.get(i)).setExecutor(new CommandListener());
    		getCommand(commands.get(i)).setTabCompleter(new TabCompletion());
    	}
    }
    
    @SuppressWarnings("unused")
	void ConfigVersion() {
    	if(config.GetInt("config-version") < 1) {
    		Log.Debug(plugin, "Creating templates.");
    		config.Set("config-version", 1);
    		config.SaveConfig();
    		
    		File file = new File(String.format("%s/recipes", plugin.getDataFolder()));
    		String[] directories = file.list(new FilenameFilter() {
    		  @Override
    		  public boolean accept(File current, String name) {
    		    return new File(current, name).isDirectory();
    		  }
    		});
			
			Config template1 = new Config(plugin, "recipes/template-simple.yml");
			Config template2 = new Config(plugin, "recipes/template-advanced.yml");
    	}
    }
}
