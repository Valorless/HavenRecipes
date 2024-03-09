package valorless.havenrecipes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;
import valorless.valorlessutils.crafting.CraftRecipe;
import valorless.valorlessutils.crafting.CraftRecipe.RecipeType;
import valorless.valorlessutils.crafting.Ingredient;
import valorless.valorlessutils.json.JsonUtils;
import valorless.valorlessutils.nbt.NBT;

public class Crafting implements Listener {

	public static List<NamespacedKey> Recipes = new ArrayList<NamespacedKey>();
	public static List<CraftRecipe> CraftRecipes = new ArrayList<CraftRecipe>();
	
	public static void PrepareRecipes() {
		if(Main.config.GetBool("enabled") == false) return;
		
		List<String> recipes = GetRecipes();
		Log.Debug(Main.plugin, "Recipes: " + recipes.size());
		
		for(String name : recipes) {
			Config recipe = new Config(Main.plugin, String.format("recipes/%s.yml", name));
			if(recipe.GetBool("enabled") == true) {
				RecipeType type;
				if(recipe.HasKey("recipe-type")) {
					if(recipe.GetString("recipe-type").equalsIgnoreCase("SHAPED")) {
						type = RecipeType.Shaped;
					}
					else if(recipe.GetString("recipe-type").equalsIgnoreCase("SHAPELESS")) {
						type = RecipeType.Shapeless;
					}
					else {
						Log.Warning(Main.plugin, "Could not add recipe '" + name + "', it does not have a valid recipe-type!");
						continue;
					}
				}else {
					type = RecipeType.Shaped;
				}
				NamespacedKey key = new NamespacedKey(Main.plugin, name);
				ItemStack result = PrepareResult(name, recipe);
				
				ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
				List<String> shape = recipe.GetStringList("shape");
				for(String s : shape) { s = s.replace("X", " "); }
				shapedRecipe.shape(shape.get(0), shape.get(1), shape.get(2));
					
				List<Ingredient> ingredients = PrepareIngredients(recipe);
				
				CraftRecipe r = new CraftRecipe(Main.plugin, name, type, ingredients, result, shape);
				
				if(recipe.HasKey("permission")) {
					Permission perm = new Permission(recipe.GetString("permission"));					
					r.SetPermission(perm);
				}

				Recipes.add(key);
				CraftRecipes.add(r);
				r.Add();
				//Log.Info(Main.plugin, String.format("Recipe '%s' added.", key.toString()));
			}
		}
	}
	
