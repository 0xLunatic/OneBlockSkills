package oneblock.skills;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ArmorListener implements Listener {
    private Main plugin;
    private final Map<Player, Long> cooldowns = new HashMap<>();
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
        Entity entity = event.getEntity();
        if (checkFullSet(player)) {
            if (checkFullSetHasItemMeta(player)) {
                if (checkFullSetLore(player)) {
                    if (getFullDisplayName(player, "§7Nightmare")) {
                        // Check if it is night time
                        World world = player.getWorld();
                        long time = world.getTime();
                        if (time >= 13000 && time <= 23000) { // Adjust the time range if needed
                            event.setDamage(event.getDamage() * 1.5);
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
}