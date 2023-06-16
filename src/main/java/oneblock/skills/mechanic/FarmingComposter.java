package oneblock.skills.mechanic;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import oneblock.skills.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class FarmingComposter implements Listener {
    private Main plugin;

    public FarmingComposter(Main plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onComposterInteract(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block != null && block.getType() == Material.HOPPER) {
            if (isOnRegion(player, "myesha")){
                event.setCancelled(true);
                openGUI(player);
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
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        int clickedSlot = event.getRawSlot();
        if (inventory.getHolder() instanceof GUIHolder && clickedSlot >= 0 && clickedSlot <= 8) {
            if (clickedSlot == 0 || clickedSlot == 6 || clickedSlot == 8){
                event.setCancelled(true);
            }
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.SLIME_BALL && clickedSlot == 7) {
                event.setCancelled(true);
                // Detect item names and amounts in slots 1-5
                int totalAmount = 0;
                Set<String> removedItems = new HashSet<>();
                for (int slot = 1; slot <= 5; slot++) {
                    ItemStack item = inventory.getItem(slot);
                    if (item != null && isExcludedItemName(Objects.requireNonNull(item.getItemMeta()).getDisplayName())) {
                        totalAmount += item.getAmount();
                        removedItems.add(item.getItemMeta().getDisplayName());
                    }
                }
                // Check if the total amount is equal to 64x5
                if (totalAmount == 64 * 5) {
                    // Do something with the items
                    for (int slot = 1; slot <= 5; slot++) {
                        inventory.setItem(slot, null); // Clear the slot
                    }
                    String itemNames = String.join(", ", removedItems);
                    Bukkit.broadcastMessage("§e§lCOMPOST §7» §e" + player.getName() + " §fjust composted " + itemNames + "§f!");
                    for (Player onlinePlayers : Bukkit.getOnlinePlayers()){
                        onlinePlayers.playSound(onlinePlayers.getLocation(), Sound.ENTITY_PLAYER_BURP, 20f, 0f);
                    }
                    int randomAmount = (int) (Math.random() * 15) + 1;
                    String command = "mi give MATERIAL FARMING_BLOCK " + player.getName() + " " + randomAmount;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    double chance = 0.3; // 10% chance (adjust this as desired)
                    Random random = new Random();
                    if (random.nextDouble() <= chance) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crates key give " +player.getName()+ " 1");
                        Bukkit.broadcastMessage("§e§lCOMPOST §7» §e" + player.getName() + " §fjust found §6Farming Key§f!");
                        for (Player onlinePlayers : Bukkit.getOnlinePlayers()){
                            onlinePlayers.playSound(onlinePlayers.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 20f, 0f);
                        }
                    }
                } else {
                    player.sendMessage("§cYou must fill all the empty slots with 64 of each block!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10f, 1f);
                }
            } else if (clickedItem != null) {
                String displayName = Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName();
                if (!isExcludedItemName(displayName)) {
                    return;
                }
                if (clickedSlot >= 1 && clickedSlot <= 5 && event.getClick() != ClickType.DROP) {
                    int clickedItemAmount = clickedItem.getAmount();
                    if (clickedItemAmount != 64) {
                        Map<Integer, ItemStack> excessItems = player.getInventory().addItem(clickedItem);
                        if (!excessItems.isEmpty()) {
                            excessItems.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
                        }
                        inventory.setItem(clickedSlot, null);
                    }
                }
            }
        }
    }




    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof GUIHolder) {
            for (int slot = 1; slot <= 5; slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item != null) {
                    // Return the item back to the player's inventory
                    Map<Integer, ItemStack> excessItems = player.getInventory().addItem(item);
                    if (!excessItems.isEmpty()) {
                        excessItems.values().forEach(dropItem -> player.getWorld().dropItem(player.getLocation(), dropItem));
                    }
                    inventory.setItem(slot, null); // Clear the slot in the GUI
                }
            }
        }
    }

    private boolean isExcludedItemName(String displayName) {
        List<String> excludedItemNames = Arrays.asList(
                "§aSugarcane Block",
                "§aWheat Block",
                "§aCarrot Block",
                "§aPumpkin Block",
                "§aBeetroot Block",
                "§aPotato Block",
                "§aKelp Block",
                "§aBamboo Block",
                "§aCactus Block",
                "§aCocoa Block"
        );
        return excludedItemNames.contains(displayName);
    }

    private void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder(), 9, "Farming Composter");

        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        assert paneMeta != null;
        paneMeta.setDisplayName("§c");
        pane.setItemMeta(paneMeta);
        for (int i = 0; i < 9 ; i++){
            if (i != 1 && i != 2 && i != 3 && i != 4 && i != 5){
                gui.setItem(i, pane);
            }
            if (i == 7){
                ItemStack confirm = new ItemStack(Material.SLIME_BALL);
                ItemMeta confirmMeta = confirm.getItemMeta();
                assert confirmMeta != null;
                confirmMeta.setDisplayName("§aClick to Compost!");
                List<String> lore = new ArrayList<>();
                lore.add("§a");
                lore.add("§7Click to compost all your §aFarming Blocks§7.");

                confirmMeta.setLore(lore);
                confirm.setItemMeta(confirmMeta);

                gui.setItem(i, confirm);
            }
        }
        player.openInventory(gui);
    }
    @SuppressWarnings("NullableProblems")
    private static class GUIHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
