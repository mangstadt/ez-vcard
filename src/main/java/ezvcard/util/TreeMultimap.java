package ezvcard.util;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A multimap that uses {@link Set} objects to store its values.
 * 
 * <p>
 * The internal {@link Map} implementation is a {@link TreeMap} that uses
 * {@link TreeSet} for its values.
 * </p>
 * @author Michael Angstadt
 * @param <K> the key
 * @param <V> the value
 */
public class TreeMultimap<K, V> {
	private final Map<K, Set<V>> map = new TreeMap<K, Set<V>>();

	public TreeMultimap() {
	}

	/**
	 * Copy constructor.
	 * @param orig the multimap to copy from
	 */
	public TreeMultimap(TreeMultimap<K, V> orig) {
		for (Map.Entry<K, Set<V>> entry : orig.map.entrySet()) {
			Set<V> values = new TreeSet<V>(entry.getValue());
			map.put(entry.getKey(), values);
		}
	}

	/**
	 * Adds a value to the multimap.
	 * @param key the key
	 * @param value the value
	 */
	public void put(K key, V value) {
		Set<V> values = map.get(key);
		if (values == null) {
			values = new TreeSet<V>();
			map.put(key, values);
		}
		values.add(value);
	}

	/**
	 * Gets the values associated with the key.
	 * @param key the key
	 * @return the set of values or empty set if the key doesn't exist
	 */
	public Set<V> get(K key) {
		Set<V> values = map.get(key);
		if (values == null) {
			values = new TreeSet<V>();
		}
		return values;
	}

	/**
	 * Removes all the values associated with a key
	 * @param key the key to remove
	 * @return the removed values
	 */
	public Set<V> remove(K key) {
		return map.remove(key);
	}

	/**
	 * Removes a particular value.
	 * @param key the key
	 * @param value the value to remove
	 * @return true if the multimap contained the value, false if not
	 */
	public boolean remove(K key, V value) {
		Set<V> values = map.get(key);
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
		for (Map.Entry<K, Set<V>> entry : map.entrySet()) {
			size += entry.getValue().size();
		}
		return size;
	}

	/**
	 * Gets the underlying {@link Map} object.
	 * @return the underlying {@link Map} object
	 */
	public Map<K, Set<V>> getMap() {
		return map;
	}
}
