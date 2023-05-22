package oneblock.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Objects;

public class InteractListener implements Listener {
    private final Main plugin;

    public InteractListener(Main plugin) {
        this.plugin = plugin;
    }

    ////////// SHADOWBANE DAGGER //////////
    @EventHandler
    public void shadowbaneDaggerBackstab(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagedEntity = event.getEntity();

        if (!(damager instanceof Player)) {
            return;
        }

        Player player = (Player) damager;
        ItemStack weapon = player.getInventory().getItemInMainHand();


        if (weapon.getType() == Material.STONE_SWORD && weapon.hasItemMeta() && Objects.requireNonNull(weapon.getItemMeta()).hasDisplayName() && weapon.getItemMeta().getDisplayName().equals("§5Shadowbane Dagger")) {
            if (isAttackingFromBehind(player, damagedEntity)) {
                event.setDamage(event.getDamage() * 1.25);
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 10f, 0.5f);

                LivingEntity ent = (LivingEntity) damagedEntity;
                PotionEffect slownessEffect = new PotionEffect(PotionEffectType.SLOW, 60, 2, false, false);
                ent.addPotionEffect(slownessEffect);
            }
        }
    }

    private boolean isAttackingFromBehind(Player player, Entity entity) {
        return player.getLocation().getDirection().dot(entity.getLocation().getDirection()) >= Math.cos(Math.toRadians(50));
    }

    ////////// LUMINESCENT BOW //////////
    @EventHandler
    public void luminescentBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();

        if (bow != null && bow.getType() == Material.BOW && bow.hasItemMeta() && Objects.requireNonNull(bow.getItemMeta()).hasDisplayName()
                && bow.getItemMeta().getDisplayName().equals("§eLuminescent Bow")) {
            Arrow arrow = (Arrow) event.getProjectile();
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

            Arrow arrow1 = player.launchProjectile(Arrow.class);
            arrow1.setDamage(50);
            arrow1.setKnockbackStrength(1);
            arrow1.setShooter(player);
            arrow1.setVelocity(event.getProjectile().getVelocity().rotateAroundY(Math.toRadians(15)));
            arrow1.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

            Arrow arrow2 = player.launchProjectile(Arrow.class);
            arrow2.setDamage(25);
            arrow2.setKnockbackStrength(1);
            arrow2.setShooter(player);
            arrow2.setVelocity(event.getProjectile().getVelocity().rotateAroundY(Math.toRadians(-15)));
            arrow2.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        }
    }

    ////////// SHADOWSTRIKE BOW //////////
    @EventHandler
    public void shadowstrikeBowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) arrow.getShooter();
        ItemStack bow = shooter.getInventory().getItemInMainHand();
        if (bow.getType() != Material.BOW || !bow.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = bow.getItemMeta();
        assert itemMeta != null;
        if (!itemMeta.hasDisplayName() || !itemMeta.hasLore() || !itemMeta.getDisplayName().equals("§8Shadowstrike Bow")) {
            return;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null || lore.size() <= 6) {
            return;
        }

        int loreIndex = 6;
        String mode = lore.get(loreIndex);
        LivingEntity target = (LivingEntity) event.getEntity();
        if (mode.contains("Poison")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
        } else if (mode.contains("Slow")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
        }
    }
    @EventHandler
    public void shadowstrikeBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        ItemStack bow = event.getBow();
        if (bow == null || !bow.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = bow.getItemMeta();
        assert itemMeta != null;
        if (!itemMeta.hasDisplayName() || !itemMeta.hasLore() || !itemMeta.getDisplayName().equals("§8Shadowstrike Bow")) {
            return;
        }

        Player player = (Player) event.getEntity();
        Arrow arrow = (Arrow) event.getProjectile();
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
    }

    @EventHandler
    public void shadowstrikeBowMode(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        ItemStack bow = player.getInventory().getItemInMainHand();
        if (bow.getType() != Material.BOW || !bow.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = bow.getItemMeta();
        assert itemMeta != null;
        if (!itemMeta.hasDisplayName() || !itemMeta.hasLore() || !itemMeta.getDisplayName().equals("§8Shadowstrike Bow")) {
            return;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null || lore.size() <= 6) {
            return;
        }

        int loreIndex = 6;
        String mode = lore.get(loreIndex);
        if (mode.contains("Poison")) {
            lore.set(loreIndex, ChatColor.AQUA + "Arrow Type: " + ChatColor.YELLOW + ChatColor.BOLD + "Slow");
        } else if (mode.contains("Slow")) {
            lore.set(loreIndex, ChatColor.AQUA + "Arrow Type: " + ChatColor.YELLOW + ChatColor.BOLD + "Poison");
        }
        itemMeta.setLore(lore);
        bow.setItemMeta(itemMeta);
    }
    ////////// ICEBORN AXES //////////
    @EventHandler
    public void icebornAxeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        Entity entity = event.getEntity();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (mainHand.getType() == Material.DIAMOND_AXE && offHand.getType() == Material.DIAMOND_AXE) {
            ItemMeta mainHandMeta = mainHand.getItemMeta();
            ItemMeta offHandMeta = offHand.getItemMeta();

            if (mainHandMeta != null && offHandMeta != null &&
                    mainHandMeta.hasLore() && offHandMeta.hasLore() &&
                    Objects.requireNonNull(mainHandMeta.getLore()).contains("§6Dual Wielding: §e§lPASSIVE") &&
                    Objects.requireNonNull(offHandMeta.getLore()).contains("§6Dual Wielding: §e§lPASSIVE")) {

                double originalDamage = event.getDamage();
                double increasedDamage = originalDamage * 3.5;
                LivingEntity ent = (LivingEntity) entity;

                if (ent.getHealth() < ent.getMaxHealth()) {
                    double additionalDamage = (ent.getMaxHealth() - ent.getHealth()) * 0.01;
                    increasedDamage += additionalDamage;
                }
                event.setDamage(increasedDamage);
                player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 5f, 1f);
            }
        }
    }
}
