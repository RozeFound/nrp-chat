import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.bukkit.Permission

plugins {
  `java-library`
  // id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
  id("xyz.jpenilla.run-paper") version "3.0.0-beta.1"
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.0"
}

group = "io.github.rozefound"
version = "1.0.0"
description = "Non-RP chat"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
  maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
  maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
  maven {
    name = "papermc"
    url = uri("https://repo.papermc.io/repository/maven-public/")
  }
}

dependencies {

  compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
  // paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
  // compileOnly("com.github.retrooper:packetevents-spigot:2.9.3")
}

tasks {
  compileJava {
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }
  runServer {
    minecraftVersion("1.21.5")
  }
}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.

bukkitPluginYaml {
  main = "io.github.rozefound.nrpchat.Main"
  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
  authors.add("RozeFound")
  apiVersion = "1.21.5"
  commands {
    register("nrp") {
      description = "Non-RP chat command with optional configurable cost of food of exp"
      usage = "/nrp <message>"
      permission = "nrpchat.nrp"
    }
  }
  permissions {
    register("nrpchat.nrp") {
      description = "Base command for nrp command"
      default = Permission.Default.OP
      children = mapOf(
        "nrpchat.nrp.chat" to true,
        "nrpchat.nrp.reload" to true,
        "nrpchat.nrp.bypass" to true
      )
    }
    register("nrpchat.nrp.chat") {
      description = "Permission to use /nrp <message> itself"
      default = Permission.Default.TRUE
    }
    register("nrpchat.nrp.reload") {
      description = "Permission to use /nrp reload"
      default = Permission.Default.OP
    }
    register("nrpchat.nrp.bypass") {
      description = "Permission to bypass cost of sending non-rp messages"
      default = Permission.Default.OP
    }

  }
}
