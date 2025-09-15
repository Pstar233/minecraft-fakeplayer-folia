package io.github.hello09x.fakeplayer.v1_21_6.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public class FakeServerConfigurationPacketListenerImpl extends ServerCommonPacketListenerImpl implements ServerConfigurationPacketListener, TickablePacketListener {


    public FakeServerConfigurationPacketListenerImpl(MinecraftServer server, Connection connection, CommonListenerCookie cookie, ServerPlayer player) {
        super(server, connection, cookie, player);
        this.createCookie(this.player.clientInformation());
    }

    @Override
    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket serverboundFinishConfigurationPacket) {

    }

    @Override
    public void handleSelectKnownPacks(ServerboundSelectKnownPacks serverboundSelectKnownPacks) {

    }

    @Override
    protected GameProfile playerProfile() {
        return null;
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket) {

    }

    @Override
    public void tick() {
        super.keepConnectionAlive();
    }

    @Override
    public boolean isAcceptingMessages() {
        return false;
    }
}
