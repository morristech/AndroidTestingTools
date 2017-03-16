package com.matthewtamlin.android_testing_tools.library.harnesses;

import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

public class ControlBinder {
	public static void bindControls(final TestHarness<?, ?, ?, ?, ?> testHarness) {
		checkNotNull(testHarness, "testHarness cannot be null.");

		final Map<Integer, View> controls = new TreeMap<>();

		for (final Method m : testHarness.getClass().getMethods()) {
			if (m.isAnnotationPresent(Control.class)) {
				View control;

				try {
					m.setAccessible(true);

					final Object result = m.invoke(testHarness);

					if (result == null) {
						throw new RuntimeException("A method annotated with @Control returned " +
								"null.");
					}

					if (!(result instanceof View)) {
						throw new RuntimeException("A method annotated with @Control returned a " +
								"object which is not a View.");
					}

					control = (View) m.invoke(testHarness);
				} catch (final IllegalAccessException e) {
					throw new RuntimeException("Unable to access a method annotated with " +
							"@Control. The method must be public or protected.", e);
				} catch (final InvocationTargetException e) {
					throw new RuntimeException("A method annotated with @Control threw an " +
							"exception when invoked.", e);
				} catch (final IllegalArgumentException e) {
					throw new RuntimeException("Unable to invoke a method annotated with " +
							"@Control. The method must accept no arguments.", e);
				}


				final Control annotation = m.getAnnotation(Control.class);
				final int controlIndex = annotation.value();

				if (controls.keySet().contains(controlIndex)) {
					throw new RuntimeException("All @Control annotations in a single class must " +
							"have unique values.");
				}

				controls.put(controlIndex, control);
			}
		}

		for (final Integer index : controls.keySet()) {
			testHarness.addControl(controls.get(index));
		}
	}
}
