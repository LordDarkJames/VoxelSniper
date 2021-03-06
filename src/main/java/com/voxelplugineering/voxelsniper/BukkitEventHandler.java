/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 The Voxel Plugineering Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.voxelplugineering.voxelsniper;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import com.google.common.base.Optional;
import com.voxelplugineering.voxelsniper.event.SnipeEvent;
import com.voxelplugineering.voxelsniper.event.SniperEvent;
import com.voxelplugineering.voxelsniper.event.SniperEvent.SniperDestroyEvent;

/**
 * An event handler for bukkit's events to post the events to Gunsmith from.
 */
public class BukkitEventHandler implements Listener
{

    /**
     * Creates a new {@link BukkitEventHandler}.
     */
    public BukkitEventHandler()
    {

    }

    /**
     * An event handler for player join events.
     * 
     * @param event the {@link org.bukkit.event.player.PlayerJoinEvent}
     */
    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event)
    {
        Optional<com.voxelplugineering.voxelsniper.api.entity.living.Player> s = Gunsmith.getPlayerRegistry().getPlayer(event.getPlayer().getName());
        if (s.isPresent())
        {
            SniperEvent.SniperCreateEvent sce = new SniperEvent.SniperCreateEvent(s.get());
            Gunsmith.getEventBus().post(sce);
        }
    }

    /**
     * An event handler for quit events, proxies to Gunsmith's {@link SniperDestroyEvent}.
     * 
     * @param event the event
     */
    @EventHandler
    public void onPlayerLeave(org.bukkit.event.player.PlayerQuitEvent event)
    {
        Optional<com.voxelplugineering.voxelsniper.api.entity.living.Player> s = Gunsmith.getPlayerRegistry().getPlayer(event.getPlayer());
        if (s.isPresent())
        {
            SniperEvent.SniperDestroyEvent sde = new SniperEvent.SniperDestroyEvent(s.get());
            Gunsmith.getEventBus().post(sde);
        }
    }

    /**
     * An event handler for player interact events.
     * 
     * @param event the {@link org.bukkit.event.player.PlayerInteractEvent}
     */
    @EventHandler
    public void onPlayerInteractEvent(org.bukkit.event.player.PlayerInteractEvent event)
    {
        Player p = event.getPlayer();
        Gunsmith.getLogger().debug("PlayerInteractEvent for " + p.getName());
        if (p.getItemInHand().getType() == (Material) Gunsmith.getConfiguration().get("arrowMaterial").get()
                && (event.getAction() == Action.RIGHT_CLICK_AIR))
        {
            Optional<com.voxelplugineering.voxelsniper.api.entity.living.Player> s = Gunsmith.getPlayerRegistry().getPlayer(event.getPlayer());
            if (s.isPresent())
            {
                SnipeEvent se = new SnipeEvent(s.get(), p.getLocation().getYaw(), p.getLocation().getPitch());
                Gunsmith.getEventBus().post(se);
            }
        }
    }
}
