package oneblock.skills.mechanic;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import oneblock.skills.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IceKingSummoner implements Listener {
    private Main plugin;
    private final Map<ProtectedRegion, String> altarTypes = new HashMap<>();
    private final Map<ProtectedRegion, Boolean> isSummonerPlaced = new HashMap<>();
    private final Map<Block, Boolean> isSummonerFull = new HashMap<>();
    int placedSummoner = 1;
    ArmorStand armorStand;

    World st = Bukkit.getWorld("s3");
    Location finish = new Location(st, 4590.5, 89, 4599.5);
    Laser laser;
    private final Map<Player, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_DURATION_SEC = 3;

    public IceKingSummoner(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK)
            return;

        String customName = getCustomNameFromItem(player.getItemInHand());
        if (customName == null)
            return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.END_PORTAL_FRAME)
            return;

        event.setCancelled(true);

        if (!checkCooldown(player)) {
            player.sendMessage("§cYou need to wait before placing another summoner.");
            return;
        }

        if (!isOnRegion(player, "altar")) {
            player.sendMessage("§cYou can only place summoners on altars.");
            return;
        }

        ProtectedRegion region = getRegionFromBlock(clickedBlock);
        if (region == null)
            return;

        if (isSummonerPlaced.containsKey(region)) {
            String existingSummonerType = altarTypes.get(region);
            if (!existingSummonerType.equals(customName)) {
                player.sendMessage("§cThis altar already has a different summoner type placed on it.");
                return;
            }
        } else {
            altarTypes.put(region, customName);
        }

        if (isSummonerFull.containsKey(clickedBlock)) {
            player.sendMessage("§cThis altar already has a summoner placed on it.");
            return;
        } else {
            isSummonerFull.put(clickedBlock, true);
        }

        if (placedSummoner <= 4) {
            isSummonerPlaced.put(region, true);
            placeSummonerThing(player, region, customName, clickedBlock);
            placedSummoner++;
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

            if (placedSummoner >= 5) {
                if (customName.equalsIgnoreCase("Ice King")) {
                    Bukkit.broadcastMessage("§7[§dSUMMONER§7] §9§lIce King §fhas been summoned!");
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 10f, 0f);
                    }
                }
                if (customName.equalsIgnoreCase("World Ender")) {
                    Bukkit.broadcastMessage("§7[§dSUMMONER§7] §4§lWorld Ender §fhas been summoned!");
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 10f, 0f);
                    }
                }
            }

            // Apply cooldown
            applyCooldown(player);
        }
    }

    private boolean checkCooldown(Player player) {
        if (cooldowns.containsKey(player)) {
            long currentTime = System.currentTimeMillis();
            long lastPlacementTime = cooldowns.get(player);
            long elapsedTime = currentTime - lastPlacementTime;
            long remainingCooldown = COOLDOWN_DURATION_SEC * 1000 - elapsedTime;

            if (remainingCooldown > 0) {
                return false;
            }
        }

        return true;
    }

    private void applyCooldown(Player player) {
        cooldowns.put(player, System.currentTimeMillis());
    }

    private String getCustomNameFromItem(ItemStack item) {
        if (item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasDisplayName() &&
                item.getItemMeta().getDisplayName().equalsIgnoreCase("§5Ice King Summoner §7[§dSUMMONER§7]")) {
            return "Ice King";
        }
        if (item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasDisplayName() &&
                item.getItemMeta().getDisplayName().equalsIgnoreCase("§5World Ender Summoner §7[§dSUMMONER§7]")) {
            return "World Ender";
        }
        return null;
    }

    // Custom method to place the summoner thing on the altar
    private void placeSummonerThing(Player player, ProtectedRegion region, String customName, Block block) {
        // Implement your logic to place the summoner thing on the altar
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 20f, 0f);
        }


        if (placedSummoner != 4) {
            Bukkit.broadcastMessage("§c☠ §d" + customName + " Summoner §fplaced on §5Summoner Altar §fby §e" + player.getName() + " §7(§e" + placedSummoner + "§7/§a4§7)");
            World world = block.getWorld();
            Location loc = new Location(world, block.getX(), block.getY(), block.getZ());
            armorStand = Objects.requireNonNull(loc.getWorld()).spawn(loc.add(0.5, 0, 0.5), ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setPersistent(true);
            armorStand.setSilent(true);
            armorStand.setHelmet(Main.getHead("darkorb"));
            armorStand.setCustomName(customName + "_" + player.getName());
            armorStand.setCustomNameVisible(false);

            assert world != null;
            ArmorStand nameHologram = loc.getWorld().spawn(armorStand.getLocation().add(0, 0.2, 0), ArmorStand.class);
            nameHologram.setVisible(false);
            nameHologram.setGravity(false);
            nameHologram.setInvulnerable(true);
            nameHologram.setPersistent(true);
            nameHologram.setSilent(true);
            nameHologram.setCustomName("§7[§dSUMMONER§7] §5" + player.getName() + " " + customName + " §e§lCLICK");
            nameHologram.setCustomNameVisible(true);

            cacheCustomName(armorStand.getCustomName());
            cacheCustomName(nameHologram.getCustomName());

            moveUpSummoner(armorStand, nameHologram, player);
            {
                try {
                    laser = new Laser.GuardianLaser(armorStand.getLocation(), finish, 5, 15);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }

            laser.start(plugin);
        } else {
            Bukkit.broadcastMessage("§c☠ §d" + customName + " Summoner §fplaced on §5Summoner Altar §fby §e" + player.getName() + " §7(§a" + placedSummoner + "§7/§a4§7)");
            World world = block.getWorld();
            Location loc = new Location(world, block.getX(), block.getY(), block.getZ());
            armorStand = Objects.requireNonNull(loc.getWorld()).spawn(loc.add(0.5, 0, 0.5), ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setPersistent(true);
            armorStand.setSilent(true);
            armorStand.setHelmet(Main.getHead("darkorb"));
            armorStand.setCustomName(customName + "_" + player.getName());
            armorStand.setCustomNameVisible(false);

            assert world != null;
            ArmorStand nameHologram = loc.getWorld().spawn(armorStand.getLocation().add(0, 0.2, 0), ArmorStand.class);
            nameHologram.setVisible(false);
            nameHologram.setGravity(false);
            nameHologram.setInvulnerable(true);
            nameHologram.setPersistent(true);
            nameHologram.setSilent(true);
            nameHologram.setCustomName("§7[§dSUMMONER§7] §5" + player.getName() + " " + customName + " §e§lCLICK");
            nameHologram.setCustomNameVisible(true);

            cacheCustomName(armorStand.getCustomName());
            cacheCustomName(nameHologram.getCustomName());

            moveUpSummoner(armorStand, nameHologram, player);

            {
                try {
                    laser = new Laser.GuardianLaser(armorStand.getLocation(), finish, 5, 15);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }

            laser.start(plugin);

        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 30, 0f);
        }
    }

    @EventHandler
    public void onSummonerOrbInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand))
            return;

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        if (armorStand.getCustomName() != null && armorStand.getCustomName().contains("Ice King_" + player.getName())) {
            if (placedSummoner <= 4) {
                for (Entity stand : armorStand.getNearbyEntities(0, 2, 0)) {
                    if (stand instanceof ArmorStand) {
                        String customName = stand.getCustomName();
                        if (customName != null && (customName.contains("§7[§dSUMMONER§7] §5" + player.getName() + " " + "Ice King" + " §e§lCLICK"))) {
                            stand.remove();
                            armorStand.remove();
                            placedSummoner--;
                            isSummonerPlaced.remove(getRegionFromCustomName(customName));
                            altarTypes.remove(getRegionFromCustomName(customName));

                            Location armorStandLocation = armorStand.getLocation();
                            int range = 3;

                            for (int y = armorStandLocation.getBlockY(); y >= armorStandLocation.getBlockY() - range; y--) {
                                Location blockLocation = new Location(armorStandLocation.getWorld(), armorStandLocation.getBlockX(), y, armorStandLocation.getBlockZ());
                                Block block = blockLocation.getBlock();
                                if (block.getType() == Material.END_PORTAL_FRAME) {
                                    isSummonerFull.remove(block);
                                }
                            }
                            if (customName.contains("Ice King")) {
                                Bukkit.broadcastMessage("§c☠ §dIce King Summoner §fremoved from §5Summoner Altar §fby §e" + player.getName() + " §7(§e" + (placedSummoner - 1) + "§7/§a4§7)");
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 20f, 0f);
                                }
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL ICE_KING_SUMMONER " + player.getName() + " 1");
                            }
                        }
                    }
                }
            }
        }
    }

    private ProtectedRegion getRegionFromCustomName(String customName) {
        for (Map.Entry<ProtectedRegion, String> entry : altarTypes.entrySet()) {
            if (entry.getValue().equals(customName)) {
                return entry.getKey();
            }
        }
        return null;
    }
    private void moveUpSummoner(ArmorStand spirit, ArmorStand spiritholo, Player p) {
        new BukkitRunnable() {
            private boolean goingUp = true;
            Location loc = spirit.getLocation();
            private final int maximumHeight = loc.getBlockY() + 2;
            private final int minimumHeight = loc.getBlockY();

            @Override
            public void run() {
                if (spirit.isDead()) {
                    spirit.remove();
                    spiritholo.remove();
                    cancel();
                    return;
                }
                if(spirit.getWorld().getName().equalsIgnoreCase(p.getWorld().getName())) {
                    if (placedSummoner <= 4){
                        if (goingUp) {
                            if (spirit.getLocation().getY() > maximumHeight) {
                                goingUp = false;
                            } else {
                                loc.setYaw(loc.getYaw() + (float) 20);
                                spirit.teleport(loc.add(0, 0.07, 0));
                                spiritholo.teleport(loc.clone().add(0, 0.22, 0));
                            }
                        } else {
                            if (spirit.getLocation().getY() < minimumHeight) {
                                goingUp = true;
                            } else {
                                loc.setYaw(loc.getYaw() + (float) 20);
                                spirit.teleport(loc.add(0, -0.07, 0));
                                spiritholo.teleport(loc.clone().add(0, 0.22, 0));
                            }
                        }
                    }else{
                        moveTowardsSummoner(spirit, spiritholo);
                        spirit.getWorld().playSound(spirit.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 50f, 0f);
                        spirit.getWorld().strikeLightning(spirit.getLocation());
                        cancel();
                    }

                }else{
                    spirit.remove();
                    spiritholo.remove();
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }
    int spawnCounter = 1;
    private void moveTowardsSummoner(ArmorStand spirit, ArmorStand spiritHolo) {

        World world = Bukkit.getWorld("s3");
        Location destination = new Location(world, 4590.5, 89, 4599.5 ); // Replace x, y, z with the coordinates of the destination location
        double speed = 0.05; // Adjust the speed of movement (0.05 is a slow speed)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!spirit.isDead()) {
                    spirit.getWorld().playSound(spirit.getLocation(), Sound.ENTITY_WOLF_SHAKE, 10f, 2f);
                }else{
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20, 20);
        new BukkitRunnable() {
            private boolean reachedDestination = false;
            @Override
            public void run() {
                if (spirit.isDead() || reachedDestination) {
                    spirit.remove();
                    spiritHolo.remove();
                    spirit.getWorld().playSound(spirit.getLocation(), Sound.ENTITY_WITHER_SHOOT, 10f, 1f);
                    spawnCounter++;
                    if (spawnCounter >= 4){
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                "mm m spawn yirouInvoque 1 s3,4590,89,4599");
                        spawnCounter = 1;
                    }
                    cancel();
                    return;
                }

                Location currentLocation = spirit.getLocation();
                Vector direction = destination.clone().subtract(currentLocation).toVector().normalize();
                Vector movement = direction.multiply(speed);

                if (currentLocation.distanceSquared(destination) <= speed) {
                    reachedDestination = true;
                    return;
                }

                Location newLocation = currentLocation.add(movement);
                newLocation.setYaw(newLocation.getYaw() + 20); // Adjust the yaw rotation if desired
                spirit.teleport(newLocation);
                spiritHolo.teleport(newLocation.clone().add(0, 0.22, 0));
            }
        }.runTaskTimer(plugin, 1, 1);
    }
    @EventHandler
    public void onIceKingDeath(MythicMobDeathEvent e) {
        if (e.getEntity().getName().equalsIgnoreCase("§9§lIce King")) {
            spawnCounter = 1;
            resetSummonerData();
        }
    }
    private void resetSummonerData() {
        altarTypes.clear();
        isSummonerPlaced.clear();
        isSummonerFull.clear();
        placedSummoner = 1;
    }
    private boolean isOnRegion(Player player, String regionTarget) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        for (ProtectedRegion region : set) {
            if (region.getId().contains("altar")) {
                return true;
            }
        }
        return false;
    }
    public void cacheCustomName(String customName) {
        try {
            File folder = new File("plugins/ArmorStandCache");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, "armorStandCache.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(customName);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private ProtectedRegion getRegionFromBlock(Block block) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(block.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        for (ProtectedRegion region : set) {
            if (region.getId().contains("altar")) {
                return region;
            }
        }
        return null;
    }
}
