
package com.android.packagedb.util;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.android.packagedb.util.PkgSerializer.ObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt.Helper;
import com.android.packagedb.util.SerializableObject.BytesWraper;

public class CertificateXmlOpt extends ObjXmlOpt {
    static Constructor t = null;
    static CertificateFactory cf = null;
    {
        try {
        cf = CertificateFactory.getInstance("X.509");
        Class c = org.apache.harmony.security.utils.JarUtils.class;
        Class[] cs = c.getDeclaredClasses();
        Class c2 = null;
        for (Class c1 : cs) {
            if (c1.getName().contains("VerbatimX509Certificate")) {
                c2 = c1;
                break;
            }
        }
        if (c2 != null) {
            t = c2.getConstructor(X509Certificate.class, byte[].class);
            if (t != null) {
                t.setAccessible(true);
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CertificateXmlOpt() {
        mClass = Certificate.class;
    }

    @Override
    public Object parse(XmlPullParser parser, Object objParent, Field f)
        throws XmlPullParserException,IOException {
        if (t == null)
            return null;

        try {
        int index = Integer.valueOf(parser.nextText());
        BytesWraper w = Helper.getSerializableObject().wraperList.get(index);
        final InputStream is = new ByteArrayInputStream(w.content);
        Certificate c = (Certificate) t.newInstance(cf.generateCertificate(is), w.content);
        if (objParent != null && f != null) {
            f.set(objParent, c);
        }

        return c;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void serialize(XmlSerializer serializer, Object obj, Field field)
        throws XmlPullParserException,IOException {
        try {
        Certificate c = (Certificate) obj;
        BytesWraper w = new BytesWraper(c.getEncoded());
        int index = Helper.getSerializableObject().wraperList.size();
        Helper.getSerializableObject().wraperList.add(w);
        Helper.saveTag(serializer, mTagName, Integer.toString(index),field!=null?field.getName():null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
