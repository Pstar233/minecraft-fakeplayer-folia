package io.github.hello09x.fakeplayer.v1_21_8.action;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class UseAction extends TraceAction {

    private final Current current = new Current();

    public UseAction(@NotNull ServerPlayer player) {
        super(player);
    }

    @Override
    @SuppressWarnings("resource")
    public CompletableFuture<Boolean> CompletableFutureTick() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        getTarget().thenAccept(hitResult -> {
            if (current.freeze > 0) {
                current.freeze--;
                completableFuture.complete(false);
            }

            if (player.isUsingItem()) {
                completableFuture.complete(true);
            }

            var hit = hitResult;
            if (hit == null) {
                completableFuture.complete(false);
            }

            for (var hand : InteractionHand.values()) {
                switch (hit.getType()) {
                    case BLOCK -> {
                        player.resetLastActionTime();
                        var world = player.level();
                        var blockHit = (BlockHitResult) hit;
                        var pos = blockHit.getBlockPos();
                        var side = blockHit.getDirection();
                        if (pos.getY() < player.level().getMaxY() - (side == Direction.UP ? 1 : 0) && world.mayInteract(player, pos)) {
                            var result = player.gameMode.useItemOn(player, world, player.getItemInHand(hand), hand, blockHit);
                            if (result.consumesAction()) {
                                player.swing(hand);
                                current.freeze = 3;
                                completableFuture.complete(true);
                            }
                        }
                    }
                    case ENTITY -> {
                        player.resetLastActionTime();
                        var entityHit = (EntityHitResult) hit;
                        var entity = entityHit.getEntity();
                        boolean handWasEmpty = player.getItemInHand(hand).isEmpty();
                        boolean itemFrameEmpty = (entity instanceof ItemFrame) && ((ItemFrame) entity).getItem().isEmpty();
                        var pos = entityHit.getLocation().subtract(entity.getX(), entity.getY(), entity.getZ());
                        if (entity.interactAt(player, pos, hand).consumesAction()) {
                            current.freeze = 3;
                            completableFuture.complete(true);
                        }
                        if (player.interactOn(entity, hand).consumesAction() && !(handWasEmpty && itemFrameEmpty)) {
                            current.freeze = 3;
                            completableFuture.complete(true);
                        }
                    }
                }
                var handItem = player.getItemInHand(hand);
                if (player.gameMode.useItem(player, player.level(), handItem, hand).consumesAction()) {
                    player.resetLastActionTime();
                    current.freeze = 3;
                    completableFuture.complete(true);
                }
            }
            completableFuture.complete(false);
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
        current.freeze = 0;
        player.releaseUsingItem();
    }

    private final static class Current {

        /**
         * 冷却, 单位: tick
         */
        public int freeze;
    }
}
