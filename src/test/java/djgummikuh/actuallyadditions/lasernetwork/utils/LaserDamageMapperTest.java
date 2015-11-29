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

import org.junit.Test;

import djgummikuh.actuallyadditions.lasernetwork.utils.LaserDamageMapper.Axis;

public class LaserDamageMapperTest {

    @Test
    public void testGetLowestNumber() throws NoSuchMethodException,
            SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
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

    @Test
    public void testGetInsecMult() throws NoSuchMethodException,
            SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Method m = LaserDamageMapper.class.getDeclaredMethod("getInsecMult",
                Double.TYPE, Integer.TYPE);
        m.setAccessible(true);
        assertEquals("Crossing", 0.08, m.invoke(null, 0.2, 10));
        assertEquals("Parallel", Double.MAX_VALUE, m.invoke(null, 0.2, 0));
    }

    private Axis invokeGetLowestNumber(double x, double y, double z)
            throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method m = LaserDamageMapper.class.getDeclaredMethod("getLowestNumber",
                Double.TYPE, Double.TYPE, Double.TYPE);
        m.setAccessible(true);
        return (Axis) m.invoke(null, x, y, z);
    }

}
