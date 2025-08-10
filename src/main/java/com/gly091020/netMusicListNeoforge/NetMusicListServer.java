package com.gly091020.netMusicListNeoforge;

import com.gly091020.netMusicListNeoforge.packet.PacketRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = NetMusicList.ModID, dist = Dist.DEDICATED_SERVER)
public class NetMusicListServer {
    public NetMusicListServer(IEventBus modEventBus) {
        modEventBus.addListener(PacketRegistry::registryServer);
    }
}
