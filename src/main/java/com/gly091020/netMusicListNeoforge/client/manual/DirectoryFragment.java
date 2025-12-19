package com.gly091020.netMusicListNeoforge.client.manual;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.mc.neoforge.MuiForgeApi;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.ImageView;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DirectoryFragment extends EntriesFragment{
    public DirectoryFragment() {
        super(new Entries(getSelf().getDisplayName(),
                "", "manual/long_net_music_list.png", List.of(), List.of(), true));
    }

    public static IModInfo getSelf(){
        return ModList.get().getModFileById(NetMusicList.ModID).getMods().getFirst();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        var out = super.onCreateView(inflater, container, savedInstanceState);
        var leftPart = getLeftPart();
        var version = new TextView(requireContext());
        version.setText("v" + getSelf().getVersion().toString());
        version.setTextSize(10);
        version.setGravity(Gravity.RIGHT);
        leftPart.addView(version);

        var text = new TextView(requireContext());
        text.setText(Component.translatable("manual.net_music_list.desc").getString());
        text.setPadding(0, 10, 0, 0);
        leftPart.addView(text);
        return out;
    }

    @Override
    protected @NotNull ScrollView initRightPart() {
        var base = new ScrollView(requireContext());
        var rightPart = new LinearLayout(requireContext());
        rightPart.setOrientation(LinearLayout.VERTICAL);
        parseDirectory(rightPart);
        base.addView(rightPart);
        return base;
    }

    private void addItem(ViewGroup part, Entries entries, int depth){
        var item = new LinearLayout(requireContext());
        item.setOrientation(LinearLayout.HORIZONTAL);
        if(entries.imgID() != null){
            var image = new ImageView(requireContext());
            image.setImage(Image.create(NetMusicList.ModID, entries.imgID()));
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setAdjustViewBounds(true);
            item.addView(image, new LinearLayout.LayoutParams(
                    part.dp(30), part.dp(30)
            ));
        }
        var text = new TextView(requireContext());
        text.setText(entries.title());
        text.setPadding(5, 0, 0, 0);
        item.addView(text);
        item.setPadding(20 * depth, 0, 0, 3);

        if(entries.enable())
            item.setOnClickListener(view ->
                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().setScreen(MuiForgeApi.get().createScreen(new EntriesFragment(entries),
                                    null, Minecraft.getInstance().screen))));

        part.addView(item);
    }

    private void parseDirectory(ViewGroup part){
        for(Object data: EntriesRegistry.getAllEntries()){
            if(data instanceof Entries entries)addItem(part, entries, 0);
            else if (data instanceof EntriesRegistry.Directory directory) {
                parseDirectory(part, directory, 1);
            }
        }
    }

    private void parseDirectory(ViewGroup part, EntriesRegistry.Directory directory, int depth){
        for(Object data: directory.getAllEntries()){
            if(data instanceof Entries entries)addItem(part, entries, depth);
            else if (data instanceof EntriesRegistry.Directory directory1) {
                parseDirectory(part, directory1, depth + 1);
            }
        }
    }
}
