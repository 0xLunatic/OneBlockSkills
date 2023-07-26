package oneblock.skills.mechanic;

import oneblock.skills.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MyeshaListener implements Listener, CommandExecutor {
    private Main plugin;
    String[] questList = {
            "WheatHarvest",
            "PumpkinPatch",
            "CarrotHarvest",
            "PotatoHarvest",
            "MelonMania",
            "BeetrootBonanza",
            "SugarcaneSuccess",
            "MushroomMayhem",
            "ChickenChampion",
            "SheepShearer",
            "CowConnoisseur",
            "PigPioneer",
            "FishermansFeast",
            "HoneyHarvest",
            "TreeTapper",
            "BambooBounty",
            "SkeletonSlayer",
            "ZombieHunter",
            "CreeperCrusher",
            "SpiderSlayer",
            "EndermanExterminator",
            "BlazeBane",
            "GhastHunter",
            "SlimeSlayer",
            "WitchHunter",
            "GuardianSlayer",
            "GemHunter",
            "Woodcutter",
            "Fisherman"
    };
    HashMap<Player, String> playerQuests = new HashMap<>();

    public MyeshaListener(Main plugin) {
        this.plugin = plugin;
        for (String quest : questList) {
            String command = "qa resetAndRemoveQuestForAllPlayers " + quest;
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            openMyeshaGUI(player);
        } else {
            sender.sendMessage("This command can only be executed by a player.");
        }
        return true;
    }

    public void openMyeshaGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new MyeshaHolder(), 45, "Farming Composter");

        // Fill the GUI with black stained glass panes
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta paneMeta = blackPane.getItemMeta();
        assert paneMeta != null;
        paneMeta.setDisplayName(" ");
        blackPane.setItemMeta(paneMeta);
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, blackPane);
        }

        // Add the slots for Farming Info, Myesha Quest, and Myesha Crafting
        ItemStack farmingInfoItem = new ItemStack(Material.PAPER);
        ItemMeta farmingInfoMeta = farmingInfoItem.getItemMeta();
        assert farmingInfoMeta != null;
        farmingInfoMeta.setDisplayName("§eFarming Info");
        farmingInfoMeta.setLore(Arrays.asList(
                "§7Learn about advanced farming techniques",
                "§7and maximize your crop yields."
        ));
        farmingInfoItem.setItemMeta(farmingInfoMeta);
        gui.setItem(20, farmingInfoItem);

        String currentQuest = playerQuests.get(player);
        if (currentQuest != null) {

            ItemStack myeshaQuestItem = new ItemStack(Material.BOOK);
            ItemMeta myeshaQuestMeta = myeshaQuestItem.getItemMeta();
            assert myeshaQuestMeta != null;
            myeshaQuestMeta.setDisplayName("§dMyesha Quest");
            myeshaQuestMeta.setLore(Arrays.asList(
                    "§7Embark on exciting quests",
                    "§7and earn unique rewards along the way.",
                    "",
                    "§7Quest Info : Check your /quest!"

            ));
            myeshaQuestItem.setItemMeta(myeshaQuestMeta);
            gui.setItem(22, myeshaQuestItem);
        } else {
            String randomQuest = generateRandomQuest();
            setOnQuest(player, randomQuest);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "qa forcegive " + player.getName() + " " + playerQuests.get(player));

            ItemStack myeshaQuestItem = new ItemStack(Material.BOOK);
            ItemMeta myeshaQuestMeta = myeshaQuestItem.getItemMeta();
            assert myeshaQuestMeta != null;
            myeshaQuestMeta.setDisplayName("§dMyesha Quest");
            myeshaQuestMeta.setLore(Arrays.asList(
                    "§7Embark on exciting quests",
                    "§7and earn unique rewards along the way.",
                    "",
                    "§aMyesha just give you new Quest!"

            ));
            myeshaQuestItem.setItemMeta(myeshaQuestMeta);
            gui.setItem(22, myeshaQuestItem);
        }

        ItemStack myeshaCraftingItem = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta myeshaCraftingMeta = myeshaCraftingItem.getItemMeta();
        assert myeshaCraftingMeta != null;
        myeshaCraftingMeta.setDisplayName("§5Myesha Crafting");
        myeshaCraftingMeta.setLore(Arrays.asList(
                "§7Discover new crafting recipes",
                "§7to create powerful items and tools."
        ));
        myeshaCraftingItem.setItemMeta(myeshaCraftingMeta);
        gui.setItem(24, myeshaCraftingItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void MyeshaGuiInteract(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        Inventory clickedInventory = event.getClickedInventory();

        // Check if the clicked item and inventory are not null
        if (clickedItem != null && clickedInventory != null) {
            // Check if the clicked inventory is the main Myesha GUI
            if (clickedInventory.getHolder() instanceof MyeshaHolder) {
                event.setCancelled(true); // Cancel the click event

                // Get the display name of the clicked item
                String itemName = clickedItem.getItemMeta().getDisplayName();

                // Check which main GUI item was clicked and open the corresponding sub-GUI
                if (itemName.equals("§eFarming Info")) {
                    openFarmingInfoGUI(player);
                } else if (itemName.equals("§5Myesha Crafting")) {
                    openMyeshaCraftingGUI(player);
                }
            }
        }
    }

    private void openFarmingInfoGUI(Player player) {
        Inventory farmingInfoGUI = Bukkit.createInventory(new MyeshaFarmingInformation(), 45, "Farming Info");

        // Set the items to Glass Panes
        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        // Set the display name for the items
        ItemMeta glassPaneMeta = glassPane.getItemMeta();
        glassPaneMeta.setDisplayName("§7");

        // Apply the modified item meta to the glass panes
        glassPane.setItemMeta(glassPaneMeta);

        // Fill the entire GUI with glass panes
        for (int i = 0; i < 45; i++) {
            farmingInfoGUI.setItem(i, glassPane);
        }

        // Set the items to Paper and Book
        ItemStack itemFarmingTechniques = new ItemStack(Material.PAPER);
        ItemStack itemSectionsAvailable = new ItemStack(Material.PAPER);
        ItemStack itemWelcomeMessage = new ItemStack(Material.BOOK);
        ItemStack itemFarmingFestival = new ItemStack(Material.PAPER);
        ItemStack backButton = new ItemStack(Material.ARROW);

        // Set the display name for the items
        ItemMeta metaFarmingTechniques = itemFarmingTechniques.getItemMeta();
        metaFarmingTechniques.setDisplayName("§e§lFarming Techniques and Tips:");

        ItemMeta metaSectionsAvailable = itemSectionsAvailable.getItemMeta();
        metaSectionsAvailable.setDisplayName("§e§lSections Available:");

        ItemMeta metaWelcomeMessage = itemWelcomeMessage.getItemMeta();
        metaWelcomeMessage.setDisplayName("§6Welcome to the Farming Info!");

        ItemMeta metaFarmingFestival = itemFarmingFestival.getItemMeta();
        metaFarmingFestival.setDisplayName("§a§lFestival: §6Farming Festival");

        ItemMeta backButtonMeta = backButton.getItemMeta();
        backButtonMeta.setDisplayName("§c§lCLOSE");

        // Set the lore for the items
        List<String> loreFarmingTechniques = new ArrayList<>();
        loreFarmingTechniques.add("");
        loreFarmingTechniques.add("§71. Ensure proper soil preparation");
        loreFarmingTechniques.add("§72. Optimize watering and irrigation");
        loreFarmingTechniques.add("§73. Use organic fertilizers for healthier crops");
        loreFarmingTechniques.add("§74. Implement crop rotation to prevent soil depletion");
        loreFarmingTechniques.add("§75. Protect plants from pests and diseases");
        loreFarmingTechniques.add("§76. Regularly monitor and maintain nutrient levels");
        loreFarmingTechniques.add("§77. Harvest crops at the right time for optimal yield");
        metaFarmingTechniques.setLore(loreFarmingTechniques);

        List<String> loreSectionsAvailable = new ArrayList<>();
        loreSectionsAvailable.add("");
        loreSectionsAvailable.add("§71. §aPlant Growth: §7Learn about factors");
        loreSectionsAvailable.add("   §7affecting plant growth and ways to promote it.");
        loreSectionsAvailable.add("§72. §aCrop Rotation: §7Discover the benefits");
        loreSectionsAvailable.add("   §7and methods of crop rotation.");
        loreSectionsAvailable.add("§73. §aFertilization Methods: §7Explore different");
        loreSectionsAvailable.add("   §7techniques for fertilizing your crops.");
        loreSectionsAvailable.add("§74. §aPest and Disease Control: §7Find out how");
        loreSectionsAvailable.add("   §7to protect your plants from common threats.");
        metaSectionsAvailable.setLore(loreSectionsAvailable);

        List<String> loreWelcomeMessage = new ArrayList<>();
        loreWelcomeMessage.add("");
        loreWelcomeMessage.add("§7This GUI provides useful information");
        loreWelcomeMessage.add("§7about various farming techniques and");
        loreWelcomeMessage.add("§7tips to maximize your crop yields.");
        loreWelcomeMessage.add("");
        loreWelcomeMessage.add("§7Explore different sections to learn");
        loreWelcomeMessage.add("§7about plant growth, crop rotation,");
        loreWelcomeMessage.add("§7fertilization methods, and more!");
        loreWelcomeMessage.add("");
        loreWelcomeMessage.add("§6Happy farming!");
        metaWelcomeMessage.setLore(loreWelcomeMessage);

        List<String> loreFarmingFestival = new ArrayList<>();
        loreFarmingFestival.add("");
        loreFarmingFestival.add("§7This festival will start every 3 hours of real-life time.");
        loreFarmingFestival.add("§7Started every:");
        loreFarmingFestival.add("");
        loreFarmingFestival.add("§7- 9:00 AM (GMT+7) / 2:00 AM (UTC)");
        loreFarmingFestival.add("§7- 12:00 PM (GMT+7) / 5:00 AM (UTC)");
        loreFarmingFestival.add("§7- 3:00 PM (GMT+7) / 8:00 AM (UTC)");
        loreFarmingFestival.add("§7- 6:00 PM (GMT+7) / 11:00 AM (UTC)");
        loreFarmingFestival.add("§7- 9:00 PM (GMT+7) / 2:00 PM (UTC)");
        loreFarmingFestival.add("");
        loreFarmingFestival.add("§7Leaderboard of this festival will get Money and Items Reward!");
        metaFarmingFestival.setLore(loreFarmingFestival);

        // Apply the modified item meta to the items
        itemFarmingTechniques.setItemMeta(metaFarmingTechniques);
        itemSectionsAvailable.setItemMeta(metaSectionsAvailable);
        itemWelcomeMessage.setItemMeta(metaWelcomeMessage);
        itemFarmingFestival.setItemMeta(metaFarmingFestival);
        backButton.setItemMeta(backButtonMeta);

        // Add the items to the GUI at specific slots
        farmingInfoGUI.setItem(20, itemFarmingTechniques);
        farmingInfoGUI.setItem(22, itemSectionsAvailable);
        farmingInfoGUI.setItem(4, itemWelcomeMessage);
        farmingInfoGUI.setItem(24, itemFarmingFestival);
        farmingInfoGUI.setItem(40, backButton);

        // Open the GUI for the player
        player.openInventory(farmingInfoGUI);
    }

    @EventHandler
    public void FarmingInfoGuiInteract(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Farming Info")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null) {
                if (clickedItem.getType() == Material.ARROW) {
                    player.closeInventory();
                }
            }
        }
    }

    private void openMyeshaCraftingGUI(Player player) {
        executeConsoleCommand("mi stations open myesha " + player.getName());
    }

    public void executeConsoleCommand(String command) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @SuppressWarnings("NullableProblems")
    private static class MyeshaHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    @SuppressWarnings("NullableProblems")
    private static class MyeshaFarmingInformation implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    @SuppressWarnings("NullableProblems")
    private static class MyeshaQuest implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private String generateRandomQuest() {
        Random random = new Random();
        int index = random.nextInt(questList.length);
        return questList[index];
    }

    private void setOnQuest(Player player, String quest) {
        if (playerQuests.get(player) == null){
            playerQuests.put(player, quest);
            player.sendMessage("§aYou just got new quest from Myesha!");
            player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 100f, 1f);
            player.closeInventory();
        }
    }
}

