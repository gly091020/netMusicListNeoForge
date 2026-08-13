package com.gly091020.netMusicListNeoforge.api;

/**
 * 服务端权威的音乐播放计算单元。
 * 每个 Ringer 对应一个可播放音乐的音源（如随身播放器物品、放置的播放器实体），
 * 由 {@link com.gly091020.netMusicListNeoforge.server.music.MusicRingManager} 统一驱动。
 * 客户端只负责按服务端下发的命令播放/停止，不做任何播放逻辑。
 */
public interface IMusicRinger extends MusicRingerAccess {
    void setRingerPlaying(boolean playing);

    void setRingerPosition(long position);

    /** 由 MusicRingManager 每 tick 调用，推进进度并处理切歌。 */
    void ringerTick();

    /** 从管理器中移除时调用，用于清理与持久化。 */
    default void onRemove() {
    }

    /** 服务端关停等场景下的强制持久化。 */
    default void ringerPersist() {
    }
}
