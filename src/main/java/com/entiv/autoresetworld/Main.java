package com.entiv.autoresetworld;

import com.entiv.autoresetworld.hook.AutoResetWorldExpansion;
import com.entiv.autoresetworld.task.ScheduleTask;
import com.entiv.autoresetworld.utils.Message;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO 组合任务，例如 unload 地图后删文件后 restart，把任务概念取消 -> 一个任务设置时间，然后可以在下面放置任务
// 模板世界功能

/*
强制删除任务:
  时间设置: "day:1, 00:00:00"
  任务列表:
    - "刷新世界: world_resource, 更换种子: true"
    - "删除文件: logs, 保留天数: 7"
    - "删除文件: crash-reports, 保留天数: 7"
    - "执行指令: co l purge t:7d"
* */

public class Main extends JavaPlugin {

    private static Main plugin;

    private final MultiverseCore multiverseCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
    private final TaskManager taskManager = new TaskManager();

    public static Main getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {

        plugin = this;

        String[] message = {
                "&e" + getName() + "&a 插件&e v" + getDescription().getVersion() + " &a已启用",
                "&a插件制作作者:&e EnTIv &aQQ群:&e 600731934"
        };
        Message.sendConsole(message);

        saveDefaultConfig();

        taskManager.loadScheduleTask();
        TaskRunnable.load();

        Objects.requireNonNull(getCommand("AutoResetWorld")).setTabCompleter(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AutoResetWorldExpansion().register();
        }
    }

    @Override
    public void onDisable() {
        String[] message = {
                "&e" + getName() + "&a 插件&e v" + getDescription().getVersion() + " &a已卸载",
                "&a插件制作作者:&e EnTIv &aQQ群:&e 600731934"
        };
        Message.sendConsole(message);
    }

    public MultiverseCore getMultiverseCore() {
        return multiverseCore;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.isOp()) return true;

        if (args.length == 0) {
            Message.send(sender,
                    "",
                    "&6━━━━━━━━━━━━━━&d  自动刷新世界指令帮助  &6━━━━━━━━━━━━━━",
                    "",
                    "&b ━ &d/asw reload &7重载配置文件",
                    "&b ━ &d/asw runtask 任务类型-任务名称 &7手动执行任务"
            );
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            Main plugin = Main.getInstance();
            plugin.reloadConfig();

            taskManager.clearTasks();
            taskManager.loadScheduleTask();

            Message.send(sender, "&9" + plugin.getName() + "&6 >> &a配置文件重载完毕");

            return true;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("runTask"))) {

            String name = args[1];
            ScheduleTask task = taskManager.getTask(name);

            if (task == null) {
                Message.send(sender, "&9&l" + plugin.getName() + "&6&l >> &c检测不到名为 &b" + name + "&c 的自动刷新任务!");
                return true;
            }

            task.setExpired(true);

            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (args.length == 1) {
            return Stream.of("reload", "runtask")
                    .filter(s -> s.toLowerCase().startsWith(args[0]))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return taskManager.getScheduleTasks().stream()
                    .map(ScheduleTask::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}

