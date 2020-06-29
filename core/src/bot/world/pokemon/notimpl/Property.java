package bot.world.pokemon.notimpl;

public class Property<T> {
	
	private final PropertySet set;
	private final int id;
	private final T defaultValue;
	
	Property(PropertySet set, T defaultValue) {
		this.set = set;
		this.defaultValue = defaultValue;
		id = set.addProperty(this);
	}
	
	public PropertyValue<T> as(T value) {
		return new PropertyValue<>(this, value);
	}
	
	public static class PropertyValue<T> {
		
		private final Property<T> prop;
		private final T value;
		
		public PropertyValue(Property<T> prop, T value) {
			this.prop = prop;
			this.value = value;
		}
	}
	
	public static class PropertySet {
		private int total = 0;
		
		public PropertySet() {}
		
		public int addProperty(Property<?> prop) {
			return total++;
		}
	}
	
	public static class PropertyMap {
		
		private Object[] values;
		
		public PropertyMap(PropertySet set, PropertyValue<?>... properties) {
			values = new Object[set.total];
			for(PropertyValue<?> propVal: properties)
				values[propVal.prop.id] = propVal.value;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T get(Property<T> prop) {
			T obj = (T) values[prop.id];
			if(obj == null) return prop.defaultValue;
			return obj;
		}
	}
	
}
