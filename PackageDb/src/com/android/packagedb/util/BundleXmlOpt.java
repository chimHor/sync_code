
package com.android.packagedb.util;

import android.util.ArraySet;
import android.util.Log;
import android.os.BaseBundle;
import android.os.Bundle;

import java.lang.reflect.Field;

import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.ObjXmlOpt;

public class BundleXmlOpt extends ObjXmlOpt {

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();

    static {
        keyFields.add("mMap");
    }

    public BundleXmlOpt() {
        mClass = Bundle.class;
    }
    @Override
    public Object createInstance(String suggestClass) {
        return new Bundle();
    }
    @Override
    public Object createInstance(Class suggestClass) {
        return new Bundle();
    }

    @Override
    protected boolean handleSerializeSpField(Object o, Field field, String fieldName) {
        if (keyFields.contains(fieldName)) {
            return false;
        }
        return true;
    }

}
