package org.spongepowered.common.item.inventory.util;

import net.minecraft.inventory.InventoryCrafting;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InventoryUtilTest {
    /**
     * This test exists solely to ensure that this method doesn't throw an NPE.
     */
    @Test
    public void tesToSpongeInventory() throws Exception {
        final InventoryCrafting mockInventory = mock(InventoryCrafting.class);
        when(mockInventory.getHeight()).thenReturn(1);
        when(mockInventory.getWidth()).thenReturn(1);

        InventoryUtil.toSpongeInventory(mockInventory);
    }

}