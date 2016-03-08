package ezvcard.property;

import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static ezvcard.util.TestUtils.assertEqualsMethodEssentials;
import static ezvcard.util.TestUtils.assertNotEqualsBothWays;
import static ezvcard.util.TestUtils.checkCodes;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.util.TestUtils;

/*
 Copyright (c) 2012-2016, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * Contains methods for testing property classes.
 * @author Michael Angstadt
 */
public class PropertySensei {
	/**
	 * Asserts the validation of a property object.
	 * @param property the property object
	 * @return chaining class for carrying out the test
	 */
	public static PropertyValidateAsserter assertValidate(VCardProperty property) {
		return new PropertyValidateAsserter(property);
	}

	public static class PropertyValidateAsserter {
		private final VCardProperty property;
		private VCard vcard;
		private VCardVersion versions[] = VCardVersion.values();

		public PropertyValidateAsserter(VCardProperty property) {
			this.property = property;
			vcard(new VCard());
		}

		/**
		 * Defines the versions to check (defaults to all versions).
		 * @param versions the versions to check
		 * @return this
		 */
		public PropertyValidateAsserter versions(VCardVersion... versions) {
			this.versions = versions;
			return this;
		}

		/**
		 * Defines the vCard instance to use (defaults to an empty vCard).
		 * @param vcard the vCard instance
		 * @return this
		 */
		public PropertyValidateAsserter vcard(VCard vcard) {
			vcard.addProperty(property);
			this.vcard = vcard;
			return this;
		}

		/**
		 * Performs the validation check.
		 * @param expectedCodes the expected warning codes
		 */
		public void run(Integer... expectedCodes) {
			for (VCardVersion version : versions) {
				List<Warning> warnings = property.validate(version, vcard);
				boolean passed = checkCodes(warnings, expectedCodes);
				if (!passed) {
					fail("For version " + version + ", expected codes were " + Arrays.toString(expectedCodes) + " but were actually:\n" + warnings);
				}
			}
		}
	}

	/**
	 * Asserts that a property class's copy constructor and
	 * {@link VCardProperty#copy} method work correctly by reflectively calling
	 * these two methods and comparing the created objects to the original
	 * instance with "equals()".
	 * @param original the object to copy (note that this object is modified in
	 * order to test to make sure the fields in its {@link VCardProperty} parent
	 * class are also copied)
	 * @return chaining class for carrying out the test
	 */
	public static CopyAsserter assertCopy(VCardProperty original) {
		return new CopyAsserter(original);
	}

	public static class CopyAsserter {
		private final VCardProperty original, copyFromConstructor, copyFromMethod;

