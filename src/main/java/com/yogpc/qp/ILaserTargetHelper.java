package com.yogpc.qp;

import java.lang.reflect.Method;

import com.yogpc.mc_lib.ReflectionHelper;

public class ILaserTargetHelper {
  private static final Class<?> cls = ReflectionHelper.getClass(
      "buildcraft.api.power.ILaserTarget", "buildcraft.silicon.ILaserTarget");
  private static final Method _getXCoord = ReflectionHelper.getMethod(cls,
      new String[] {"getXCoord"}, new Class<?>[] {});
  private static final Method _getYCoord = ReflectionHelper.getMethod(cls,
      new String[] {"getYCoord"}, new Class<?>[] {});
  private static final Method _getZCoord = ReflectionHelper.getMethod(cls,
      new String[] {"getZCoord"}, new Class<?>[] {});
  private static final Method _isInvalidTarget = ReflectionHelper.getMethod(cls, new String[] {
      "isInvalidTarget", "isInvalid"}, new Class<?>[] {});
  private static final Method _receiveLaserEnergy = ReflectionHelper.getMethod(cls,
      new String[] {"receiveLaserEnergy"}, new Class<?>[] {double.class},
      new Class<?>[] {float.class});
  private static final Method _requiresLaserEnergy = ReflectionHelper.getMethod(cls, new String[] {
      "requiresLaserEnergy", "hasCurrentWork"}, new Class<?>[] {});

  private static int call_int(final Method m, final Object o, final Object[] a, final int d) {
    final Integer r = (Integer) ReflectionHelper.invoke(m, o, a);
    return r == null ? d : r.intValue();
  }

  private static boolean call_bool(final Method m, final Object o, final Object[] a, final boolean d) {
    final Boolean r = (Boolean) ReflectionHelper.invoke(m, o, a);
    return r == null ? d : r.booleanValue();
  }

  static int getXCoord(final Object o) {
    return call_int(_getXCoord, o, new Object[] {}, Integer.MIN_VALUE);
  }

  static int getYCoord(final Object o) {
    return call_int(_getYCoord, o, new Object[] {}, Integer.MIN_VALUE);
  }

  static int getZCoord(final Object o) {
    return call_int(_getZCoord, o, new Object[] {}, Integer.MIN_VALUE);
  }

  static boolean requiresLaserEnergy(final Object o) {
    return call_bool(_requiresLaserEnergy, o, new Object[] {}, false);
  }

  static boolean isInvalidTarget(final Object o) {
    return call_bool(_isInvalidTarget, o, new Object[] {}, true);
  }

  static void receiveLaserEnergy(final Object o, final float f) {
    ReflectionHelper.invoke(_receiveLaserEnergy, o, new Object[] {new Float(f)});
  }

  static boolean isValid(final Object o) {
    return !isInvalidTarget(o) && requiresLaserEnergy(o);
  }

  static boolean isInstance(final Object o) {
    return cls.isInstance(o);
  }
}
