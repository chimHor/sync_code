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

class ObjXmlOtpImpl2 {

    static final boolean DEBUG = true;
    static final String TAG = "ObjXmlOtpImpl2";

    static final String ATTR_PARA = "p";
    static final String ATTR_SIZE = "s";
    static final String ATTR_CLASS = "c";

    //helper
    public static void saveTag(XmlSerializer serializer, String name, String value) {
        if (value != null && value.length()!=0) {
            try {
                serializer.startTag(null, name).text(value).endTag(null, name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static abstract class AbstractObjXmlOpt  {
        public int mClassCode;
        public AbstractObjXmlOpt(int i) { mClassCode = i;}
        public abstract Object parse(XmlPullParser parser, Object obj, Field f) throws XmlPullParserException,IOException ;
        public abstract void serialize(XmlSerializer serializer, Object obj, Field f) throws XmlPullParserException,IOException ;
    }

    static final int OBJ_CODE = 0;
    static final int ARRAY_CODE = 1;
    static final int ARRAYLIST_CODE = 2;
    static final int ARRAYSET_CODE = 3;
    static final int ARRAYMAP_CODE = 4;
    static final int STRING_CODE = 5;


    static final int INT_CODE = OBJ_CODE - 1;
    static final int BOOLEAN_CODE = OBJ_CODE - 2;
    static final int LONG_CODE = OBJ_CODE - 3;

    public static int classToCode(Class c) {
        if (c.equals(int.class)) {
            return INT_CODE;
        } else if (c.equals(boolean.class)) {
            return BOOLEAN_CODE;
        } else if (c.equals(long.class)) {
            return LONG_CODE;
        } else if (c.equals(java.util.ArrayList.class)) {
            return ARRAYLIST_CODE;
        } else if (c.equals(android.util.ArraySet.class)) {
            return ARRAYSET_CODE;
        } else if (c.equals(android.util.ArrayMap.class)) {
            return ARRAYMAP_CODE;
        } else if (c.isArray()) {
            return ARRAY_CODE;
        } else {
            return OBJ_CODE;
        }
    }
    public static Class codeToClass(int i) {
        return Object.class;
    }

    static final AbstractObjXmlOpt[] optArray = {
        new ObjXmlOpt(),   //0
        new ArrayXmlOpt(),
        };


    public static class ArrayXmlOpt extends AbstractObjXmlOpt {
        public ArrayXmlOpt() {
            super(ARRAY_CODE);
        }

        public Object parse(XmlPullParser parser, Object obj, Field f)
            throws XmlPullParserException,IOException {
            return null;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {
                //String typeStr = f.getType().getName();
                if (obj == null) {
                    return;
                }
                int length = Array.getLength(obj);
                if (length == 0) {
                    return;
                }
                Class c = obj.getClass();
                Class itemC = c.getComponentType();

                if (f != null) {
                    serializer.startTag(null, f.getName());
                } else {
                    serializer.startTag(null, "Array");
                }

                for (int i = 0; i < length; i++) {
                    if (itemC.equals(int.class)) {
                        saveTag(serializer, "int", Integer.toString(Array.getInt(obj,i)));
                    } else if (itemC.equals(boolean.class)) {
                        saveTag(serializer, "boolean", Boolean.toString(Array.getBoolean(obj,i)));
                    } else if (itemC.equals(long.class)) {
                        saveTag(serializer, "long", Long.toString(Array.getLong(obj,i)));
                    } else {
                        optArray[OBJ_CODE].serialize(serializer, Array.get(obj,i), null);
                    }
                }

                if (f != null) {
                    serializer.endTag(null, f.getName());
                } else {
                    serializer.endTag(null, "Array");
                }

        }
    }

    public static class ArrayListXmlOpt extends AbstractObjXmlOpt {
        public ArrayListXmlOpt() {
            super(ARRAYLIST_CODE);
        }
        public Object parse(XmlPullParser parser, Object obj, Field f)
            throws XmlPullParserException,IOException {
            return null;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
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
                ArrayList list = (ArrayList) obj;
                int size = list.size();
                if (DEBUG) {
                    Log.w(TAG, "serializer member: "+ f.getName()+ "  size : " + size);
                }
                if (size == 0) {
                    return ;
                }
                //serializer.startTag(null, f.getName());
                if (f != null) {
                    serializer.startTag(null, f.getName());
                } else {
                    serializer.startTag(null, "ArrayList");
                }


                serializer.attribute(null,ATTR_SIZE,""+size);
                for (int i = 0 ; i < size; i++) {
                    optArray[OBJ_CODE].serialize(serializer, list.get(i), null);
                }
                //serializer.endTag(null, f.getName());
                if (f != null) {
                    serializer.endTag(null, f.getName());
                } else {
                    serializer.endTag(null, "ArrayList");
                }
        }
    }


    public static class ArraySetXmlOpt extends AbstractObjXmlOpt {
        public ArraySetXmlOpt() {
            super(ARRAYSET_CODE);
        }
        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {
            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }

            int size = Integer.valueOf(parser.getAttributeValue(null,ATTR_SIZE));
            if (size < 1) {
                return null;
            }

            Object objT;
            Class objTClass = f.getType();
            try {
                objT = f.get(objParent);
                if (objT == null) {
                    objT = objTClass.newInstance();
                    f.set(objParent, objT);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return null;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
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
                ArraySet set = (ArraySet) obj;
                int size = set.size();
                if (DEBUG) {
                    Log.w(TAG, "serializer member: "+ f.getName()+ "  size : " + size);
                }
                if (size == 0) {
                    return;
                }
                serializer.startTag(null, Integer.toString(mClassCode));
                if (f != null) {
                    serializer.attribute(null,ATTR_PARA,f.getName());
                }

                serializer.attribute(null,ATTR_SIZE,""+size);
                for (int i = 0 ; i < size; i++) {
                    optArray[OBJ_CODE].serialize(serializer, set.valueAt(i), null);
                }
                serializer.endTag(null, Integer.toString(mClassCode));
        }
    }

    public static class ArrayMapXmlOpt extends AbstractObjXmlOpt {
        public ArrayMapXmlOpt() {
            super(ARRAYMAP_CODE);
        }
        public Object parse(XmlPullParser parser, Object obj, Field f)
            throws XmlPullParserException,IOException {
            return null;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {
        }
    }

/*
    public static class ArrayXmlOpt extends AbstractObjXmlOpt {
        public ArrayXmlOpt() {
            super(ARRAY_CODE);
        }
        public Object parse(XmlPullParser parser, Object obj, Field f) {
            return null;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {
        }
    }

*/


    public static class ObjXmlOpt extends AbstractObjXmlOpt {
        public ObjXmlOpt() {
            super(OBJ_CODE);
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


        public Object parse(XmlPullParser parser, Object obj, Field f)
            throws XmlPullParserException,IOException {

            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }
            String classCodeStr = parser.getName();
            int classCode = Integer.valueOf(classCodeStr);
            if (classCode > OBJ_CODE ) {
                return optArray[classCode].parse(parser, obj ,f);
            }

            Object objT = null;
            Class objTClass = null;
            if ( f != null) {
                objTClass = f.getType();
                try {
                    objT = f.get(obj);
                    if (objT == null) {
                        objT = objTClass.newInstance();
                        f.set(obj, objT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                objTClass = codeToClass(classCode);
            }

            final int innerDepth = parser.getDepth();
            int xmlType;
            while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (xmlType != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {

                if (xmlType == XmlPullParser.START_TAG) {
                    String subClassCodeStr = parser.getName();
                    int subClassCode = Integer.valueOf(subClassCodeStr);
                    String name = parser.getAttributeValue(null,ATTR_PARA);
                    if (name == null) {
                        continue;
                    }
                    Field subField = null;
                    try {
                        subField = objTClass.getField(name);
                        if (subField == null) {
                            continue;
                        }

                        if (subClassCode == INT_CODE) {
                            subField.setInt(objT, parseInt(parser));
                        } else if (subClassCode == BOOLEAN_CODE) {
                            subField.setBoolean(objT, parseBoolean(parser));
                        } else if (subClassCode == LONG_CODE) {
                            subField.setLong(objT, parseLong(parser));
                        } else {
                            optArray[classCode].parse(parser, objT ,subField);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return objT;
        }

         public void serialize(XmlSerializer serializer, Object obj, Field field)
            throws XmlPullParserException,IOException {
            try {
                Class c = obj.getClass();
                int classCode = classToCode(c);
                if (classCode > OBJ_CODE) {
                    optArray[classCode].serialize(serializer, obj ,field);
                } else if (classCode < OBJ_CODE) {
                    return;
                } else {
                    serializer.startTag(null, Integer.toString(classCode));

                    if (field != null){
                        serializer.attribute(null,ATTR_PARA,field.getName());
                    } else {
                        serializer.attribute(null,ATTR_CLASS,c.getName());
                    }
                    /*
                    if (c.equals(String.class)) {
                        serializer.text((String) obj);
                    } else if (c.equals(Integer.class)) {
                        serializer.text(((Integer)obj).toString());
                    } else if (c.equals(Long.class)) {
                        serializer.text(((Long)obj).toString());
                    } else if (c.equals(Boolean.class)) {
                        serializer.text(((Boolean)obj).toString());
                    } else {
                    }
                    */

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
                        Class subClass = f.getType();
                        int subClassCode = classToCode(subClass);

                        if (subClassCode < OBJ_CODE) {
                            if (subClassCode == INT_CODE) {
                                saveTag(serializer, Integer.toString(INT_CODE), Integer.toString(f.getInt(obj)));
                            } else if (subClassCode == BOOLEAN_CODE) {
                                saveTag(serializer, Integer.toString(BOOLEAN_CODE), Boolean.toString(f.getBoolean(obj)));
                            } else if (subClassCode == LONG_CODE) {
                                saveTag(serializer, Integer.toString(LONG_CODE), Long.toString(f.getLong(obj)));
                            }
                        } else {

                            Object subObj = f.get(obj);
                            if (subObj == null) {
                                continue;
                            }
                            optArray[classCode].serialize(serializer, subObj, f);
                        }
                    }

                    serializer.endTag(null, Integer.toString(classCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

// wrapper
    XmlPullParserFactory xppFactory;
    XmlPullParser xpp;
    XmlSerializer xs;


    public PackageParser.Package parsePkg(String value) {
        PackageParser.Package pkg = new PackageParser.Package("");
        /*
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
        */
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
        optArray[OBJ_CODE].serialize(xs, (Object)pkg, null);
        xs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

}