		public CopyAsserter(VCardProperty original) {
			this.original = original;

			//make sure it checks group and parameters
			original.setGroup("group");
			original.addParameter("PARAM", "value");

			try {
				Constructor<? extends VCardProperty> constructor = original.getClass().getConstructor(original.getClass());
				this.copyFromConstructor = constructor.newInstance(original);
				assertEqualsAndHash(original, copyFromConstructor);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			this.copyFromMethod = original.copy();
			assertNotSame(original, copyFromMethod);
			assertEqualsAndHash(original, copyFromMethod);

			assertEqualsAndHash(copyFromMethod, copyFromConstructor);
		}

		/**
		 * Asserts that a given method returns different objects when invoked
		 * from each copy. For example, this method asserts that a {@link List}
		 * field has been truly copied and that the property object copies are
		 * not just referencing the same object.
		 * @param methodName the name of the method to call (should be a
		 * "getter" method)
		 * @return this
		 */
		public CopyAsserter notSame(String methodName) {
			try {
				Method method = original.getClass().getMethod(methodName);
				Object fromOriginal = method.invoke(original);
				String message = "Object returned by \"" + methodName + "\" is the same object, but it shouldn't be.";
				assertNotSame(message, fromOriginal, method.invoke(copyFromConstructor));
				assertNotSame(message, fromOriginal, method.invoke(copyFromMethod));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return this;
		}
	}

	/**
	 * <p>
	 * Asserts some essential behaviors of the equals method (see
	 * {@link TestUtils#assertEqualsMethodEssentials}). Also asserts that the
	 * fields in the {@link VCardProperty} base class are checked.
	 * </p>
	 * <p>
	 * Calling the chaining methods of the object that this method returns will
	 * create identical instances of the given property and assert that the
	 * instances are equal and have identical hash codes.
	 * </p>
	 * @param clazz the property class
	 * @param constructorValues values to pass into the constructor for creating
	 * instances that test the equals method essentials
	 * @return a chainer object to perform the test
	 */
	public static EqualsMethodAsserter assertEqualsMethod(Class<? extends VCardProperty> clazz, Object... constructorValues) {
		return new EqualsMethodAsserter(clazz, constructorValues);
	}

	public static class EqualsMethodAsserter {
		private final Class<? extends VCardProperty> clazz;
		private Constructor<? extends VCardProperty> constructor;
		private Object[] constructorValues;
		private List<Method> methods = new ArrayList<Method>();
		private List<Object[]> methodValues = new ArrayList<Object[]>();

		public EqualsMethodAsserter(Class<? extends VCardProperty> clazz, Object... constructorValues) {
			this.clazz = clazz;

			try {
				Constructor<? extends VCardProperty> constructor;
				VCardProperty instance1, instance2;
				constructor = clazz.getConstructor(toTypes(constructorValues));
				instance1 = constructor.newInstance(constructorValues);
				instance2 = constructor.newInstance(constructorValues);

				assertEqualsMethodEssentials(instance1);
				assertEqualsBaseClassFields(instance1, instance2);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Defines what constructor to use to create the objects.
		 * @param values the values to pass into the constructor
		 * @return this
		 */
		public EqualsMethodAsserter constructor(Object... values) {
			Class<?> parameterTypes[] = new Class<?>[values.length];
			for (int i = 0; i < values.length; i++) {
				parameterTypes[i] = values[i].getClass();
			}

			return constructor(parameterTypes, values);
		}

		/**
		 * Defines what constructor to use to create the objects.
		 * @param parameterTypes the constructor's parameter types
		 * @param values the values to pass into the constructor
		 * @return this
		 */
		public EqualsMethodAsserter constructor(Class<?> parameterTypes[], Object... values) {
			if (parameterTypes.length != values.length) {
				throw new IllegalArgumentException("Number of parameter types must match number of values.");
			}

			try {
				constructor = clazz.getConstructor(parameterTypes);
				constructorValues = values;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return this;
		}

		/**
		 * Defines a method that should be called after creating each object.
		 * @param name the method name
		 * @param values the values to pass into the method
		 * @return this
		 */
		public EqualsMethodAsserter method(String name, Object... values) {
			Class<?> parameterTypes[] = new Class<?>[values.length];
			for (int i = 0; i < values.length; i++) {
				parameterTypes[i] = values[i].getClass();
			}

			return method(name, parameterTypes, values);
		}

		/**
		 * Defines a method that should be called after creating each object.
		 * @param name the method name
		 * @param parameterTypes the parameter types of the method (required if
		 * you're passing null values)
		 * @param values the values to pass into the method
		 * @return this
		 */
		public EqualsMethodAsserter method(String name, Class<?> parameterTypes[], Object... values) {
			if (parameterTypes.length != values.length) {
				throw new IllegalArgumentException("Number of parameter types must match number of values.");
			}

			try {
				Method method = clazz.getMethod(name, parameterTypes);
				methods.add(method);
				methodValues.add(values);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			return this;
		}

		/**
		 * Executes the test using the specified constructor and methods.
		 * @return this
		 */
		public EqualsMethodAsserter test() {
			try {
				VCardProperty instance1 = createInstance();
				VCardProperty instance2 = createInstance();
				methods.clear();
				methodValues.clear();
				return test(instance1, instance2);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public EqualsMethodAsserter test(VCardProperty instance1, VCardProperty instance2) {
			assertEqualsAndHash(instance1, instance2);
			return this;
		}

		private VCardProperty createInstance() throws Exception {
			VCardProperty instance = constructor.newInstance(constructorValues);
			for (int i = 0; i < methods.size(); i++) {
				Method method = methods.get(i);
				Object[] values = methodValues.get(i);
				method.invoke(instance, values);
			}
			return instance;
		}

		private Class<?>[] toTypes(Object... values) {
			Class<?> parameterTypes[] = new Class<?>[values.length];
			for (int i = 0; i < values.length; i++) {
				parameterTypes[i] = values[i].getClass();
			}
			return parameterTypes;
		}
	}

	/**
	 * Asserts that none of the given property instances are equal to each
	 * other. Also creates copies of the first property, setting the fields in
	 * {@link VCardProperty} to assert that these fields are checked too.
	 * @param properties the properties
	 */
	public static void assertNothingIsEqual(VCardProperty... properties) {
		assertNothingIsEqual(Arrays.asList(properties));
	}

	/**
	 * Asserts that none of the given property instances are equal to each
	 * other. Also creates copies of the first property, setting the fields in
	 * {@link VCardProperty} to assert that these fields are checked too.
	 * @param properties the properties
	 */
	public static void assertNothingIsEqual(Iterable<VCardProperty> properties) {
		for (VCardProperty property : properties) {
			for (VCardProperty property2 : properties) {
				if (property != property2) {
					assertNotEquals(property, property2);
				}
			}
		}

		VCardProperty first = properties.iterator().next();
		VCardProperty copy = first.copy();
		copy.setGroup("group");
		assertNotEqualsBothWays(first, copy);

		copy = first.copy();
		copy.addParameter("PARAM", "value");
		assertNotEqualsBothWays(first, copy);
	}

	/**
	 * Asserts that the fields in the parent {@link VCardProperty} class are
	 * checked for equality by the property class's "equals" method. Note that
	 * the property instances passed into this method will be modified as part
	 * of the test.
	 * @param instance1 the first property instance
	 * @param instance2 the second property instance
	 */
	public static <T extends VCardProperty> void assertEqualsBaseClassFields(T instance1, T instance2) {
		assertNotSame("You must pass two separate object instances into this method.", instance1, instance2);
		assertEqualsAndHash(instance1, instance2);

		instance1.setGroup("group");
		assertNotEqualsBothWays(instance1, instance2);
		instance2.setGroup("group");
		assertEqualsAndHash(instance1, instance2);

		instance1.setGroup("group2");
		assertNotEqualsBothWays(instance1, instance2);
		instance2.setGroup("group2");
		assertEqualsAndHash(instance1, instance2);

		instance1.setParameter("PARAM", "value");
		assertNotEqualsBothWays(instance1, instance2);
		instance2.setParameter("PARAM", "value");
		assertEqualsAndHash(instance1, instance2);

		instance1.setParameter("PARAM", "value2");
		assertNotEqualsBothWays(instance1, instance2);
		instance2.setParameter("PARAM", "value2");
		assertEqualsAndHash(instance1, instance2);
	}
}
