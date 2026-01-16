package com.build_9.hyxin;

import com.build_9.hyxin.mixin.Bootstrap;
import com.build_9.hyxin.mixin.MixinService;
import com.build_9.hyxin.scanner.PluginManifest;
import com.build_9.hyxin.scanner.PluginScanner;
import com.hypixel.hytale.plugin.early.ClassTransformer;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class HyxinTransformer implements ClassTransformer {

    public HyxinTransformer() {

        // Captures the system class loader, and the early plugin class loader.
        // TODO In dev the early plugin classloader is wrong.
        // This is caused by Hyxin being loaded by Gradle/Idea and not through
        // the standard launch process.
        LaunchEnvironment.create(this.getClass().getClassLoader(), Thread.currentThread().getContextClassLoader());

        // Hytale requires us to use their log manager. Setting it now prevents
        // Mixin from trying to initialize their own.
        System.setProperty("java.util.logging.manager", "com.hypixel.hytale.logger.backend.HytaleLogManager");

        // Set the bootstrap and mixin service manually. This will avoid the
        // scan Mixin usually performs, and prevents invalid platforms like
        // LaunchWrapper or ModLauncher from loading.
        System.setProperty("mixin.bootstrapService", Bootstrap.class.getName());
        System.setProperty("mixin.service", MixinService.class.getName());

        // Scan for other plugins that we can try to load Hyxin configs from.
        final PluginScanner plugins = new PluginScanner();
        plugins.scanDirectory(new File("./earlyplugins"));
        // TODO Early plugins can be loaded from other folders via commandline args, but we don't have access yet.
        // TODO Normal plugins can not have Mixins yet, because of classloader issues. Waiting on input from Hytale team.

        // Use hyxin-target property to load manifests from given build/resource folders.
        final String hyxinTarget = System.getProperty("hyxin-target");
        Constants.log("Targets: " + hyxinTarget);
        if (hyxinTarget != null && !hyxinTarget.isBlank()) {
            final String[] parts = hyxinTarget.split(",");
            for (String rawPath : parts) {
                final String trimmed = rawPath.trim();
                if (trimmed.isEmpty()) continue;
                final File f = new File(trimmed);
                try {
                    if (f.isDirectory()) {
                        // Try reading a plain manifest.json from the top-level of the directory
                        final PluginManifest manifest = PluginScanner.readManifestFromDirectory(f);
                        if (manifest != null) {
                            Constants.log("Loaded manifest.json from directory '" + f.getAbsolutePath() + "'");
                            plugins.addManifest(f, manifest);
                        }
                        else {
                            // If directory contains jars, scan them as well.
                            File[] files = f.listFiles();
                            if (files != null) {
                                for (File child : files) {
                                    if (child.isFile() && child.getName().toLowerCase().endsWith(".jar")) {
                                        try {
                                            final PluginManifest m = PluginScanner.readManifest(child);
                                            if (m != null) {
                                                Constants.log("Loaded manifest.json from jar '" + child.getAbsolutePath() + "'");
                                                plugins.addManifest(child, m);
                                            }
                                        }
                                        catch (IOException e) {
                                            System.err.println("[Hyxin] Failed to read jar '" + child.getAbsolutePath() + "' when scanning hyxin-target.");
                                            e.printStackTrace(System.err);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else if (f.isFile() && f.getName().toLowerCase().endsWith(".jar")) {
                        try {
                            final PluginManifest manifest = PluginScanner.readManifest(f);
                            if (manifest != null) {
                                Constants.log("Loaded manifest.json from jar '" + f.getAbsolutePath() + "'");
                                plugins.addManifest(f, manifest);
                            }
                        }
                        catch (IOException e) {
                            System.err.println("[Hyxin] Failed to read jar '" + f.getAbsolutePath() + "' from hyxin-target.");
                            e.printStackTrace(System.err);
                        }
                    }
                }
                catch (Exception e) {
                    System.err.println("[Hyxin] Error while processing hyxin-target path '" + trimmed + "'.");
                    e.printStackTrace(System.err);
                }
            }
        }

        MixinBootstrap.init();


        // Load Mixin configs from plugin manifests.
        for (Map.Entry<File, PluginManifest> entry : plugins.entries().entrySet()) {
            if (entry.getValue().hasMixinConfigs()) {
                for (String config : entry.getValue().hyxin().configs()) {
                    Constants.log("Loading Mixin config '" + config + "' from '" + entry.getKey().getName() + "'");
                    Mixins.addConfiguration(config);
                }
            }
        }
    }

    private void setupRuntimeEnvironment() {
        // Captures the runtime classloader. This will be the transforming
        // class loader that the server and normal plugins load on.
        LaunchEnvironment.get().captureRuntimeLoader(Thread.currentThread().getContextClassLoader());

        // Move the phase from PreInit to Default. This allows standard mixin
        // configs to be applied.
        MixinService.changePhase(MixinEnvironment.Phase.INIT);
        MixinService.changePhase(MixinEnvironment.Phase.DEFAULT);
    }

    @Override
    public int priority() {
        // Under normal circumstances mixins are applied after patches and
        // other forms of class transformers. We picked -100 to load after
        // default transformers.
        return -100;
    }

    @Nullable
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String path, @Nonnull byte[] bytes) {
        if (LaunchEnvironment.get().getRuntimeLoader() == null) {
            this.setupRuntimeEnvironment();
        }
        return MixinService.transformer.transformClassBytes(name, name, bytes);
    }
}