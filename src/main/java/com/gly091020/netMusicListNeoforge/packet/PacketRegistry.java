package com.gly091020.netMusicListNeoforge.packet;

import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class PacketRegistry {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.6"); // 协议版本

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
                UpdateMusicTickCTSPacket.TYPE,
                UpdateMusicTickCTSPacket.STREAM_CODEC,
                ServerHandler::handlePlayerUpdateTickPacket
        );

        registrar.playToServer(
                StopMusicPacketServer.TYPE,
                StopMusicPacketServer.STREAM_CODEC,
                ServerHandler::handleStopMusicPacket
        );

        registrar.playBidirectional(
                PlayerPlayMusicPacket.TYPE,
                PlayerPlayMusicPacket.STREAM_CODEC,
                (playerPlayMusicPacket, iPayloadContext) -> {
                    if(iPayloadContext.flow() == PacketFlow.SERVERBOUND){
                        ServerHandler.handleServerPlayerPlayPacket(playerPlayMusicPacket, iPayloadContext);
                    }else{
                        ClientHandler.handleClientPlayerPlayPacket(playerPlayMusicPacket, iPayloadContext);
                    }
                }
        );

        if(FMLEnvironment.dist.isClient()){
            registrar.playToClient(
                    PlayEnderMusicPlayerPacket.TYPE,
                    PlayEnderMusicPlayerPacket.STREAM_CODEC,
                    ClientHandler::handleClientEnderPlayerPlayPacket
            );

            registrar.playToClient(
                    StopMusicPacket.TYPE,
                    StopMusicPacket.STREAM_CODEC,
                    ClientHandler::handleStopMusicPacket
            );
        }else{
            registrar.playToClient(
                    PlayEnderMusicPlayerPacket.TYPE,
                    PlayEnderMusicPlayerPacket.STREAM_CODEC,
                    (playEnderMusicPlayerPacket, iPayloadContext) -> {}
            );

            registrar.playToClient(
                    StopMusicPacket.TYPE,
                    StopMusicPacket.STREAM_CODEC,
                    (playEnderMusicPlayerPacket, iPayloadContext) -> {}
            );
        }
    }
}
