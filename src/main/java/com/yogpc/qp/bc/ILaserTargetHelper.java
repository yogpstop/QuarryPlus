package com.yogpc.qp.bc;

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
      new Class<?>[] {float.class}, new Class<?>[] {int.class});
  private static final Method _requiresLaserEnergy = ReflectionHelper.getMethod(cls, new String[] {
      "requiresLaserEnergy", "hasCurrentWork"}, new Class<?>[] {});

  private static int call_num(final Method m, final Object o, final Object[] a, final int d) {
    final Number r = (Number) ReflectionHelper.invoke(m, o, a);
    return r == null ? d : r.intValue();
  }

  private static boolean call_bool(final Method m, final Object o, final Object[] a, final boolean d) {
    final Boolean r = (Boolean) ReflectionHelper.invoke(m, o, a);
    return r == null ? d : r.booleanValue();
  }

  public static int getXCoord(final Object o) {
    return call_num(_getXCoord, o, new Object[] {}, Integer.MIN_VALUE);
  }

  public static int getYCoord(final Object o) {
    return call_num(_getYCoord, o, new Object[] {}, Integer.MIN_VALUE);
  }

  public static int getZCoord(final Object o) {
    return call_num(_getZCoord, o, new Object[] {}, Integer.MIN_VALUE);
  }

  public static boolean requiresLaserEnergy(final Object o) {
    return call_bool(_requiresLaserEnergy, o, new Object[] {}, false);
  }

  public static boolean isInvalidTarget(final Object o) {
    return call_bool(_isInvalidTarget, o, new Object[] {}, true);
  }

  public static void receiveLaserEnergy(final Object o, final double v) {
    final Class<?> t = _receiveLaserEnergy.getParameterTypes()[0];
    Object p = null;
    if (double.class.equals(t))
      p = new Double(v);
    if (float.class.equals(t))
      p = new Float((float) v);
    if (int.class.equals(t))
      p = new Integer((int) v);
    ReflectionHelper.invoke(_receiveLaserEnergy, o, new Object[] {p});
  }

  public static boolean isValid(final Object o) {
    return !isInvalidTarget(o) && requiresLaserEnergy(o);
  }

  public static boolean isInstance(final Object o) {
    return cls != null && cls.isInstance(o);
  }
}
