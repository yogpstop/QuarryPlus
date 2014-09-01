package com.yogpc.mc_lib;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReflectionHelper {
	public static final List<Method> getMethods(final Class<?> t, final Class<? extends Annotation> a) {
		final List<Method> ms = new ArrayList<Method>();
		Class<?> c = t;
		while (c != Object.class) {
			for (final Method m : c.getDeclaredMethods())
				if (m.isAnnotationPresent(a)) {
					m.setAccessible(true);
					ms.add(m);
				}
			c = c.getSuperclass();
		}
		return ms;
	}

	public static final Method getMethod(final Class<?> t, final String[] sv, final Class<?>[]... av) {
		Collection<Exception> ec = new ArrayList<Exception>();
		Class<?> c = t;
		while (c != Object.class) {
			for (int i = 0; i < sv.length; i++) {
				for (int j = 0; j < av.length; j++) {
					try {
						Method tmp = c.getDeclaredMethod(sv[i], av[j]);
						tmp.setAccessible(true);
						return tmp;
					} catch (Exception e) {
						ec.add(e);
					}
				}
			}
			c = c.getSuperclass();
		}
		for (Exception e : ec)
			e.printStackTrace();
		return null;
	}

	public static final Field getField(final Class<?> t, final String... sv) {
		Collection<Exception> ec = new ArrayList<Exception>();
		Class<?> c = t;
		while (c != Object.class) {
			for (String s : sv) {
				try {
					Field tmp = c.getDeclaredField(s);
					tmp.setAccessible(true);
					return tmp;
				} catch (Exception e) {
					ec.add(e);
				}
			}
			c = c.getSuperclass();
		}
		for (Exception e : ec)
			e.printStackTrace();
		return null;
	}

	public static final Class<?> getClass(final String... sv) {
		Collection<Exception> ec = new ArrayList<Exception>();
		for (String s : sv) {
			try {
				return Class.forName(s, false, ReflectionHelper.class.getClassLoader());
			} catch (Exception e) {
				ec.add(e);
			}
		}
		for (Exception e : ec)
			e.printStackTrace();
		return null;
	}
}
