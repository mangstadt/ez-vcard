package ezvcard.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 Copyright (c) 2012, Michael Angstadt
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
 * A multimap that uses {@link List} objects to store its values.
 * 
 * <p>
 * The internal {@link Map} implementation is a {@link LinkedHashMap} that uses
 * {@link ArrayList} for its values.
 * </p>
 * @author Michael Angstadt
 * @param <K> the key
 * @param <V> the value
 */
public class ListMultimap<K, V> {
	private final Map<K, List<V>> map = new LinkedHashMap<K, List<V>>();

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
		List<V> values = get(key, true);
		values.add(value);
	}

	/**
	 * Adds a value to the multimap.
	 * @param key the key
	 * @param value the value
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
	 * Returns all the values.
	 * @return all the values
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
}
