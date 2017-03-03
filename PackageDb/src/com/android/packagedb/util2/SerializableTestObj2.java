package com.android.packagedb.util2;

import java.io.Serializable;
import java.util.Random;

public class SerializableTestObj2 implements Serializable{
	
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
    
	public static SerializableTestObj2 randomTestObj() {
		SerializableTestObj2 obj = new SerializableTestObj2();
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

}
