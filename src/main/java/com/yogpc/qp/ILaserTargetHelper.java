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
	
	private static int call_int(Method m,Object o,Object[] a,int d) {
	  Integer r = (Integer) call(m,o,a);
	  return r==null?d:r.intValue();
	}
	private static boolean call_bool(Method m,Object o,Object[] a,boolean d) {
      Boolean r = (Boolean) call(m,o,a);
      return r==null?d:r.booleanValue();
    }

	static int getXCoord(Object o) {
		return call_int(_getXCoord, o, new Object[] {},Integer.MIN_VALUE);
	}

	static int getYCoord(Object o) {
		return call_int(_getYCoord, o, new Object[] {},Integer.MIN_VALUE);
	}

	static int getZCoord(Object o) {
		return call_int(_getZCoord, o, new Object[] {},Integer.MIN_VALUE);
	}

	static boolean requiresLaserEnergy(Object o) {
		return call_bool(_requiresLaserEnergy, o, new Object[] {},false);
	}

	static boolean isInvalidTarget(Object o) {
		return call_bool(_isInvalidTarget, o, new Object[] {},true);
	}

	static void receiveLaserEnergy(Object o, float f) {
		call(_receiveLaserEnergy, o, new Object[] { new Float(f) });
	}

	static boolean isValid(Object o) {
		return !isInvalidTarget(o) && requiresLaserEnergy(o);
	}

	static boolean isInstance(Object o) {
		return cls.isInstance(o);
	}
}
