package com.yogpc.qp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.yogpc.mc_lib.ReflectionHelper;

public class ILaserTargetHelper {
	private static final Class<?> cls = ReflectionHelper.getClass("buildcraft.api.power.ILaserTarget", "buildcraft.silicon.ILaserTarget");
	private static final Method _getXCoord = ReflectionHelper.getMethod(cls, new String[] { "getXCoord" }, new Class<?>[] {});
	private static final Method _getYCoord = ReflectionHelper.getMethod(cls, new String[] { "getYCoord" }, new Class<?>[] {});
	private static final Method _getZCoord = ReflectionHelper.getMethod(cls, new String[] { "getZCoord" }, new Class<?>[] {});
	private static final Method _requiresLaserEnergy = ReflectionHelper.getMethod(cls, new String[] { "isInvalidTarget", "isInvalid" }, new Class<?>[] {});
	private static final Method _receiveLaserEnergy = ReflectionHelper.getMethod(cls, new String[] { "receiveLaserEnergy" }, new Class<?>[] { double.class },
			new Class<?>[] { float.class });
	private static final Method _isInvalidTarget = ReflectionHelper.getMethod(cls, new String[] { "requiresLaserEnergy", "hasCurrentWork" }, new Class<?>[] {});

	private static Object call(Method m, Object o, Object[] a) {
		try {
			return m.invoke(o, a);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	static int getXCoord(Object o) {
		return (Integer) call(_getXCoord, o, new Object[] {});
	}

	static int getYCoord(Object o) {
		return (Integer) call(_getYCoord, o, new Object[] {});
	}

	static int getZCoord(Object o) {
		return (Integer) call(_getZCoord, o, new Object[] {});
	}

	static boolean requiresLaserEnergy(Object o) {
		return (Boolean) call(_requiresLaserEnergy, o, new Object[] {});
	}

	static boolean isInvalidTarget(Object o) {
		return (Boolean) call(_isInvalidTarget, o, new Object[] {});
	}

	static void receiveLaserEnergy(Object o, float f) {
		call(_receiveLaserEnergy, o, new Object[] { f });
	}

	static boolean isValid(Object o) {
		return !isInvalidTarget(o) && requiresLaserEnergy(o);
	}

	static boolean isInstance(Object o) {
		return cls.isInstance(o);
	}
}
