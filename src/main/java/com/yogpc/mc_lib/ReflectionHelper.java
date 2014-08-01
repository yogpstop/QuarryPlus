package com.yogpc.mc_lib;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionHelper {
	public static final List<Method> getMethodsAnnotatedWith(final Class<?> t, final Class<? extends Annotation> a) {
		final List<Method> ms = new ArrayList<Method>();
		Class<?> c = t;
		while (c != Object.class) {
			for (final Method m : c.getDeclaredMethods())
				if (m.isAnnotationPresent(a)) ms.add(m);
			c = c.getSuperclass();
		}
		return ms;
	}
}
