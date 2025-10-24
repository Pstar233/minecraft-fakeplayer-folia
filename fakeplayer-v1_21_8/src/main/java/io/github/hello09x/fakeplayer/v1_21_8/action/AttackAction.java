package io.github.hello09x.fakeplayer.v1_21_8.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.concurrent.CompletableFuture;


public class AttackAction extends TraceAction {

    private final ServerPlayer player;

    public AttackAction(ServerPlayer player) {
        super(player);
        this.player = player;
    }


    @Override
    public CompletableFuture<Boolean> CompletableFutureTick() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        getTarget().thenAccept(hitResult -> {
            var hit = hitResult;
            if (hit == null) {
                completableFuture.complete(false);
            }

            if (hit.getType() != HitResult.Type.ENTITY) {
                completableFuture.complete(false);
            }

            var entityHit = (EntityHitResult) hit;
            player.attack(entityHit.getEntity());
            player.swing(InteractionHand.MAIN_HAND);
            player.resetAttackStrengthTicker();
            player.resetLastActionTime();
            completableFuture.complete(true);
        });
        return completableFuture;
    }

    @Override
    public boolean tick() {
        return false;
    }

    @Override
    public void inactiveTick() {

    }

    @Override
    public void stop() {

    }


}
