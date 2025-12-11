package com.build_9.hyxin.scanner;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

/**
 * Represents the Hyxin section of the manifest file.
 *
 * @param configs An array of Mixin config file names written as strings.
 */
public record HyxinConfig(@SerializedName("Configs") Set<String> configs) {
}