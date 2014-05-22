package ezvcard.android;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class AndroidCustomFieldScribeTest {
	private final AndroidCustomFieldScribe scribe = new AndroidCustomFieldScribe();
	private final Sensei<AndroidCustomField> sensei = new Sensei<AndroidCustomField>(scribe);

	private final AndroidCustomField itemWithoutType = new AndroidCustomField();
	{
		itemWithoutType.setItem(true);
	}
	private final AndroidCustomField itemWithoutValues = new AndroidCustomField();
	{
		itemWithoutValues.setItem(true);
		itemWithoutValues.setType("type");
	}
	private final AndroidCustomField itemWithValues = new AndroidCustomField();
	{
		itemWithValues.setItem(true);
		itemWithValues.setType("type");
		itemWithValues.getValues().add("one");
		itemWithValues.getValues().add("two");
	}
	private final AndroidCustomField dirWithoutValues = new AndroidCustomField();
	{
		dirWithoutValues.setDir(true);
		dirWithoutValues.setType("type");
	}
	private final AndroidCustomField dirWithValues = new AndroidCustomField();
	{
		dirWithValues.setDir(true);
		dirWithValues.setType("type");
		dirWithValues.getValues().add("one");
		dirWithValues.getValues().add("two");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("not-a-uri;one;two").skipMe();
		sensei.assertParseText("http://incorrect-uri;one;two").skipMe();
		sensei.assertParseText("vnd.android.cursor.foo/type").skipMe();
		sensei.assertParseText("vnd.android.cursor.item/type").run(is(itemWithoutValues));
		sensei.assertParseText("vnd.android.cursor.item/type;one;two").run(is(itemWithValues));

		sensei.assertParseText("vnd.android.cursor.dir/type").run(is(dirWithoutValues));
		sensei.assertParseText("vnd.android.cursor.dir/type;one;two").run(is(dirWithValues));

		sensei.assertParseText("").skipMe();
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(itemWithoutValues).run("vnd.android.cursor.item/type;");
		sensei.assertWriteText(itemWithValues).run("vnd.android.cursor.item/type;one");
		sensei.assertWriteText(itemWithoutType).run("vnd.android.cursor.item/;");

		sensei.assertWriteText(dirWithoutValues).run("vnd.android.cursor.dir/type");
		sensei.assertWriteText(dirWithValues).run("vnd.android.cursor.dir/type;one;two");
	}

	private static Sensei.Check<AndroidCustomField> is(final AndroidCustomField expected) {
		return new Sensei.Check<AndroidCustomField>() {
			public void check(AndroidCustomField property) {
				assertEquals(expected.isDir(), property.isDir());
				assertEquals(expected.getType(), property.getType());
				assertEquals(expected.getValues(), property.getValues());
			}
		};
	}
}
