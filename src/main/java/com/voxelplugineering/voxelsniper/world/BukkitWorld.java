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
package com.voxelplugineering.voxelsniper.world;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;

import com.google.common.base.Optional;
import com.google.common.collect.MapMaker;
import com.voxelplugineering.voxelsniper.Gunsmith;
import com.voxelplugineering.voxelsniper.api.entity.Entity;
import com.voxelplugineering.voxelsniper.api.registry.MaterialRegistry;
import com.voxelplugineering.voxelsniper.api.shape.MaterialShape;
import com.voxelplugineering.voxelsniper.api.shape.Shape;
import com.voxelplugineering.voxelsniper.api.world.Block;
import com.voxelplugineering.voxelsniper.api.world.Chunk;
import com.voxelplugineering.voxelsniper.api.world.Location;
import com.voxelplugineering.voxelsniper.api.world.biome.Biome;
import com.voxelplugineering.voxelsniper.registry.WeakWrapper;
import com.voxelplugineering.voxelsniper.shape.ComplexMaterialShape;
import com.voxelplugineering.voxelsniper.util.math.Vector3i;
import com.voxelplugineering.voxelsniper.world.material.BukkitMaterial;

/**
 * A wrapper for bukkit's {@link World}s.
 */
public class BukkitWorld extends WeakWrapper<World> implements com.voxelplugineering.voxelsniper.api.world.World
{

    private final MaterialRegistry<Material> materials;
    private final Map<org.bukkit.Chunk, Chunk> chunks;
    private final Thread worldThread;

    /**
     * Creates a new {@link BukkitWorld}.
     * 
     * @param world the world
     * @param materialRegistry the registry
     * @param thread The world Thread
     */
    public BukkitWorld(World world, MaterialRegistry<Material> materialRegistry, Thread thread)
    {
        super(world);
        this.materials = materialRegistry;
        this.chunks = new MapMaker().weakKeys().weakValues().makeMap();
        this.worldThread = thread;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return this.getThis().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chunk> getChunk(int x, int y, int z)
    {
        if (!checkAsyncChunkAccess(x, y, z))
        {
            return Optional.absent();
        }
        org.bukkit.Chunk chunk = getThis().getChunkAt(x, z);
        if (this.chunks.containsKey(chunk))
        {
            return Optional.of(this.chunks.get(chunk));
        }
        BukkitChunk newChunk = new BukkitChunk(chunk, this);
        this.chunks.put(chunk, newChunk);
        return Optional.<Chunk>of(newChunk);
    }

    private boolean checkAsyncChunkAccess(int x, int y, int z)
    {
        if (Thread.currentThread() != this.worldThread)
        {
            if (!getThis().isChunkLoaded(x, z))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chunk> getChunk(Vector3i vector)
    {
        return getChunk(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<com.voxelplugineering.voxelsniper.api.world.Block> getBlock(int x, int y, int z)
    {
        if (!checkAsyncBlockAccess(x, y, z))
        {
            return Optional.absent();
        }
        org.bukkit.block.Block b = getThis().getBlockAt(x, y, z);
        CommonLocation l = new CommonLocation(this, b.getX(), b.getY(), b.getZ());
        Optional<com.voxelplugineering.voxelsniper.api.world.material.Material> m = this.materials.getMaterial(b.getType().name());
        if (!m.isPresent())
        {
            return Optional.absent();
        }
        return Optional.<com.voxelplugineering.voxelsniper.api.world.Block>of(new CommonBlock(l, m.get()));
    }

    private boolean checkAsyncBlockAccess(int x, int y, int z)
    {
        if (Thread.currentThread() != this.worldThread)
        {
            int cx = x < 0 ? (x / 16 - 1) : x / 16;
            int cz = z < 0 ? (z / 16 - 1) : z / 16;
            if (!getThis().isChunkLoaded(cx, cz))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<com.voxelplugineering.voxelsniper.api.world.Block> getBlock(Location location)
    {
        if (location.getWorld() != this)
        {
            return Optional.absent();
        }
        return getBlock(location.getFlooredX(), location.getFlooredY(), location.getFlooredZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<com.voxelplugineering.voxelsniper.api.world.Block> getBlock(Vector3i vector)
    {
        return getBlock(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlock(com.voxelplugineering.voxelsniper.api.world.material.Material material, int x, int y, int z)
    {
        //TODO range checks
        if (material instanceof BukkitMaterial)
        {
            BukkitMaterial bukkitMaterial = (BukkitMaterial) material;
            getThis().getBlockAt(x, y, z).setType(bukkitMaterial.getThis());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlock(com.voxelplugineering.voxelsniper.api.world.material.Material material, Location location)
    {
        setBlock(material, location.getFlooredX(), location.getFlooredY(), location.getFlooredZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlock(com.voxelplugineering.voxelsniper.api.world.material.Material material, Vector3i vector)
    {
        setBlock(material, vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Biome> getBiome(int x, int y, int z)
    {
        org.bukkit.block.Biome biome = getThis().getBiome(x, z);
        return Gunsmith.getBiomeRegistry().getBiome(biome.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Biome> getBiome(Location location)
    {
        if (location.getWorld() != this)
        {
            return Optional.absent();
        }
        return getBiome(location.getFlooredX(), location.getFlooredY(), location.getFlooredZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Biome> getBiome(Vector3i vector)
    {
        return getBiome(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBiome(Biome biome, int x, int y, int z)
    {
        if (biome instanceof BukkitBiome)
        {
            BukkitBiome bukkitBiome = (BukkitBiome) biome;
            getThis().setBiome(x, z, bukkitBiome.getThis());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBiome(Biome biome, Location location)
    {
        if (location.getWorld() != this)
        {
            return;
        }
        setBiome(biome, location.getFlooredX(), location.getFlooredY(), location.getFlooredZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBiome(Biome biome, Vector3i vector)
    {
        setBiome(biome, vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MaterialRegistry<?> getMaterialRegistry()
    {
        return this.materials;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Entity> getLoadedEntities()
    {
        return null;//TODO
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MaterialShape getShapeFromWorld(Location origin, Shape shape)
    {
        MaterialShape mat = new ComplexMaterialShape(shape, Gunsmith.getDefaultMaterialRegistry().getAirMaterial());
        for (int x = 0; x < shape.getWidth(); x++)
        {
            int ox = x + origin.getFlooredX() - shape.getOrigin().getX();
            for (int y = 0; y < shape.getHeight(); y++)
            {
                int oy = y + origin.getFlooredY() - shape.getOrigin().getY();
                for (int z = 0; z < shape.getLength(); z++)
                {
                    int oz = z + origin.getFlooredZ() - shape.getOrigin().getZ();
                    if (shape.get(x, y, z, false))
                    {
                        Optional<Block> block = getBlock(ox, oy, oz);
                        if (!block.isPresent())
                        {
                            shape.unset(x, y, z, false);
                        } else
                        {
                            mat.setMaterial(x, y, z, false, block.get().getMaterial());
                        }
                    }
                }
            }
        }
        return mat;
    }

}
