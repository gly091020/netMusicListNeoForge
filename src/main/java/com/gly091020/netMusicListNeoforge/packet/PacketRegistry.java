package com.gly091020.netMusicListNeoforge.packet;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PacketRegistry {
    public static void registryServer(final RegisterPayloadHandlersEvent event){
        var CHANNEL = event.registrar("1.3");
        CHANNEL.commonToServer(
                DeleteMusicDataPacket.TYPE,
                DeleteMusicDataPacket.STREAM_CODEC,
                ServerHandler::handleServerDeleteMusicDataPacket
        );
        CHANNEL.commonToServer(
                MusicListDataPacket.TYPE,
                MusicListDataPacket.STREAM_CODEC,
                ServerHandler::handleServerMusicListDataPacket
        );

        CHANNEL.commonToServer(MoveMusicDataPacket.TYPE,
                MoveMusicDataPacket.STREAM_CODEC,
                ServerHandler::handleServerMoveMusicDataPacket
        );

        CHANNEL.commonToServer(
                PlayerPlayMusicCTSPacket.TYPE,
                PlayerPlayMusicCTSPacket.STREAM_CODEC,
                ServerHandler::handleServerPlayerPlayPacket
        );
        CHANNEL.commonToServer(
                StopMusicCTSPacket.TYPE,
                StopMusicCTSPacket.STREAM_CODEC,
                ServerHandler::handleStopMusicCTSPacket
        );
        CHANNEL.commonToServer(
                UpdatePlayerMusicPacket.TYPE,
                UpdatePlayerMusicPacket.STREAM_CODEC,
                ServerHandler::handleUpdatePlayerMusicPacket
        );
        CHANNEL.commonToClient(
                StopMusicSTCPacket.TYPE,
                StopMusicSTCPacket.STREAM_CODEC,
                (packet, content) -> {}
        );
        CHANNEL.commonToClient(
                PlayerPlayMusicSTCPacket.TYPE,
                PlayerPlayMusicSTCPacket.STREAM_CODEC,
                (packet, content) -> {}
        );
    }

    public static void registryClient(final RegisterPayloadHandlersEvent event){
        var CHANNEL = event.registrar("1.3");
        CHANNEL.commonToServer(
                DeleteMusicDataPacket.TYPE,
                DeleteMusicDataPacket.STREAM_CODEC,
                ServerHandler::handleServerDeleteMusicDataPacket
        );
        CHANNEL.commonToServer(
                MusicListDataPacket.TYPE,
                MusicListDataPacket.STREAM_CODEC,
                ServerHandler::handleServerMusicListDataPacket
        );

        CHANNEL.commonToServer(MoveMusicDataPacket.TYPE,
                MoveMusicDataPacket.STREAM_CODEC,
                ServerHandler::handleServerMoveMusicDataPacket
        );

        CHANNEL.commonToServer(
                PlayerPlayMusicCTSPacket.TYPE,
                PlayerPlayMusicCTSPacket.STREAM_CODEC,
                ServerHandler::handleServerPlayerPlayPacket
        );
        CHANNEL.commonToServer(
                StopMusicCTSPacket.TYPE,
                StopMusicCTSPacket.STREAM_CODEC,
                ServerHandler::handleStopMusicCTSPacket
        );
        CHANNEL.commonToServer(
                UpdatePlayerMusicPacket.TYPE,
                UpdatePlayerMusicPacket.STREAM_CODEC,
                ServerHandler::handleUpdatePlayerMusicPacket
        );
        CHANNEL.commonToClient(
                StopMusicSTCPacket.TYPE,
                StopMusicSTCPacket.STREAM_CODEC,
                ClientHandler::handleStopMusicSTCPacket
        );
        CHANNEL.commonToClient(
                PlayerPlayMusicSTCPacket.TYPE,
                PlayerPlayMusicSTCPacket.STREAM_CODEC,
                ClientHandler::handleClientPlayerPlayPacket
        );
    }
}
