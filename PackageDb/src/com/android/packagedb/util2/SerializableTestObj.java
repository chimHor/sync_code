package com.android.packagedb.util2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

public class SerializableTestObj implements Serializable{
	
    private static Random ran = new Random();
    private final static String WORDS = "abcdefghijklmnopqrstuvwxyz";
    public static int randomInt() {
        return ran.nextInt(999);
    }

    public static String randomString() {
        StringBuilder sb = new StringBuilder();
        int wordsSize = WORDS.length();
        for(int i = 0; i < 150; i++) {
            int pos = ran.nextInt(wordsSize);
            sb.append(WORDS.substring(pos,pos+1));
        }
        return sb.toString();
    }
    
    public int a;
    public int b;
    public int c;
    public int d;
    public String sa;
    public String sb;
    public String sc;
    public String sd;
    
	public static SerializableTestObj randomTestObj() {
		SerializableTestObj obj = new SerializableTestObj();
		obj.a = randomInt();
		obj.b = randomInt();
		obj.c = randomInt();
		obj.d = randomInt();
		obj.sa = randomString();
		obj.sb = randomString();
		obj.sc = randomString();
		obj.sd = randomString();
		return obj;
	}
    
	private void writeObject(ObjectOutputStream stream) throws IOException { 
		stream.writeInt(a);
		stream.writeObject(sa);
		stream.writeInt(b);
		stream.writeObject(sb);
		stream.writeInt(c);
		stream.writeObject(sc);
		stream.writeInt(d);
		stream.writeObject(sd);
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		a = stream.readInt();
		sa = (String) stream.readObject();
		b = stream.readInt();
		sb = (String) stream.readObject();
		c = stream.readInt();
		sc = (String) stream.readObject();
		d = stream.readInt();
		sd = (String) stream.readObject();
	}
	
}
