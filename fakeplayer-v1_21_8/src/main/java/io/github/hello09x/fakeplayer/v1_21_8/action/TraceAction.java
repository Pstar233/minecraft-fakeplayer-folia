package io.github.hello09x.fakeplayer.v1_21_8.action;

import io.github.hello09x.fakeplayer.api.spi.Action;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.v1_21_8.action.util.Tracer;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public abstract class TraceAction implements Action {

    protected final ServerPlayer player;

    protected TraceAction(@NotNull ServerPlayer player) {
        this.player = player;
    }

    protected @Nullable CompletableFuture<HitResult> getTarget() {
        CompletableFuture<HitResult> completableFuture = new CompletableFuture<>();
        player.getBukkitEntity().getScheduler().run(Main.getInstance(), task -> {
            double reach = player.gameMode.isCreative() ? 5 : 4.5f;
            HitResult hitResult = Tracer.rayTrace(player, 1, reach, false);
            completableFuture.complete(hitResult);
        }, null);

        return completableFuture;
        //return Tracer.rayTrace(player, 1, reach, false);
    }


}
