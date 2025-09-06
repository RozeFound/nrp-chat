package io.github.rozefound.nrpchat;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

  private NRPCommand NRPCommand = null;

  @Override
  public void onEnable() {

    saveDefaultConfig();

    NRPCommand = new NRPCommand(this);

    this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
      commands.registrar().register(NRPCommand.buildCommand());
    });

  }

}
