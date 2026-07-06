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
| 首页 | 关注流，下拉刷新，表情富文本，长微博展开，话题跳转搜索，列表滚动位置记忆 |
| 详情 | 评论/转发分区，楼中楼，评论弹窗（文字/表情/图片/@） |
| 搜索 | 微博/用户搜索，热搜，综合/实时排序，话题临时搜索 |
| 我的 | 微博与相册，粉丝/关注列表，多账号，设置 |
| 写微博 | 原生编辑界面，支持文字/表情/图片/@，草稿保留 |
| 消息 | 嵌入 `m.weibo.cn/message`，与首页共用登录态 |
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

- **登录**：首次在设置中完成登录后，会自动返回首页并同步数据；消息页在登录后自动加载
- **列表滑动**：首页、个人主页等列表使用系统默认滑动手感；惯性滚动中点击图片/视频会先停止滚动，避免误触
- **话题**：首页或用户主页点击 `#话题#` 会进入搜索；从用户主页进入时，返回一次回到该主页
- **视频**：单击中心播放；双击打开浮窗；长按预览，上滑进浮窗、下滑关闭
- **详情续播**：首页播放中进入详情，会自动续播并保持进度
- **详情浮窗**：详情页上滑使视频卡片离开屏幕后自动浮窗；下拉至卡片完全回到屏幕后缩回卡片播放
- **用户主页**：顶部封面（含视频封面）上滑时资料区平滑收起；微博与相册内视频手势与首页一致
- **评论图片**：点击评论缩略图以过渡动画全屏查看，收起时回到原位置；长按可保存或分享
- **搜索联想**：输入关键词时，搜索框上方会显示毛玻璃联想词面板
- **浮窗**：右上角可全屏；竖屏全屏时可切换横屏/竖屏；底部胶囊进度条可拖动；上下滑优先关闭
- **底部栏**：上滑列表时自动收起到小胶囊，单击展开，长按拖动快速切 Tab；文字大小不受系统字体缩放影响

## v1.5 更新摘要

- 搜索框对齐底栏大胶囊（20dp）；联想词、键盘弹出时与搜索框间距 8dp
- 修复竖屏全屏播放时进度条跨黑边的液态玻璃显示问题
- 优化底栏 Tab 切换小胶囊未放大时的磨砂质感
- 首页、个人微博与相册统一为距列表底部 3 条时加载更多
- 他人主页浏览时底栏收起可正确停止列表滚动

## v1.4 更新摘要

- 首次登录成功后自动跳转首页并同步数据
- 修复消息页在登录后仍显示网页登录页的问题
- 恢复系统默认列表滑动手感，优化首页与个人主页滚动流畅度
- 修复个人主页顶部封面区域上滑时偶发「滑过远」的问题
- 优化富文本正文滑动性能，保留完整表情渲染
- 修复底栏随列表上滑自动收起，以及惯性滚动中误触图片/视频

## 数据来源说明

- 信息流、评论、转发、用户资料：`https://weibo.com/ajax/*`
- 表情同步：`/ajax/statuses/config`
- 评论 @ 候选：互相关注用户 + 全部关注/粉丝昵称索引
- 消息：移动版微博网页；写微博：原生界面 + `m.weibo.cn` 接口

## 免责声明

本应用为非官方客户端，仅供学习交流。请遵守微博用户协议，勿用于商业或批量抓取。
