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

    public static boolean debug = false;

    public static void mapDamageVoxels(ConnectionPair pair) {
        WorldPos start = pair.getStart();
        int coordX = start.getX();
        int coordY = start.getY();
        int coordZ = start.getZ();
        WorldPos end = pair.getEnd();
        print("Going from " + coordX + "," + coordY + "," + coordZ + " to "
                + end.getX() + "," + end.getY() + "," + end.getZ());
        int diffX = end.getX() - coordX;
        int diffY = end.getY() - coordY;
        int diffZ = end.getZ() - coordZ;
        double startX = 0.5;
        double startY = 0.5;
        double startZ = 0.5;
        double distanceSquared = diffX * diffX + diffY * diffY + diffZ * diffZ;
        int counter = 1;
        while (distanceSquared > 0) {
            double oldStartX = startX;
            double oldStartY = startY;
            double oldStartZ = startZ;
            print("Distance left: " + distanceSquared);
            boolean upX = diffX > 0;
            boolean upY = diffY > 0;
            boolean upZ = diffZ > 0;

            double multiX = getInsecMult(startX, diffX);
            double multiY = getInsecMult(startY, diffY);
            double multiZ = getInsecMult(startZ, diffZ);

            Axis whereToGo = getLowestNumber(multiX, multiY, multiZ);
            print("Going to " + whereToGo);
            switch (whereToGo) {
            case X:
                coordX += upX ? 1 : -1;
                startX = 0;
                startY = (startY + (diffY * multiX)) % 1.0;
                startZ = (startZ + (diffZ * multiX)) % 1.0;
                break;
            case Y:
                coordY += upY ? 1 : -1;
                startX = (startX + (diffX * multiY)) % 1.0;
                startY = 0;
                startZ = (startZ + (diffZ * multiY)) % 1.0;
                break;
            case Z:
                coordZ += upZ ? 1 : -1;
                startX = (startX + (diffX * multiZ)) % 1.0;
                startY = (startY + (diffY * multiZ)) % 1.0;
                startZ = 0;
                break;
            case XY:
                coordX += upX ? 1 : -1;
                coordY += upY ? 1 : -1;
                startX = 0;
                startY = 0;
                startZ = (startZ + (diffZ * multiX)) % 1.0;
                break;
            case XZ:
                coordX += upX ? 1 : -1;
                coordZ += upZ ? 1 : -1;
                startX = 0;
                startY = (startY + (diffY * multiX)) % 1.0;
                startZ = 0;
                break;
            case YZ:
                coordY += upY ? 1 : -1;
                coordZ += upZ ? 1 : -1;
                startX = (startX + (diffX * multiY)) % 1.0;
                startY = 0;
                startZ = 0;
                break;
            case XYZ:
                coordX += upX ? 1 : -1;
                coordY += upY ? 1 : -1;
                coordZ += upZ ? 1 : -1;
                startX = 0;
                startY = 0;
                startZ = 0;
                break;
            }

            handleBlock(coordX, coordY, coordZ, oldStartX, oldStartY,
                    oldStartZ, startX, startY, startZ);

            print("Step " + counter + ": I'm now at " + coordX + "," + coordY
                    + "," + coordZ);
            multiX = getInsecMult(startX, diffX);
            multiY = getInsecMult(startY, diffY);
            multiZ = getInsecMult(startZ, diffZ);
            // We can use squared distance here. It's MUCH cheaper and for our
            // check
            // > 1.0 it doesn't make a difference (since 1*1 = 1)
            distanceSquared = (end.getX() - coordX) * (end.getX() - coordX)
                    + (end.getY() - coordY) * (end.getY() - coordY)
                    + (end.getZ() - coordZ) * (end.getZ() - coordZ);
            counter++;
        }

    }

    /**
     * This method takes the block coordinates and the inner-block vector
     * endpoints and writes them to the map so we can query for it.
     * 
     * @param locX
     *            the x coordinate of this block
     * @param locY
     *            the y coordinate of this block
     * @param locZ
     *            the z coordinate of this block
     * @param fromX
     *            the x coordinate of the inner vector start poimt
     * @param fromY
     *            the y coordinate of the inner vector start poimt
     * @param fromZ
     *            the y coordinate of the inner vector start poimt
     * @param toX
     *            the x coordinate of the inner vector end poimt
     * @param toY
     *            the y coordinate of the inner vector end poimt
     * @param toZ
     *            the z coordinate of the inner vector end poimt
     */
    private static void handleBlock(int locX, int locY, int locZ, double fromX,
            double fromY, double fromZ, double toX, double toY, double toZ) {
        if (toX == 0)
            toX++;
        if (toY == 0)
            toY++;
        if (toZ == 0)
            toZ++;
        print("In-Block coordinates are now " + locX + "," + locY + "," + locZ);
        print("Handling in-Block vector from " + fromX + "," + fromY + ","
                + fromZ + " to " + toX + "," + toY + "," + toZ);
    }

    /**
     * Enumeration denoting the 3 axises and all combinations of those.
     * 
     * @author johannes
     *
     */
    public static enum Axis {
        X, Y, Z, XY, XZ, YZ, XYZ;

        @Override
        public String toString() {
            return this.name();
        };
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
        // If relStart equals 0.0, we are starting on a wall and need to reach
        // the OTHER wall.
        // This means that we need to treat it as if it were 0.0
        if (relStart == 1.0) {
            relStart = 0.0;
        }
        // if diff equals 0, the following caluclation would return NaN so
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

    /**
     * Prints out log messages. Can be supressed by not setting debug to TRUE.
     * 
     * @param message
     *            the message to log
     */
    private static void print(String message) {
        if (debug) {
            System.out.println(message);
        }
    }
}
