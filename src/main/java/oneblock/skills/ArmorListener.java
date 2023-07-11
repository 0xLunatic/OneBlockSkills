package oneblock.skills;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArmorListener implements Listener {
    private Main plugin;
    private final Map<Player, Long> cooldowns = new HashMap<>();
    private final Map<Player, Long> soulreaperCooldown = new HashMap<>();
    private final Map<Player, Long> divineCooldown = new HashMap<>();
    private final Map<Player, Boolean> nightmarePassive = new HashMap<>();
    private long cooldownDuration = 0;

    public ArmorListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void nightmareArmorSetSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (player.isSneaking()) {
            if (checkFullSet(player)) {
                if (checkFullSetHasItemMeta(player)) {
                    if (checkFullSetLore(player)) {
                        if (getFullDisplayName(player, "§7Nightmare")) {
                            if (canUseAbility(player)) {
                                applyCooldown(player);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 2));
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 4));
                                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 50, 0f);
                                cooldownDuration = 45000;


                            } else {
                                long remainingCooldown = getRemainingCooldown(player);
                                player.sendMessage("§cYour ability still on " + remainingCooldown + "s cooldown!");
                            }
                        }
                    }
                }
            }
        }
    }
    int hitCounter;
    private long lastActivationTimeIceBorn = 0;
    private final long COOLDOWN_ICEBORN = 10 * 1000;
    @EventHandler
    public void icebornSetPassive(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        if (checkFullSet(player)) {
            if (checkFullSetHasItemMeta(player)) {
                if (checkFullSetLore(player)) {
                    if (getFullDisplayName(player, "§bIceborn")) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastActivationTimeIceBorn >= COOLDOWN_ICEBORN) {
                            hitCounter++; // Increment the hit counter

                            if (hitCounter >= 5) {
                                // Apply freezing effect to the damaged entity
                                LivingEntity ent = (LivingEntity) event.getEntity();
                                ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 2));
                                ent.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 2));
                                ent.setFreezeTicks(20 * 3);
                                hitCounter = 0; // Reset the hit counter
                                lastActivationTimeIceBorn = currentTime; // Update the last activation time
                                player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 10f, 0f);
                                player.sendMessage("§6Frostbite Skills §fapplied to §b" + event.getEntity().getName());
                            }
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void nightmareArmorSetPassive(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        if (checkFullSet(player)) {
            if (checkFullSetHasItemMeta(player)) {
                if (checkFullSetLore(player)) {
                    if (getFullDisplayName(player, "§7Nightmare")) {
                        // Check if it is night time
                        World world = player.getWorld();
                        long time = world.getTime();
                        if (time >= 13000 && time <= 23000) { // Adjust the time range if needed
                            if (!nightmarePassive.containsKey(player)) {
                                nightmarePassive.put(player, true);
                                player.sendMessage(ChatColor.GOLD + "Nocturnal Fury " + ChatColor.AQUA + "is active!");
                                double attackDamage = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).getBaseValue() + 20;
                                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(attackDamage);
                            }
                        } else {
                            if (nightmarePassive.containsKey(player)) {
                                nightmareDisablePassive(player);
                            }
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void nightmareArmorFix(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        if (!checkFullSet(player)) {
            nightmareDisablePassive(player);
        }
        if (checkFullSet(player)) {
            if (checkFullSetHasItemMeta(player)) {
                if (checkFullSetLore(player)) {
                    if (!getFullDisplayName(player, "§7Nightmare")) {
                        nightmareDisablePassive(player);
                    }
                }
            }
        }
    }
    private void nightmareDisablePassive(Player player) {
        double attackDamage = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).getBaseValue();
        if (attackDamage != 1) {
            nightmarePassive.remove(player);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(1);
            player.sendMessage(ChatColor.GOLD + "Nocturnal Fury " + ChatColor.RED + "inactive!");
        }
    }
    @EventHandler
    public void soulreaperArmorSetPassive(EntityDamageByEntityEvent event) {
        String loreLine;
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (checkFullSet(player)) {
            if (checkFullSetHasItemMeta(player)) {
                if (checkFullSetLore(player)) {
                    if (getFullDisplayName(player, "Soulreaper")) {
                        if (checkRPGonHand(player, "Soulreaper")) {
                            ItemMeta itemMeta = player.getInventory().getItemInMainHand().getItemMeta();
                            assert itemMeta != null;
                            List<String> lore = itemMeta.getLore();
                            int souls = Integer.parseInt(Objects.requireNonNull(getLoreLineValue(lore,
                                    findLoreContaining(lore, "Souls Collected"))));
                            if (souls > 0) {
                                if (event.getDamage() > player.getHealth()) {
                                    if (!soulreaperCooldown.containsKey(player)) {
                                        event.setDamage(0);

                                        double currentHealth = 1;
                                        float healthAmount = (float) (currentHealth + souls / 10);

                                        if (healthAmount <= player.getMaxHealth()) {
                                            player.setHealth(healthAmount);
                                        } else {
                                            player.setHealth(player.getMaxHealth());
                                        }

                                        int loreValue = (int) Math.ceil(souls - player.getHealth());
                                        loreLine = "§8Souls Collected: " + loreValue;
                                        player.setInvulnerable(true);
//                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "totemanimation animatedtitles:bruh " + player.getName());

                                        for (int i = 0; i < Objects.requireNonNull(lore).size(); i++) {
                                            String line = lore.get(i);
                                            if (line.startsWith("§8Souls Collected: ")) {
                                                lore.set(i, loreLine);
                                                itemMeta.setLore(lore);
                                                player.getInventory().getItemInMainHand().setItemMeta(itemMeta);
                                                break;
                                            }
                                        }

                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            player.setInvulnerable(false);
                                            player.sendMessage(ChatColor.RED + "Veil of the Departed is on cooldown!");

                                            long cooldownEnd = System.currentTimeMillis() + 300000;
                                            soulreaperCooldown.put(player, cooldownEnd);

                                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                                player.sendMessage(ChatColor.WHITE + "Veil of the Departed is ready to be used!");
                                                soulreaperCooldown.remove(player);
                                            }, 300000 / 50);
                                        }, 3000 / 50);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void divineArmorSetPassive(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        String victim = player.getName();
        if (player.getName() != victim) {
            return;
        }

        if (checkFullSet(player)) {
            if (checkFullSetHasItemMeta(player)) {
                if (checkFullSetLore(player)) {
                    if (getFullDisplayName(player, "§7Divine")) {
                        if (!divineCooldown.containsKey(player)) {
                            int duration = 6;
                            int interval = 10;

                            player.setInvulnerable(true);
                            BukkitRunnable task = new BukkitRunnable() {
                                int ticks = 0;
                                @Override
                                public void run() {
                                    ticks++;
                                    Location center = player.getLocation();
                                    double radius = 2.0; // Radius of the dome
                                    double height = 2.0; // Height of the dome

                                    int density = 15; // Number of particles per block

                                    for (double theta = 0; theta <= Math.PI; theta += Math.PI / density) {
                                        for (double phi = 0; phi <= 2 * Math.PI; phi += Math.PI / density) {
                                            double x = center.getX() + radius * Math.sin(theta) * Math.cos(phi);
                                            double y = center.getY() + height * Math.cos(theta) + 1;
                                            double z = center.getZ() + radius * Math.sin(theta) * Math.sin(phi);

                                            Location particleLocation = new Location(center.getWorld(), x, y, z);
                                            player.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, new Particle.DustOptions(org.bukkit.Color.AQUA, 1.0f));
                                        }
                                    }
                                    if (ticks >= duration) {
                                        cancel();
                                        player.setInvulnerable(false);
                                        long cooldownEnd = System.currentTimeMillis() + 30000;
                                        divineCooldown.put(player, cooldownEnd);

                                        player.sendMessage(ChatColor.RED + "Divine Shield is on cooldown!");

                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            divineCooldown.remove(player);
                                            player.sendMessage(ChatColor.GOLD + "Divine Shield " + ChatColor.GREEN + "is ready to use!");
                                        }, 30000 / 50);
                                    }
                                }
                            };
                            task.runTaskTimer(plugin, 0, interval);
                        }
                    }
                }
            }
        }
    }

    public boolean checkFullSet(Player p) {
        if (p.getInventory().getHelmet() != null) {
            if (p.getInventory().getChestplate() != null) {
                if (p.getInventory().getLeggings() != null) {
                    return p.getInventory().getBoots() != null;
                }
            }
        }
        return false;
    }

    public boolean checkFullSetHasItemMeta(Player p) {
        if (Objects.requireNonNull(p.getInventory().getHelmet()).hasItemMeta()) {
            if (Objects.requireNonNull(p.getInventory().getChestplate()).hasItemMeta()) {
                if (Objects.requireNonNull(p.getInventory().getLeggings()).hasItemMeta()) {
                    return Objects.requireNonNull(p.getInventory().getBoots()).hasItemMeta();
                }
            }
        }
        return false;
    }

    public boolean checkFullSetLore(Player p) {
        return Objects.requireNonNull(Objects.requireNonNull(p.getInventory().getHelmet()).getItemMeta()).hasDisplayName() ||
                Objects.requireNonNull(Objects.requireNonNull(p.getInventory().getChestplate()).getItemMeta()).hasDisplayName() ||
                Objects.requireNonNull(Objects.requireNonNull(p.getInventory().getLeggings()).getItemMeta()).hasDisplayName() ||
                Objects.requireNonNull(Objects.requireNonNull(p.getInventory().getBoots()).getItemMeta()).hasDisplayName();
    }

    public boolean getFullDisplayName(Player p, String loreDetect) {
        if (Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(p.getInventory().getHelmet()).getItemMeta()).getDisplayName()).contains(loreDetect)) {
            if (Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(p.getInventory().getChestplate()).getItemMeta()).getDisplayName()).contains(loreDetect)) {
                if (Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(p.getInventory().getLeggings()).getItemMeta()).getDisplayName()).contains(loreDetect)) {
                    if (Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(p.getInventory().getBoots()).getItemMeta()).getDisplayName()).contains(loreDetect)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean checkRPGonHand(Player p,String loreDetect) {
        if (p.getInventory().getItemInMainHand() != null) {
            if (Objects.requireNonNull(p.getInventory().getItemInMainHand().getItemMeta()).getDisplayName().contains(loreDetect)) {
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean canUseAbility(Player player) {
        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(player)) {
            long lastUseTime = cooldowns.get(player);
            if (currentTime - lastUseTime >= cooldownDuration) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private void applyCooldown(Player player) {
        long currentTime = System.currentTimeMillis();
        cooldowns.put(player, currentTime);

    }
    private long getRemainingCooldown(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastUseTime = cooldowns.get(player);
        long remainingCooldown = cooldownDuration - (currentTime - lastUseTime);
        return Math.max(0, remainingCooldown / 1000);
    }

    private int findLoreContaining(List<String> lore, String searchString) {
        if (lore == null || searchString == null) {
            return -1;
        }
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains(searchString)) {
                return i;
            }
        }
        return -1;
    }

    private String getLoreLineValue(List<String> lore, int lineNumber) {
        if (lineNumber <= 0 || lineNumber > Objects.requireNonNull(lore).size()) {
            return null;
        }
        String line = lore.get(lineNumber);
        String[] parts = line.split(": ");
        if (parts.length >= 2) {
            return parts[1].trim();
        }
        return null;
    }
}