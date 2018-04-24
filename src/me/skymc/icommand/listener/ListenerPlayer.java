package me.skymc.icommand.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.skymc.icommand.ICommandExecuter;
import me.skymc.icommand.ICommandPlugin;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.damage.GetDamager;
import me.skymc.taboolib.entity.EntityTag;

/**
 * @author Bkm016
 * @since 2018-03-21
 */
public class ListenerPlayer implements Listener {
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void PLAYER(PlayerJoinEvent e) {
		ICommandExecuter.execute(e.getPlayer(), "PLAYER_JOIN", e);
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void PLAYER(PlayerQuitEvent e) {
		ICommandExecuter.execute(e.getPlayer(), "PLAYER_QUIT", e);
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void PLAYER(PlayerRespawnEvent e) {
		ICommandExecuter.execute(e.getPlayer(), "PLAYER_RESPAWN", e);
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void PLAYER(PlayerDeathEvent e) {
		ICommandExecuter.execute(e.getEntity(), "PLAYER_DEATH", e);
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void PLAYER(PlayerLevelChangeEvent e) {
		ICommandExecuter.execute(e.getPlayer(), "PLAYER_LEVEL_CHANGE", e);
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void PLAYER(PlayerExpChangeEvent e) {
		if (e.getAmount() > 0) {
			ICommandExecuter.execute(e.getPlayer(), "PLAYER_EXP_CHANGE", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void PLAYER(PlayerTeleportEvent e) {
		if (!e.isCancelled()) {
			ICommandExecuter.execute(e.getPlayer(), "PLAYER_TELEPORT", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void PLAYER(PlayerToggleSneakEvent e) {
		if (!e.isCancelled()) {
			ICommandExecuter.execute(e.getPlayer(), "PLAYER_SNEAK", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void PLAYER(AsyncPlayerChatEvent e) {
		if (!e.isCancelled()) {
			ICommandExecuter.execute(e.getPlayer(), "PLAYER_CHAT", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void FISH(PlayerFishEvent e) {
		if (!e.isCancelled()) {
			ICommandExecuter.execute(e.getPlayer(), "PLAYER_FISH", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void INTERACT_ENTITY(PlayerInteractAtEntityEvent e) {
		if ((TabooLib.getVerint() > 10800 && e.getHand() == EquipmentSlot.HAND) && !e.isCancelled()) {
			ICommandExecuter.execute(e.getPlayer(), "RIGHT_CLICK_ENTITY", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void CONSUME(PlayerItemConsumeEvent e) {
		if (!e.isCancelled()) {
			ICommandExecuter.execute(e.getPlayer(), "CONSUME", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void BLOCK(BlockBreakEvent e) {
		if (!e.isCancelled()) {
			ICommandExecuter.execute(e.getPlayer(), "BLOCK_BREAK", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void BLOCK(BlockPlaceEvent e) {
		if (!e.isCancelled() && (TabooLib.getVerint() > 10800 && e.getHand() == EquipmentSlot.HAND)) {
			ICommandExecuter.execute(e.getPlayer(), "BLOCK_PLACE", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void DAMAGE(EntityDamageEvent e) {
		if (!e.isCancelled() && e.getEntity() instanceof Player && !EntityTag.getInst().hasKey("icommand|damaged", e.getEntity())) {
			ICommandExecuter.execute((Player) e.getEntity(), "DAMAGED_OTHER", e);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void DAMAGE(EntityDamageByBlockEvent e) {
		if (!e.isCancelled() && e.getEntity() instanceof Player) {
			ICommandExecuter.execute((Player) e.getEntity(), "DAMAGED_BLOCK", e);
			bypassEntityDamageEvent(e.getEntity());
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void DAMAGE(EntityDamageByEntityEvent e) {
		if (!e.isCancelled()) {
			Player player = GetDamager.get(e);
			if (player != null) {
				ICommandExecuter.execute(player, "ATTACK", e);
			}
			if (e.getEntity() instanceof Player) {
				ICommandExecuter.execute((Player) e.getEntity(), "DAMAGED_ENTITY", e);
				bypassEntityDamageEvent(e.getEntity());
			}
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void INTERACT_BLOCK(PlayerInteractEvent e) {
		if ((TabooLib.getVerint() > 10800 && e.getHand() == EquipmentSlot.OFF_HAND)) {
			return;
		}
		if (!e.isCancelled() && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ICommandExecuter.execute(e.getPlayer(), "RIGHT_CLICK_BLOCK", e);
		}
		else if (!e.isCancelled() && e.getAction() == Action.LEFT_CLICK_BLOCK) {
			ICommandExecuter.execute(e.getPlayer(), "LEFT_CLICK_BLOCK", e);
		} 
		else if (e.getAction() == Action.RIGHT_CLICK_AIR) {
			ICommandExecuter.execute(e.getPlayer(), "RIGHT_CLICK_AIR", e);
		}
		else if (e.getAction() == Action.LEFT_CLICK_AIR) {
			ICommandExecuter.execute(e.getPlayer(), "LEFT_CLICK_AIR", e);
		} 
		else {
			ICommandExecuter.execute(e.getPlayer(), "PHYSICAL", e);
		}
	}
	
	private void bypassEntityDamageEvent(Entity entity) {
		EntityTag.getInst().set("icommand|damaged", true, entity);
		Bukkit.getScheduler().runTask(ICommandPlugin.getInst(), () -> EntityTag.getInst().remove("icommand|damaged", entity));	
	}
}
