package ezvcard.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A multimap that uses {@link List} objects to store its values.
 * 
 * <p>
 * The internal {@link Map} implementation is a {@link HashMap} that uses
 * {@link ArrayList} for its values.
 * </p>
 * @author Michael Angstadt
 * @param <K> the key
 * @param <V> the value
 */
public class ListMultimap<K, V> {
	private final Map<K, List<V>> map = new HashMap<K, List<V>>();

	public ListMultimap() {
	}

	/**
	 * Copy constructor.
	 * @param orig the multimap to copy from
	 */
	public ListMultimap(ListMultimap<K, V> orig) {
		for (Map.Entry<K, List<V>> entry : orig.map.entrySet()) {
			List<V> values = new ArrayList<V>(entry.getValue());
			map.put(entry.getKey(), values);
		}
	}

	/**
	 * Adds a value to the multimap.
	 * @param key the key
	 * @param value the value
	 */
	public void put(K key, V value) {
		List<V> values = map.get(key);
		if (values == null) {
			values = new ArrayList<V>();
			map.put(key, values);
		}
		values.add(value);
	}

	/**
	 * Gets the values associated with the key.
	 * @param key the key
	 * @return the list of values or empty list if the key doesn't exist
	 */
	public List<V> get(K key) {
		List<V> values = map.get(key);
		if (values == null) {
			values = new ArrayList<V>();
		}
		return values;
	}

	/**
	 * Removes all the values associated with a key
	 * @param key the key to remove
	 * @return the removed values
	 */
	public List<V> remove(K key) {
		return map.remove(key);
	}

	/**
	 * Removes a particular value.
	 * @param key the key
	 * @param value the value to remove
	 * @return true if the multimap contained the value, false if not
	 */
	public boolean remove(K key, V value) {
		List<V> values = map.get(key);
		if (values != null) {
			return values.remove(value);
		}
		return false;
	}

	/**
	 * Returns all the keys.
	 * @return all the keys
	 */
	public Set<K> keySet() {
		return map.keySet();
	}

	/**
	 * Returns the number of key/value pairs.
	 * @return the number of key/value pairs
	 */
	public Collection<V> values() {
		Collection<V> list = new ArrayList<V>();
		for (List<V> value : map.values()) {
			list.addAll(value);
		}
		return list;
	}

	/**
	 * Determines if the multimap is empty or not.
	 * @return true if it's empty, false if not
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Returns the number of key/value pairs.
	 * @return the number of key/value pairs
	 */
	public int size() {
		int size = 0;
		for (Map.Entry<K, List<V>> entry : map.entrySet()) {
			size += entry.getValue().size();
		}
		return size;
	}

	/**
	 * Gets the underlying {@link Map} object.
	 * @return the underlying {@link Map} object
	 */
	public Map<K, List<V>> getMap() {
		return map;
	}
}
