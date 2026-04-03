package com.highpalace.dynastycore.listener;

import io.th0rgal.oraxen.api.OraxenFurniture;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureInteractEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CalligraphyTableListener implements Listener {
    private final JavaPlugin plugin;

    private static final String EMPTY_TABLE = "calligraphy_table";
    private static final String TABLE_WITH_SCROLL = "calligraphy_table_with_scroll";
    private static final String SCROLL_ITEM = "scroll_hongtu";

    public CalligraphyTableListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFurnitureInteract(OraxenFurnitureInteractEvent event) {
        String furnitureId = event.getMechanic().getItemID();
        Player player = event.getPlayer();
        Entity baseEntity = event.getBaseEntity();
        ItemStack itemInHand = event.getItemInHand();

        if (furnitureId.equals(EMPTY_TABLE)) {
            // Right-click empty desk with scroll → place scroll on desk
            if (itemInHand != null && isOraxenItem(itemInHand, SCROLL_ITEM)) {
                event.setCancelled(true);
                Location loc = baseEntity.getLocation();
                float yaw = loc.getYaw();

                // Remove empty table
                OraxenFurniture.remove(loc, player);

                // Place table with scroll
                OraxenFurniture.place(TABLE_WITH_SCROLL, loc, yaw, BlockFace.UP);

                // Consume one scroll from hand
                itemInHand.setAmount(itemInHand.getAmount() - 1);

                player.sendMessage(Component.text("You place the scroll upon the desk.", NamedTextColor.GRAY));
            }
        } else if (furnitureId.equals(TABLE_WITH_SCROLL)) {
            // Right-click desk with scroll (empty hand) → retrieve scroll
            if (itemInHand == null || itemInHand.getType().isAir()) {
                event.setCancelled(true);
                Location loc = baseEntity.getLocation();
                float yaw = loc.getYaw();

                // Remove table with scroll
                OraxenFurniture.remove(loc, player);

                // Place empty table back
                OraxenFurniture.place(EMPTY_TABLE, loc, yaw, BlockFace.UP);

                // Give scroll back
                ItemStack scroll = OraxenItems.getItemById(SCROLL_ITEM).build();
                player.getInventory().addItem(scroll);

                player.sendMessage(Component.text("You retrieve the scroll from the desk.", NamedTextColor.GRAY));
            }
        }
    }

    private boolean isOraxenItem(ItemStack item, String id) {
        String oraxenId = OraxenItems.getIdByItem(item);
        return id.equals(oraxenId);
    }
}
