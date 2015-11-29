/*
 * This file ("LaserDamageMapperTest.java") is part of the Actually Additions Mod for Minecraft.
 * It is created and owned by DJGummikuh and distributed
 * under the Actually Additions License to be found at
 * http://github.com/Ellpeck/ActuallyAdditions/blob/master/README.md
 * View the source code at https://github.com/DJGummikuh/ActuallyAdditions
 *
 * © 2015 DJGummikuh
 */
package djgummikuh.actuallyadditions.lasernetwork.utils;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.Test;

import djgummikuh.actuallyadditions.lasernetwork.utils.LaserDamageMapper.Axis;
import ellpeck.actuallyadditions.misc.LaserRelayConnectionHandler.ConnectionPair;
import ellpeck.actuallyadditions.util.WorldPos;

public class LaserDamageMapperTest {

    /**
     * Prepare the class for unit testing (turn on debug logging)
     */
    @BeforeClass
    public static void initializeLogging() {
        LaserDamageMapper.debug = true;
    }

    /**
     * Integration-tests the actual mapping algorithm.
     */
    @Test
    public void testMapDamageVoxels() {
        WorldPos start = new WorldPos(0, 0, 0, 0);
        WorldPos end = new WorldPos(0, 10, 1, 0);
        ConnectionPair cp = new ConnectionPair(start, end);
        LaserDamageMapper.mapDamageVoxels(cp);
    }

    /**
     * Test the utility method to find out which coordinate is "nearest" to a
     * block boundary.
     * 
     * @throws Exception
     *             when BOOM.
     */
    @Test
    public void testGetLowestNumber() throws Exception {
        assertEquals("X=Y=Z", Axis.XYZ, invokeGetLowestNumber(1, 1, 1));
        assertEquals("X=Y<Z", Axis.XY, invokeGetLowestNumber(1, 1, 2));
        assertEquals("X=Z<Y", Axis.XZ, invokeGetLowestNumber(1, 2, 1));
        assertEquals("Y=Z<X", Axis.YZ, invokeGetLowestNumber(2, 1, 1));
        assertEquals("X=Y>Z", Axis.Z, invokeGetLowestNumber(2, 2, 1));
        assertEquals("X=Z>Y", Axis.Y, invokeGetLowestNumber(2, 1, 2));
        assertEquals("Y=Z>X", Axis.X, invokeGetLowestNumber(1, 2, 2));
        assertEquals("X<Y<Z", Axis.X, invokeGetLowestNumber(1, 2, 3));
        assertEquals("X<Z<Y", Axis.X, invokeGetLowestNumber(1, 3, 2));
        assertEquals("Y<X<Z", Axis.Y, invokeGetLowestNumber(2, 1, 3));
        assertEquals("Y<Z<X", Axis.Y, invokeGetLowestNumber(3, 1, 2));
        assertEquals("Z<X<Y", Axis.Z, invokeGetLowestNumber(2, 3, 1));
        assertEquals("Z<Y<X", Axis.Z, invokeGetLowestNumber(3, 2, 1));
    }

    /**
     * Tests the utility method that calculates the multiplier required to reach
     * a boundary.
     * 
     * @throws Exception
     *             when BOOM.
     */
    @Test
    public void testGetInsecMult() throws Exception {
        Method m = LaserDamageMapper.class.getDeclaredMethod("getInsecMult",
                Double.TYPE, Integer.TYPE);
        m.setAccessible(true);
        assertEquals("Crossing", 0.08, m.invoke(null, 0.2, 10));
        assertEquals("Parallel", Double.MAX_VALUE, m.invoke(null, 0.2, 0));
        assertEquals("Already on Wall", 0.1, m.invoke(null, 0.0, 10));
        assertEquals("Already on Wall", 0.1, m.invoke(null, 1.0, 10));
    }

    /**
     * Helper method to reflectively invoke a private method.
     * 
     * @param x
     *            X
     * @param y
     *            Y
     * @param z
     *            Z
     * @return the Axis
     * @throws Exception
     *             when BOOM.
     */
    private Axis invokeGetLowestNumber(double x, double y, double z)
            throws Exception {
        Method m = LaserDamageMapper.class.getDeclaredMethod("getLowestNumber",
                Double.TYPE, Double.TYPE, Double.TYPE);
        m.setAccessible(true);
        return (Axis) m.invoke(null, x, y, z);
    }

}
