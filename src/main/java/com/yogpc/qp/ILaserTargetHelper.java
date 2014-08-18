package com.yogpc.qp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.yogpc.mc_lib.ReflectionHelper;

public class ILaserTargetHelper {
	private static Class<?> cls = null;
	private static Method _getXCoord = null;
	private static Method _getYCoord = null;
	private static Method _getZCoord = null;
	private static Method _requiresLaserEnergy = null;
	private static Method _receiveLaserEnergy = null;
	private static Method _isInvalidTarget = null;
	static {
		try {
			cls = Class.forName("buildcraft.api.power.ILaserTarget");
		} catch (ClassNotFoundException e1) {
			try {
				cls = Class.forName("buildcraft.silicon.ILaserTarget");
			} catch (ClassNotFoundException e2) {
				e1.printStackTrace();
				e2.printStackTrace();
			}
		}
		try {
			_getXCoord = cls.getMethod("getXCoord", new Class<?>[] {});
			_getYCoord = cls.getMethod("getYCoord", new Class<?>[] {});
			_getZCoord = cls.getMethod("getZCoord", new Class<?>[] {});
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		_isInvalidTarget = ReflectionHelper.getDeclaredMethod(cls, new String[] { "isInvalidTarget", "isInvalid" }, new Class<?>[] {});
		_receiveLaserEnergy = ReflectionHelper.getDeclaredMethod(cls, new String[] { "receiveLaserEnergy" }, new Class<?>[] { double.class },
				new Class<?>[] { float.class });
		_requiresLaserEnergy = ReflectionHelper.getDeclaredMethod(cls, new String[] { "requiresLaserEnergy", "hasCurrentWork" }, new Class<?>[] {});
	}

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
