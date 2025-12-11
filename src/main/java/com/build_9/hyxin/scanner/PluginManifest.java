package com.build_9.hyxin.scanner;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a plugin with a manifest file.
 *
 * @param group   The group entry in the manifest.
 * @param name    The plugin name in the manifest.
 * @param version The version name in the manifest.
 * @param hyxin   The hyxin config entries in the manifest.
 */
public record PluginManifest(@SerializedName("Group") String group, @SerializedName("Name") String name, @SerializedName("Version") String version, @SerializedName("Hyxin") HyxinConfig hyxin) {

    /**
     * Checks if the plugin manifest contains a hyxin entry, and that the entry contains at least one mixin config.
     *
     * @return If the plugin has hyxin data to load.
     */
    public boolean hasMixinConfigs() {
        return hyxin != null && !hyxin.configs().isEmpty();
    }
}