package com.thevoxelbox.voxelsniper.brush;

import com.google.common.base.Objects;
import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.Undo;
import com.thevoxelbox.voxelsniper.util.UndoDelegate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;


/**
 * http://www.voxelwiki.com/minecraft/Voxelsniper#The_Tree_Brush
 *
 * @author Mick
 */
public class TreeSnipeBrush extends Brush
{
    private static int timesUsed = 0;
    private TreeType treeType = TreeType.TREE;

    /**
     *
     */
    public TreeSnipeBrush()
    {
        this.setName("Tree Snipe");
    }

    private void single(final SnipeData v, Block targetBlock)
    {
        UndoDelegate undoDelegate = new UndoDelegate(targetBlock.getWorld());
        Block blockBelow = targetBlock.getRelative(BlockFace.DOWN);
        BlockState currentState = blockBelow.getState();
        undoDelegate.setBlock(blockBelow);
        blockBelow.setType(Material.GRASS);
        this.getWorld().generateTree(targetBlock.getLocation(), this.treeType, undoDelegate);
        Undo undo = undoDelegate.getUndo();
        blockBelow.setTypeIdAndData(currentState.getTypeId(), currentState.getRawData(), true);
        undo.put(blockBelow);
        v.storeUndo(undo);
    }

    private int getYOffset()
    {
        for (int _i = 1; _i < (getTargetBlock().getWorld().getMaxHeight() - 1 - getTargetBlock().getY()); _i++)
        {
            if (Objects.equal(getTargetBlock().getRelative(0, _i + 1, 0).getType(), Material.AIR))
            {
                return _i;
            }
        }
        return 0;
    }

    private void printTreeType(final Message vm)
    {
        String _printout = "";

        boolean _delimiterHelper = true;
        for (final TreeType _treeType : TreeType.values())
        {
            if (_delimiterHelper)
            {
                _delimiterHelper = false;
            }
            else
            {
                _printout += ", ";
            }
            _printout += ((_treeType.equals(this.treeType)) ? ChatColor.GRAY + _treeType.name().toLowerCase() : ChatColor.DARK_GRAY + _treeType.name().toLowerCase()) + ChatColor.WHITE;
        }

        vm.custom(_printout);
    }

    @Override
    protected final void arrow(final SnipeData v)
    {
        Block targetBlock = getTargetBlock().getRelative(0, getYOffset(), 0);
        this.single(v, targetBlock);
    }

    @Override
    protected final void powder(final SnipeData v)
    {
        this.single(v, getTargetBlock());
    }

    @Override
    public final void info(final Message vm)
    {
        vm.brushName(this.getName());
        this.printTreeType(vm);
    }

    @Override
    public final void parameters(final String[] par, final SnipeData v)
    {
        for (int _i = 1; _i < par.length; _i++)
        {
            if (par[_i].equalsIgnoreCase("info"))
            {
                v.sendMessage(ChatColor.GOLD + "Tree snipe brush:");
                v.sendMessage(ChatColor.AQUA + "/b t treetype");
                this.printTreeType(v.getVoxelMessage());
                return;
            }
            try
            {
                this.treeType = TreeType.valueOf(par[_i].toUpperCase());
                this.printTreeType(v.getVoxelMessage());
            }
            catch (final IllegalArgumentException _ex)
            {
                v.getVoxelMessage().brushMessage("No such tree type.");
            }
        }
    }

    @Override
    public final int getTimesUsed()
    {
        return TreeSnipeBrush.timesUsed;
    }

    @Override
    public final void setTimesUsed(final int tUsed)
    {
        TreeSnipeBrush.timesUsed = tUsed;
    }
}