	public static List<String> GetRecipes(){
		try {
			List<String> recipes = Stream.of(new File(String.format("%s/recipes/", Main.plugin.getDataFolder())).listFiles())
					.filter(file -> !file.isDirectory())
					.map(File::getName)
					.collect(Collectors.toList());
			for(int i = 0; i < recipes.size(); i++) {
				recipes.set(i, recipes.get(i).replace(".yml", ""));
			}
			return recipes;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
	
	@SuppressWarnings("deprecation")
	static ItemStack PrepareResult(String name, Config recipe) {
		try {
			ItemStack item = new ItemStack(Material.AIR);
		
		if(recipe.HasKey("custom-item")) {
			String json = recipe.GetString("custom-item").replace("◊", "'");
			item = JsonUtils.fromJson(json);
			item.setAmount(recipe.GetInt("amount"));
			return item;
			// No need to continue, when dealing with already established items.
		}
		
		if(recipe.HasKey("material")) item.setType(recipe.GetMaterial("material"));
		
		if(recipe.HasKey("amount")) item.setAmount(recipe.GetInt("amount"));
		
		ItemMeta meta = item.getItemMeta();
		if(recipe.HasKey("displayname")) meta.setDisplayName(Lang.Parse(recipe.GetString("displayname")));
		
		if(recipe.HasKey("lore")) { 
			List<String> lore = new ArrayList<String>();
        	for (String l : recipe.GetStringList("lore")) {
        		lore.add(Lang.Parse(l));
        	}
        	meta.setLore(lore);
		}
        
		if(recipe.HasKey("enchants")) { 
			List<String> enchants = recipe.GetStringList("enchants");
        	for(String enchant : enchants) {
        		String[] e = enchant.split(":");
        		try {
        			meta.addEnchant(Enchantment.getByName(e[0]), Integer.parseInt(e[1]), true);
        		}catch(Exception ex) {
        			Log.Error(Main.plugin, String.format("Enchantment '%s' does not exist."
        				+ "\nPlease check https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html for available enchantments.", e[0]));
        		}
        	}
		}
        

		if(recipe.HasKey("attributes")) { 
			List<String> attributes = recipe.GetStringList("attributes");
        	for(String attri : attributes) {
        		String[] a = attri.split(":");
        		try {
        		meta.addAttributeModifier(Attribute.valueOf(a[0]), GetModifier(Attribute.valueOf(a[0]), a[1], Double.valueOf(a[2])));
        		}catch(Exception ex) {
        			Log.Error(Main.plugin, String.format("Attribute '%s' does not exist."
        				+ "\nPlease check https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html for available attributes.", a[0]));
        		}
        	}
		}
        
		if(recipe.HasKey("custom-model-data")) meta.setCustomModelData(recipe.GetInt("custom-model-data"));
		
		item.setItemMeta(meta);
		
		//NBT
		if(recipe.GetConfigurationSection("nbt") != null) {
			Object[] tags = recipe.GetConfigurationSection("nbt").getKeys(false).toArray();
			for(Object tag : tags) {
				Log.Debug(Main.plugin, String.valueOf(tag));
				String type = recipe.GetString("nbt." + tag + ".type");
				String value = recipe.GetString("nbt." + tag + ".value");
				SetTag(item, String.valueOf(tag), type, value);
			}
		}
		//Log.Debug(Main.plugin, item.toString());
		return item;
		}catch(Exception e) {
			Log.Error(Main.plugin, String.format("Failed to load recipe '%s'.", name));
			e.printStackTrace();
		}
		return new ItemStack(Material.DIRT);
		
	}
	
	static List<Ingredient> PrepareIngredients(Config recipe) {
		List<Ingredient> ingredients = new ArrayList<Ingredient>();
		Object[] ings = recipe.GetConfigurationSection("ingredients").getKeys(false).toArray();
		for(Object ingredient : ings) {
			String letter = String.valueOf(ingredient);
			Material material = Material.getMaterial(recipe.GetString("ingredients." + ingredient + ".material"));			
			ingredients.add(new Ingredient(letter, material));
		}
		return ingredients;
	}
	
	@SuppressWarnings("unused")
	public static void RemoveRecipes() {
		if(Recipes.size() != 0) {
    		for(NamespacedKey recipe : Recipes) {
    			//Bukkit.removeRecipe(recipe);
    		}
    		for(CraftRecipe recipe : CraftRecipes) {
    			recipe.Remove();
    		}
    		Recipes.clear();
    	}
	}
	
    public static AttributeModifier GetModifier(Attribute attribute, String slot, double amount) {
    	String type = attribute.toString().replace("_", ".").toLowerCase();
    	return new AttributeModifier(UUID.randomUUID(), type, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.valueOf(slot));
    }
    
    public static void SetTag(ItemStack item, String name, String type, String value) {
    	if(type.equalsIgnoreCase("string")) {
    		try {
    			NBT.SetString(item, name, value);
    		}catch(Exception e) {
    			Log.Error(Main.plugin, String.format("String of NBT tag '%s' is invalid! (%s)", name, value));
    		}
    	}
    	else if(type.equalsIgnoreCase("boolean")) {
    		try {
    			NBT.SetBool(item, name, Boolean.valueOf(value));
			}catch(Exception e) {
				Log.Error(Main.plugin, String.format("Boolean of NBT tag '%s' is invalid! (%s)", name, value));
			}
    	}
    	else if(type.equalsIgnoreCase("integer")) {
    		try {
    			NBT.SetInt(item, name, Integer.parseInt(value));
			}catch(Exception e) {
				Log.Error(Main.plugin, String.format("Integer of NBT tag '%s' is invalid! (%s)", name, value));
			}
    	}
    	else if(type.equalsIgnoreCase("float")) {
    		try {
    			NBT.SetFloat(item, name, Float.parseFloat(value));
			}catch(Exception e) {
				Log.Error(Main.plugin, String.format("Float of NBT tag '%s' is invalid! (%s)", name, value));
			}
    	}
    	else if(type.equalsIgnoreCase("double")) {
    		try {
    			NBT.SetDouble(item, name, Double.valueOf(value));
			}catch(Exception e) {
				Log.Error(Main.plugin, String.format("Double of NBT tag '%s' is invalid! (%s)", name, value));
			}
    	}
    	else if(type.equalsIgnoreCase("uuid")) {
    		try {
    			NBT.SetUUID(item, name, UUID.fromString(value));
    		}catch(Exception e) {
    			Log.Error(Main.plugin, String.format("UUID of NBT tag '%s' is invalid! (%s)", name, value));
    		}
    	}
    	else {
    		Log.Error(Main.plugin, String.format("Could not set NBT tag '%s' (type: %s). Is the type correct?", name, type));
    	}
    }
	
	@EventHandler
	public void onCraftItem (CraftItemEvent event) {
		Log.Debug(Main.plugin, event.getInventory().getType().toString());
		Log.Debug(Main.plugin, event.getRecipe().toString());
	}
}
