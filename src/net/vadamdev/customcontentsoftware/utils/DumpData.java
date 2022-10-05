package net.vadamdev.customcontentsoftware.utils;

/**
 * @author VadamDev
 * @since 29/09/2022
 */
public class DumpData {
    private final String registryName;
    private final int itemId;

    public DumpData(String registryName, int itemId) {
        this.registryName = registryName;
        this.itemId = itemId;
    }

    public String getRegistryName() {
        return registryName;
    }

    public int getItemId() {
        return itemId;
    }
}
