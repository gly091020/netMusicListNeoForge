package com.gly091020.netMusicListNeoforge.datagen;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BlockModelGenerator extends BlockStateProvider {
    public BlockModelGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, NetMusicList.ModID, exFileHelper);
    }
    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(NetMusicList.ENDER_MUSIC_PLAYER.get(),
                models().cubeBottomTop(
                        "ender_music_player",
                        modLoc("block/ender_player_side"),
                        modLoc("block/ender_player_bottom"),
                        modLoc("block/ender_player_top")
                )
        );
    }
}
