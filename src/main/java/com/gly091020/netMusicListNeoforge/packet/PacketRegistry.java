package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class PacketRegistry {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.10"); // 协议版本

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
                MusicPlayerActionPacket.TYPE,
                MusicPlayerActionPacket.STREAM_CODEC,
                ServerHandler::handleMusicPlayerAction
        );

        if(NetMusicList.CONFIG.allowLyricToServer)
            registrar.playToServer(
                    UpdateBlockLyricPacket.TYPE,
                    UpdateBlockLyricPacket.STREAM_CODEC,
                    ServerHandler::handleUpdateBlockLyricPacket
            );

        if(FMLEnvironment.dist.isClient()){
            registrar.playToClient(
                    MusicPlayCommandPacket.TYPE,
                    MusicPlayCommandPacket.STREAM_CODEC,
                    ClientHandler::handleMusicPlayCommand
            );

            registrar.playToClient(
                    MusicStopCommandPacket.TYPE,
                    MusicStopCommandPacket.STREAM_CODEC,
                    ClientHandler::handleMusicStopCommand
            );

            registrar.playToClient(
                    MusicSyncCommandPacket.TYPE,
                    MusicSyncCommandPacket.STREAM_CODEC,
                    ClientHandler::handleMusicSyncCommand
            );

            registrar.playToClient(
                    PlayEnderMusicPlayerPacket.TYPE,
                    PlayEnderMusicPlayerPacket.STREAM_CODEC,
                    ClientHandler::handleClientEnderPlayerPlayPacket
            );

        }else{
            registrar.playToClient(
                    MusicPlayCommandPacket.TYPE,
                    MusicPlayCommandPacket.STREAM_CODEC,
                    (musicPlayCommandPacket, iPayloadContext) -> {}
            );

            registrar.playToClient(
                    MusicStopCommandPacket.TYPE,
                    MusicStopCommandPacket.STREAM_CODEC,
                    (musicStopCommandPacket, iPayloadContext) -> {}
            );

            registrar.playToClient(
                    MusicSyncCommandPacket.TYPE,
                    MusicSyncCommandPacket.STREAM_CODEC,
                    (musicSyncCommandPacket, iPayloadContext) -> {}
            );

            registrar.playToClient(
                    PlayEnderMusicPlayerPacket.TYPE,
                    PlayEnderMusicPlayerPacket.STREAM_CODEC,
                    (playEnderMusicPlayerPacket, iPayloadContext) -> {}
            );
        }
    }
}
