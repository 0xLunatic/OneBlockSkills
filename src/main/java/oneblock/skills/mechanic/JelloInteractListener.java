package oneblock.skills.mechanic;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.md_5.bungee.api.ChatColor;
import oneblock.skills.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class JelloInteractListener implements Listener {
    private Main plugin;

    public JelloInteractListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCheck(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.PLAYER_HEAD) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null && itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals(ChatColor.GOLD + "Jello Bucket Empty")) {
                Block clickedBlock = e.getClickedBlock();
                e.setCancelled(true);

                if (clickedBlock != null && clickedBlock.getType() == Material.LIME_STAINED_GLASS || Objects.requireNonNull(clickedBlock).getType() == Material.LIME_STAINED_GLASS_PANE) {
                    World world = clickedBlock.getWorld();
                    if (isOnRegion(player, "jello-farm")) {

                        if (world != null && world.getName().equalsIgnoreCase("s3")) {
                            for (Entity ent : player.getNearbyEntities(0.5, 0.5, 0.5)) {
                                if (ent instanceof ArmorStand) {
                                    if (!Objects.requireNonNull(ent.getCustomName()).contains("Jello")) {
                                        return;
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Move a bit!");
                                    }
                                }
                            }
                            item.setAmount(item.getAmount() - 1);
                            ArmorStand armorStand = world.spawn(player.getLocation().add(0, -1.5, 0), ArmorStand.class);
                            armorStand.setVisible(false);
                            armorStand.setGravity(false);
                            armorStand.setInvulnerable(true);
                            armorStand.setPersistent(true);
                            armorStand.setSilent(true);
                            armorStand.setHelmet(Main.getHead("JelloBucketEmpty"));
                            armorStand.setCustomName(player.getName() + "_JelloBucketEmpty");

                            ArmorStand nameHologram = world.spawn(armorStand.getLocation().add(0, 0.2, 0), ArmorStand.class);
                            nameHologram.setVisible(false);
                            nameHologram.setGravity(false);
                            nameHologram.setInvulnerable(true);
                            nameHologram.setPersistent(true);
                            nameHologram.setSilent(true);
                            nameHologram.setCustomName(ChatColor.GREEN + player.getName() + " Jello Bucket");
                            nameHologram.setCustomNameVisible(true);

                            ArmorStand progressHologram = world.spawn(nameHologram.getLocation().add(0, 0.35, 0), ArmorStand.class);
                            progressHologram.setVisible(false);
                            progressHologram.setGravity(false);
                            progressHologram.setInvulnerable(true);
                            progressHologram.setPersistent(true);
                            progressHologram.setSilent(true);
                            progressHologram.setCustomName(ChatColor.GREEN + player.getName() + "'s Progress " + formatProgressBar(0.01));
                            progressHologram.setCustomNameVisible(true);

                            // Cache the custom name to a file
                            cacheCustomName(armorStand.getCustomName());
                            cacheCustomName(nameHologram.getCustomName());

                            new BukkitRunnable() {
                                double progress = 0.01;

                                @Override
                                public void run() {
                                    progress += 0.01; // Increase progress by 1%
                                    progressHologram.getWorld().spawnParticle(Particle.WATER_DROP, progressHologram.getLocation().add(0, 2, 0), 5, 0, 0, 0, 0);
                                    progressHologram.setCustomName(ChatColor.GREEN + player.getName() + "'s Progress " + formatProgressBar(progress));
                                    if (progress >= 1.0) {
                                        progress = 1.0; // Clamp progress to 100%
                                        cancel(); // Stop the task when progress reaches 100%
                                        playFinishSound(player); // Play the finishing sound effect
                                        armorStand.setHelmet(Main.getHead("JelloBucketFull"));
                                        armorStand.setCustomName(player.getName() + "_JelloBucketFull");
                                        cacheCustomName(armorStand.getCustomName());
                                        progressHologram.setCustomName(ChatColor.GREEN + player.getName() + "'s Progress §e§lFULL");
                                        return;
                                    }
                                    playFillingBucketSound(player);
                                    for (Entity ent : armorStand.getNearbyEntities(1.5, 1.5, 1.5)){
                                        if (ent instanceof Monster){
                                            cancel();
                                            player.sendMessage("§cOh no! Your bucket was spilled by a monster.");
                                            armorStand.remove();
                                            nameHologram.remove();
                                            progressHologram.remove();
                                            player.playSound(armorStand.getLocation(), Sound.BLOCK_GLASS_BREAK, 20f, 0f);
                                        }
                                    }
                                }
                            }.runTaskTimer(plugin, 20L, 20L);
                        }else {
                            player.sendMessage(ChatColor.RED + "You can't use it here!");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJelloBucketInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand))
            return;

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        if (armorStand.getCustomName() != null && armorStand.getCustomName().contains(player.getName() + "_JelloBucketFull")) {
            for (Entity stand : armorStand.getNearbyEntities(0, 2, 0)) {
                if (stand instanceof ArmorStand) {
                    String customName = stand.getCustomName();
                    if (customName != null && (customName.contains(player.getName() + "'s Progress §e§lFULL") || customName.contains(player.getName() + " Jello Bucket"))) {
                        Location loc = stand.getLocation();
                        stand.remove();
                        armorStand.remove();
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.CLOUD, loc, 30, 0.5, 0.5, 0.5, 0.2);
                        loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, 10, 0f);
                        double chance = Math.random() * 500; // Generate a random chance value between 0 and 100
                        if (!player.hasPermission("jello.booster")) {
                            if (chance < 20) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL SLIME_GEL " + player.getName() + " 1");
                            } else if (chance < 30) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL BLOBS_OF_JELLO " + player.getName() + " 1");
                            } else if (chance < 35) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL STINKY_JELLO " + player.getName() + " 1");
                                Bukkit.broadcastMessage("§e§lCongratulations! §d" +player.getName()+ " §fjust found §2§lStinky Jello§f!");
                                for (Player online : Bukkit.getOnlinePlayers()){
                                    online.playSound(online.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                                }
                            } else if (chance < 37) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL UNIDENTIFIED_HANDLE " + player.getName() + " 1");
                                Bukkit.broadcastMessage("§e§lCongratulations! §d" +player.getName()+ " §fjust found §5§lUnidentified Handle§f!");
                                for (Player online : Bukkit.getOnlinePlayers()){
                                    online.playSound(online.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                                }
                            } else {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL RAW_JELLO " + player.getName() + " 1-3");
                            }
                            break;
                        } else {
                            if (chance < 20) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL SLIME_GEL " + player.getName() + " 1");
                            } else if (chance < 30) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL BLOBS_OF_JELLO " + player.getName() + " 1");
                            } else if (chance < 35) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL STINKY_JELLO " + player.getName() + " 1");
                                Bukkit.broadcastMessage("§e§lCongratulations! §d" +player.getName()+ " §fjust found §2§lStinky Jello§f!");
                                for (Player online : Bukkit.getOnlinePlayers()){
                                    online.playSound(online.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                                }
                            } else if (chance < 37) {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL UNIDENTIFIED_HANDLE " + player.getName() + " 1");
                                Bukkit.broadcastMessage("§e§lCongratulations! §d" +player.getName()+ " §fjust found §5§lUnidentified Handle§f!");
                                for (Player online : Bukkit.getOnlinePlayers()){
                                    online.playSound(online.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                                }
                            } else {
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "" +
                                        "mi give MATERIAL RAW_JELLO " + player.getName() + " 1-5");
                            }
                            break;
                        }

                    }
                }
            }
        }
    }



    @EventHandler
    public void disableInteractNamedStand(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand))
            return;

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();

        if (armorStand.getCustomName() != null && !armorStand.getCustomName().isEmpty()) {
            event.setCancelled(true);
        }
    }

    public void playFillingBucketSound(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 10.0f, 1.5f);
    }

    public void playFinishSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10.0f, 1.0f);
    }
    public boolean isOnRegion(Player p, String regionTarget) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(p.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        for (ProtectedRegion region : set) {
            if (region.getId().contains(regionTarget)) {
                return true;
            }
        }
        return false;
    }

    private String formatProgressBar(double progress) {
        int barLength = 10; // Length of the progress bar
        int filledBars = (int) (barLength * progress);
        int emptyBars = barLength - filledBars;

        StringBuilder progressBar = new StringBuilder(ChatColor.GREEN + "[");
        for (int i = 0; i < filledBars; i++) {
            progressBar.append(ChatColor.GREEN).append("|");
        }
        for (int i = 0; i < emptyBars; i++) {
            progressBar.append(ChatColor.GRAY).append("|");
        }
        progressBar.append(ChatColor.GREEN).append("]");

        String formattedProgress = String.format("%.0f", progress * 100); // Format progress as percentage
        return progressBar + " " + formattedProgress + "%";
    }


    private void cacheCustomName(String customName) {
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
}
