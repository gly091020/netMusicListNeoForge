// 允许其他模组/代码自定义 ffmpeg 命令行的输入级参数（如 -headers、-http_proxy）。
package com.gly091020.netMusicListNeoforge.api.ffmpeg;

import java.net.URL;
import java.util.List;

/**
 * 自定义 ffmpeg 命令行参数。
 * <p>
 * 返回的参数会被插入到输入 URL（{@code -i <url>}）之前，因此只能添加输入级选项。
 * 多个注册的 {@link FFmpegArgCustomizer} 会按注册顺序依次生效。
 */
@FunctionalInterface
public interface FFmpegArgCustomizer {
    /**
     * 根据将要播放的音频 URL 返回需要附加的 ffmpeg 参数；返回 null 或空列表表示不添加。
     *
     * @param url 即将交给 ffmpeg 的音频 URL（可能已经是解析后的直链）
     * @return 要插入到 {@code -i <url>} 之前的参数列表
     */
    List<String> customize(URL url);
}
