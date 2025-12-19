package com.gly091020.netMusicListNeoforge.client.manual;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.markflow.Markflow;
import icyllis.modernui.markflow.MarkflowPlugin;
import icyllis.modernui.markflow.MarkflowTheme;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.transition.Fade;
import icyllis.modernui.transition.Slide;
import icyllis.modernui.transition.TransitionSet;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntriesFragment extends Fragment {
    private Markflow mMarkflow;
    private final Entries entries;
    private ViewGroup leftPart;
    private ViewGroup rightPart;

    public EntriesFragment(Entries entries){
        super();
        this.entries = entries;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        initMarkDown();
        var base = new LinearLayout(Objects.requireNonNull(getContext()));
        base.setOrientation(LinearLayout.HORIZONTAL);

        var leftPart = new LinearLayout(getContext());
        var leftParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        leftParams.weight = 0.3f;
        leftPart.setGravity(Gravity.CENTER_HORIZONTAL);
        leftPart.setOrientation(LinearLayout.VERTICAL);
        var rightParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        rightParams.weight = 0.7f;
        leftParams.setMargins(10, 10, 10, 10);
        rightParams.setMargins(10, 10, 10, 10);

        var rightPart = initRightPart();

        var title = new TextView(getContext());
        title.setText(entries.title());
        title.setTextSize(30);
        var titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 0);
        leftPart.addView(title, titleParams);
        if(entries.imgID() != null){
            var image = new ImageView(getContext());
            var imageParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            imageParams.setMargins(20, 20, 20, 20);
            image.setImage(Image.create(NetMusicList.ModID, entries.imgID()));
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setAdjustViewBounds(true);
            leftPart.addView(image, imageParams);
        }

        var line = new View(getContext());
        var lintParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            3
        );
        line.setBackground(new ColorDrawable(0x55E0E0E0));
        leftPart.addView(line, lintParams);

        var line1 = new View(getContext());
        var lintParams1 = new LinearLayout.LayoutParams(
                5,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        line1.setBackground(new ColorDrawable(0x55E0E0E0));

        for(Entries.Button buttonData: entries.buttons()){
            var button = new Button(getContext(), null, R.attr.buttonOutlinedStyle);
            button.setText(buttonData.name());
            var buttonParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            buttonParams.topMargin = 10;
            button.setOnClickListener(view -> Minecraft.getInstance().execute(buttonData.runnable()));
            leftPart.addView(button, buttonParams);
        }

        var imagesPopup = initPopupWindow();
        if(!entries.images().isEmpty()){
            var button = new Button(getContext(), null, R.attr.buttonOutlinedStyle);
            button.setText(Component.translatable("manual.net_music_list.images").getString());
            var buttonParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            buttonParams.topMargin = 10;
            button.setOnClickListener(view -> imagesPopup.showAtLocation(base, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0));
            leftPart.addView(button, buttonParams);
        }

        base.addView(leftPart, leftParams);
        base.addView(line1, lintParams1);
        base.addView(rightPart, rightParams);
        this.leftPart = leftPart;
        this.rightPart = rightPart;
        return base;
    }

    protected @NotNull ScrollView initRightPart() {
        var rightPart = new ScrollView(requireContext());
        var rightText = new TextView(requireContext());
        mMarkflow.setMarkdown(rightText, entries.markdown());
        rightText.setTextIsSelectable(true);
        rightPart.addView(rightText);
        return rightPart;
    }

    public ViewGroup getLeftPart() {
        return leftPart;
    }

    public ViewGroup getRightPart() {
        return rightPart;
    }

    private void initMarkDown(){
        Markflow.Builder builder = Markflow.builder(requireContext());
        final Typeface monoFont = Typeface.getSystemFont("JetBrains Mono Medium");
        if (monoFont != Typeface.SANS_SERIF) {
            builder.usePlugin(new MarkflowPlugin() {
                public void configureTheme(@NonNull MarkflowTheme.Builder builder) {
                    builder.codeTypeface(monoFont);
                }
            });
        }
        mMarkflow = builder.build();
    }

    private View createContentView() {
        var frameLayout = new FrameLayout(requireContext());

        var horizontalScrollView = new HorizontalScrollView(requireContext());
        var horizontalContainer = new LinearLayout(requireContext());
        horizontalContainer.setOrientation(LinearLayout.HORIZONTAL);
        horizontalScrollView.addView(horizontalContainer);
        var scrollViewParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        horizontalScrollView.setLayoutParams(scrollViewParams);

        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadii(
                frameLayout.dp(12), frameLayout.dp(12),
                0, 0
        );
        background.setColor(0x99999999);
        horizontalContainer.setBackground(background);
        horizontalContainer.setPadding(frameLayout.dp(16), frameLayout.dp(16),
                frameLayout.dp(16), frameLayout.dp(16));

        for(Entries.Image imageData: entries.images()){
            var imageBase = new LinearLayout(requireContext());
            imageBase.setOrientation(LinearLayout.VERTICAL);
            imageBase.setGravity(Gravity.CENTER);

            var layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            layoutParams.setMargins(0, 0, frameLayout.dp(16), 0);
            imageBase.setLayoutParams(layoutParams);

            var image = new ImageView(requireContext());
            image.setImage(Image.create(NetMusicList.ModID, imageData.id()));
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setAdjustViewBounds(true);

            var imageParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0, 1.0f
            );
            imageParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            imageParams.height = 0;
            imageParams.weight = 1.0f;
            imageParams.setMargins(0, 0, 0, frameLayout.dp(8));
            imageBase.addView(image, imageParams);

            var text = new TextView(requireContext());
            text.setText(imageData.tip());
            text.setTextSize(12);
            text.setTextStyle(Typeface.BOLD);
            text.setGravity(Gravity.CENTER);
            text.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            imageBase.addView(text);
            horizontalContainer.addView(imageBase);
        }

        frameLayout.addView(horizontalScrollView);

        return frameLayout;
    }

    private PopupWindow initPopupWindow() {
        var mPopupWindow = new PopupWindow(requireContext());
        var mContentView = createContentView();
        mPopupWindow.setContentView(mContentView);
        var displayMetrics = requireContext().getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        mPopupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight((int) (screenHeight * 0.5f));
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        var t = new TransitionSet();
        t.setOrdering(0);
        t.addTransition(new Fade()).addTransition(new Slide());
        mPopupWindow.setExitTransition(t);
        mPopupWindow.setEnterTransition(t);
        return mPopupWindow;
    }
}