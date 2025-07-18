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
package studio.xekek.pkpraccompanion;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import studio.xekek.pkpraccompanion.listeners.PlayerEventListener;
import studio.xekek.pkpraccompanion.managers.CommandManager;
import studio.xekek.pkpraccompanion.managers.ConfigManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public abstract class PKPracServer extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private final File armorStandFile = new File(getDataFolder(), "armor_stands.txt");
    private final Map<String, Location> armorStandLocations = new HashMap<>();

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this, configManager), this);
        getCommand("pkprac").setExecutor(new CommandManager(this, configManager));
        loadArmorStandLocations();
        getLogger().info("PKPracServer has been enabled.");
    }

    @Override
    public void onDisable() {
        saveArmorStandLocations();
        cleanupArmorStands();
        configManager.saveConfig();
        getLogger().info("PKPracServer has been disabled.");
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getInventory().getItemInHand().getType() == Material.STICK) {
            ItemMeta meta = player.getInventory().getItemInHand().getItemMeta();
            if (meta != null && "NoPractice Wand".equals(meta.getDisplayName())) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
                    event.setCancelled(true);

                    if (!player.hasPermission("pkprac.admin")) {
                        return;
                    }

                    try {
                        ArmorStand marker = player.getWorld().spawn(event.getClickedBlock().getLocation().add(0.5, 1, 0.5), ArmorStand.class);
                        marker.setVisible(false);
                        marker.setGravity(false);
                        marker.setMarker(true);
                        marker.setCustomName("NoPractice");
                        marker.setCustomNameVisible(false);

                        player.sendMessage("NoPractice marker placed successfully!");

                        new BukkitRunnable() {
                            int count = 0;

                            @Override
                            public void run() {
                                if (count >= 5) {
                                    cancel();
                                    return;
                                }
                                player.getWorld().playEffect(marker.getLocation(), Effect.HEART, 3 );
                                count++;
                            }
                        }.runTaskTimer(this, 0, 10);
                    } catch (Exception e) {
                        player.sendMessage("Failed to place NoPractice marker: " + e.getMessage());
                    }
                }
            }
        }
    }


    @EventHandler
    public void OnEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            if (armorStand.getCustomName() != null && armorStand.getCustomName().startsWith("NoPractice")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            if (armorStand.getCustomName() != null && armorStand.getCustomName().startsWith("NoPractice")) {
                event.setCancelled(true);
            }
        }
    }

    public void handleFloatingEntity(Player player) {
        removeFloatingEntity(player);
        if (configManager.getNoPracticePlayers().contains(player.getUniqueId()) ||
            configManager.getNonPracticeWorlds().contains(player.getWorld().getName())) {
            Location spawnLocation = player.getLocation().add(0, 2, 0);
            ArmorStand floatingEntity = player.getWorld().spawn(spawnLocation, ArmorStand.class);
            floatingEntity.setVisible(false);
            floatingEntity.setCustomName("NoPractice[" + player.getUniqueId() + "]");
            floatingEntity.setCustomNameVisible(false);
            floatingEntity.setGravity(false);
            floatingEntity.setMarker(true);

            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !player.getWorld().getName().equals(floatingEntity.getWorld().getName())) {
                        floatingEntity.remove();
                        cancel();
                        return;
                    }
                    Location headLocation = player.getLocation().add(0, 2, 0);
                    floatingEntity.teleport(headLocation);
                }
            }.runTaskTimer(this, 0, 10);

            configManager.getFloatingEntityTasks().put(player, task);
        }
    }

    public void removeFloatingEntity(Player player) {
        BukkitTask task = configManager.getFloatingEntityTasks().remove(player);
        if (task != null) {
            task.cancel();
        }
        for (ArmorStand stand : player.getWorld().getEntitiesByClass(ArmorStand.class)) {
            if (stand.getCustomName() != null && stand.getCustomName().equals("NoPractice[" + player.getUniqueId() + "]")) {
                stand.remove();
            }
        }

    }

    private void saveArmorStandLocations() {
        try (FileWriter writer = new FileWriter(armorStandFile)) {
            for (Map.Entry<String, Location> entry : armorStandLocations.entrySet()) {
                Location loc = entry.getValue();
                writer.write(entry.getKey() + "," + loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "\n");
            }
        } catch (IOException e) {
            getLogger().severe("Failed to save armor stand locations: " + e.getMessage());
        }
    }

    private void loadArmorStandLocations() {
        if (!armorStandFile.exists()) return;
        try (Scanner scanner = new Scanner(armorStandFile)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 5) {
                    String name = parts[0];
                    String worldName = parts[1];
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    double z = Double.parseDouble(parts[4]);
                    Location loc = new Location(getServer().getWorld(worldName), x, y, z);
                    armorStandLocations.put(name, loc);
                }
            }
        } catch (IOException | NumberFormatException e) {
            getLogger().severe("Failed to load armor stand locations: " + e.getMessage());
        }
    }

    private void cleanupArmorStands() {
        for (Map.Entry<String, Location> entry : armorStandLocations.entrySet()) {
            Location loc = entry.getValue();
            if (loc.getWorld() != null) {
                for (ArmorStand stand : loc.getWorld().getEntitiesByClass(ArmorStand.class)) {
                    if (stand.getCustomName() != null && stand.getCustomName().equals(entry.getKey())) {
                        stand.remove();
                    }
                }
            }
        }
        armorStandLocations.clear();
    }
}
