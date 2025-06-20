/*
 * PKPrac Companion - Control who can use PKPrac on your server.
 * Copyright (C) 2025 xeepy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
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
