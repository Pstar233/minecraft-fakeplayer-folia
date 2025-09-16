package io.github.hello09x.fakeplayer.v1_21_8.network;

import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.netty.util.internal.ThreadLocalRandom;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Lombok;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class FakeServerGamePacketListenerImpl extends ServerGamePacketListenerImpl implements NMSServerGamePacketListener {

    private final FakeplayerManager manager = Main.getInjector().getInstance(FakeplayerManager.class);
    private int latency = 0;

    public FakeServerGamePacketListenerImpl(
            @NotNull MinecraftServer server,
            @NotNull Connection connection,
            @NotNull ServerPlayer player,
            @NotNull CommonListenerCookie cookie
    ) {
        super(server, connection, player, cookie);
        Optional.ofNullable(Bukkit.getPlayer(player.getUUID()))
                .ifPresent(p -> this.addChannel(p, BUNGEE_CORD_CORRECTED_CHANNEL));
    }

    private boolean addChannel(@NotNull Player player, @NotNull String channel) {
        try {
            var method = player.getClass().getMethod("addChannel", String.class);
            var ret = method.invoke(player, channel);
            if (ret instanceof Boolean success) {
                return success;
            }
            return true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    @Override
    public void send(Packet<?> packet) {
        //System.out.println("收到包||"+packet.getClass().getName());
        if (packet instanceof ClientboundCustomPayloadPacket p) {
            this.handleCustomPayloadPacket(p);
        } else if (packet instanceof ClientboundSetEntityMotionPacket p) {
            this.handleClientboundSetEntityMotionPacket(p);
        }

        //给假人一个假延迟
        String playername = player.getBukkitEntity().getName();
        if (scheduledTask && !playername.matches(".*_\\d+$")) {
            scheduledTask = false;

            Bukkit.getAsyncScheduler().runDelayed(Main.getInstance(), task -> {
                if (!player.getBukkitEntity().getPlayer().isOnline()) {
                    latencymap.remove(playername);
                    return;
                }
                if (!latencymap.containsKey(playername)) {
                    if (ThreadLocalRandom.current().nextInt(0, 101) >= 50) {
                        latencymap.put(playername, new int[]{111, 144});
                    } else {
                        latencymap.put(playername, new int[]{22, 34});
                    }
                } else {
                    int[] ints = latencymap.get(playername);
                    latency = ThreadLocalRandom.current().nextInt(ints[0], ints[1]);
                }

                //等任务执行完成了在改成true
                scheduledTask = true;
            }, 1, TimeUnit.SECONDS);
        }
    }

    private Map<String, int[]> latencymap = new HashMap<>();
    private boolean scheduledTask = true;

    /**
     * 玩家被击退的动作由客户端完成, 假人没有客户端因此手动完成这个动作
     */
    public void handleClientboundSetEntityMotionPacket(@NotNull ClientboundSetEntityMotionPacket packet) {
        if (packet.getId() == this.player.getId() && this.player.hurtMarked) {
            Bukkit.getRegionScheduler().run(Main.getInstance(), player.getBukkitEntity().getLocation(), task -> {
                this.player.hurtMarked = true;
                this.player.lerpMotion(packet.getXa(), packet.getYa(), packet.getZa());
            });
        }
    }

    private void handleCustomPayloadPacket(@NotNull ClientboundCustomPayloadPacket packet) {
        var payload = packet.payload();
        var resourceLocation = payload.type().id();
        var channel = resourceLocation.getNamespace() + ":" + resourceLocation.getPath();

        if (!channel.equals(BUNGEE_CORD_CORRECTED_CHANNEL)) {
            return;
        }

        if (!(payload instanceof DiscardedPayload discardedPayload)) {
            return;
        }

        var recipient = Bukkit
                .getOnlinePlayers()
                .stream()
                .filter(manager::isNotFake)
                .findAny()
                .orElse(null);

        if (recipient == null) {
            //注解掉烦人的日志，因为没有真人玩家的时候一直触发
            //log.warning("Failed to forward a plugin message cause non real players in the server");
            return;
        }

        var message = getDiscardedPayloadData(discardedPayload);
        recipient.sendPluginMessage(Main.getInstance(), BUNGEE_CORD_CHANNEL, message);
    }

    private byte[] getDiscardedPayloadData(@NotNull DiscardedPayload payload) {
        return payload.data();
    }


    /**
     * 重写获取 ping 的方法
     * 我觉得假人的延迟都是0，看的不舒服，我直接写一个随机数的范围来模拟延迟.
     *
     * @return
     */
    @Override
    public int latency() {
        return latency;
    }

}