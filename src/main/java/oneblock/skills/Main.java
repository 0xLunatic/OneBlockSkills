package oneblock.skills;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import oneblock.skills.heads.HeadList;
import oneblock.skills.mechanic.IceKingSummoner;
import oneblock.skills.mechanic.JelloInteractListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.print("Custom Skills by Mornov Enabled!");
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorListener(this), this);
        getServer().getPluginManager().registerEvents(new MornovOnly(this), this);
        getServer().getPluginManager().registerEvents(new JelloInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new IceKingSummoner(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        removeCustomNameArmorStands();
        clearCacheFile();
    }

    private void removeCustomNameArmorStands() {
        List<String> customNames = readCustomNamesFromFile();
        if (customNames.isEmpty()) {
            return; // No custom names found in the file
        }

        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                if (armorStand.getCustomName() != null && customNames.contains(armorStand.getCustomName())) {
                    armorStand.remove();
                }
                if (armorStand.getCustomName() != null && armorStand.getCustomName().contains("'s Progress")){
                    armorStand.remove();
                }
            }
        }
    }

    private List<String> readCustomNamesFromFile() {
        List<String> customNames = new ArrayList<>();
        File file = new File("plugins/ArmorStandCache/armorStandCache.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                customNames.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return customNames;
    }

    private void clearCacheFile() {
        try {
            File file = new File("plugins/ArmorStandCache/armorStandCache.txt");
            if (file.exists()) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("");
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack createSkull(String url, String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
        if (url.isEmpty()) return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));

        try {
            assert headMeta != null;
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
            error.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }

    public static ItemStack getHead(String name) {
        for (HeadList head : HeadList.values()) {
            if (head.getName().equals(name)) {
                return head.getItemStack();
            }
        }
        return null;

    }
}
