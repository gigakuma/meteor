package lab.meteor.core.type;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lab.meteor.core.MCollection;
import lab.meteor.core.MNotifiable;

public class MDictionary extends MCollection {
	
	public MDictionary(MNotifiable parent) {
		super(parent);
	}

	private Map<String, Object> dict = new TreeMap<String, Object>();
	
	public Object add(String key, Object value) {
		checkType(value);
		
		value = toInputObject(value);
		Object o = dict.put(key, value);
		o = toOutputObject(value);
		this.notifyChanged();
		return o;
	}

	public Object remove(Object key) {
		Object o = dict.remove(key);
		o = toOutputObject(o);
		this.notifyChanged();
		return o;
	}

	public Object get(Object key) {
		Object o = dict.get(key);
		o = toOutputObject(o);
		return o;
	}

	public void clear() {
		dict.clear();
		this.notifyChanged();
	}

	public boolean containsKey(Object key) {
		return dict.containsKey(key);
	}

	public boolean containsValue(Object value) {
		value = toInputObject(value);
		return dict.containsValue(value);
	}

	public boolean isEmpty() {
		return dict.isEmpty();
	}

	public Set<String> keySet() {
		return new TreeSet<String>(dict.keySet());
	}

	public int size() {
		return dict.size();
	}

	public Iterator<Entry<String, Object>> iterator() {
		return new MapItr();
	}
	
	private class DictEntry implements Entry<String, Object> {

		private final Entry<String, Object> entry;
		
		public DictEntry(Entry<String, Object> entry) {
			this.entry = entry;
		}
		
		@Override
		public String getKey() {
			return entry.getKey();
		}

		@Override
		public Object getValue() {
			Object o = entry.getValue();
			o = toOutputObject(o);
			return o;
		}

		@Override
		public Object setValue(Object value) {
			checkType(value);
			
			value = toInputObject(value);
			Object o = entry.setValue(value);
			o = toOutputObject(o);
			MDictionary.this.notifyChanged();
			return o;
		}
		
	}
	
	private class MapItr implements Iterator<Entry<String, Object>> {

		private final Iterator<Map.Entry<String, Object>> it;
		
		public MapItr() {
			it = MDictionary.this.dict.entrySet().iterator();
		}
		
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Entry<String, Object> next() {
			Map.Entry<String, Object> entry = it.next();
			return new DictEntry(entry);
		}

		@Override
		public void remove() {
			it.remove();
			MDictionary.this.notifyChanged();
		}
		
	}


	@Override
	public void forEach(ForEachCallback callback) {
		if (callback == null)
			return;
		Iterator<Entry<String, Object>> it = dict.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> e = it.next();
			Object o = e.getValue();
			o = toOutputObject(o);
			callback.function(o);
		}
	}
}
