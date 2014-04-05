package com.example.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class URLListHolder {
	private static List<String> list;
	private final static String filename = "list_data";
	
	public static synchronized List<String> getList(Context ctx) {
		if (list == null) {
			File file = new File(ctx.getFilesDir(), filename);
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(
						new FileInputStream(file));
				list = (List<String>) objectInputStream.readObject();
			} catch (Exception e) {
				list = new ArrayList<String>();
			}
		}
		return list;
	}

	public static synchronized void saveList(Context ctx) {
		try {
			File file = new File(ctx.getFilesDir(), filename);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					new FileOutputStream(file));
			objectOutputStream.writeObject(list);
		} catch (IOException e) {
			// yeah, die freely!
			e.printStackTrace();
		}
	}
}
