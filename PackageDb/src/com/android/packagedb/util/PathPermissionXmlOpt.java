
package com.android.packagedb.util;

import android.content.pm.PackageParser.Package;
import android.content.pm.PathPermission;
import android.util.ArraySet;
import android.util.Log;
import android.os.PatternMatcher;

import java.lang.reflect.Field;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.ObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt.Helper;

public class PathPermissionXmlOpt extends ObjXmlOpt {

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();
    private String objPattern;
    private int objType;
    private String objReadPermission;
    private String objWritePermission;
    static {
    }

    public PathPermissionXmlOpt() {
        mClass = PathPermission.class;
    }

    @Override
    public void collectInfoBeforeCreateInstance(XmlPullParser parser)
        throws XmlPullParserException,IOException {
        int count = 0;
        final int innerDepth = parser.getDepth();
        int xmlType = 0;
        while ((count < 2) && (xmlType = parser.next()) != XmlPullParser.END_DOCUMENT
                && (xmlType != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
            if (xmlType == XmlPullParser.START_TAG && (parser.getDepth() == innerDepth + 1)) {
                String subClassCodeStr = parser.getName();
                count++;
                int subClassCode = Helper.tagNameToClassCode(subClassCodeStr);
                String name = parser.getAttributeValue(null,PkgSerializer.ATTR_PARA);
                if (name == null) {
                    continue;
                }
                try {
                    if (subClassCode == PkgSerializer.INT_CODE) {
                        objType = Helper.parseInt(parser);
                    } else if (name.equals("mPattern")) {
                        objPattern = parser.nextText();
                    } else if (name.equals("mReadPermission")) {
                    	objReadPermission = parser.nextText();
                    } else if (name.equals("mWritePermission")) {
                    	objWritePermission = parser.nextText();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        while (true) {
            if (((parser.getDepth()<=innerDepth + 1) &&
                parser.getEventType() == XmlPullParser.END_TAG) ||
                parser.getEventType() == XmlPullParser.END_DOCUMENT
                ) {
                break;
            }
            parser.next();
        }
    }

    @Override
    public Object createInstance(String suggestClass) {
    	return new PathPermission(objPattern, objType,objReadPermission,objWritePermission );

    }
    @Override
    public Object createInstance(Class suggestClass) {
    	return new PathPermission(objPattern, objType,objReadPermission,objWritePermission );
    }

    @Override
    protected boolean handleSerializeSpField(Object o, Field field, String fieldName) {
        if (skipFields.contains(fieldName)) {
            return true;
        }
        return false;
    }
}
