plugins {
    java
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
    id("com.gradleup.shadow") version "9.3.0"
    `maven-publish`
    id("hytale-mod") version("0.+")
}

val java_version: String by project
val options: String by project
val asm_version: String by project
val mixin_version: String by project
val mixin_extras_version: String by project

val website: String by project
val server_version: String by project
val plugin_group: String by project
val author: String by project

ext["userHome"] = System.getProperty("user.home")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(java_version)
    withSourcesJar()
    withJavadocJar()
}

// Quiet warnings about missing Javadocs.
/*javadoc {
    options.addStringOption("Xdoclint:-missing", "-quiet")
}*/

val shade by configurations.creating

configurations {
    api {
        extendsFrom(configurations["shade"])
    }
}

repositories {
    mavenCentral()
}

dependencies {
    shade("org.ow2.asm:asm:${asm_version}")
    shade("org.ow2.asm:asm-analysis:${asm_version}")
    shade("org.ow2.asm:asm-commons:${asm_version}")
    shade("org.ow2.asm:asm-tree:${asm_version}")
    shade("org.ow2.asm:asm-util:${asm_version}")
    shade("net.fabricmc:sponge-mixin:${mixin_version}")
    shade("org.ow2.sat4j:org.ow2.sat4j.core:2.3.6")
    shade("org.ow2.sat4j:org.ow2.sat4j.pb:2.3.6")
    shade("io.github.llamalad7:mixinextras-fabric:${mixin_extras_version}")
}

// Determines which properties will be passed from the Gradle runtime to
// resources as they are compiled into your plugin.
tasks.processResources {
    val expandProps = mapOf(
            "name"           to project.name,
            "version"        to project.version,
            "group"          to project.group,
            "description"    to description,
            "website"        to website,
            "server_version" to server_version,
            "project_group"  to plugin_group,
            "author"         to author
    )
    filesMatching("manifest.json") {
        expand(expandProps)
    }
    inputs.properties(expandProps)
}

hytale {
    updateChannel.set("pre-release")
}

// Creates a run configuration in IDEA that will run the Hytale server with
// your plugin and the default assets.
/*idea.project.settings.runConfigurations {
    "HytaleServer"(org.jetbrains.gradle.ext.Application) {
        mainClass = "com.hypixel.hytale.Main"
        moduleName = project.idea.module.name + ".main"
        programParameters = "--allow-op --accept-early-plugins --assets=$userHome/AppData/Roaming/Hytale/install/release/package/game/latest/Assets --packs=$userHome/AppData/Roaming/Hytale/UserData/Packs"
        workingDirectory = serverRunDir.absolutePath
    }
}*/
/*
publishing {
    publications {
        mavenJava(MavenPublication) {
            from(components.java)
            groupId = project.group
            artifactId = project.name
            version = project.version
        }
    }
    repositories {
        mavenLocal()
    }
}
*/
tasks.shadowJar {
    configurations.set(listOf(shade))
    exclude("about.html", "about.html", "fabric.mod.json", "LICENSE.txt", "LICENSE_MixinExtras", "sat4j.version")
    exclude("META-INF/maven/**")
    exclude("META-INF/services/cpw.**", "META-INF/services/org.spongepowered.tools.obfuscation.service.IObfuscationService")
    exclude("com/google/gson/**", "org/sat4j/**")
    mergeServiceFiles()
}