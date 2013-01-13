package ezvcard.util;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
