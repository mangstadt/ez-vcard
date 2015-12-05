package ezvcard.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * A multimap that uses {@link List} objects to store its values. The internal
 * {@link Map} implementation is a {@link LinkedHashMap} that uses
 * {@link ArrayList} for its values.
 * @author Michael Angstadt
 * @param <K> the key
 * @param <V> the value
 */
public class ListMultimap<K, V> implements Iterable<Map.Entry<K, List<V>>> {
	private final Map<K, List<V>> map;

	/**
	 * Creates an empty multimap.
	 */
	public ListMultimap() {
		map = new LinkedHashMap<K, List<V>>();
	}

	/**
	 * Creates a multimap using a specific backing map.
	 */
	public ListMultimap(Map<K, List<V>> backingMap) {
		map = backingMap;
	}

	/**
	 * Creates an empty multimap.
	 * @param initialCapacity the initial capacity of the underlying map.
	 */
	public ListMultimap(int initialCapacity) {
		map = new LinkedHashMap<K, List<V>>(initialCapacity);
	}

	/**
	 * Creates a copy of an existing multimap.
	 * @param orig the multimap to copy from
	 */
	public ListMultimap(ListMultimap<K, V> orig) {
		this();
		for (Map.Entry<K, List<V>> entry : orig) {
			List<V> values = new ArrayList<V>(entry.getValue());
			map.put(entry.getKey(), values);
		}
	}

	/**
	 * Adds a value to the multimap.
	 * @param key the key
	 * @param value the value to add
	 */
	public void put(K key, V value) {
		List<V> values = get(key, true);
		values.add(value);
	}

	/**
	 * Adds multiple values to the multimap.
	 * @param key the key
	 * @param values the values to add
	 */
	public void putAll(K key, Collection<V> values) {
		List<V> existingValues = get(key, true);
		existingValues.addAll(values);
	}

	/**
	 * Gets the values associated with the key.
	 * @param key the key
	 * @return the list of values or empty list if the key doesn't exist
	 */
	public List<V> get(K key) {
		return get(key, false);
	}

	/**
	 * Gets the values associated with the key.
	 * @param key the key
	 * @param add true to add an empty element to the map if the key doesn't
	 * exist, false not to
	 * @return the list of values or empty list if the key doesn't exist
	 */
	private List<V> get(K key, boolean add) {
		key = sanitizeKey(key);
		List<V> values = map.get(key);
		if (values == null) {
			values = new ArrayList<V>();
			if (add) {
				map.put(key, values);
			}
		}
		return values;
	}

	/**
	 * Gets the first value that's associated with a key.
	 * @param key the key
	 * @return the first value or null if the key doesn't exist
	 */
	public V first(K key) {
		List<V> values = get(key);
		return (values == null || values.isEmpty()) ? null : values.get(0);
	}

	/**
	 * Determines whether the given key exists.
	 * @param key the key
	 * @return true if the key exists, false if not
	 */
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	/**
	 * Removes a particular value.
	 * @param key the key
	 * @param value the value to remove
	 * @return true if the multimap contained the value, false if not
	 */
	public boolean remove(K key, V value) {
		List<V> values = map.get(sanitizeKey(key));
		if (values != null) {
			return values.remove(value);
		}
		return false;
	}

	/**
	 * Removes all the values associated with a key
	 * @param key the key to remove
	 * @return the removed values or empty list if the key doesn't exist
	 */
	public List<V> removeAll(K key) {
		List<V> removed = map.remove(sanitizeKey(key));
		return (removed == null) ? Collections.<V> emptyList() : removed;
	}

	/**
	 * Replaces all values with the given value.
	 * @param key the key
	 * @param value the value with which to replace all existing values, or null
	 * to remove all values
	 * @return the values that were replaced
	 */
	public List<V> replace(K key, V value) {
		List<V> replaced = removeAll(key);
		if (value != null) {
			put(key, value);
		}
		return replaced;
	}

	/**
	 * Replaces all values with the given values.
	 * @param key the key
	 * @param values the values with which to replace all existing values
	 * @return the values that were replaced
	 */
	public List<V> replace(K key, Collection<V> values) {
		List<V> replaced = removeAll(key);
		if (values != null && !values.isEmpty()) {
			putAll(key, values);
		}
		return replaced;
	}

	/**
	 * Clears all entries from the multimap.
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * Returns all the keys.
	 * @return all the keys
	 */
	public Set<K> keySet() {
		return map.keySet();
	}

	/**
	 * Returns all the values.
	 * @return all the values
	 */
	public List<V> values() {
		List<V> list = new ArrayList<V>();
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
	 * Returns the number of values in the map.
	 * @return the number of values
	 */
	public int size() {
		int size = 0;
		for (List<V> value : map.values()) {
			size += value.size();
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

	/**
	 * Modifies a given key before it is used to interact with the internal map.
	 * This method is meant to be overridden by child classes if necessary.
	 * @param key the key
	 * @return the modified key (by default, the key is returned as-is)
	 */
	protected K sanitizeKey(K key) {
		return key;
	}

	//@Override
	public Iterator<Map.Entry<K, List<V>>> iterator() {
		return map.entrySet().iterator();
	}

	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		ListMultimap<?, ?> other = (ListMultimap<?, ?>) obj;
		return map.equals(other.map);
	}
}