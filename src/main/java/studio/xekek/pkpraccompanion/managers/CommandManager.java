package studio.xekek.pkpraccompanion.managers;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.xekek.pkpraccompanion.PKPracServer;

public class CommandManager implements CommandExecutor {
    private final PKPracServer plugin;
    private final ConfigManager configManager;

    public CommandManager(PKPracServer plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("pkprac")) {
            if (args.length > 1 && "disable".equalsIgnoreCase(args[0])) {
                if (sender.hasPermission("pkprac.admin")) {
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target != null) {
                        configManager.getNoPracticePlayers().add(target.getUniqueId());
                        plugin.handleFloatingEntity(target);
                        sender.sendMessage("Enabled PKPrac for " + target.getName() + ".");
                    } else {
                        sender.sendMessage("Player not found.");
                    }
                } else {
                    sender.sendMessage("You do not have permission to use this command.");
                }
                return true;
            }

            if (args.length > 1 && "enable".equalsIgnoreCase(args[0])) {
                if (sender.hasPermission("pkprac.admin")) {
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target != null) {
                        configManager.getNoPracticePlayers().remove(target.getUniqueId());
                        plugin.removeFloatingEntity(target);
                        sender.sendMessage("Disabled PKPrac for " + target.getName() + ".");
                    } else {
                        sender.sendMessage("Player not found.");
                    }
                } else {
                    sender.sendMessage("You do not have permission to use this command.");
                }
                return true;
            }

            if (args.length > 0 && "toggleworld".equalsIgnoreCase(args[0])) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("pkprac.admin")) {
                        String worldName = player.getWorld().getName();
                        if (configManager.getNonPracticeWorlds().contains(worldName)) {
                            configManager.getNonPracticeWorlds().remove(worldName);
                            for (Player p : player.getWorld().getPlayers()) {
                                plugin.removeFloatingEntity(p);
                            }
                            player.sendMessage("[PKPrac] " + worldName + " is now a PKPrac Practice world.");
                        } else {
                            configManager.getNonPracticeWorlds().add(worldName);
                            for (Player p : player.getWorld().getPlayers()) {
                                plugin.handleFloatingEntity(p);
                            }
                            player.sendMessage("[PKPrac] " + worldName + " is now a PKPrac Non-Practice world.");
                        }
                    } else {
                        sender.sendMessage("You do not have permission to use this command.");
                    }
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            }

            if (args.length > 0 && "givewand".equalsIgnoreCase(args[0])) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("pkprac.admin")) {
                        ItemStack wand = new ItemStack(Material.STICK);
                        ItemMeta meta = wand.getItemMeta();
                        meta.setDisplayName("NoPractice Wand");
                        wand.setItemMeta(meta);

                        player.getInventory().addItem(wand);
                        player.sendMessage("You have been given the NoPractice Wand.");
                    } else {
                        sender.sendMessage("You do not have permission to use this command.");
                    }
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            }

            if (args.length > 0 && "removenear".equalsIgnoreCase(args[0])) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("pkprac.admin")) {
                        ArmorStand nearest = null;
                        double nearestDistance = Double.MAX_VALUE;

                        for (ArmorStand stand : player.getWorld().getEntitiesByClass(ArmorStand.class)) {
                            if ("NoPractice".equals(stand.getCustomName())) {
                                double distance = stand.getLocation().distance(player.getLocation());
                                if (distance < nearestDistance) {
                                    nearest = stand;
                                    nearestDistance = distance;
                                }
                            }
                        }

                        if (nearest != null) {
                            nearest.remove();
                            player.sendMessage("Removed the nearest NoPractice marker.");
                        } else {
                            player.sendMessage("No NoPractice markers found nearby.");
                        }
                    } else {
                        sender.sendMessage("You do not have permission to use this command.");
                    }
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            }

            if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("pkprac.admin")) {
                        boolean found = false;
                        for (ArmorStand stand : player.getWorld().getEntitiesByClass(ArmorStand.class)) {
                            if ("NoPractice".equals(stand.getCustomName())) {
                                player.sendMessage("Marker at: " + stand.getLocation().getBlockX() + ", " + stand.getLocation().getBlockY() + ", " + stand.getLocation().getBlockZ());
                                found = true;
                            }
                        }

                        if (!found) {
                            player.sendMessage("No NoPractice markers found in this world.");
                        }
                    } else {
                        sender.sendMessage("You do not have permission to use this command.");
                    }
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            }
        }
        return false;
    }
}
