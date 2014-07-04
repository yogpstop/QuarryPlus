package com.yogpc.qp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
			_receiveLaserEnergy = cls.getMethod("receiveLaserEnergy", new Class<?>[] { float.class });
			_isInvalidTarget = cls.getMethod("isInvalidTarget", new Class<?>[] {});
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		try {
			_requiresLaserEnergy = cls.getMethod("requiresLaserEnergy", new Class<?>[] {});
		} catch (NoSuchMethodException e1) {
			try {
				_requiresLaserEnergy = cls.getMethod("hasCurrentWork", new Class<?>[] {});
			} catch (NoSuchMethodException e2) {
				e1.printStackTrace();
				e2.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
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
