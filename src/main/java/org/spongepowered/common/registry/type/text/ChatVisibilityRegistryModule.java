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
package org.spongepowered.common.registry.type.text;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.chat.ChatVisibilities;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.common.interfaces.IMixinEnumChatVisibility;
import org.spongepowered.common.registry.type.MinecraftEnumBasedAlternateCatalogTypeRegistryModule;

@RegisterCatalog(ChatVisibilities.class)
@RegistrationDependency(ChatTypeRegistryModule.class)
public final class ChatVisibilityRegistryModule extends MinecraftEnumBasedAlternateCatalogTypeRegistryModule<EntityPlayer.EnumChatVisibility, ChatVisibility>{

    @Override
    public void registerDefaults() {
        this.setChatTypes();
    }

    private void setChatTypes() {
        // We can't do this in the EnumChatVisibility constructor, since the registry isn't initialized then
        EntityPlayer.EnumChatVisibility FULL = EntityPlayer.EnumChatVisibility.FULL;
        EntityPlayer.EnumChatVisibility SYSTEM = EntityPlayer.EnumChatVisibility.SYSTEM;
        EntityPlayer.EnumChatVisibility HIDDEN = EntityPlayer.EnumChatVisibility.HIDDEN;

        ((IMixinEnumChatVisibility) (Object) FULL).setChatTypes(ImmutableSet.copyOf(ChatTypeRegistryModule.chatTypeMappings.values()));
        ((IMixinEnumChatVisibility) (Object) SYSTEM).setChatTypes(ImmutableSet.of(ChatTypes.SYSTEM, ChatTypes.ACTION_BAR));
        ((IMixinEnumChatVisibility) (Object) HIDDEN).setChatTypes(ImmutableSet.of());
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (EntityPlayer.EnumChatVisibility visibility : EntityPlayer.EnumChatVisibility.values()) {
            if (!this.catalogTypeMap.containsKey(enumAs(visibility).getId())) {
                this.catalogTypeMap.put(enumAs(visibility).getId(), enumAs(visibility));
            }
        }
    }

    @Override
    protected EntityPlayer.EnumChatVisibility[] getValues() {
        return EntityPlayer.EnumChatVisibility.values();
    }

}
