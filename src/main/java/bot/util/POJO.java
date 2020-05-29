package bot.util;

// "Plain Old Java Object"
// meant to be a Java analogue to a JSON object, i.e. easily serializable
public abstract class POJO<T> {
	
	public POJO(T instance) {
		// nothing actually happens in the superclass, but impls should be copying the signature.
	}
	
	// must be able to create instance from pojo
	public abstract T create();
	
}
