
package com.android.packagedb.util;

import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Activity;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Field;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import java.io.IOException;

import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.ObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt.Helper;

public class ActivityIntentInfoXmlOpt extends ObjXmlOpt {

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();

    static final String ATTR_ARG1 = "a1";
    int activityRefId = -1;
    static {
    	skipFields.add("activity");
    }

    public ActivityIntentInfoXmlOpt() {
        mClass = ActivityIntentInfo.class;
    }
    @Override
    public Object createInstance(String suggestClass) {
        final Activity a = (Activity)Helper.getObjByRefId(activityRefId);
        return new ActivityIntentInfo(a);
    }
    @Override
    public Object createInstance(Class suggestClass) {
        final Activity a = (Activity)Helper.getObjByRefId(activityRefId);
        return new ActivityIntentInfo(a);
    }

    @Override
    public void collectInfoBeforeCreateInstance(XmlPullParser parser)
        throws XmlPullParserException,IOException {
        String activityRefIdStr = parser.getAttributeValue(null, ATTR_ARG1);
        if (activityRefIdStr != null) {
            activityRefId = Integer.parseInt(activityRefIdStr);
        } else {
            activityRefId = -1;
        }
    }

    @Override
    public boolean serializeFieldsBefore(XmlSerializer serializer, Object obj)
        throws XmlPullParserException,IOException {
        ActivityIntentInfo aInfo = (ActivityIntentInfo) obj;
        int refId = Helper.getRefIdByObj(aInfo.activity);
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
