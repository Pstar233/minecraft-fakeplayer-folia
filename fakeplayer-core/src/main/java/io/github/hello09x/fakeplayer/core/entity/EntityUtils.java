package io.github.hello09x.fakeplayer.core.entity;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class EntityUtils {

    private EntityUtils(){}
    public static CompletableFuture<Boolean> teleportAndSoundCompletable(@NotNull Entity entity, @NotNull Location loc) {
        return entity.teleportAsync(loc).thenApply(success -> {
            if (success) {
                loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
            return success;
        });
    }
    public static void teleportAndSound(@NotNull Entity entity, @NotNull Location loc) {
        entity.teleportAsync(loc).thenAccept(aBoolean -> {
           if (aBoolean){
               loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
           }
        });
    }
}
