package io.github.rozefound.nrpchat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;


public class NRPCommand {

  Main plugin;
  MiniMessage mm;

  String message_template;

  boolean info_message_enabled;
  String info_message_template;

  String not_enough_exp_string;
  String not_enough_food_string;

  int per_message_exp_cost;
  int per_message_food_cost;

  int per_char_exp_cost;
  int per_char_food_cost;

  public NRPCommand(Main plugin) {

    this.plugin = plugin;

    mm = MiniMessage.miniMessage();

    load_config();

  }

  public void load_config() {

    var config = plugin.getConfig();

    message_template = config.getString("nrp-chat-template", "<dark_gray><<player>> <message></dark_gray>");

    info_message_enabled = config.getBoolean("cost-info.enable", true);
    info_message_template = config.getString("cost-info.message-template", "This message has costed you <food_cost> food and <exp_cost> exp points.");

    not_enough_exp_string = config.getString("not-enough-exp-string", "You don't have enough EXP points to send this message!");
    not_enough_food_string = config.getString("not-enough-food-string", "You don't have enough food points to send this message!");

    per_message_exp_cost = config.getInt("per-message.xp", 0);
    per_message_food_cost = config.getInt("per-message.food", 0);

    per_char_exp_cost = config.getInt("per-char.exp", 10);
    per_char_food_cost = config.getInt("per-char.food", 0);

  }

  public LiteralCommandNode<CommandSourceStack> buildCommand() {

    var reload_subcommand = Commands.literal("reload")
      .requires(sender -> sender.getSender().hasPermission("nrpchat.nrp.reload"))
      .executes(ctx -> {
        plugin.reloadConfig();
        load_config();
        return Command.SINGLE_SUCCESS;
      });

    var default_subcommand = Commands.argument("message", StringArgumentType.greedyString())
      .executes(ctx -> {

        final String message = StringArgumentType.getString(ctx, "message");

        if (!(ctx.getSource().getExecutor() instanceof Player player)) return -1;

        boolean bypassCost = player.hasPermission("nrpchat.nrp.bypass");

        var currentExp = player.calculateTotalExperiencePoints();
        var expCost = per_message_exp_cost + (message.length() * per_char_exp_cost);

        if (currentExp < expCost && !bypassCost) {
          player.sendMessage(not_enough_exp_string);
          return -1;
        }

        var currentFood = player.getFoodLevel();
        var foodCost = per_message_food_cost + (message.length() * per_char_food_cost);

        if (currentFood < foodCost && !bypassCost) {
          player.sendMessage(not_enough_food_string);
          return -1;
        }

        var final_message = mm.deserialize(message_template,
          Placeholder.unparsed("player", player.getName()),
          Placeholder.unparsed("message", message));

        player.getServer().broadcast(final_message);

        if (info_message_enabled && !bypassCost) {
          var info_message = mm.deserialize(info_message_template,
            Placeholder.unparsed("food_cost", String.valueOf(foodCost)),
            Placeholder.unparsed("exp_cost", String.valueOf(expCost))
          );
          player.sendMessage(info_message);
        }

        if (!bypassCost) {
          player.setExperienceLevelAndProgress(currentExp - expCost);
          player.setFoodLevel(currentFood - foodCost);
        }

        return Command.SINGLE_SUCCESS;
      });

    var root = Commands.literal("nrp")
      .then(default_subcommand)
      .then(reload_subcommand);


    return root.build();

  }

}
