package oneblock.skills;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MornovOnly implements Listener {
    private Main plugin;
    private final ArrayList<Player> cdballoon = new ArrayList<>();
    private final ArrayList<Player> fluxcd = new ArrayList<>();
    public static ArrayList<ArmorStand> standList = new ArrayList<>();

    public MornovOnly(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void ShootBalloon(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getItem() == null) {
        } else if (e.getItem().getType().equals(Material.BLAZE_ROD)) {
            if (!Objects.requireNonNull(e.getItem().getItemMeta()).hasDisplayName()) {
            } else if (e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GOLD + "Mornov Shooter")) {
                if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    if (!cdballoon.contains(p)) {

                        Random rand = new Random();
                        int randomNumber = rand.nextInt(6);
                        Vector dir = p.getEyeLocation().getDirection().multiply(2);
                        ArmorStand balloon = p.getWorld().spawn(p.getLocation().add(0, -0.1, 0), ArmorStand.class);
                        if (randomNumber == 1) {
                            balloon.setHelmet(Main.getHead("lightblue"));
                        } else if (randomNumber == 2) {
                            balloon.setHelmet(Main.getHead("blue"));
                        } else if (randomNumber == 3) {
                            balloon.setHelmet(Main.getHead("lime"));
                        } else if (randomNumber == 4) {
                            balloon.setHelmet(Main.getHead("green"));
                        } else if (randomNumber == 5) {
                            balloon.setHelmet(Main.getHead("orange"));
                        } else if (randomNumber == 6) {
                            balloon.setHelmet(Main.getHead("pink"));
                        }
                        balloon.setGravity(false);
                        balloon.setInvulnerable(true);
                        balloon.setVisible(false);
                        ForwardOnly(balloon, dir, p);
                        p.playSound(p.getLocation(), Sound.ENTITY_GHAST_AMBIENT, 1F, 1F);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> cdballoon.remove(p), 3);
                    }
                }
            }
        }
    }


    private void ForwardOnly(ArmorStand balloon, Vector dir, Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                EulerAngle getPose = balloon.getHeadPose();
                EulerAngle setPose = getPose.add(0, 0.5, 0);

                balloon.setHeadPose(setPose);

                double distance = p.getLocation().distance(balloon.getLocation());
                if (distance < 25) {
                    balloon.teleport(balloon.getLocation().add(dir));
                } else {
                    balloon.remove();
                    Firework f = Objects.requireNonNull(balloon.getLocation().getWorld()).spawn(balloon.getLocation().add(0, 1.5, 0), Firework.class);
                    FireworkMeta fm = f.getFireworkMeta();
                    f.setMetadata("nodamage", new FixedMetadataValue(plugin, true));
                    randomcolor(fm);
                    f.setSilent(true);
                    fm.setPower(0);
                    f.setFireworkMeta(fm);
                    f.detonate();
                    if (balloon.isDead()) {
                        cancel();
                    }
                }
                if (balloon.getLocation().getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
                    for (Entity ent : balloon.getNearbyEntities(3, 3, 3)) {
                        if (ent instanceof Player) {
                            if(!ent.hasMetadata("NPC")) {
                                ent.setVelocity(balloon.getLocation().getDirection().multiply(-1.5));
                            }
                        }
                    }
                    balloon.remove();
                    Firework f = Objects.requireNonNull(balloon.getLocation().getWorld()).spawn(balloon.getLocation().add(0, 1.5, 0), Firework.class);
                    FireworkMeta fm = f.getFireworkMeta();
                    f.setMetadata("nodamage", new FixedMetadataValue(plugin, true));
                    randomcolor(fm);
                    f.setSilent(true);
                    fm.setPower(0);
                    f.setFireworkMeta(fm);
                    f.detonate();
                    if (balloon.isDead()) {
                        cancel();
                    }
                } else {
                    for (Entity ent : balloon.getNearbyEntities(1, 1, 1)) {
                        if (ent instanceof Monster) {
                            if (ent.getName().equalsIgnoreCase("§c§lClown")) {
                                p.sendMessage("§cClown has absorp your balloon!");
                                balloon.remove();
                                cancel();
                            } else {
                                ((Monster) ent).damage(15);
                                balloon.remove();
                                Firework f = Objects.requireNonNull(balloon.getLocation().getWorld()).spawn(balloon.getLocation().add(0, 1.5, 0), Firework.class);
                                FireworkMeta fm = f.getFireworkMeta();
                                f.setMetadata("nodamage", new FixedMetadataValue(plugin, true));
                                randomcolor(fm);
                                f.setSilent(true);
                                fm.setPower(0);
                                f.setFireworkMeta(fm);
                                f.detonate();
                                if (balloon.isDead()) {
                                    cancel();
                                }
                            }
                            if (ent instanceof Animals) {
                                ((Animals) ent).damage(5);
                                balloon.remove();
                                Firework f = Objects.requireNonNull(balloon.getLocation().getWorld()).spawn(balloon.getLocation().add(0, 1.5, 0), Firework.class);
                                FireworkMeta fm = f.getFireworkMeta();
                                f.setMetadata("nodamage", new FixedMetadataValue(plugin, true));
                                randomcolor(fm);
                                f.setSilent(true);
                                fm.setPower(0);
                                f.setFireworkMeta(fm);
                                f.detonate();
                                if (balloon.isDead()) {
                                    cancel();
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }
    private void randomcolor(FireworkMeta fm){
        Random rand = new Random();
        int randomNumber = rand.nextInt(6);
        if (randomNumber == 1) {
            fm.addEffect(FireworkEffect.builder()
                    .flicker(false)
                    .trail(false)
                    .withColor(Color.AQUA)
                    .withColor(Color.PURPLE)
                    .with(FireworkEffect.Type.STAR)
                    .withFlicker()
                    .build());
        } else if (randomNumber == 2) {
            fm.addEffect(FireworkEffect.builder()
                    .flicker(false)
                    .trail(false)
                    .withColor(Color.GREEN)
                    .withColor(Color.YELLOW)
                    .with(FireworkEffect.Type.STAR)
                    .withFlicker()
                    .build());

        } else if (randomNumber == 3) {
            fm.addEffect(FireworkEffect.builder()
                    .flicker(false)
                    .trail(false)
                    .withColor(Color.RED)
                    .withColor(Color.WHITE)
                    .with(FireworkEffect.Type.STAR)
                    .withFlicker()
                    .build());
        } else if (randomNumber == 4) {
            fm.addEffect(FireworkEffect.builder()
                    .flicker(false)
                    .trail(false)
                    .withColor(Color.GREEN)
                    .withColor(Color.YELLOW)
                    .with(FireworkEffect.Type.STAR)
                    .withFlicker()
                    .build());
        } else if (randomNumber == 5) {
            fm.addEffect(FireworkEffect.builder()
                    .flicker(false)
                    .trail(false)
                    .withColor(Color.FUCHSIA)
                    .withColor(Color.PURPLE)
                    .with(FireworkEffect.Type.STAR)
                    .withFlicker()
                    .build());
        } else if (randomNumber == 6) {
            fm.addEffect(FireworkEffect.builder()
                    .withColor(Color.SILVER)
                    .withColor(Color.LIME)
                    .with(FireworkEffect.Type.STAR)
                    .withFlicker()
                    .build());
        }

    }
    @EventHandler
    public void SpiritFlux(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getItem() == null) {
        } else if (e.getItem().getType().equals(Material.PLAYER_HEAD)) {
            if (!Objects.requireNonNull(e.getItem().getItemMeta()).hasDisplayName()) {
            } else if (e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GOLD + "Spirit Power Orb")) {
                if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    if (!e.getPlayer().getWorld().getName().equalsIgnoreCase("Samael") && !e.getPlayer().getWorld().getName().equalsIgnoreCase("void-sb")) {
                        if (!fluxcd.contains(p)) {
                            if (p.getFoodLevel() > 10) {
                                e.setCancelled(true);
                                ArmorStand spirit = p.getWorld().spawn(p.getLocation().add(0, 1, 0), ArmorStand.class);
                                ArmorStand spiritholo = p.getWorld().spawn(p.getLocation().add(0, 1.2, 0), ArmorStand.class);
                                spirit.setHelmet(Main.getHead("spirit"));
                                spirit.setVisible(false);
                                spirit.setInvulnerable(true);
                                spirit.setCustomName("spiritflux");
                                spirit.setGravity(false);

                                spiritholo.setVisible(false);
                                spiritholo.setInvulnerable(true);
                                spiritholo.setCustomName("§6" + (p.getName()) + "'s Spirit Power Orb");
                                spiritholo.setCustomNameVisible(true);
                                spiritholo.setGravity(false);
                                p.setFoodLevel(p.getFoodLevel() - 10);
                                fluxcd.add(p);
                                standList.add(spirit);
                                standList.add(spiritholo);
                                moveUpSpirit(spirit, spiritholo, p);
                                secondDamageSpirit(p, spirit, spiritholo);
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                    spirit.remove();
                                    fluxcd.remove(p);
                                    spiritholo.remove();
                                    standList.remove(spirit);
                                    standList.remove(spiritholo);
                                    p.sendMessage("§aYour flux has been removed!");
                                }, 200);
                            } else {
                                e.setCancelled(true);
                                p.sendMessage("§cYou don't have enough mana!");
                            }
                        } else {
                            e.setCancelled(true);
                            p.sendMessage("§cYou can't place more flux!");
                        }

                    } else {
                        p.sendMessage("§cThis area is disabled for this item!");
                    }
                }

            }
        }
    }

    private void secondDamageSpirit(Player p, ArmorStand spirit, ArmorStand spiritholo) {
        new BukkitRunnable() {
            int time = 10;

            @Override
            public void run() {
                spiritholo.setCustomName("§6" + (p.getName()) + "'s Spirit Power Orb §e§l" + time + "s");
                if (time > 0) {
                    if (!spirit.isDead()) {
                        time--;
                        for (Entity ent : spirit.getNearbyEntities(18, 18, 18)) {
                            if (ent instanceof Player) {
                                ((Player) ent).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2));
                            }
                            if (ent instanceof Monster) {
                                Objects.requireNonNull(spirit.getLocation().getWorld()).playSound(spirit.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 5, 2);
                                Objects.requireNonNull(ent.getLocation().getWorld()).playSound(ent.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 5, 0);
                                ent.getLocation().getWorld().playEffect(ent.getLocation(), Effect.MOBSPAWNER_FLAMES, 2004);
                                drawLineSpiritFlux(spirit.getEyeLocation(), ((Monster) ent).getEyeLocation(), 1D);
                                if (((Monster) ent).getHealth() < 2000) {
                                    Monster monster = (Monster) ent;
                                    monster.damage(monster.getHealth() * 0.01);
                                }
                                if (((Monster) ent).getHealth() > 2000) {
                                    Monster monster = (Monster) ent;
                                    monster.damage(monster.getHealth() * 0.01);
                                }
                                if (((Monster) ent).getHealth() > 2000) {
                                    Monster monster = (Monster) ent;
                                    monster.damage(monster.getHealth() * 0.02);
                                }
                                if (((Monster) ent).getHealth() > 4000) {
                                    Monster monster = (Monster) ent;
                                    monster.damage(monster.getHealth() * 0.03);
                                }
                                if (((Monster) ent).getHealth() > 6000) {
                                    Monster monster = (Monster) ent;
                                    monster.damage(monster.getHealth() * 0.04);
                                }
                                if (((Monster) ent).getHealth() > 8000) {
                                    Monster monster = (Monster) ent;
                                    monster.damage(monster.getHealth() * 0.05);
                                }
                                if (((Monster) ent).getHealth() > 10000) {
                                    Monster monster = (Monster) ent;
                                    monster.damage(monster.getHealth() * 0.06);
                                }
                                if (((Monster) ent).getHealth() > 12000) {
                                    Monster monster = (Monster) ent;
                                    monster.damage(monster.getHealth() * 0.07);
                                }
                            }
                        }
                    }
                }
                if(time == 0){
                    spirit.remove();
                    spiritholo.remove();
                }
            }
        }.runTaskTimer(plugin, 20,20);
    }


    private void moveUpSpirit(ArmorStand spirit, ArmorStand spiritholo, Player p) {
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
                    standList.remove(spirit);
                    standList.remove(spiritholo);
                    cancel();
                    return;
                }
                if(spirit.getWorld().getName().equalsIgnoreCase(p.getWorld().getName())) {
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
                    spirit.remove();
                    spiritholo.remove();
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }
    public void drawLineSpiritFlux(
            /* Would be your orange wool */Location point1,
            /* Your white wool */ Location point2,
            /*Space between each particle*/double space
    ) {

        World world = point1.getWorld();

        /*Throw an error if the points are in different worlds*/

        /*Distance between the two particles*/
        double distance = point1.distance(point2);

        /* The points as vectors */
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();

        /* Subtract gives you a vector between the points, we multiply by the space*/
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);

        /*The distance covered*/
        double covered = 0;

        /* We run this code while we haven't covered the distance, we increase the point by the space every time*/
        for (; covered < distance; p1.add(vector)) {
            /*Spawn the particle at the point*/
            assert world != null;
            world.spawnParticle(Particle.FLAME, p1.getX(), p1.getY(), p1.getZ(), 10, 0F, 0F, 0F, 0F);

            /* We add the space covered */
            covered += space;
        }
    }
}
