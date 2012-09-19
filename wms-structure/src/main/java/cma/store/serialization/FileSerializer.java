package cma.store.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

public class FileSerializer<T> implements IFileSerializer<T> {

	Logger log = Logger.getLogger(getClass());
	Properties properties;
	public static final String fileName = "external.properties";
	public final static String propertyName = "serialization.folder";
	
	public FileSerializer() {
		properties = new Properties();
		try {
			URL url =  ClassLoader.getSystemResource(fileName);
		    properties.load(new FileInputStream(new File(url.getFile())));
		} catch (IOException e) {
			log.error("", e);
		}
	}

	
	@Override
	public void serialize(T clazz, String key) throws IOException {
		 FileOutputStream fos = new FileOutputStream(properties.getProperty(propertyName)+"/"+key+".dat");
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(clazz);
         oos.flush();
         oos.close();
	}

	@Override
	public T deserialize(String key) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(properties.getProperty(propertyName)+"/"+key+".dat");
        ObjectInputStream ois = new ObjectInputStream(fis);
        T res = ((T) ois.readObject());
        ois.close();
        return res;
	}

}