package kr.dongyounyi.mbitresearch;

import java.util.Collections;

public class MapUtilities {

public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValue(Map<K, V> map) {
	List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
	Collections.sort(entries, new ByValue<K, V>());
	return entries;
}

private static class ByValue<K, V extends Comparable<V>> implements Comparator<Entry<K, V>> {
	public int compare(Entry<K, V> o1, Entry<K, V> o2) {
		return o1.getValue().compareTo(o2.getValue());
	}
}