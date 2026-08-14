package com.gly091020.netMusicListNeoforge.media.qq_music.resource;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QQMusicURLData {
    @SerializedName("code")
    public int code;

    @SerializedName("req_0")
    public REQ req;

    public static class REQ{
        @SerializedName("data")
        public Data data;
    }

    public static class Data{
        @SerializedName("sip")
        public List<String> sip;

        @SerializedName("midurlinfo")
        public List<UrlInfo> urlInfos;
    }

    public static class UrlInfo{
        @SerializedName("purl")
        public String purl;
    }

    @Nullable
    public String getUrl(){
        if(req == null)return null;
        if(req.data == null)return null;
        if(req.data.sip.isEmpty())return null;
        if(req.data.urlInfos.isEmpty())return null;
        var sip = req.data.sip.getFirst();
        // 多个品质候选按顺序返回，取第一个可用的 mp3
        for (var info : req.data.urlInfos) {
            if (info.purl != null && !info.purl.isBlank()) {
                return sip + info.purl;
            }
        }
        return null;
    }
}
