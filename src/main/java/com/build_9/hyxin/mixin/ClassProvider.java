package com.build_9.hyxin.mixin;

import com.build_9.hyxin.LaunchEnvironment;
import org.spongepowered.asm.service.IClassProvider;

import java.net.URL;

public class ClassProvider implements IClassProvider {

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return LaunchEnvironment.get().findLoaderForClass(name).loadClass(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, LaunchEnvironment.get().findLoaderForClass(name));
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return this.findClass(name, initialize);
    }
}