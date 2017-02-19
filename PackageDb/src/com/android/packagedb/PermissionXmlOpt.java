
package com.android.packagedb;

import android.content.pm.PackageParser.Permission;
import android.content.pm.PackageParser.Package;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Field;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import java.io.IOException;

import com.android.packagedb.ObjXmlOtpImpl2.AbstractObjXmlOpt.Helper;
import com.android.packagedb.ObjXmlOtpImpl2.AbstractObjXmlOpt;
import com.android.packagedb.ObjXmlOtpImpl2.ObjXmlOpt;

public class PermissionXmlOpt extends ObjXmlOpt {

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();

    static final String ATTR_ARG1 = "a1";

    int pkgRefId = -1;
    static {
    }

    public PermissionXmlOpt() {
        mClass = Permission.class;
    }
    @Override
    public Object createInstance(String suggestClass) {
        return new Permission((Package)Helper.getObjByRefId(pkgRefId));
    }
    @Override
    public Object createInstance(Class suggestClass) {
        return new Permission((Package)Helper.getObjByRefId(pkgRefId));
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
        Permission a = (Permission) obj;
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
