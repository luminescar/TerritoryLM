package me.luminescar.territory;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TerritoryPlugin extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, Location[]> markedTerritories = new HashMap<>();
    private String pluginPrefix;
    private Player player;

    @Override
    public void onEnable() {
        getLogger().info("TerritoryPlugin has been enabled!");

        // Register command
        Objects.requireNonNull(getCommand("territory")).setExecutor(this);

        // Load config settings
        loadConfigSettings();

        // Create plugin folder if not exists
        File pluginFolder = new File(getDataFolder(), "TerritoryLM");
        if (!pluginFolder.exists()) {
            if (pluginFolder.mkdirs()) {
                getLogger().info("Plugin folder created successfully.");
            } else {
                getLogger().severe("Failed to create plugin folder.");
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("TerritoryPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "mark":
                        handleMarkCommand(player);
                        break;
                    case "create":
                        handleCreateCommand(player, args);
                        break;
                    case "delete":
                        handleDeleteCommand(player, args);
                        break;
                    case "list":
                        listTerritories(player);
                        break;
                    case "help":
                        showHelp(player);
                        break;
                    default:
                        player.sendMessage(pluginPrefix + ChatColor.RED + "Unknown command. Type /territory help for assistance.");
                        break;
                }
            } else {
                player.sendMessage(pluginPrefix + ChatColor.RED + "Usage: /territory <mark/create/delete/list/help>");
            }
        } else {
            sender.sendMessage("This command can only be executed by a player.");
        }

        return true;
    }

    private void handleMarkCommand(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!markedTerritories.containsKey(playerUUID)) {
            markedTerritories.put(playerUUID, new Location[2]);
            player.sendMessage(pluginPrefix + ChatColor.GREEN + "First point marked! Now mark the second point.");
            sendTitle(player, "1/2");
        } else if (markedTerritories.get(playerUUID)[0] == null) {
            markedTerritories.get(playerUUID)[0] = player.getLocation();
            player.sendMessage(pluginPrefix + ChatColor.GREEN + "Second point marked! Now use /territory create <name> to create the territory.");
            sendTitle(player, "2/2");
        }
    }

    private void handleCreateCommand(Player player, String[] args) {
        UUID playerUUID = player.getUniqueId();

        if (!markedTerritories.containsKey(playerUUID) || markedTerritories.get(playerUUID)[1] == null) {
            if (markedTerritories.get(playerUUID)[0] == null) {
                player.sendMessage(pluginPrefix + ChatColor.RED + "You need to mark both points before creating a territory.");
            } else {
                player.sendMessage(pluginPrefix + ChatColor.GREEN + "Second point marked! Now use /territory create <name> to create the territory.");
                sendTitle(player, "2/2");
            }
            return;
        }

        if (args.length > 1) {
            String territoryName = args[1];
            createTerritory(player, territoryName);
        } else {
            player.sendMessage(pluginPrefix + ChatColor.RED + "Usage: /territory create <name>");
        }

        // Stop marking effect
        markedTerritories.remove(playerUUID);
    }

    private void handleDeleteCommand(Player player, String[] args) {
        if (args.length > 1) {
            String territoryName = args[1];
            deleteTerritory(player, territoryName);
        } else {
            player.sendMessage(pluginPrefix + ChatColor.RED + "Usage: /territory delete <name>");
        }
    }

    private void listTerritories(Player player) {
        // Implementation of list command
        // ...
        this.player = player;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', pluginPrefix + "&6&lCommands:"));
        player.sendMessage(pluginPrefix + ChatColor.GREEN + "/territory mark - Mark territory points.");
        player.sendMessage(pluginPrefix + ChatColor.GREEN + "/territory create <name> - Create a territory between marked points.");
        player.sendMessage(pluginPrefix + ChatColor.GREEN + "/territory delete <name> - Delete a territory by name.");
        player.sendMessage(pluginPrefix + ChatColor.GREEN + "/territory list - List all territories.");
        player.sendMessage(pluginPrefix + ChatColor.GREEN + "/territory help - Show this help message.");
    }

    private void loadConfigSettings() {
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);

        // Create default config if not present
        saveDefaultConfig();

        // Load settings
        ParticleEffectManager.setParticleEffect(Particle.valueOf(config.getString("settings.particle-effect", "SOUL_FIRE_FLAME")));
        ParticleEffectManager.setParticlesPerSecond(config.getInt("settings.particles-per-second", 10));
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', config.getString("settings.plugin-prefix", "&7[&6Territory&7] "));
    }

    private void createTerritory(Player player, String territoryName) {
        UUID playerUUID = player.getUniqueId();
        Location[] points = markedTerritories.get(playerUUID);

        // Implementation of territory creation
        WorldEditCuboid cuboid = new WorldEditCuboid(points[0], points[1]);

        // Apply particle effect to the territory
        cuboid.fillWithParticles(ParticleEffectManager.getParticleEffect());

        // Save territory settings to file
        saveTerritorySettings(player, cuboid, territoryName);
    }

    private void deleteTerritory(Player player, String territoryName) {
        UUID playerUUID = player.getUniqueId();
        File territoryFile = new File(getDataFolder(), "TerritoryLM/" + playerUUID + "/" + territoryName + ".yml");

        if (territoryFile.exists()) {
            if (territoryFile.delete()) {
                player.sendMessage(pluginPrefix + ChatColor.GREEN + "Territory '" + territoryName + "' deleted successfully.");
            } else {
                player.sendMessage(pluginPrefix + ChatColor.RED + "Failed to delete territory '" + territoryName + "'.");
            }
        } else {
            player.sendMessage(pluginPrefix + ChatColor.RED + "Territory '" + territoryName + "' not found.");
        }
    }

    private void saveTerritorySettings(Player player, WorldEditCuboid cuboid, String territoryName) {
        UUID playerUUID = player.getUniqueId();
        File territoryFolder = new File(getDataFolder(), "TerritoryLM/" + playerUUID);

        if (!territoryFolder.exists()) {
            if (territoryFolder.mkdirs()) {
                getLogger().info("Territory folder created successfully.");
            } else {
                getLogger().severe("Failed to create territory folder.");
            }
        }

        File settingsFile = new File(territoryFolder, territoryName + ".yml");
        FileConfiguration settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

        settingsConfig.set("particle-effect", ParticleEffectManager.getParticleEffect().name());
        settingsConfig.set("particles-per-second", ParticleEffectManager.getParticlesPerSecond());

        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            getLogger().severe("An error occurred while saving the configuration file: " + e.getMessage());
        }
    }

    private void sendTitle(Player player, String message) {
        player.sendTitle("", ChatColor.translateAlternateColorCodes('&', message), 10, 40, 10);
    }
}
