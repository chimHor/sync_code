package com.android.packagedb.test;

import android.util.ArrayMap;
import android.util.ArraySet;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import java.util.Map;

public class TestObj {

    private static Random ran = new Random();
    private final static String WORDS = "abcdefghijklmnopqrstuvwxyz";
    public static int randomInt() {
        return ran.nextInt(10);
    }

    public static String randomString() {
        StringBuilder sb = new StringBuilder();
        int wordsSize = WORDS.length();
        for(int i = 0; i < 3; i++) {
            int pos = ran.nextInt(wordsSize);
            sb.append(WORDS.substring(pos,pos+1));
        }
        return sb.toString();
    }
    public String s;
    public int i;
    public int[] intArray;
    public ArrayList<String> sList = new ArrayList<String>();
    public ArrayMap<String,TestSubObj> map = new ArrayMap<String,TestSubObj>();

    public static class TestSubObj {
        public String[] sArray;
        public ArraySet<String> sSet = new ArraySet<String>();

        public static TestSubObj createRandomTestSubObj() {
            if (ran.nextInt(6)==0) {
                return null;
            }

            TestSubObj obj = new TestSubObj();
            int s = ran.nextInt(5);
            if (s>0) {
                obj.sArray = new String[s];
                for (int i = 0; i < s; i++) {
                    (obj.sArray)[i] = randomString();
                }
            }

            s = ran.nextInt(5);
            if (s>0) {
                for (int i = 0; i < s; i++) {
                    obj.sSet.add(randomString());
                }
            }

            return obj;
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof TestSubObj)) {
                Log.e("xxx", "TestSubObj obj not TestSubObj");
                return false;
            }
            TestSubObj aObj = (TestSubObj) obj;
            if (!Arrays.equals(sArray, aObj.sArray)) {
                Log.e("xxx", "TestSubObj sArray notmatch");
                return false;
            }
            if ( (sSet == null) && (aObj.sSet != null)) {
                Log.e("xxx", "TestSubObj sSet notmatch");
                return false;
            } else if (sSet != null && !sSet.equals(aObj.sSet)) {
                Log.e("xxx", "TestSubObj sSet notmatch");
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("TestSubObj: [ \n");
            sb.append("sArray: "+ Arrays.toString(sArray) +"\n");
            sb.append("sSet: "+ sSet.toString()+"\n");
            sb.append("]");
            return sb.toString();
        }
    }


        public boolean equals(TestObj aObj) {
            if (aObj == null) {
                Log.e("xxx", "TestObj null");
                return false;
            }
            if (!s.equals(aObj.s)) {
                Log.e("xxx", "TestObj s nomatch");
                return false;
            }
            if (i != aObj.i) {
                Log.e("xxx", "TestObj i nomatch");
                return false;
            }
            if (!Arrays.equals(intArray, aObj.intArray)) {
                Log.e("xxx", "TestObj intArray nomatch");
                return false;
            }

            if ( sList == null  && aObj.sList != null) {
                Log.e("xxx", "TestObj sList nomatch");
                return false;
            } else if (sList != null && !sList.equals(aObj.sList)) {
                Log.e("xxx", "TestObj sList nomatch");
                return false;
            }
            if ( map == null  && aObj.map != null) {
                Log.e("xxx", "TestObj map nomatch");
                return false;
            } else if ((map != null) && (!map.equals(aObj.map))) {
                Log.e("xxx", "TestObj map nomatch");
                return false;
            }
            return true;
        }

    @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("TestObj: [ \n");
            sb.append("s : "+ s+"\n");
            sb.append("i : "+ i+"\n");
            sb.append("intArray :" + Arrays.toString(intArray)+"\n");
            sb.append("sList :"+sList.toString()+"\n");
            sb.append("map :"+map.toString()+"\n");
            sb.append("]");
            return sb.toString();
        }

    public static TestObj createRandomTestObj() {
        TestObj obj = new TestObj();
        obj.i = randomInt();
        obj.s = randomString();
        int s = ran.nextInt(3)+2;
        if (s>0) {
            obj.intArray = new int[s];
            for (int i = 0; i < s; i++) {
                (obj.intArray)[i] = randomInt();
            }
        }
        if (s>0) {
            for (int i = 0; i < s; i++) {
                obj.sList.add(randomString());
            }
        }


        s = ran.nextInt(7);
        s = s/2+1;
        if (s>0) {
            for (int i = 0; i < s; i++) {
                obj.map.put(randomString(),TestSubObj.createRandomTestSubObj());
            }
        }
        return obj;

    }

}
