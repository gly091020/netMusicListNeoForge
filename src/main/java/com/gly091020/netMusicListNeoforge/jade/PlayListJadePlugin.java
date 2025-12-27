package com.gly091020.netMusicListNeoforge.jade;

import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class PlayListJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(
                PlayerBlockComponentProvider.INSTANCE, BlockMusicPlayer.class
        );
        registration.registerEntityComponent(
                PlayerEntityComponentProvider.INSTANCE, MusicPlayerEntity.class
        );
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(
                PlayerEntityServerDataProvider.INSTANCE, MusicPlayerEntity.class
        );
    }
}
