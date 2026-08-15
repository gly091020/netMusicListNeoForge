package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.compat.cloth.MenuIntegration;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.gly091020.netMusicListNeoforge.config.ProxyEntry;
import com.gly091020.netMusicListNeoforge.config.UserProxy;
import com.gly091020.netMusicListNeoforge.util.ProxyUtil;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Mixin(MenuIntegration.class)
public class MenuIntegrationMixin {
    @Redirect(method = "generalConfig", at = @At(value = "INVOKE", target = "Lme/shedaniel/clothconfig2/api/ConfigCategory;addEntry(Lme/shedaniel/clothconfig2/api/AbstractConfigListEntry;)Lme/shedaniel/clothconfig2/api/ConfigCategory;",
            ordinal = 1))
    private static ConfigCategory addProxyConfig(ConfigCategory instance, AbstractConfigListEntry<?> abstractConfigListEntry){
        instance.addEntry(new ProxyEntry(
                Component.translatable("config.net_music_list.proxy"),
                userProxy -> {
                    if(userProxy.equals(UserProxy.EMPTY)){
                        GeneralConfig.PROXY_TYPE.set(Proxy.Type.DIRECT);
                    }else {
                        var proxy = userProxy.getProxy();
                        GeneralConfig.PROXY_TYPE.set(proxy.type());
                        if(proxy.address() instanceof InetSocketAddress address)
                            GeneralConfig.PROXY_ADDRESS.set(address.getHostString() + ":" + address.getPort());
                    }
                },
                false,
                UserProxy.of(ProxyUtil.getSystemProxy()),
                UserProxy.of(NetWorker.getProxyFromConfig())
        ));
        return instance;
    }

    @Redirect(method = "generalConfig", at = @At(value = "INVOKE", target = "Lme/shedaniel/clothconfig2/api/ConfigCategory;addEntry(Lme/shedaniel/clothconfig2/api/AbstractConfigListEntry;)Lme/shedaniel/clothconfig2/api/ConfigCategory;",
            ordinal = 2))
    private static ConfigCategory clearProxyConfig(ConfigCategory instance, AbstractConfigListEntry<?> abstractConfigListEntry){
        return instance;
    }
}
