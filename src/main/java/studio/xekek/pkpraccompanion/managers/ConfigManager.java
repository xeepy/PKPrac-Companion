package studio.xekek.pkpraccompanion.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Set<String> nonPracticeWorlds = new HashSet<>();
    private final Set<UUID> noPracticePlayers = new HashSet<>();
    private final Map<Player, BukkitTask> floatingEntityTasks = new ConcurrentHashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        nonPracticeWorlds.addAll(config.getStringList("nonPracticeWorlds"));
        for (String uuid : config.getStringList("noPracticePlayers")) {
            noPracticePlayers.add(UUID.fromString(uuid));
        }
    }

    public void saveConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("nonPracticeWorlds", new ArrayList<>(nonPracticeWorlds));
        List<String> uuids = noPracticePlayers.stream().map(UUID::toString).collect(Collectors.toList());
        config.set("noPracticePlayers", uuids);
        plugin.saveConfig();
    }

    public Set<String> getNonPracticeWorlds() {
        return nonPracticeWorlds;
    }

    public Set<UUID> getNoPracticePlayers() {
        return noPracticePlayers;
    }

    public Map<Player, BukkitTask> getFloatingEntityTasks() {
        return floatingEntityTasks;
    }
}
