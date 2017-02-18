
package com.android.packagedb;

import android.content.pm.PackageParser;
import android.util.ArraySet;
import android.util.Log;
import android.util.Base64;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import java.io.IOException;
import java.lang.reflect.Field;

import java.security.PublicKey;
import com.android.packagedb.ObjXmlOtpImpl2.AbstractObjXmlOpt;
import com.android.packagedb.ObjXmlOtpImpl2.ObjXmlOpt;

public class PublicKeyXmlOpt extends ObjXmlOpt {

    public PublicKeyXmlOpt() {
        mClass = PublicKey.class;
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
    public Object parse(XmlPullParser parser, Object objParent, Field f)
        throws XmlPullParserException,IOException {
        try {
            String encodedPublicKey = parser.nextText();
            PublicKey pub = PackageParser.parsePublicKey(encodedPublicKey);
            if (objParent != null && f != null) {
                f.set(objParent, pub);
            }
            return pub;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void serialize(XmlSerializer serializer, Object obj, Field field)
        throws XmlPullParserException,IOException {
        try {
            PublicKey key = (PublicKey) obj;
            String encodedKey = new String(Base64.encode(key.getEncoded(), 0));
            Helper.saveTag(serializer, mTagName, encodedKey, field!=null?field.getName():null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
