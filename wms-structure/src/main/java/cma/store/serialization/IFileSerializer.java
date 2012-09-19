package cma.store.serialization;

import java.io.IOException;

public interface IFileSerializer<T> {
	public void serialize(T clazz, String key) throws IOException;
	public T deserialize(String key) throws IOException,ClassNotFoundException;
}
