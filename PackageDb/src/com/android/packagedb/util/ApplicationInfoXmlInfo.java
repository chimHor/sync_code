
package com.android.packagedb.util;

import android.content.pm.ApplicationInfo;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Field;

import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.ObjXmlOpt;

public class ApplicationInfoXmlInfo extends ObjXmlOpt {

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();

    static {
    }

    public ApplicationInfoXmlInfo() {
        mClass = ApplicationInfo.class;
    }
    @Override
    public boolean needSaveRef() {
        return true;
    }

    @Override
    public Object createInstance(String suggestClass) {
        return new ApplicationInfo();
    }
    @Override
    public Object createInstance(Class suggestClass) {
        return new ApplicationInfo();
    }

    @Override
    protected boolean handleSerializeSpField(Object o, Field field, String fieldName) {
        return false;
    }
}
