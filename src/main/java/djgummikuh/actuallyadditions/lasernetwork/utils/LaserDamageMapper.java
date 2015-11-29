/*
 * This file ("LaserDamageMapper.java") is part of the Actually Additions Mod for Minecraft.
 * It is created and owned by DJGummikuh and distributed
 * under the Actually Additions License to be found at
 * http://github.com/Ellpeck/ActuallyAdditions/blob/master/README.md
 * View the source code at https://github.com/DJGummikuh/ActuallyAdditions
 *
 * © 2015 DJGummikuh
 */
package djgummikuh.actuallyadditions.lasernetwork.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ellpeck.actuallyadditions.misc.LaserRelayConnectionHandler.ConnectionPair;
import ellpeck.actuallyadditions.util.WorldPos;

/**
 * This class maps a connection pair to find out the blocks the laser passes and
 * to rig those with information so that when a player requests a coordinate, we
 * can tell if it is crossing one of the lines and cause damage.
 * 
 * @author johannes
 *
 */
public class LaserDamageMapper {

    public static void mapDamageVoxels(ConnectionPair pair) {
        WorldPos start = pair.getStart();
        WorldPos end = pair.getEnd();
        int diffX = end.getX() - start.getX();
        int diffY = end.getY() - start.getY();
        int diffZ = end.getZ() - start.getZ();
        double startX = 0.5;
        double startY = 0.5;
        double startZ = 0.5;
        double multiX = getInsecMult(startX, diffX);
        double multiY = getInsecMult(startY, diffY);
        double multiZ = getInsecMult(startZ, diffZ);
        Axis whereToGo = getLowestNumber(multiX, multiY, multiZ);
        switch (whereToGo) {
        case X:
        case XY:
        case XZ:
        case XYZ:
            break;
        case YZ:
        case Y:
            break;
        case Z:
            break;
        }

    }

    /**
     * Enumeration denoting the 3 axises and all combinations of those.
     * 
     * @author johannes
     *
     */
    public static enum Axis {
        X, Y, Z, XY, XZ, YZ, XYZ;
    }

    /**
     * This method returns the axis in which walking the diff vector will cause
     * a boundary crossing first.
     * 
     * @param multX
     *            the multiplier for the vector to run in X.
     * @param multY
     *            the multiplier for the vector to run in Y.
     * @param multZ
     *            the multiplier for the vector to run in Z.
     * @return the axis in which we will hit the next wall the quickest.
     *         combined Axis mean we're hitting an edge or even a corner.
     */
    private static Axis getLowestNumber(double multX, double multY, double multZ) {
        double absX = Math.abs(multX);
        double absY = Math.abs(multY);
        double absZ = Math.abs(multZ);
        if (absX < absY) {
            if (absX < absZ) {
                return Axis.X;
            } else if (absX == absZ) {
                return Axis.XZ;
            } else {
                return Axis.Z;
            }
        } else if (absX < absZ) {
            if (absX == absY) {
                return Axis.XY;
            } else {
                return Axis.Y;
            }
        } else if (absY < absZ) {
            return Axis.Y;
        } else if (absY == absZ) {
            if (absX == absY) {
                return Axis.XYZ;
            } else {
                return Axis.YZ;
            }
        } else {
            return Axis.Z;
        }
    }

    /**
     * Returns the intersection point multiplier for the given values. The lower
     * the multiplier, the nearer the point is towards a border, regarding to
     * the translational vector to apply.
     * 
     * @param relStart
     *            the relative coordinate of the point. Must be between 0 and 1
     *            inclusive.
     * @param diff
     *            the diff of the vector to apply in the end. If this value is
     *            0, the method returns Double.MAX_VALUE.
     * @return the multiplier needet to translate this coordinate to the next
     *         border or Double.MAX_VALUE if vector component is null (vector is
     *         parallel to this axis).
     */
    private static double getInsecMult(double relStart, int diff) {
        // if relStart equals 1, the following caluclation would return NaN so
        // we catch it beforehand.
        if (diff == 0) {
            return Double.MAX_VALUE;
        }
        return (1 - relStart) / (double) diff;
    }

    /**
     * Class that is used as Metadata for the blocks. When a player enters a
     * block with this metadata in it, it will check if the Player is actually
     * touching one of the present voxels.
     * 
     * @author johannes
     *
     */
    private static class BlockDamageMap {
        /** The lixt of voxels that are considered harmful */
        private final List<DamageVoxel> damageVoxels = new LinkedList<DamageVoxel>();

        public BlockDamageMap() {

        }

        public BlockDamageMap(Collection<DamageVoxel> newList) {
            damageVoxels.addAll(newList);
        }
    }

    /**
     * Class containing a single Damage Voxel for a block.
     * 
     * @author johannes
     *
     */
    private static class DamageVoxel {
        private final int x, y, z;

        public DamageVoxel(int theX, int theY, int theZ) {
            this.x = theX;
            this.y = theY;
            this.z = theZ;
        }
    }
}
