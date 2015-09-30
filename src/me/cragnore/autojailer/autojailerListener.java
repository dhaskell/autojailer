package me.cragnore.autojailer;

import java.util.Set;

import me.cragnore.autojailer.autojailerListener;
import me.cragnore.autojailer.main;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class autojailerListener implements Listener {
	// Create a reference to the main class
	public static main plugin;

	public autojailerListener(main plugin) {
		autojailerListener.plugin = plugin;
	}

	@EventHandler
	public void jailListen(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		if (!p.isOp() && (boolean) plugin.getConfig().get("enabled")) {
			Set<String> members = plugin.getConfig()
					.getConfigurationSection("members").getKeys(false);
			
			if (!plugin.contains(members, p.getUniqueId().toString())) {
				plugin.sendToJail(p);

			} else {

				Boolean jailed = (Boolean) plugin.getConfig().get(
						"members." + p.getUniqueId() + ".inJail");

				if (jailed) {

					plugin.sendToJail(p);

				}
			}
		}
	}
}
