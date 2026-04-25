package com.gly091020.netMusicListNeoforge.packet;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class PacketRegistry {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("2.0"); // 协议版本

        registrar.playToServer(
                MusicListDataPacket.TYPE,
                MusicListDataPacket.STREAM_CODEC,
                ServerHandler::handleServerMusicListDataPacket
        );

        registrar.playToServer(
                DeleteMusicDataPacket.TYPE,
                DeleteMusicDataPacket.STREAM_CODEC,
                ServerHandler::handleServerDeleteMusicDataPacket
        );

        registrar.playToServer(
                MoveMusicDataPacket.TYPE,
                MoveMusicDataPacket.STREAM_CODEC,
                ServerHandler::handleServerMoveMusicDataPacket
        );

        registrar.playToServer(
                UpdatePlayerMusicPacket.TYPE,
                UpdatePlayerMusicPacket.STREAM_CODEC,
                ServerHandler::handleServerUpdateMusicPacket
        );

        registrar.playToServer(
                StopMusicPacketServer.TYPE,
                StopMusicPacketServer.STREAM_CODEC,
                ServerHandler::handleStopMusicPacket
        );

        registrar.playToServer(
                UpdateMusicIndexCTSPacket.TYPE,
                UpdateMusicIndexCTSPacket.STREAM_CODEC,
                ServerHandler::handleUpdateMusicIndexCTSPacket
        );

        if(FMLEnvironment.getDist().isClient()){
            registrar.playBidirectional(
                    PlayerPlayMusicPacket.TYPE,
                    PlayerPlayMusicPacket.STREAM_CODEC,
                    ServerHandler::handleServerPlayerPlayPacket,
                    ClientHandler::handleClientPlayerPlayPacket
            );

            registrar.playBidirectional(
                    MusicPlayerEntityPlayMusicPacket.TYPE,
                    MusicPlayerEntityPlayMusicPacket.STREAM_CODEC,
                    ServerHandler::handleServerMusicPlayerEntityPlayMusicPacket,
                    ClientHandler::handleClientMusicPlayerEntityPlayMusicPacket
            );
            registrar.playToClient(
                    StopMusicPacket.TYPE,
                    StopMusicPacket.STREAM_CODEC,
                    ClientHandler::handleStopMusicPacket
            );
        }else{
            registrar.playToClient(
                    StopMusicPacket.TYPE,
                    StopMusicPacket.STREAM_CODEC,
                    (playEnderMusicPlayerPacket, iPayloadContext) -> {}
            );
            registrar.playBidirectional(
                    PlayerPlayMusicPacket.TYPE,
                    PlayerPlayMusicPacket.STREAM_CODEC,
                    ServerHandler::handleServerPlayerPlayPacket,
                    (payload, context) -> {}
            );

            registrar.playBidirectional(
                    MusicPlayerEntityPlayMusicPacket.TYPE,
                    MusicPlayerEntityPlayMusicPacket.STREAM_CODEC,
                    ServerHandler::handleServerMusicPlayerEntityPlayMusicPacket,
                    (payload, context) -> {}
            );
        }
    }
}
