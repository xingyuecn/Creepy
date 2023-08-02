package me.eric.creepy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class Creepy extends JavaPlugin implements Listener {

    private static final int NUM_SKULLS = 5;
    private final int SNOWBALL_RADIUS = 5;
    private final String SIGN_CONTENT = "shtm";

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        System.out.println("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("Plugin disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        generateRandomSkulls(player);
        generateSnowGlobes(player.getWorld(), 10);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() == Material.PLAYER_HEAD) {
            Skull skull = (Skull) block.getState();
            skull.setOwningPlayer(event.getPlayer());
            skull.update();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.PLAYER_HEAD) {
            ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
            skullMeta.setOwningPlayer(((Skull) block.getState()).getOwningPlayer());
            skullItem.setItemMeta(skullMeta);
            block.getWorld().dropItemNaturally(block.getLocation(), skullItem);
        }
    }

    private void generateRandomSkulls(Player player) {
        Random random = new Random();
        for (int i = 0; i < NUM_SKULLS; i++) {
            int y = random.nextInt(15) - 64;
            Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());
            block.setType(Material.PLAYER_HEAD);
            Skull skull = (Skull) block.getState();
            skull.setOwningPlayer(player);
            skull.update();
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() == World.Environment.NORMAL) {
            generateSnowGlobes(world, 10);
        }
    }

    private void generateSnowGlobes(World world, int numSnowGlobes) {
        Random random = new Random();

        for (int i = 0; i < numSnowGlobes; i++) {
            int x = random.nextInt(200) - 100;
            int z = random.nextInt(200) - 100;
            int y = world.getHighestBlockYAt(x, z);

            Location snowGlobeLocation = new Location(world, x, y, z);

            for (int dx = -SNOWBALL_RADIUS; dx <= SNOWBALL_RADIUS; dx++) {
                for (int dy = -SNOWBALL_RADIUS; dy <= SNOWBALL_RADIUS; dy++) {
                    for (int dz = -SNOWBALL_RADIUS; dz <= SNOWBALL_RADIUS; dz++) {
                        if (dx * dx + dy * dy + dz * dz <= SNOWBALL_RADIUS * SNOWBALL_RADIUS) {
                            Block block = snowGlobeLocation.clone().add(dx, dy, dz).getBlock();
                            if (dx * dx + dy * dy + dz * dz < (SNOWBALL_RADIUS - 1) * (SNOWBALL_RADIUS - 1)) {
                                block.setType(Material.AIR);
                            } else {
                                block.setType(Material.SNOW_BLOCK);
                            }
                        }
                    }
                }
            }

            for (int dx = -SNOWBALL_RADIUS - 1; dx <= SNOWBALL_RADIUS + 1; dx++) {
                for (int dy = -SNOWBALL_RADIUS - 1; dy <= SNOWBALL_RADIUS + 1; dy++) {
                    for (int dz = -SNOWBALL_RADIUS - 1; dz <= SNOWBALL_RADIUS + 1; dz++) {
                        if (dx * dx + dy * dy + dz * dz <= (SNOWBALL_RADIUS + 1) * (SNOWBALL_RADIUS + 1)
                                && dx * dx + dy * dy + dz * dz > SNOWBALL_RADIUS * SNOWBALL_RADIUS) {
                            Block block = snowGlobeLocation.clone().add(dx, dy, dz).getBlock();
                            block.setType(Material.IRON_BARS);
                        }
                    }
                }
            }
            Block signBlock = snowGlobeLocation.getBlock();
            signBlock.setType(Material.OAK_SIGN);
            Sign sign = (Sign) signBlock.getState();
            sign.setLine(0, SIGN_CONTENT);
            sign.update();

            Location armorStandLocation = snowGlobeLocation.clone().add(0, -SNOWBALL_RADIUS - 1, 0);
            ArmorStand armorStand = (ArmorStand) world.spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setBasePlate(false);
        }
    }
}