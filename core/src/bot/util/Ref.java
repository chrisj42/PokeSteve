package bot.util;

import java.util.function.Function;

public class Ref<T> {
	
	// used to ease serialization of POJO objects
	// to allow a pokemon to be serialized as a pojo, objects which would serialize long are instead convered to a Ref type
	
	// alternate: for the would be "Ref" types, instead just provide custom serializers
	
	private final int id;
	private final Function<Integer, T> resolver;
	private boolean resolved = false;
	private T value;
	
	public Ref(int id, Function<Integer, T> resolver) {
		this.id = id;
		this.resolver = resolver;
	}
	
	public T resolve() {
		if(!resolved) {
			resolved = true;
			value = resolver.apply(id);
		}
		return value;
	}
}
