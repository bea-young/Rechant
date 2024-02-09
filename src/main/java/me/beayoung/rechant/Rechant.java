package me.beayoung.rechant;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@SuppressWarnings("unused")
public final class Rechant extends JavaPlugin implements Listener {
    private Integer cost;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Configuring...");
        saveDefaultConfig();

        this.cost = getConfig().getInt("cost");

        getLogger().info("Registering events...");
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Plugin enabled!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void prepareToRecycle(PrepareAnvilEvent event) {
        ItemStack slotOne = event.getInventory().getItem(0); // This will be the tool we are 'recycling' the enchantments from
        ItemStack slotTwo = event.getInventory().getItem(1); // This will be the book we are putting them on

        if (slotOne == null || slotTwo == null) return;

        if (slotOne.getType().equals(Material.ENCHANTED_BOOK)) return;
        if (!slotTwo.getType().equals(Material.BOOK)) return;
        if (!(slotTwo.getAmount() == 1)) return;

        if (slotOne.getEnchantments().isEmpty()) return;

        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.getPersistentDataContainer().set(NamespacedKey.minecraft("rechant"), PersistentDataType.STRING, "true");

        result.setItemMeta(resultMeta);

        EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) result.getItemMeta();

        slotOne.getEnchantments().forEach((enchantment, level) -> enchantMeta.addStoredEnchant(enchantment, level, true));

        result.setItemMeta(enchantMeta);
        event.setResult(result);

        getServer().getScheduler().runTask(this, () -> event.getInventory().setRepairCost(this.cost));
    }

    @EventHandler
    public void doRecycle(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() != InventoryType.ANVIL) return;

        if (event.getSlot() != 2) return;
        if (!Objects.equals(Objects.requireNonNull(event.getInventory().getItem(2)).getItemMeta().getPersistentDataContainer().get(NamespacedKey.minecraft("rechant"), PersistentDataType.STRING), "true")) return;

        ItemStack slotOne = event.getInventory().getItem(0);

        if (slotOne == null) return;

        slotOne.getEnchantments().forEach((enchantment, level) -> slotOne.removeEnchantment(enchantment));

        getServer().getScheduler().runTask(this, () -> event.getInventory().setItem(0, slotOne));
    }
}
