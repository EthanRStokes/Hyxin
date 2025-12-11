package com.build_9.hyxin.mixin;

import com.build_9.hyxin.Constants;
import com.build_9.hyxin.LaunchEnvironment;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.service.MixinServiceAbstract;
import org.spongepowered.asm.util.IConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

public class MixinService extends MixinServiceAbstract {

    public static IMixinTransformer transformer;
    private static IConsumer<MixinEnvironment.Phase> phaseConsumer;
    private final IClassProvider classProvider = new ClassProvider();
    private final IClassBytecodeProvider bytecodeProvider = new BytecodeProvider();

    @Override
    public String getName() {
        return Constants.NAME;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public IClassProvider getClassProvider() {
        return this.classProvider;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this.bytecodeProvider;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        // No thanks
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        // No thanks
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        // No thanks
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return List.of("com.build_9.hyxin.mixin.Platform");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        try {
            return new ContainerHandleURI(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        }
        catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        return new ContainerHandleVirtual(this.getName());
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory) {
            transformer = ((IMixinTransformerFactory) internal).createTransformer();
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            return LaunchEnvironment.get().findResourceStream(name);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void wire(MixinEnvironment.Phase phase, IConsumer<MixinEnvironment.Phase> phaseConsumer) {
        super.wire(phase, phaseConsumer);
        MixinService.phaseConsumer = phaseConsumer;
    }

    public static void changePhase(MixinEnvironment.Phase phase) {
        phaseConsumer.accept(phase);
    }
}