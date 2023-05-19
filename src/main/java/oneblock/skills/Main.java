package oneblock.skills;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.print("Custom Skills by Mornov Enabled!");
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
