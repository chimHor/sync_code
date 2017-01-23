package com.android.packagedb;
import android.content.pm.PackageParser;

import java.io.Reader;
import java.io.Writer;
import java.io.StringWriter;
import java.io.StringReader;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import java.util.Map;
import android.util.ArrayMap;


import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import com.android.internal.util.FastXmlSerializer;


import android.util.Log;

class XmlPkgSerializer {

    private static void saveTag(XmlSerializer xs, String name, String value) {
        if (value != null && value.length()!=0) {
        try {
        xs.startTag(null, name).text(value).endTag(null, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    }

    private static void arrayListSerializer(XmlSerializer xs, Field f, Object t) {
        Type genericFieldType =  f.getGenericType();
        if (genericFieldType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) genericFieldType;
            Type[] argTypes = pType.getActualTypeArguments();
            if (argTypes.length < 1)
                return;
            Log.w("xxx", "mem: "+ f.getName()+ "  argType : " + ((Class)argTypes[0]).getName());
        }
        //xs.startTag(null, f.getName());

        //xs.endTag(null, f.getName());
    }
    private static void comomSerializer(XmlSerializer xs, Object t) {
        try {

        Class c = t.getClass();
        Field[] fields = c.getFields();
        for (Field f : fields) {
            if (f.getType().equals(int.class)) {
                saveTag(xs, f.getName(), Integer.toString(f.getInt(t)));
            } else if (f.getType().equals(boolean.class)) {
                saveTag(xs, f.getName(), Boolean.toString(f.getBoolean(t)));
            } else if (f.getType().equals(String.class)) {
                saveTag(xs, f.getName(), (String) f.get(t));
            } else if (f.getType().equals(long.class)) {
                saveTag(xs, f.getName(), Long.toString(f.getLong(t)));
            } else if (f.getType().equals(java.util.ArrayList.class)) {
                arrayListSerializer(xs, f, t);
            } else {
                //todo: other class type

            }
        }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void comomParser(XmlPullParser parser, Object t) {
        try {

        Class c = t.getClass();
        Field[] fields = c.getFields();

        final int innerDepth = parser.getDepth();
        int xmlType;
        while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT
                && (xmlType != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
            if (xmlType == XmlPullParser.END_TAG || xmlType == XmlPullParser.TEXT) {
                continue;
            }

            String tagName = parser.getName();
            for (Field f : fields) {
                if (!tagName.equals(f.getName())) {
                    continue;
                }
                if (f.getType().equals(int.class)) {
                    f.setInt(t,Integer.valueOf(parser.nextText()));
                } else if (f.getType().equals(boolean.class)) {
                    f.setBoolean(t,Boolean.valueOf(parser.nextText()));
                } else if (f.getType().equals(String.class)) {
                    f.set(t,(Object)(parser.nextText()));
                } else if (f.getType().equals(long.class)) {
                    f.setLong(t,Long.valueOf(parser.nextText()));
                } else if (f.getType().equals(java.util.ArrayList.class)) {


                } else {
                    //todo: other class type
                }
            }
        }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    XmlPullParserFactory xppFactory;
    XmlPullParser xpp;
    XmlSerializer xs;

    public PackageParser.Package parsePkg(String value) {
        PackageParser.Package pkg = new PackageParser.Package("abc");
        try {
        if (xpp == null) {
            if (xppFactory == null) {
                xppFactory = XmlPullParserFactory.newInstance();
            }
            xpp = xppFactory.newPullParser();
        }
        java.io.StringReader reader = new StringReader(value);
        xpp.setInput(reader);
        xpp.nextTag();
        if (xpp.getName().equals("Package")) {
            comomParser(xpp,(Object) pkg);
        } else
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pkg;
    }


    public String serializerPkg(PackageParser.Package pkg) {
        StringWriter writer = new StringWriter();
        try {
        if (xs == null) {
            xs = new FastXmlSerializer();
        }
        xs.setOutput(writer);
        xs.startDocument(null,true);
        xs.startTag(null, "Package");
        comomSerializer(xs, (Object)pkg);
        xs.endTag(null, "Package");
        xs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writer.toString();
    }


}
