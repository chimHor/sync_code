
package com.android.packagedb;

import android.content.pm.PackageParser.Package;
import android.util.ArraySet;
import android.util.Log;
import android.os.PatternMatcher;

import java.lang.reflect.Field;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

import com.android.packagedb.ObjXmlOtpImpl2.AbstractObjXmlOpt.Helper;
import com.android.packagedb.ObjXmlOtpImpl2.AbstractObjXmlOpt;
import com.android.packagedb.ObjXmlOtpImpl2.ObjXmlOpt;

public class PatternMatcherXmlOpt extends ObjXmlOpt {

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();
    private String objPattern;
    private int objType;
    static {
    }

    public PatternMatcherXmlOpt() {
        mClass = PatternMatcher.class;
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
                String name = parser.getAttributeValue(null,ObjXmlOtpImpl2.ATTR_PARA);
                if (name == null) {
                    continue;
                }
                try {
                    if (subClassCode == ObjXmlOtpImpl2.INT_CODE) {
                        objType = Helper.parseInt(parser);
                    } else if (name.equals("mPattern")) {
                        objPattern = parser.nextText();
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
        return new PatternMatcher(objPattern, objType);
    }
    @Override
    public Object createInstance(Class suggestClass) {
        return new PatternMatcher(objPattern, objType);
    }

    @Override
    protected boolean handleSerializeSpField(Object o, Field field, String fieldName) {
        if (skipFields.contains(fieldName)) {
            return true;
        }
        return false;
    }
}