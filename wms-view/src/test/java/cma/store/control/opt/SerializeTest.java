package cma.store.control.opt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cma.store.data.Pos;
import cma.store.serialization.FileSerializer;
import cma.store.serialization.IFileSerializer;

public class SerializeTest {
	
	public SerializeTest() {
		IFileSerializer<List<Pos>> posSerializer = new FileSerializer<List<Pos>>();
		Pos pos = new Pos(1,3);
		Pos pos2 = new Pos(2,2);
		List<Pos> posList = new ArrayList<Pos>();
		posList.add(pos);
		posList.add(pos2);
		try {
			posSerializer.serialize(posList, "test");
			
			posList = posSerializer.deserialize("test");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new SerializeTest();
	}
}
