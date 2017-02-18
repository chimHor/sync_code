
package com.android.packagedb;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import android.util.Log;
import android.content.pm.ManifestDigest;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import java.io.IOException;

import com.android.packagedb.ObjXmlOtpImpl2.AbstractObjXmlOpt.Helper;
import com.android.packagedb.ObjXmlOtpImpl2.ObjXmlOpt;
import com.android.packagedb.SerializableObject.BytesWraper;

public class ManifestDigestXmlOpt extends ObjXmlOpt {
    static Constructor t = null;
    static Field f = null;
    {
        try {
        Class c = android.content.pm.ManifestDigest.class;
        f = c.getDeclaredField("mDigest");
        if (f != null) {
            f.setAccessible(true);
        }
        t = c.getConstructor(byte[].class);
        if (t != null) {
            t.setAccessible(true);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ManifestDigestXmlOpt() {
        mClass = ManifestDigest.class;
    }

    @Override
    public Object parse(XmlPullParser parser, Object objParent, Field f)
        throws XmlPullParserException,IOException {
        try{
        int index = Integer.valueOf(parser.nextText());
        BytesWraper w = Helper.getSerializableObject().wraperList.get(index);

        ManifestDigest m = (ManifestDigest) t.newInstance(w.content);
        //ManifestDigest m = new ManifestDigest(w.content);
        if (objParent != null && f != null) {
            f.set(objParent, m);
        }
        return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void serialize(XmlSerializer serializer, Object obj, Field field)
        throws XmlPullParserException,IOException {
        try{
        ManifestDigest m = (ManifestDigest) obj;
        BytesWraper w = new BytesWraper((byte[])f.get(m));
        Helper.getSerializableObject().wraperList.add(w);
        int index = Helper.getSerializableObject().wraperList.size();
        Helper.saveTag(serializer, mTagName, Integer.toString(index),field!=null?field.getName():null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
