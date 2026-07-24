# BilicubeWarp

Paper 1.21.11 地标传送插件。

## 等级

| 等级 | 名 | 图标 | 欢迎语行数 |
|------|-----|------|-----------|
| I | 萤火 | 火把 | 0 |
| II | 灯塔 | 灯笼 | 1 |
| III | 晨星 | 下界之星 | 2 |
| IV | 极光 | 末影水晶 | 2 |

## 命令

根命令 `/warp`

### 玩家

| 命令 | 说明 |
|------|------|
| `/warp` | 打开菜单 |
| `/warp menu` | 打开菜单 |
| `/warp list` | 列出所有地标 |
| `/warp info <内部名>` | 查看详情 |
| `/warp tp <内部名>` | 传送（需 `warp.tp`，默认 OP） |

### 管理员 (`warp.admin`)

| 命令 | 说明 |
|------|------|
| `/warp set <内部名> <萤火\|灯塔\|晨星\|极光>` | 创建地标 |
| `/warp remove <内部名>` | 删除地标 |
| `/warp level <内部名> <等级>` | 修改等级 |
| `/warp owner <内部名> <玩家>` | 分配所有者 |
| `/warp open <玩家>` | 为玩家打开菜单 |
| `/warp sign <内部名>` | 创建传送牌（看着木牌执行） |
| `/warp reload` | 重载配置 |

### 所有者/地标管理员

| 命令 | 说明 |
|------|------|
| `/warp edit <内部名> displayname <显示名>` | 设显示名（支持 `#RRGGBB` hex） |
| `/warp edit <内部名> desc <简介>` | 设简介（`</br>` 换行，支持 hex） |
| `/warp edit <内部名> welcome <1\|2> <文本>` | 设欢迎语 |
| `/warp edit <内部名> icon` | 设图标（手持物品，保留 NBT） |
| `/warp member add\|remove\|list <内部名> [玩家]` | 管理成员 |
| `/warp admin add\|remove\|list <内部名> [玩家]` | 管理地标管理员（仅 owner 或 warp.admin） |

## 传送

| 方式 | 权限 | 默认 |
|------|------|------|
| 右键木牌 | `warp.tp.sign` | true |
| 菜单左键 | `warp.tp.menu` | true |
| `/warp tp` | `warp.tp` | OP |

## 构建

```bash
./gradlew build
./gradlew runServer
```
