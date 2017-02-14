
package com.android.packagedb;

import android.content.pm.ApplicationInfo;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Field;

import com.android.packagedb.ObjXmlOtpImpl2.AbstractObjXmlOpt;
import com.android.packagedb.ObjXmlOtpImpl2.ObjXmlOpt;

public class ApplicationInfoOpt extends ObjXmlOpt {

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();

    static {
        skipFields.add("metaData");
    }

    public ApplicationInfoOpt() {
        mClass = ApplicationInfo.class;
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
        if (skipFields.contains(fieldName)) {
            return true;
        }
        return false;
    }
}
