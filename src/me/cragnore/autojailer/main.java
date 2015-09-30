package me.cragnore.autojailer;

import java.io.File;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import me.cragnore.autojailer.autojailerListener;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {

	public final Logger logger = Logger.getLogger("Minecraft");

	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info("[" + pdfFile.getName() + "] v."
				+ pdfFile.getVersion() + " has been Enabled");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new autojailerListener(this), this);
		this.folderVerify();

		getCommand("setJailMessage").setExecutor(this);
		getCommand("setfreedomMessage").setExecutor(this);
		getCommand("setFreedomLocation").setExecutor(this);
		getCommand("setvouchThreshold").setExecutor(this);
		getCommand("vouch").setExecutor(this);
		getCommand("free").setExecutor(this);
		getCommand("jailertoggle").setExecutor(this);
	}

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " has been Disabled");
	}

	public void folderVerify() {

		String configPath = this.getDataFolder() + "/config.yml";
		File file = new File(configPath);

		if (!file.exists()) {
			PluginDescriptionFile pdfFile = this.getDescription();
			this.logger.info("[" + pdfFile.getName() + "] v."
					+ pdfFile.getVersion() + " Created config.yml file.");
			this.getConfig().addDefault("enabled", true);
			this.getConfig().addDefault("vouchThreshold", 1);
			this.getConfig().addDefault("jailMessage",
					"Stop right there, criminal scum!");
			this.getConfig().addDefault("freedomMessage",
					"Don't let me catch you stealing anymore sweetrolls.");
			this.getConfig().addDefault("freedomLocation.world", "world");
			this.getConfig().addDefault("freedomLocation.x",
					Bukkit.getWorld("world").getSpawnLocation().getX());
			this.getConfig().addDefault("freedomLocation.y",
					Bukkit.getWorld("world").getSpawnLocation().getY());
			this.getConfig().addDefault("freedomLocation.z",
					Bukkit.getWorld("world").getSpawnLocation().getZ());
			this.getConfig().addDefault("members.placeholder.inJail", true);
			this.getConfig().options().copyDefaults(true);
			this.saveConfig();
		}
	}

	public FileConfiguration getJail() {
		String jailPath = "plugins/Essentials/jail.yml";
		File jailFile = new File(jailPath);
		FileConfiguration customConfig = YamlConfiguration
				.loadConfiguration(jailFile);
		return customConfig;
	}

	public String randomJailName() {
		String result = null;
		Set<String> jails = this.getJail().getConfigurationSection("jails")
				.getKeys(false);
		int size = jails.size();
		int item = new Random().nextInt(size);
		int i = 0;

		for (String name : jails) {
			if (i == item)
				result = name;
			i = i + 1;
		}

		return result;
	}

	public Location jailLocation(String jailName) {
		String world = (String) this.getJail().get(
				"jails." + jailName + ".world");
		double x = (double) this.getJail().get("jails." + jailName + ".x");
		double y = (double) this.getJail().get("jails." + jailName + ".y");
		double z = (double) this.getJail().get("jails." + jailName + ".z");
		Location result = new Location(Bukkit.getWorld(world), x, y, z);
		return result;
	}

	public void sendToJail(Player p) {

		String jailName = this.randomJailName();

		p.teleport(this.jailLocation(jailName));
		p.setBedSpawnLocation(this.jailLocation(jailName));
		p.sendMessage(ChatColor.RED
				+ (String) this.getConfig().get("jailMessage"));
		p.setGameMode(GameMode.ADVENTURE);

		this.getConfig().set("members." + p.getUniqueId() + ".displayName",
				p.getDisplayName());
		this.getConfig().set("members." + p.getUniqueId() + ".inJail", true);
		this.getConfig().set("members." + p.getUniqueId() + ".jailName",
				jailName);
		this.saveConfig();
	}

	public void vouch(Player voucher, String playerName) {

		Player vouchee = Bukkit.getServer().getPlayer(playerName);

		if (vouchee != null) {

			if (voucher.getUniqueId() == vouchee.getUniqueId()) {
				voucher.sendMessage(ChatColor.RED
						+ "You can't vouch for yourself!");
			} else {
				
				if((boolean) this.getConfig().get("members."+voucher.getUniqueId()+".inJail"))
				{
					voucher.sendMessage(ChatColor.RED+"Only free players can vouch for others!");
				}
				else
				{
					String uuid = vouchee.getUniqueId().toString();
					this.getConfig()
							.set("members." + uuid + ".vouches."
									+ voucher.getUniqueId(), voucher.getName());
					this.saveConfig();

					Set<String> vouchers = this
							.getConfig()
							.getConfigurationSection("members." + uuid + ".vouches")
							.getKeys(false);
					int vouchCount = vouchers.size();
					int threshold = (int) this.getConfig().get("vouchThreshold");

					if (vouchCount >= threshold) {
						this.free(voucher, playerName);
					}
				}
			}
		} else {
			voucher.sendMessage(ChatColor.RED + "Could not find player "
					+ ChatColor.GOLD + playerName);
		}

	}

	public void free(Player liborator, String playerName) {

		Player prisoner = Bukkit.getServer().getPlayer(playerName);

		if (prisoner != null) {
			String uuid = prisoner.getUniqueId().toString();
			this.getConfig().set("members." + uuid + ".freedBy",
					liborator.getName());
			this.getConfig().set("members." + uuid + ".inJail", false);
			this.saveConfig();

			// Grabbing the freedom location
			String world = (String) this.getConfig().get(
					"freedomLocation.world");
			double x = (double) this.getConfig().get("freedomLocation.x");
			double y = (double) this.getConfig().get("freedomLocation.y");
			double z = (double) this.getConfig().get("freedomLocation.z");
			Location freedom = new Location(Bukkit.getWorld(world), x, y, z);
			prisoner.teleport(freedom);
			prisoner.setGameMode(Bukkit.getServer().getDefaultGameMode());
			prisoner.sendMessage(ChatColor.GREEN
					+ (String) this.getConfig().get("freedomMessage"));

		} else {
			liborator.sendMessage(ChatColor.RED + "Could not find player "
					+ ChatColor.GOLD + playerName);
		}

	}
	
	public boolean contains(Set<String> s, String item)
	{
		for(String toCompare: s) {
		    if(toCompare.equalsIgnoreCase(item)) {
		        return true;
		    }
		  }
		  return false;
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {

		if (commandLabel.equalsIgnoreCase("setJailMessage")) {
			
			if(sender.hasPermission("autojailer."+cmd.getName()))
			{
				String newMessage = StringUtils.join(args, " ");
				
				if (newMessage.length() > 0) {
					this.getConfig().set("jailMessage", newMessage);
					this.saveConfig();

					if (sender instanceof Player) {
						Player p = (Player) sender;
						p.sendMessage(ChatColor.GREEN + "Jail message set: "
								+ newMessage);
						return true;
					} else {
						this.logger.info("Jail message set: " + newMessage);
						return true;
					}
				} else {
					return false;
				}
			}
			else
			{
				sender.sendMessage("You do not have permission to do that!");
				return true;
			}
		}

		if (commandLabel.equalsIgnoreCase("setfreedomMessage")) {
			
			if(sender.hasPermission("autojailer."+cmd.getName()))
			{
				String newMessage = StringUtils.join(args, " ");

				if (newMessage.length() > 0) {
					this.getConfig().set("freedomMessage", newMessage);
					this.saveConfig();

					if (sender instanceof Player) {
						Player p = (Player) sender;
						p.sendMessage(ChatColor.GREEN + "Freedom message set: "
								+ newMessage);
						return true;
					} else {
						this.logger.info("Freedom message set: " + newMessage);
						return true;
					}
				} else {
					return false;
				}
			}
			else
			{
				sender.sendMessage("You do not have permission to do that!");
				return true;
			}
		}

		if (commandLabel.equalsIgnoreCase("setFreedomLocation")) {
			if(sender.hasPermission("autojailer."+cmd.getName()))
			{
				if (sender instanceof Player) {
					Player p = (Player) sender;
					this.getConfig().addDefault("freedomLocation.world",
							p.getLocation().getWorld().getName());
					this.getConfig().addDefault("freedomLocation.x",
							p.getLocation().getX());
					this.getConfig().addDefault("freedomLocation.y",
							p.getLocation().getY());
					this.getConfig().addDefault("freedomLocation.z",
							p.getLocation().getZ());
					this.saveConfig();
					p.sendMessage(ChatColor.GREEN + "Freedom location set!");
					return true;
				} else {
					this.logger.info("Command can only be issued in game.");
					return true;
				}
			}
			else
			{
				sender.sendMessage("You do not have permission to do that!");
				return true;
			}
		}

		if (commandLabel.equalsIgnoreCase("vouch")) {
			if(sender.hasPermission("autojailer."+cmd.getName()))
			{
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (args.length > 0) {
						this.vouch(p, args[0]);
						return true;
					} else {
						return false;
					}
				} else {
					this.logger.info("Command can only be issued in game.");
					return true;
				}
			}
			else
			{
				sender.sendMessage("You do not have permission to do that!");
				return true;
			}
		}

		if (commandLabel.equalsIgnoreCase("free")) {
			if(sender.hasPermission("autojailer."+cmd.getName()))
			{
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (args.length > 0) {
						this.free(p, args[0]);
						return true;
					} else {
						return false;
					}
				} else {
					this.logger.info("Command can only be issued in game.");
					return true;
				}
			}
			else
			{
				sender.sendMessage("You do not have permission to do that!");
			}
		}

		if (commandLabel.equalsIgnoreCase("setvouchThreshold")) {
			if(sender.hasPermission("autojailer."+cmd.getName()))
			{
				int vouchThreshold = 0;
				Boolean isInt = false;
				try {
					vouchThreshold = Integer.parseInt(args[0]);
					isInt = true;
				} catch (NumberFormatException e) {
					isInt = false;
				}

				if (isInt) {
					this.getConfig().set("vouchThreshold", vouchThreshold);
					this.saveConfig();
					if (sender instanceof Player) {
						Player p = (Player) sender;
						p.sendMessage(ChatColor.GREEN + "Vouch threshold set:"
								+ vouchThreshold);
					} else {
						this.logger.info("Vouch threshold set:" + vouchThreshold);
						return true;
					}
				} else {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						p.sendMessage(ChatColor.RED
								+ "Vouch threshold must be a number!");
					} else {
						this.logger.info("Vouch threshold must be a number!");
					}
				}
				return true;
			}
			else
			{
				sender.sendMessage("You do not have permission to do that!");
				return true;
			}
			
		}

		if (commandLabel.equalsIgnoreCase("jailertoggle")) {
			if(sender.hasPermission("autojailer."+cmd.getName()))
			{
				Boolean enabled = (Boolean) this.getConfig().get("enabled");
				this.getConfig().set("enabled", !enabled);
				this.saveConfig();
				String message = (boolean) this.getConfig().get("enabled") ? "Autojailer enabled!"
						: "Autojailer disabled!";

				if (sender instanceof Player) {
					sender.sendMessage(message);
					return true;
				} else {
					this.logger.info(message);
					return true;
				}
			}
			else
			{
				sender.sendMessage("You do not have permission to do that!");
				return true;
			}
		}

		return false;
	}
}
