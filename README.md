# ICommand
> Powerful ItemStack extensions.

---
### Customization
---

You can use the **ICommand** to do anything about **Item**.

```yaml
CustomRequire:
  Lore: 'CustomRequire'
  Type: RIGHT_CLICK_BLOCK|RIGHT_CLICK_AIR
  Commands:
  - '[console] tell $player Executed CustomRequire!'
  Require: '!player.hasPermission("CustomRequire")'
  RequireMessage: '&4You don^t have permission to do it'
```

---
### Execution order
---

The execution order of items inside the **ICommnad**.
1. `JAVA` onExecutePre()
2. `YAML` Require
3. `YAML` Cooldown 
4. `YAML` Consume
5. `YAML` Chance
6. `YAML` Cancelled
7. `JAVA` onExecutePost()
8. `YAML` Commands
9. `YAML` ExecuteCode

---
### Events
---

**PLAYER**
- PLAYER_JOIN
- PLAYER_QUIT
- PLAYER_RESPAWN
- PLAYER_DEATH
- PLAYER_EXP_CHANGE
- PLAYER_LEVEL_CHANGE
- PLAYER_TELEPORT
- PLAYER_SNEAK
- PLAYER_CHAT
- PLAYER_FISH
- PLAYER_SWAPHAND
  
**INTERACT**
- RIGHT_CLICK_ENTITY
- RIGHT_CLICK_BLOCK
- RIGHT_CLICK_AIR (Ignored Cancelled)
- LEFT_CLICK_BLOCK
- LEFT_CLICK_AIR (Ignored Cancelled)
- BLOCK_PLACE
- BLOCK_BREAK
- PHYSICAL
- CONSUME
  
**ATTACK**
- ATTACK
- DAMAGED_ENTITY
- DAMAGED_OTHER
- DAMAGED_BLOCK
