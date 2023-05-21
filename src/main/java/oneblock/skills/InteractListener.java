package oneblock.skills;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    public void shadowstrikebow(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player))
            return;

        Player shooter = (Player) arrow.getShooter();
        ItemStack bow = shooter.getInventory().getItemInMainHand();
        if (bow.getType() != Material.BOW || !bow.hasItemMeta())
            return;

        ItemMeta itemMeta = bow.getItemMeta();
        if (!itemMeta.hasDisplayName() || !itemMeta.hasLore() || !itemMeta.getDisplayName().equals("§8Shadowstrike Bow"))
            return;

        int lineIndex = getLineIndex(itemMeta.getLore(), "Potion: ");
        if (lineIndex == -1)
            return;

        String potion = itemMeta.getLore().get(lineIndex);
        LivingEntity target = (LivingEntity) event.getEntity();

        if (potion.contains("poison")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
        } else if (potion.contains("slow")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
        }
    }

    private int getLineIndex(List<String> lore, String prefix) {
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }
}
