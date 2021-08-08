package com.entiv.autoresetworld;

import com.entiv.autoresetworld.scheduletask.ScheduleTask;
import com.entiv.autoresetworld.scheduletask.ScheduleTaskRunnable;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

//TODO 支持 papi 变量, 支持用指令刷新世界
public class Main extends JavaPlugin {

    private static Main plugin;
    private static MultiverseCore multiverseCore;

    public static Main getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        multiverseCore = (MultiverseCore) Main.getInstance().getServer().getPluginManager().getPlugin("Multiverse-Core");

        String[] message = {
                "&e&l" + getName() + "&a 插件&e v" + getDescription().getVersion() + " &a已启用",
                "&a插件制作作者:&e EnTIv &aQQ群:&e 600731934"
        };
        Message.sendConsole(message);

        saveDefaultConfig();

        setupRespawnWorld();
        setupDeleteFileTask();

        ScheduleTaskRunnable.load();
    }

    @Override
    public void onDisable() {
        String[] message = {
                "&e&l" + getName() + "&a 插件&e v" + getDescription().getVersion() + " &a已卸载",
                "&a插件制作作者:&e EnTIv &aQQ群:&e 600731934"
        };
        Message.sendConsole(message);
    }

    private void setupRespawnWorld() {
        ConfigurationSection section = getConfig().getConfigurationSection("自动刷新世界");
        if (section == null) throw new NullPointerException("配置文件错误, 请检查配置文件");

        for (String worldName : section.getKeys(false)) {

            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                Message.sendConsole("&c世界 &e" + worldName + "&c 不存在, 请检查配置文件");
                continue;
            }

            RegenWorldTask regenWorldTask = new RegenWorldTask(worldName);
            regenWorldTask.load();
        }
    }

    private void setupDeleteFileTask() {
        ConfigurationSection section = getConfig().getConfigurationSection("自动删除文件");
        if (section == null) throw new NullPointerException("配置文件错误, 请检查配置文件");

        for (String name : section.getKeys(false)) {
            ScheduleTask deleteFileTask = new DeleteFileTask(name);
            deleteFileTask.load();
        }
    }

    public static MultiverseCore getMultiverseCore() {
        return multiverseCore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.isOp()) return true;

        if (args.length == 0) {
            Message.send(sender,
                    "",
                    "&6━━━━━━━━━━━━━━&e  自动刷新世界指令帮助  &6━━━━━━━━━━━━━━",
                    "",
                    "&b ━ &a/asw reload &7重载配置文件",
                    "&b ━ &a/asw reset 世界名 &7手动刷新世界"
            );
            return true;
        }

        if (args.length == 1) {
            Main plugin = Main.getInstance();
            plugin.reloadConfig();

            ScheduleTask.scheduleTasks.clear();

            setupRespawnWorld();
            setupDeleteFileTask();

            Message.send(sender, "&9&l" + plugin.getName() + "&6&l >> &a配置文件重载完毕");

            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {

            String name = args[1];

            for (ScheduleTask scheduleTask : ScheduleTask.scheduleTasks) {
                if (name.equalsIgnoreCase(scheduleTask.getName())) {
                    scheduleTask.setExpired(true);
                    return true;
                }
            }

            Message.send(sender, "&9&l" + plugin.getName() + "&6&l >> &c检测不到名为 &b" + name + "&c 的自动刷新任务!");


        }


        return true;
    }
}

