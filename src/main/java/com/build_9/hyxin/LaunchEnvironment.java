package com.build_9.hyxin;

import org.objectweb.asm.ClassReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides a unified environment for managing and resolving classes and resources across multiple class loaders used
 * during the different phases of the launch process.
 * <p>
 * This class is a singleton and must be created once using {@link #create(ClassLoader, ClassLoader)} before it can be
 * accessed using {@link #get()}.
 */
public class LaunchEnvironment {

    private static LaunchEnvironment instance;

    private final ClassLoader systemLoader;
    private final ClassLoader earlyPluginLoader;
    private ClassLoader runtimeLoader;

    private LaunchEnvironment(ClassLoader earlyPluginLoader, ClassLoader systemLoader) {
        this.systemLoader = systemLoader;
        this.earlyPluginLoader = earlyPluginLoader;
    }

    /**
     * Returns the global LaunchEnvironment instance.
     *
     * @return The global LaunchEnvironment instance.
     * @throws IllegalStateException if the environment has not been created yet.
     */
    public static LaunchEnvironment get() {
        if (instance == null) {
            throw new IllegalStateException("Can not access LaunchEnvironment before it has been created.");
        }
        return instance;
    }

    /**
     * Creates a new launch environment and sets the global instance.
     *
     * @param systemLoader      The main classloader responsible for loading the game.
     * @param earlyPluginLoader The classloader that earlyplugins are loaded from.
     * @throws IllegalStateException if the environment has already been created.
     */
    public static void create(ClassLoader systemLoader, ClassLoader earlyPluginLoader) {
        if (instance != null) {
            throw new IllegalStateException("LaunchEnvironment has already been created!");
        }
        instance = new LaunchEnvironment(systemLoader, earlyPluginLoader);
    }

    /**
     * Captures and stores the runtime class loader for later lookups.
     *
     * @param loader The runtime class loader.
     * @throws IllegalStateException if the runtime loader has already been captured.
     */
    public void captureRuntimeLoader(ClassLoader loader) {
        if (this.runtimeLoader != null) {
            throw new IllegalStateException("Runtime ClassLoader has already been captured! '" + this.runtimeLoader + "'");
        }
        this.runtimeLoader = loader;
    }

    /**
     * Returns the system class loader.
     *
     * @return The system class loader.
     */
    public ClassLoader getSystemLoader() {
        return this.systemLoader;
    }

    /**
     * Returns the early plugin class loader.
     *
     * @return The early plugin class loader.
     */
    public ClassLoader getEarlyPluginLoader() {
        return this.earlyPluginLoader;
    }

    /**
     * Returns the runtime class loader.
     *
     * @return The runtime class loader.
     */
    public ClassLoader getRuntimeLoader() {
        return this.runtimeLoader;
    }

    /**
     * Attempts to find the class loader responsible for loading a class by the name of the class.
     *
     * @param resourceName The fully qualified name of the class. For example, com.example.MyClass.
     * @return The class loader that can load the corresponding class file.
     * @throws ClassNotFoundException If no class loader can load the given class.
     */
    public ClassLoader findLoaderForClass(String resourceName) throws ClassNotFoundException {
        try {
            return this.findLoaderFor(resourceName.replace(".", "/").concat(".class"));
        }
        catch (IOException e) {
            throw new ClassNotFoundException("Could not find class '" + resourceName + "'.", e);
        }
    }

    /**
     * Attempts to find the class loader responsible for loading a resource.
     *
     * @param resourceName The relative resource path. For example, com/example/MyClass.class.
     * @return The class loader that can provide the resource.
     * @throws IOException If no class loader can provide the resource.
     */
    public ClassLoader findLoaderFor(String resourceName) throws IOException {
        if (this.runtimeLoader != null && this.runtimeLoader.getResource(resourceName) != null) {
            return this.runtimeLoader;
        }
        if (this.earlyPluginLoader != null && this.earlyPluginLoader.getResource(resourceName) != null) {
            return this.earlyPluginLoader;
        }
        if (this.systemLoader != null && this.systemLoader.getResource(resourceName) != null) {
            return this.systemLoader;
        }
        throw new FileNotFoundException("Could not find resource '" + resourceName + "' on any class loader.");
    }

    /**
     * Opens an {@link InputStream} for the given resource from one of the tracked class loaders.
     *
     * @param resourceName The resource path.
     * @return An input stream for the resource.
     * @throws IOException If the resource could not be found.
     */
    public InputStream findResourceStream(String resourceName) throws IOException {
        return findLoaderFor(resourceName).getResourceAsStream(resourceName);
    }

    /**
     * Creates an ASM {@link ClassReader} for the requested class.
     *
     * @param name The fully qualified class name.
     * @return A {@link ClassReader} for the requested class.
     * @throws IOException            If the requested class could not be read.
     * @throws ClassNotFoundException IIf the requested class could not be found.
     */
    public ClassReader getClassReader(String name) throws IOException, ClassNotFoundException {
        final String fileName = name.replace(".", "/").concat(".class");
        try (InputStream stream = this.findResourceStream(fileName)) {
            if (stream != null) {
                return new ClassReader(stream);
            }
        }
        throw new ClassNotFoundException("Could not find class '" + fileName + "'.");
    }
}