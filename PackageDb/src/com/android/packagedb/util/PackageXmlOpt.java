
package com.android.packagedb.util;

import android.content.pm.PackageParser;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Field;

import com.android.packagedb.util.PkgSerializer;
import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.ObjXmlOpt;


import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;

public class PackageXmlOpt extends ObjXmlOpt {

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();

    static {
        skipFields.add("mExtras");
        skipFields.add("mSignatures");
        skipFields.add("mSigningKeys");
        skipFields.add("applicationInfo");
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

    public boolean serializeFieldsBefore(XmlSerializer serializer, Object obj)
        throws XmlPullParserException,IOException {
        try {
        Field field = PackageParser.Package.class.getField("applicationInfo");
        Object appInfo = field.get(obj);
        int subClassCode = Helper.classToCode(field.getType());
        PkgSerializer.optArray[subClassCode].serialize(serializer, appInfo, field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleSerializeSpField(Object o, Field field, String fieldName) {
        if (skipFields.contains(fieldName)) {
            return true;
        }
        return false;
    }
}
