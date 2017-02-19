
package com.android.packagedb;

import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.Package;
import android.content.pm.ActivityInfo;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Field;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import java.io.IOException;


import com.android.packagedb.ObjXmlOtpImpl2.AbstractObjXmlOpt;
import com.android.packagedb.ObjXmlOtpImpl2.ObjXmlOpt;

public class ActivityXmlOpt extends ObjXmlOpt {

    static final String ATTR_ARG1 = "a1";

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();
    int pkgRefId = -1;
    static {
        skipFields.add("metaData");
    }

    public ActivityXmlOpt() {
        mClass = Activity.class;
    }

    @Override
    public boolean needSaveRef() {
        return true;
    }

    @Override
    public Object createInstance(String suggestClass) {
        final Package pkg = (Package)Helper.getObjByRefId(pkgRefId);
        return new Activity(pkg, new ActivityInfo());
    }
    @Override
    public Object createInstance(Class suggestClass) {
        final Package pkg = (Package)Helper.getObjByRefId(pkgRefId);
        return new Activity(pkg, new ActivityInfo());
    }

    @Override
    public void collectInfoBeforeCreateInstance(XmlPullParser parser)
        throws XmlPullParserException,IOException {
        String pkgRefIdStr = parser.getAttributeValue(null, ATTR_ARG1);
        if (pkgRefIdStr != null) {
            pkgRefId = Integer.parseInt(pkgRefIdStr);
        } else {
            pkgRefId = -1;
        }
    }
    @Override
    public boolean serializeFieldsBefore(XmlSerializer serializer, Object obj)
        throws XmlPullParserException,IOException {
        Activity a = (Activity) obj;
        int refId = Helper.getRefIdByObj(a.owner);
        serializer.attribute(null,ATTR_ARG1,Integer.toString(refId));
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
