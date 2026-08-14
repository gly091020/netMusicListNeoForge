package com.gly091020.netMusicListNeoforge.media.netease;

import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class NetEaseMusicSearch {

    @SerializedName("code")
    private int code;

    @SerializedName("result")
    private Result result;

    public static class Result {

        @SerializedName("songs")
        private List<NetEaseMusicSong> songs;
    }

    public List<NetEaseMusicSong> getSongs() {
        if (result == null || result.songs == null) {
            return Collections.emptyList();
        }
        return result.songs;
    }

    public int getCode() {
        return code;
    }
}
