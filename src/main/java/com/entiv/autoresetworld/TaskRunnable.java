package com.entiv.autoresetworld;

import com.entiv.autoresetworld.task.ScheduleTask;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskRunnable extends BukkitRunnable {

    private static TaskRunnable taskRunnable;

    @Override
    public void run() {

        for (ScheduleTask scheduleTask : Main.getInstance().getTaskManager().getScheduleTasks()) {
            if (scheduleTask.isExpired()) {
                try {
                    scheduleTask.runTask();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    scheduleTask.taskComplete();
                }
                return;
            }
        }
    }

    public static void load() {
        if (taskRunnable == null) {
            taskRunnable = new TaskRunnable();
        }
        taskRunnable.runTaskTimer(Main.getInstance(), 0, 20);
    }
}
