package com.ezvcard.android;

import static com.ezvcard.android.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class AndroidCustomFieldTest {
	@Test
	public void item() {
		AndroidCustomField property = AndroidCustomField.item("type", "value");
		assertTrue(property.isItem());
		assertFalse(property.isDir());
		assertEquals("type", property.getType());
		assertEquals(Arrays.asList("value"), property.getValues());
	}

	@Test
	public void dir() {
		AndroidCustomField property = AndroidCustomField.dir("type", "value1", "value2");
		assertFalse(property.isItem());
		assertTrue(property.isDir());
		assertEquals("type", property.getType());
		assertEquals(Arrays.asList("value1", "value2"), property.getValues());
	}

	@Test
	public void validate_item() {
		AndroidCustomField property = new AndroidCustomField();
		property.setItem(true);

		assertValidate(property).run(2);

		property.setType("type");
		assertValidate(property).run(1);

		property.getValues().add("value1");
		assertValidate(property).run(0);

		property.getValues().add("value2");
		assertValidate(property).run(1);
	}

	@Test
	public void validate_dir() {
		AndroidCustomField property = new AndroidCustomField();
		property.setDir(true);

		assertValidate(property).run(1);

		property.setType("type");
		assertValidate(property).run(0);

		property.getValues().add("value1");
		assertValidate(property).run(0);

		property.getValues().add("value2");
		assertValidate(property).run(0);
	}
}
