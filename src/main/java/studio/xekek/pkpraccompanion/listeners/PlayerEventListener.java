package studio.xekek.pkpraccompanion.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import studio.xekek.pkpraccompanion.PKPracServer;
import studio.xekek.pkpraccompanion.managers.ConfigManager;

public class PlayerEventListener implements Listener {
    private final PKPracServer plugin;
    private final ConfigManager configManager;

    public PlayerEventListener(PKPracServer plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (configManager.getNoPracticePlayers().contains(player.getUniqueId()) ||
            configManager.getNonPracticeWorlds().contains(player.getWorld().getName())) {
            plugin.handleFloatingEntity(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.removeFloatingEntity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        plugin.removeFloatingEntity(player);
        plugin.handleFloatingEntity(player);
    }
}
