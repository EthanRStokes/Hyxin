package com.build_9.hyxin.mixin;

import com.build_9.hyxin.LaunchEnvironment;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;

import java.io.IOException;

public class BytecodeProvider implements IClassBytecodeProvider {

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, true);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException, IOException {
        // TODO Consider how to handle runTransformers
        final ClassReader reader = LaunchEnvironment.get().getClassReader(name);
        final ClassNode node = new ClassNode();
        reader.accept(node, readerFlags);
        return node;
    }
}
