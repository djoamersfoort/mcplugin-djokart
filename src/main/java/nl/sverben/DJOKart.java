package nl.sverben;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DJOKart extends JavaPlugin implements Listener {
    Boolean debug = false;
    Boolean started = false;

    @Override
    public void onEnable() {
        Logger logger = getLogger();
        logger.info("DJOKart enabled");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onVehicleExitEvent(VehicleExitEvent event) {
        if (debug) {
            return;
        }
        Location location = event.getExited().getLocation();
        if (!location.getWorld().getName().equalsIgnoreCase("bedwars")) {
            return;
        }
        if (location.getX() < 322 && location.getX() > 224 && location.getZ() < 328 && location.getZ() > 236) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDestroyEvent(VehicleDestroyEvent event) {
        if (debug) {
            return;
        }
        Location location = event.getVehicle().getLocation();
        if (!location.getWorld().getName().equalsIgnoreCase("bedwars")) {
            return;
        }
        if (location.getX() < 322 && location.getX() > 224 && location.getZ() < 328 && location.getZ() > 236) {
            event.setCancelled(true);
        }
    }

    private void wall(Material material) {
        World world = getServer().getWorld("bedwars");
        for(int i = 0; i < 7; i++) {
            world.getBlockAt(new Location(world, 288, 78, 244 + i)).setType(material);
            world.getBlockAt(new Location(world, 288, 79, 244 + i)).setType(material);

            if (material.equals(Material.RED_CONCRETE) || material.equals(Material.AIR)) {
                world.getBlockAt(new Location(world, 285, 78, 244 + i)).setType(material);
                world.getBlockAt(new Location(world, 285, 79, 244 + i)).setType(material);
            }
        }
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.getLocation().getWorld().getName().equalsIgnoreCase("bedwars")) {
            return;
        }

        Location location = player.getLocation();
        if (!(location.getX() < 322 && location.getX() > 224 && location.getZ() < 328 && location.getZ() > 236)) {
            return;
        }

        if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("" + ChatColor.AQUA + ChatColor.BOLD + "Start game")) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                wall(Material.ORANGE_CONCRETE);

            }, 20L);

            Bukkit.getScheduler().runTaskLater(this, () -> {
                wall(Material.GREEN_CONCRETE);
            }, 40L);

            Bukkit.getScheduler().runTaskLater(this, () -> {
                wall(Material.AIR);
                for (Player onlineplayer : player.getLocation().getWorld().getPlayers()) {
                    Location playerloc = onlineplayer.getLocation();
                    if (playerloc.getX() < 322 && playerloc.getX() > 224 && playerloc.getZ() < 328 && playerloc.getZ() > 236) {
                        onlineplayer.sendTitle(ChatColor.GREEN + "Start!", "", 1, 20, 1);
                        onlineplayer.getInventory().clear();
                        onlineplayer.sendMessage("Ronde 1 van 3");
                    }
                }

                started = true;
            }, 60L);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        if (!location.getWorld().getName().equalsIgnoreCase("bedwars")) {
            return;
        }

        if (!(location.getX() < 322 && location.getX() > 224 && location.getZ() < 328 && location.getZ() > 236)) {
            return;
        }

        if (!started) {
            return;
        }

        if (location.getBlockX() < 280 && location.getBlockX() > 273 && location.getBlockZ() < 307 && location.getBlockZ() > 301) {
            player.setMetadata("tussenronde", new FixedMetadataValue(this, true));
        }

        if (location.getBlockX() > 286 && location.getBlockX() < 292 && location.getBlockZ() < 251 && location.getBlockZ() > 243) {
            if (player.getMetadata("tussenronde").get(0).asBoolean()) {
                int rondes = player.getMetadata("ronde").get(0).asInt();
                rondes++;
                player.setMetadata("ronde", new FixedMetadataValue(this, rondes));
                player.setMetadata("tussenronde", new FixedMetadataValue(this, false));
                if (rondes == 4) {
                    started = false;
                    for (Player onlineplayer : player.getLocation().getWorld().getPlayers()) {
                        Location playerloc = onlineplayer.getLocation();
                        if (playerloc.getX() < 322 && playerloc.getX() > 224 && playerloc.getZ() < 328 && playerloc.getZ() > 236) {
                            onlineplayer.getVehicle().remove();
                            onlineplayer.teleport(new Location(onlineplayer.getWorld(), -230, 70, -200));
                            onlineplayer.sendTitle(ChatColor.YELLOW + player.getName(), ChatColor.YELLOW + "heeft gewonnen!", 1, 20, 1);
                        }
                    }
                } else {
                    player.sendMessage("Ronde " + rondes + " van 3");
                }
            }
        }
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("kartdebug")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You must be op to do that!");
                return true;
            }
            if (debug) {
                debug = false;
            } else {
                debug = true;
            }
            sender.sendMessage(ChatColor.GREEN + "Debug toggled");
        }
        if (cmd.getName().equalsIgnoreCase("kartjoin")) {
            Player player = (Player) sender;
            if (player.getName().startsWith("^")) {
                player.sendMessage(ChatColor.RED + "Deze minigame werkt niet met bedrock");
                return true;
            }

            for(Entity boat : getServer().getWorld("bedwars").getEntities()) {
                if (boat.getType().equals(EntityType.BOAT)) {
                    if (boat.getPassengers().isEmpty()) {
                        boat.remove();
                    }
                }
            }
            if (started) {
                sender.sendMessage(ChatColor.RED + "The game has already started try again later!");
                return true;
            }
            ItemStack item = new ItemStack(Material.DIAMOND);

            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("" + ChatColor.AQUA + ChatColor.BOLD + "Start game");
            item.setItemMeta(meta);

            World world = player.getWorld();

            wall(Material.RED_CONCRETE);

            if (!world.getName().equalsIgnoreCase("bedwars")) {
                return true;
            }
            Entity boat = world.spawnEntity(new Location(world, 287, 78, 247, -90, 0), EntityType.BOAT);
            player.teleport(boat);
            boat.addPassenger(player);

            player.setMetadata("ronde", new FixedMetadataValue(this, 1));
            player.setMetadata("tussenronde", new FixedMetadataValue(this, false));
            player.getInventory().setItem(0, item);
        }
        return true;
    }
}
