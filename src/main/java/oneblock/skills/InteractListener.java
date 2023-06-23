package oneblock.skills;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.lumine.mythic.bukkit.MythicBukkit;
import oneblock.skills.task.HomingTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InteractListener implements Listener {
    private final Main plugin;
    private final Map<Player, Long> soulreaperCooldown = new HashMap<>();

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
            arrow1.setKnockbackStrength(1);
            arrow1.setShooter(player);
            arrow1.setDamage(350*1.5);
            arrow1.setVelocity(event.getProjectile().getVelocity().rotateAroundY(Math.toRadians(15)));

            Arrow arrow2 = player.launchProjectile(Arrow.class);
            arrow2.setKnockbackStrength(1);
            arrow2.setDamage(350*1.5);
            arrow2.setShooter(player);
            arrow2.setVelocity(event.getProjectile().getVelocity().rotateAroundY(Math.toRadians(-15)));

            arrow1.setMetadata("luminescentArrow", new FixedMetadataValue(plugin, true));
            arrow2.setMetadata("luminescentArrow", new FixedMetadataValue(plugin, true));

            new BukkitRunnable(){
                @Override
                public void run(){
                    if (!arrow.isDead()){
                        for (Entity entity : arrow.getNearbyEntities(5, 5, 5)){
                            if (entity instanceof Boat || entity instanceof Minecart){
                                arrow.remove();
                                arrow1.remove();
                                arrow2.remove();
                                cancel();
                            }
                        }
                    }else{
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 1, 1);

            if (isOnRegion(player, "ruins")){
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isOnRegion(player, "ruins")) {
                            double minAngle = 6.283185307179586D;
                            Entity minEntity = null;
                            Arrow arrow = (Arrow) event.getProjectile();
                            for (Entity entity : arrow.getNearbyEntities(15.0D, 15.0D, 15.0D)) {
                                if (player.hasLineOfSight(entity) && (entity instanceof Monster) && !entity.isDead()) {
                                    Vector toTarget = entity.getLocation().toVector().clone().subtract(player.getLocation().toVector());
                                    double angle = arrow.getVelocity().angle(toTarget);
                                    if (angle < minAngle) {
                                        minAngle = angle;
                                        minEntity = entity;
                                    }
                                }
                            }
                            if (minEntity != null) {
                                new HomingTask(arrow1, (LivingEntity) minEntity, plugin);
                                new HomingTask(arrow2, (LivingEntity) minEntity, plugin);
                            }
                        }
                    }
                }.runTaskLater(plugin, 5L); // Delay execution by 1 second (20 ticks = 1 second)
            }
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
                    double additionalDamage = (ent.getMaxHealth() - ent.getHealth()) * 0.005;
                    increasedDamage += additionalDamage;
                }
                if (increasedDamage >= 20000){
                    increasedDamage = 20000;
                }
                event.setDamage(increasedDamage);
                player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 5f, 1f);
            }
        }
    }
    private boolean isOnRegion(Player player, String regionTarget) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());
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
    ////////// SOULREAPER KATANA //////////
    @EventHandler
    public void soulreaperKatana(PlayerInteractEntityEvent event) {
        if (!event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }

        Player player = event.getPlayer();
        Entity clickedEntity = event.getRightClicked();
        long Cooldown = 20000;

        if (!(clickedEntity instanceof LivingEntity)) {
            return;
        }

        ItemStack katana = player.getInventory().getItemInMainHand();
        if (katana.getType() != Material.NETHERITE_SWORD || !katana.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = katana.getItemMeta();
        assert itemMeta != null;
        if (!itemMeta.hasDisplayName() || !itemMeta.getDisplayName().equals("§8Soulreaper Katana")) {
            return;
        }

        if (soulreaperCooldown.containsKey(player)) {
            long currentTime = System.currentTimeMillis();
            long cooldownEnd = soulreaperCooldown.get(player);
            if (currentTime < cooldownEnd) {
                long remainingTime = (cooldownEnd - currentTime) / 1000; // Convert to seconds
                player.sendMessage(ChatColor.RED + "Skill is on cooldown " + remainingTime + " seconds");
                event.setCancelled(true);
                return;
            }
        }

        AttributeInstance attackDamage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        assert attackDamage != null;
        double originalDamage = attackDamage.getValue();
        double increasedDamage = originalDamage + Double.parseDouble(getLoreLineValue(katana, 7));

        LivingEntity livingEntity = (LivingEntity) clickedEntity;
        livingEntity.damage(increasedDamage);
        double health = livingEntity.getHealth();
        if (increasedDamage >= health) {
            if (Objects.requireNonNull(livingEntity.getLocation().getWorld()).getName().equals("s3")) {
                if (MythicBukkit.inst().getAPIHelper().isMythicMob(livingEntity)) {
                    collectSouls(katana);
                }
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 5f, 1f);

        long cooldownEnd = System.currentTimeMillis() + Cooldown;
        soulreaperCooldown.put(player, cooldownEnd);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            soulreaperCooldown.remove(player);
            player.sendMessage(ChatColor.GOLD + "Embrace of Eternity " + ChatColor.GREEN + "is ready to be used!");
        }, Cooldown / 50);
    }
    private String getLoreLineValue(ItemStack item, int lineNumber) {
        if (item == null || !item.hasItemMeta() || !Objects.requireNonNull(item.getItemMeta()).hasLore()) {
            return null;
        }

        List<String> lore = item.getItemMeta().getLore();
        if (lineNumber <= 0 || lineNumber > Objects.requireNonNull(lore).size()) {
            return null;
        }

        String line = lore.get(lineNumber - 1);
        String[] parts = line.split(": ");
        if (parts.length >= 2) {
            return parts[1].trim();
        }

        return null;
    }
    public void collectSouls(ItemStack item) {
        int souls = Integer.parseInt(getLoreLineValue(item, 7)) + 1;

        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        List<String> lore = itemMeta.getLore();
        String loreLine = "§8Souls Collected: " + souls;

        for (int i = 0; i < Objects.requireNonNull(lore).size(); i++) {
            String line = lore.get(i);
            if (line.startsWith("§8Souls Collected: ")) {
                lore.set(i, loreLine);
                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
                break;
            }
        }
    }
}
