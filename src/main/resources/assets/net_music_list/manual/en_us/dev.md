<!-- entry.title:以防你忘记了怎么写手册 -->
<!-- entry.button:example -->
<!-- entry.enable:true -->
<!-- entry.ico:gui/gly091020.png -->
<!-- entry.img:gui/bg.png,img1 -->
<!-- entry.img:gui/old_bg.png,img2 -->
<!-- entry.img:gui/server.png,img3 -->
<!-- entry.img:gui/default.png,img4 -->

# 网络音乐机：更好的体验手册编写方法

写完markdown手册系统才发现已经有模组实现了啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊

## 文件结构

- assets/net_music_list/manual
  - en_us
    - all_entries.json
    - 一些文件.md
  - zh_cn
      - all_entries.json
      - 一些文件.md

## all_entries.json定义格式

```json
[
    "一些文件.md",
    ["文件夹/一些文件.md"],
    [
      "文件夹1/一些文件.md", 
      ["文件夹1/文件夹/一些文件.md"]
    ]
]
```

## markdown专有方法

如果文件中出现以下注释：

```markdown
<!-- entry.方法:任意内容 -->
```

则是此模组的专有方法。目前可使用的方法：

### title

指定游戏内显示的章节标题。

### ico

图标，假设冒号后面为：
```gui/old_bg.png```
则会寻找```assets/net_music_list/textures/gui/old_bg.png```。

### img

画廊图片，假设冒号后面为：```gui/old_bg.png,图片1```则会寻找```assets/net_music_list/textures/gui/old_bg.png```并显示图片备注```图片1```。

此方法可多次使用。

### button

按钮组ID，可用```EntriesRegistry.java```注册，每个按钮显示在画廊按钮上方。

### enable

是否启用，禁用（false）的章节不能点击。

此方法是为了给章节组添加标题用的。
