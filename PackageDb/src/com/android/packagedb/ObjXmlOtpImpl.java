package com.android.packagedb;
import android.content.pm.PackageParser;

import java.io.Reader;
import java.io.Writer;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Modifier;

import android.util.ArrayMap;
import android.util.ArraySet;

import java.util.ArrayList;


import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import com.android.internal.util.FastXmlSerializer;


import android.util.Log;

class ObjXmlOtpImpl {
    static final boolean DEBUG = true;
    static final String TAG = "ObjXmlOtpImpl";

    private static void saveTag(XmlSerializer serializer, String name, String value) {
        if (value != null && value.length()!=0) {
        try {
        serializer.startTag(null, name).text(value).endTag(null, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    }

    private static void arrayListSerializer(XmlSerializer serializer, Object t, Field f)
        throws XmlPullParserException,IOException {
        Type genericFieldType =  f.getGenericType();
        if (genericFieldType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) genericFieldType;
            Type[] argTypes = pType.getActualTypeArguments();
            if (argTypes.length < 1)
                return;
            //if (DEBUG)
            //    Log.w(TAG, "serializer member: "+ f.getName()+ "  argType : " + ((Class)argTypes[0]).getName());
        } else {
            return;
        }
        ArrayList list = (ArrayList) t;
        int size = list.size();
        if (DEBUG) {
            Log.w(TAG, "serializer member: "+ f.getName()+ "  size : " + size);
        }
        if (size == 0) {
            return ;
        }
        serializer.startTag(null, f.getName());
        serializer.attribute(null,"size",""+size);
        for (int i = 0 ; i < size; i++) {
            serialize(serializer, list.get(i), null);
        }
        serializer.endTag(null, f.getName());
    }


    private static void arraySetSerializer(XmlSerializer serializer, Object t, Field f)
        throws XmlPullParserException,IOException {
        Type genericFieldType =  f.getGenericType();
        if (genericFieldType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) genericFieldType;
            Type[] argTypes = pType.getActualTypeArguments();
            if (argTypes.length < 1)
                return;
            //if (DEBUG)
            //    Log.w(TAG, "serializer member: "+ f.getName()+ "  argType : " + ((Class)argTypes[0]).getName());
        }
        ArraySet set = (ArraySet) t;
        int size = set.size();
        if (DEBUG) {
            Log.w(TAG, "serializer member: "+ f.getName()+ "  size : " + size);
        }
        if (size == 0) {
            return;
        }
        serializer.startTag(null, f.getName());
        serializer.attribute(null,"size",""+size);
        for (int i = 0 ; i < size; i++) {
            serialize(serializer, set.valueAt(i), null);
        }
        serializer.endTag(null, f.getName());
    }

    private static void arraySerializer(XmlSerializer serializer, Object t, Field f) {
//        String typeStr = f.getType().getName();

    }

    private static void arrayMapSerializer(XmlSerializer serializer, Object t, Field f) {
        if (DEBUG)
            Log.w(TAG, "serializer skip map type member: "+ f.getName() + "("+ f.getType().getName() + ")");
    }


    public static void serialize(XmlSerializer serializer, Object t, Field field) {
        try {

            Class c = t.getClass();
            if (field == null) {
                serializer.startTag(null, c.getName());
            } else {
                serializer.startTag(null, field.getName());
            }

            if (c.equals(java.util.ArrayList.class)) {
                arrayListSerializer(serializer, t ,field);
            } else if (c.equals(android.util.ArraySet.class)) {
                arraySetSerializer(serializer, t ,field);
            } else if (c.equals(android.util.ArrayMap.class)) {
                arrayMapSerializer(serializer, t ,field);
            } else if (c.isArray()) {
                arraySerializer(serializer, t ,field);
            } else if (c.equals(String.class)) {
                serializer.text((String) t);
            } else if (c.equals(Integer.class)) {
                serializer.text(((Integer)t).toString());
            } else if (c.equals(Long.class)) {
                serializer.text(((Long)t).toString());
            } else if (c.equals(Boolean.class)) {
                serializer.text(((Boolean)t).toString());
            } else {
                Field[] fields = c.getFields();
                for (Field f : fields) {
                    //base type
                    int mode = f.getModifiers();
                    if (Modifier.isFinal(mode) && Modifier.isStatic(mode)) {
                        if (DEBUG)
                            Log.w(TAG, c.getName()+" field:" + f.getName() + " is FINAL and STATIC");
                        continue;
                    } else if (Modifier.isFinal(mode)) {
                        if (DEBUG)
                            Log.w(TAG, c.getName()+" field:" + f.getName() + " is FINAL");
                    } else if (Modifier.isStatic(mode)) {
                        if (DEBUG)
                            Log.w(TAG, c.getName()+" field:" + f.getName() + " is STATIC");
                        continue;
                    }
                    if (f.getType().equals(int.class)) {
                        saveTag(serializer, f.getName(), Integer.toString(f.getInt(t)));
                    } else if (f.getType().equals(boolean.class)) {
                        saveTag(serializer, f.getName(), Boolean.toString(f.getBoolean(t)));
                    } else if (f.getType().equals(long.class)) {
                        saveTag(serializer, f.getName(), Long.toString(f.getLong(t)));
                    }

                    Object m = f.get(t);
                    if (m == null) {
                        continue;
                    }

                    if (f.getType().equals(String.class)) {
                        saveTag(serializer, f.getName(), (String) m);
                    } else if (f.getType().equals(java.util.ArrayList.class)) {
                        arrayListSerializer(serializer, m, f);
                    } else if (f.getType().equals(android.util.ArraySet.class)) {
                        arraySetSerializer(serializer, m, f);
                    } else if (f.getType().equals(android.util.ArrayMap.class)) {
                        arrayMapSerializer(serializer, m, f);
                    } else if (f.getType().isArray()) {
                        arraySerializer(serializer, m, f);
                    } else {
                        //todo: other class type

                    }
                }
            }
            if (field == null) {
                serializer.endTag(null, c.getName());
            } else {
                serializer.endTag(null, field.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static int parseInt(XmlPullParser parser)
        throws XmlPullParserException,IOException {
        return Integer.valueOf(parser.nextText());
    }
    private static boolean parseBoolean(XmlPullParser parser)
        throws XmlPullParserException,IOException {
        return Boolean.valueOf(parser.nextText());
    }
    private static long parseLong(XmlPullParser parser)
        throws XmlPullParserException,IOException {
        return Long.valueOf(parser.nextText());
    }

    private static String parseString(XmlPullParser parser)
        throws XmlPullParserException,IOException {
        String s = new String(parser.nextText());
        return s;
    }

    public static Object parse(XmlPullParser parser, Object t, Class c) {
        try {
        if (t == null) {
            if (c == null) {
                return null;
            }
            t = c.newInstance();
        }

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
                    f.setInt(t, parseInt(parser));
                } else if (f.getType().equals(boolean.class)) {
                    f.setBoolean(t, parseBoolean(parser));
                } else if (f.getType().equals(String.class)) {
                    f.set(t, (Object)(parseString(parser)));
                } else if (f.getType().equals(long.class)) {
                    f.setLong(t, parseLong(parser));
                } else if (f.getType().equals(java.util.ArrayList.class)) {


                } else {
                    //todo: other class type
                }
            }
        }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return t;
    }

// wrapper
    XmlPullParserFactory xppFactory;
    XmlPullParser xpp;
    XmlSerializer xs;


    public PackageParser.Package parsePkg(String value) {
        PackageParser.Package pkg = new PackageParser.Package("");
        try {
        if (xpp == null) {
            if (xppFactory == null) {
                xppFactory = XmlPullParserFactory.newInstance();
            }
            xpp = xppFactory.newPullParser();
        }
        java.io.StringReader reader = new StringReader(value);
        xpp.setInput(reader);
        parse(xpp,(Object) pkg, PackageParser.Package.class);
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
        serialize(xs, (Object)pkg, null);
        xs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

}
