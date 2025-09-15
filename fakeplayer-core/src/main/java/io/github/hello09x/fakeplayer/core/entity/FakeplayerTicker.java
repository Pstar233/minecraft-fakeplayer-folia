package io.github.hello09x.fakeplayer.core.entity;

import io.github.hello09x.fakeplayer.api.spi.NMSServer;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class FakeplayerTicker {
    public final static long NON_REMOVE_AT = -1;
    @NotNull
    private final Fakeplayer player;
    private final long removeAt;
    private boolean firstTick;
    private ScheduledTask task;
    private net.minecraft.world.level.ChunkPos lastChunkPos;
    private ServerLevel lastLevel;


    //我把假人的视距设置成10了，太大没用还占用性能;
    private int viewDistance = 10;
    private int simulationDistance = 10;

    public FakeplayerTicker(@NotNull Fakeplayer player, long lifespan) {
        this.player = player;
        this.removeAt = lifespan > 0 ? System.currentTimeMillis() + lifespan : NON_REMOVE_AT;
        this.firstTick = true;
    }

    /**
     * 启动 Folia 定时任务
     */
    public void start(ServerPlayer handle) {
        this.task = player.getPlayer().getScheduler().runAtFixedRate(Main.getInstance(), t -> {
            this.runTick(handle);
        }, null, 1L, 2L);
        Bukkit.getAsyncScheduler().runAtFixedRate(Main.getInstance(), taskasync -> {
            if (!player.isOnline()) {
                //System.out.println("玩家不在线清除区块");
                clearTickets();
                taskasync.cancel();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * 停止任务
     */
    public void stop() {
        clearTickets();
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    private void runTick(ServerPlayer handle1) {
        if (!player.isOnline()) {
            stop();
            return;
        }
        if (this.removeAt != NON_REMOVE_AT && this.player.getTickCount() % 20 == 0 && System.currentTimeMillis() > removeAt) {
            Main.getInjector().getInstance(FakeplayerManager.class).remove(player.getName(), "lifespan ends");
            stop();
            return;
        }
        if (this.firstTick) {
            this.doFirstTick();
        } else {
            this.doTick(handle1);
        }
    }

    private void doFirstTick() {
        var handle = this.player.getHandle();
        var player = this.player.getPlayer();
        var x = handle.getX();
        var y = handle.getY();
        var z = handle.getZ();
        handle.setXo(x);
        handle.setYo(y);
        handle.setZo(z);
        handle.doTick();
        player.teleportAsync(new Location(player.getWorld(), x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch()));
        handle.absMoveTo(x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
        this.firstTick = false;
    }

    /**
     * 每 tick 刷新区块加载
     */
    private void doTick(ServerPlayer handle) {
        handle.doTick();

        var level = (ServerLevel) handle.level();
        var pos = handle.chunkPosition();

        // 如果位置没变，不需要重复操作
        if (lastChunkPos != null && pos.equals(lastChunkPos) && level == lastLevel) {
            return;
        }

        //player.getPlayer().getScheduler().runDelayed(Main.getInstance(), task1 -> {

            if (!player.isOnline()){
                return;
            }

            //我在这里设置了假人的视距了，
            //int viewDistance = Bukkit.getViewDistance();
            //int simulationDistance = Bukkit.getSimulationDistance();

            // 移除旧位置的 ticket
            if (lastChunkPos != null && lastLevel != null) {
                lastLevel.getChunkSource().removeTicketWithRadius(TicketType.PLAYER_LOADING, lastChunkPos, viewDistance);
                lastLevel.getChunkSource().removeTicketWithRadius(TicketType.PLAYER_SIMULATION, lastChunkPos, simulationDistance);
            }

            // 添加当前位置的 ticket
            level.getChunkSource().addTicketWithRadius(TicketType.PLAYER_LOADING, pos, viewDistance);
            level.getChunkSource().addTicketWithRadius(TicketType.PLAYER_SIMULATION, pos, simulationDistance); // 更新缓存
            lastChunkPos = pos;
            lastLevel = level;

        //}, null, 200);

    }

    private void clearTickets() {
        if (lastChunkPos != null && lastLevel != null) {
            //int viewDistance = Bukkit.getViewDistance();
            //int simulationDistance = Bukkit.getSimulationDistance();
            lastLevel.getChunkSource().removeTicketWithRadius(TicketType.PLAYER_LOADING, lastChunkPos, viewDistance);
            lastLevel.getChunkSource().removeTicketWithRadius(TicketType.PLAYER_SIMULATION, lastChunkPos, simulationDistance);
            lastChunkPos = null;
            lastLevel = null;
        }
    }
}