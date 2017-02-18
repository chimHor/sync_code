
package com.android.packagedb;

import android.content.pm.PackageParser;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Field;

import com.android.packagedb.ObjXmlOtpImpl2.AbstractObjXmlOpt;
import com.android.packagedb.ObjXmlOtpImpl2.ObjXmlOpt;

public class PackageXmlOpt extends ObjXmlOpt {

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();

    static {
        skipFields.add("mAppMetaData");
        skipFields.add("mExtras");
        skipFields.add("providers");
        skipFields.add("services");
        skipFields.add("Instrumentation");


        skipFields.add("mSignatures");
        skipFields.add("mSigningKeys");
    }

    public PackageXmlOpt() {
        mClass = PackageParser.Package.class;
    }
    @Override
    public boolean needSaveRef() {
        return true;
    }

    @Override
    public Object createInstance(String suggestClass) {
        return new PackageParser.Package("");
    }
    @Override
    public Object createInstance(Class suggestClass) {
        return new PackageParser.Package("");
    }

    @Override
    protected boolean handleSerializeSpField(Object o, Field field, String fieldName) {
        if (skipFields.contains(fieldName)) {
            return true;
        }
        return false;
    }
}
