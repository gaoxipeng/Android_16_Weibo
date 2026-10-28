# 微博认证图标来源

2026-10-28 使用应用现有登录态读取官方微博网页，定位其当前部署资源：

- https://h5.sinaimg.cn/m/weibo-pro-next/assets/index-WivZsFc9.js
- https://h5.sinaimg.cn/m/weibo-pro-next/assets/index-0kGZqrOp.css

认证规则取自该脚本的 `getVerifiedIcon()`，图形取自内嵌的 `vred`、`vorange`、`vblue`、`vyellow`、`vgold`、`vgrey` SVG。原始路径转换为 Android VectorDrawable，圆形几何保持不变；`currentColor` 根据官方 CSS 解析为白色或金黄色外圈。使用静态图形，不包含网页光效精灵动画。

| verified_type | verified_type_ext | 官方图标 |
| --- | --- | --- |
| 0 | 1 | vgold 金V，高影响力创作者 |
| 0 | 2 | vorange 橙V，优质创作者 |
| 0 | 其他 | vyellow 黄V，认证作者 |
| 1–7 | -1 | vgrey 灰V，过期组织认证 |
| 3 | 53 | vred 红色官方组织认证，例如央视新闻 |
| 1–7 | 其他 | vblue 蓝V，官方组织认证 |

明确 `verified=false` 时不显示认证；会员等级不参与认证类型判定。
