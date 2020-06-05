package bot.util;

public interface Ref<T> {
	
	// used to ease serialization of POJO objects
	// to allow a pokemon to be serialized as a pojo, objects which would serialize long are instead convered to a Ref type
	
	// alternate: for the would be "Ref" types, instead just provide custom serializers
	
}
