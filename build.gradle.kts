plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    kotlin("jvm") version "2.4.21"
    id("com.google.devtools.ksp") version "2.3.10"
    id("com.gradleup.shadow") version "9.6.1"
    `maven-publish`
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.essentialsx.net/releases/") // EssentialsX
    maven("https://repo.glaremasters.me/repository/towny/") // Towny
    maven("https://jitpack.io") // Vault
    maven("https://repo.mikeprimm.com/") // Dynmap
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("org.chromagaming:Chroma-Core:2.0-SNAPSHOT")
    compileOnly("net.essentialsx:EssentialsX:2.21.2")
    compileOnly("com.palmergames.bukkit.towny:towny:0.103.2.0")
    compileOnly("com.github.TownyAdvanced:Dynmap-Towny:1.3.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("us.dynmap:dynmap-api:2.5")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G")

        downloadPlugins {
            github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
        }
    }

    processResources {
        val props = mapOf("version" to version.toString().replace("-SNAPSHOT", ""))
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    test {
        useJUnitPlatform()
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

kotlin {
    jvmToolchain(25)
}
