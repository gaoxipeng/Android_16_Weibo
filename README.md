# Android_16_Weibo

一个无广告的微博安卓客户端。

基于 Jetpack Compose 的第三方微博客户端，通过 WebView 登录态 + 微博 PC 端 `ajax` 接口读取数据，原生渲染信息流、详情、搜索与个人主页。

## 技术栈

- **UI**：Jetpack Compose + Material 3
- **网络**：隐藏 WebView 维持 Cookie，`HttpURLConnection` 调用 `weibo.com/ajax/*`
- **媒体**：Coil 风格自研 `RemoteImage`、Media3 ExoPlayer（HLS/DASH）
- **存储**：本地 JSON / SharedPreferences 缓存时间线、个人资料、表情、账号

## 项目结构

```
app/src/main/java/com/example/myweibo/
├── MainActivity.kt              # 入口，Cookie 桥接
├── VideoPipActivity.kt          # 视频画中画
├── data/
│   ├── WeiboWebSession.kt       # WebView 会话、接口请求、上传
│   ├── WeiboJsonParser.kt       # 微博 JSON/HTML 解析
│   ├── WeiboEndpoints.kt        # ajax 路径常量
│   ├── WeiboModels.kt           # FeedItem、UserProfile 等模型
│   ├── MentionCandidateUtils.kt   # 评论 @ 候选合并与匹配
│   ├── FeedTimelineUtils.kt       # 时间线排序、表情收集
│   ├── EmoticonCacheStore.kt      # 表情本地缓存
│   ├── TimelineCacheStore.kt      # 首页时间线缓存
│   ├── MineCacheStore.kt          # 个人主页缓存
│   ├── WeiboAccountStore.kt       # 多账号 Cookie
│   └── *Store.kt                  # 搜索、评论排序、图片/播放设置
└── ui/
    ├── WeiboApp.kt                # 主界面（导航、Feed、详情、设置等）
    └── theme/                     # 主题色与字体
```

## 主要功能

| 模块 | 说明 |
|------|------|
| 首页 | 最新微博 / 好友圈切换，下拉刷新，表情渲染，长微博展开，话题跳转搜索 |
| 详情 | 评论/转发分区，楼中楼，评论弹窗（文字/表情/图片/@） |
| 搜索 | 微博/用户搜索，热搜，综合/实时排序，话题临时搜索 |
| 我的 | 微博与相册，粉丝/关注列表，多账号，设置 |
| 消息/写博 | 嵌入 `m.weibo.cn/message` 与 `m.weibo.cn/compose/` |
| 媒体 | 图片全屏/保存/分享；视频内联/浮窗/全屏/画中画，横竖屏切换 |

## 构建

### Debug

```bash
./gradlew :app:assembleDebug
```

### Release

1. 复制 `keystore.properties.example` 为 `keystore.properties` 并填写签名信息
2. 执行：

```bash
./gradlew :app:assembleRelease
```

输出：`app/build/outputs/apk/release/app-release.apk`

## 配置要求

- `minSdk` / `targetSdk`：36
- JDK 11+
- Release 签名文件需自行配置，未配置时 Release 包不可直接安装覆盖 Debug 包

## 使用说明

应用内路径：**我的 → 设置 → 使用说明**

主要交互摘要：

- **话题**：首页或用户主页点击 `#话题#` 会进入搜索；从用户主页进入时，返回一次回到该主页
- **视频**：单击中心播放；双击打开浮窗；长按预览后全屏；浮窗可边浏览边播放
- **浮窗**：右上角可全屏；竖屏全屏时可切换横屏/竖屏；底部胶囊进度条可拖动
- **底部栏**：滚动收起为小胶囊，单击展开，长按拖动快速切 Tab

## 数据来源说明

- 信息流、评论、转发、用户资料：`https://weibo.com/ajax/*`
- 表情同步：`/ajax/statuses/config`
- 评论 @ 候选：互相关注用户 + 全部关注/粉丝昵称索引
- 消息/写博：移动版微博网页，与首页共用 Cookie

## 免责声明

本应用为非官方客户端，仅供学习交流。请遵守微博用户协议，勿用于商业或批量抓取。
