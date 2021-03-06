/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.test;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.UUID;

import javax.inject.Inject;

/**
 * Test for User(Offline-Player) Inventory
 */
@Plugin(id = "offlineinventorytest", name = "Offline Inventory Test", description = "A plugin to test offline inventories")
public class OfflineInventoryTest {

    @Inject private Logger logger;

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event, @Root Player player) {
        UUID uuid = player.getUniqueId();
        Sponge.getScheduler().createTaskBuilder().delayTicks(20).execute(() -> this.run(uuid)).submit(this);
    }

    private void run(UUID uuid) {
        User user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid).get();
        logger.info(user.getName() + " has an Inventory with:");
        for (Inventory slot : user.getInventory()) {
            slot.peek().ifPresent(stack -> logger.info(stack.getType().getId() + "x" + stack.getQuantity()));
        }
        user.getHelmet().ifPresent(s -> logger.info("Helmet: " + s.getType().getId()));
        user.getChestplate().ifPresent(s -> logger.info("Chestplate: " + s.getType().getId()));
        user.getLeggings().ifPresent(s -> logger.info("Leggings: " + s.getType().getId()));
        user.getBoots().ifPresent(s -> logger.info("Boots: " + s.getType().getId()));

        logger.info("and a hotbar full of diamonds!");
        for (Inventory inv : user.getInventory().query(Hotbar.class).slots()) {
            inv.offer(ItemStack.of(ItemTypes.DIAMOND, 1));
        }
    }

}
