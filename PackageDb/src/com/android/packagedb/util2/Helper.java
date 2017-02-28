package com.android.packagedb.util2;

import android.util.ArraySet;
import java.util.ArrayList;

public class Helper {
    public static <T> ArrayList<T> add(ArrayList<T> cur, ArrayList<T> addL) {
    	if (addL == null) {
    		return cur;
    	}
        if (cur == null) {
            cur = new ArrayList<T>();
        }
        cur.addAll(addL);
        return cur;
    }
    public static <T> ArraySet<T> add(ArraySet<T> cur, ArrayList<T> addL) {
    	if (addL == null) {
    		return cur;
    	}
        if (cur == null) {
            cur = new ArraySet<T>();
        }
        cur.addAll(addL);
        return cur;
    }

}
