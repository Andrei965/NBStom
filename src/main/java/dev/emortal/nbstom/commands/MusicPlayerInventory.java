package dev.emortal.nbstom.commands;

import dev.emortal.nbstom.MusicDisc;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class MusicPlayerInventory {

    private static MusicPlayerInventory INSTANCE;
    private final Inventory inventory;

    public MusicPlayerInventory() {
        Component inventoryTitle = Component.text("Music Discs", NamedTextColor.BLACK);
        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, inventoryTitle);

        var i = 10;
        for (MusicDisc disc : MusicDisc.values()) {
            if ((i + 1) % 9 == 0) i += 2;

            inventory.setItemStack(i, ItemStack.builder(disc.getMaterial())
                    .build());

            i++;
        }

        inventory.setItemStack(40, ItemStack.builder(Material.BARRIER)
                .set(DataComponents.ITEM_NAME, Component.text("Stop", NamedTextColor.RED, TextDecoration.BOLD))
                .build());

        inventory.eventNode().addListener(InventoryPreClickEvent.class, e -> {
            e.setCancelled(true);

            if (e.getClickedItem() == ItemStack.AIR) return;

            if (e.getSlot() == 40) {
                MusicCommand.stop(e.getPlayer());
                return;
            }

            MusicDisc nowPlayingDisc = MusicDisc.fromMaterial(e.getClickedItem().material());

            MusicCommand.playDisc(e.getPlayer(), nowPlayingDisc.getShortName());
        });

        this.inventory = inventory;
    }

    public static Inventory getInventory() {
        if (INSTANCE == null) {
            INSTANCE = new MusicPlayerInventory();
        }

        return INSTANCE.inventory;
    }
}