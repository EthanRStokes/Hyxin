package com.build_9.hyxin.impl.mixins;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Level;

@Mixin(HytaleServer.class)
public class ExampleMixin {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/hypixel/hytale/plugin/early/EarlyPluginLoader;hasTransformers()Z"))
    private static void onMain(CallbackInfo ci) {
        HytaleLogger.get("Hyxin-Example").at(Level.INFO).log("Hello from Hyxin! The server has been patched!");
    }
}