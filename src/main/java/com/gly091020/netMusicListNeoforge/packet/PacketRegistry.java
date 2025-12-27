package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
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
        final PayloadRegistrar registrar = event.registrar("1.9"); // 协议版本

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

        if(NetMusicList.CONFIG.allowLyricToServer)
            registrar.playToServer(
                    UpdateBlockLyricPacket.TYPE,
                    UpdateBlockLyricPacket.STREAM_CODEC,
                    ServerHandler::handleUpdateBlockLyricPacket
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

        registrar.playBidirectional(
                MusicPlayerEntityPlayMusicPacket.TYPE,
                MusicPlayerEntityPlayMusicPacket.STREAM_CODEC,
                (musicPlayerEntityPlayMusicPacket, iPayloadContext) -> {
                    if(iPayloadContext.flow() == PacketFlow.SERVERBOUND){
                        ServerHandler.handleServerMusicPlayerEntityPlayMusicPacket(musicPlayerEntityPlayMusicPacket, iPayloadContext);
                    }else{
                        ClientHandler.handleClientMusicPlayerEntityPlayMusicPacket(musicPlayerEntityPlayMusicPacket, iPayloadContext);
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
