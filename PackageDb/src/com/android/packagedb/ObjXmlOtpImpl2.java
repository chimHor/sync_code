package com.android.packagedb;
import android.content.pm.PackageParser;

import java.io.Reader;
import java.io.Writer;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Modifier;

import android.util.ArrayMap;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;


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
    //for map key type
    static final String ATTR_KCLASS = "kc";
    //for map value type
    static final String ATTR_VCLASS = "kc";

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


    public static Object newInstance(Class c) {
        //to do
        return null;
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
    static final int MAPENTRY_CODE = 5;

    //static final int STRING_CODE = 5;


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
        new ArrayListXmlOpt(),
        new ArraySetXmlOpt(),
        new ArrayMapXmlOpt(),
        new MapEntryXmlOpt(),   //5

        };


    public static class ArrayXmlOpt extends AbstractObjXmlOpt {
        private ArrayList<Integer> arrayDim = new ArrayList<Integer>();
        private Class arrayItemType = null;

        public ArrayXmlOpt() {
            super(ARRAY_CODE);
        }

        private Class getClass(String className) {
            if ("int".equals(className)) return int.class;
            if ("boolean".equals(className)) return boolean.class;
            if ("long".equals(className)) return long.class;
            Class c = null;
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return c;
        }


        private void collectArrayInfo(XmlPullParser parser) {
            try {
                    int parserSize = Integer.valueOf(parser.getAttributeValue(null,ATTR_SIZE));
                    int xmlType;
                    arrayDim.add(parserSize);
                    final int depth = parser.getDepth();
                    while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT) {
                        // to case? <depth ,  end_tag & =depth
                        if (xmlType == XmlPullParser.END_TAG && parser.getDepth() <= depth) {
                            break;
                        }
                        if (xmlType == XmlPullParser.START_TAG && parser.getDepth()== depth+1) {
                            String classCodeStr = parser.getName();
                            int classCode = Integer.valueOf(classCodeStr);
                            if (classCode == ARRAY_CODE) {
                                collectArrayInfo(parser);
                            } else {
                                arrayItemType = getClass(parser.getAttributeValue(null,ATTR_CLASS));
                            }
                            break;
                        }
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private int checkClassAndSize(XmlPullParser parser, Object obj) {
            try {

                if (obj != null && obj.getClass().isArray()) {
                    int xmlType;
                    int objSize = Array.getLength(obj);
                    int parserSize = Integer.valueOf(parser.getAttributeValue(null,ATTR_SIZE));
                    if (objSize != parserSize) {
                        return Integer.MIN_VALUE;
                    }
                    arrayDim.add(parserSize);
                    final int depth = parser.getDepth();
                    while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT) {

                        if (xmlType == XmlPullParser.END_TAG && parser.getDepth() <= depth) {
                            break;
                        }

                        if (xmlType == XmlPullParser.START_TAG && parser.getDepth()== depth+1) {
                            String classCodeStr = parser.getName();
                            int classCode = Integer.valueOf(classCodeStr);
                            if (classCode == ARRAY_CODE) {
                                return checkClassAndSize(parser,Array.get(obj, 0))+1;
                            } else {
                                Object subObject = Array.get(obj, 0);
                                if (subObject != null && subObject.getClass().isArray()) {
                                    break;
                                }
                                // compare type?
                                Class itemClass = obj.getClass().getComponentType();
                                arrayItemType = getClass(parser.getAttributeValue(null,ATTR_CLASS));
                                if (itemClass.equals(arrayItemType)) {
                                    return 1;
                                }
                                arrayItemType = null;
                            }
                            break;
                        }
                    }
                }
                return Integer.MIN_VALUE;
            } catch (Exception e) {
                e.printStackTrace();
                return Integer.MIN_VALUE;
            }

        }

        private void parserArrayObj(XmlPullParser parser, Object obj, int topDepth, int lowDepth, int dim)
            throws XmlPullParserException,IOException {
            if (obj == null) { return;}
            int xmlType = parser.getEventType();
            do {
                if (xmlType == XmlPullParser.END_TAG && parser.getDepth() == topDepth) {
                    return;
                }
                if (xmlType == XmlPullParser.START_TAG && parser.getDepth() == lowDepth) {
                    break;
                }
            } while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT);

            int size = Array.getLength(obj);

            for (int i = 0; i < size; i++) {
                Object subObj = Array.get(obj,i);
                if (dim > 1) {
                    parserArrayObj(parser, subObj, topDepth, lowDepth, dim-1);
                } else {
                    do {
                        if (parser.getDepth() < lowDepth) {
                            break;
                        }
                        if (xmlType == XmlPullParser.START_TAG && parser.getDepth() == lowDepth) {
                            String subClassCodeStr = parser.getName();
                            int subClassCode = Integer.valueOf(subClassCodeStr);
                            if (subClassCode == INT_CODE) {
                                Array.setInt(subObj, i, parseInt(parser));
                            } else if (subClassCode == BOOLEAN_CODE) {
                                Array.setBoolean(subObj, i, parseBoolean(parser));
                            } else if (subClassCode == LONG_CODE) {
                                Array.setLong(subObj, i, parseLong(parser));
                            } else {
                                Object item = optArray[OBJ_CODE].parse(parser, null, null);
                                Array.set(subObj, i, item);
                            }
                        }
                    } while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT
                            && (xmlType != XmlPullParser.END_TAG || parser.getDepth() > lowDepth));
                    if (parser.getDepth() < lowDepth) {
                        break;
                    }
                }
            }
        }

        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {
            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }

            //todo : support 0 size array?
            int size = Integer.valueOf(parser.getAttributeValue(null,ATTR_SIZE));
            if (size < 1) {
                return null;
            }

            Object objT = null;
            Class objTClass = null;
            if ( objParent != null && f != null ) {
                objTClass = f.getType();
                try {
                    objT = f.get(objParent);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            final int topDepth = parser.getDepth();
            int xmlType = parser.getEventType();
            arrayDim.clear();
            arrayItemType = null;
            if ( objT != null) {
                checkClassAndSize(parser, objT);
            } else {
                collectArrayInfo(parser);
            }
            if (arrayItemType == null) {
                arrayDim.clear();
                do {
                    if (xmlType == XmlPullParser.END_TAG && parser.getDepth() == topDepth) {
                        break;
                    }
                } while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT);
                return null;
            } else {
                final int lowDepth = parser.getDepth();
                if (objT == null) {
                    int[] dims = new int[arrayDim.size()];
                    for (int i=0; i < dims.length; i++) {
                        dims[i] = arrayDim.get(i).intValue();
                    }
                    objT = Array.newInstance(arrayItemType, dims);
                }
                parserArrayObj(parser, objT, topDepth, lowDepth, arrayDim.size());
            }

            return objT;
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

            serializer.startTag(null, Integer.toString(mClassCode));
            serializer.attribute(null, ATTR_SIZE, Integer.toString(length));
            if (f != null) {
                serializer.attribute(null, ATTR_PARA, f.getName());
            }

            for (int i = 0; i < length; i++) {
                int itemClassCode = classToCode(itemC);
                if (itemClassCode < OBJ_CODE) {
                    if (itemClassCode == INT_CODE) {
                        saveTag(serializer, Integer.toString(INT_CODE), Integer.toString(Array.getInt(obj,i)));
                    } else if (itemClassCode == BOOLEAN_CODE) {
                        saveTag(serializer, Integer.toString(BOOLEAN_CODE), Boolean.toString(Array.getBoolean(obj,i)));
                    } else if (itemClassCode == LONG_CODE) {
                        saveTag(serializer, Integer.toString(LONG_CODE), Long.toString(Array.getLong(obj,i)));
                    }
                } else {
                    optArray[OBJ_CODE].serialize(serializer, Array.get(obj, i), null);
                }

            }
            serializer.endTag(null, Integer.toString(mClassCode));
        }
    }

    public static class ArrayListXmlOpt extends AbstractObjXmlOpt {
        public ArrayListXmlOpt() {
            super(ARRAYLIST_CODE);
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

            Object objT = null;
            Class objTClass = null;
            //to do : convert to helper func
            if ( objParent != null && f != null ) {
                objTClass = f.getType();
                try {
                    objT = f.get(objParent);
                    if (objT == null) {
                        objT = newInstance(objTClass);
                        f.set(objParent, objT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                try {
                String className = parser.getAttributeValue(null,ATTR_CLASS);
                objTClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
                if (objTClass == null) { return null; }
                objT = newInstance(objTClass);
                if (objT == null) { return null; }
            }

            Method addMethod;
            try {
                addMethod = ArrayList.class.getDeclaredMethod("add", Object.class);
            } catch (Exception e) {
                e.printStackTrace();
                return objT;
            }

            final int innerDepth = parser.getDepth();
            int xmlType;
            while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (xmlType != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (xmlType == XmlPullParser.START_TAG && parser.getDepth() == innerDepth + 1) {
                    Object item = optArray[OBJ_CODE].parse(parser, null, null);
                    try {
                        addMethod.invoke(objT, item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }

            return objT;
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

                serializer.startTag(null, Integer.toString(mClassCode));
                if (f != null) {
                    serializer.attribute(null,ATTR_PARA,f.getName());
                } else {
                    serializer.attribute(null,ATTR_CLASS, ArrayList.class.getName());
                }

                serializer.attribute(null,ATTR_SIZE,""+size);
                for (int i = 0 ; i < size; i++) {
                    optArray[OBJ_CODE].serialize(serializer, list.get(i), null);
                }
                serializer.endTag(null, Integer.toString(mClassCode));
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

            Object objT = null;
            Class objTClass = null;
            //to do : convert to helper func
            if ( objParent != null && f != null ) {
                objTClass = f.getType();
                try {
                    objT = f.get(objParent);
                    if (objT == null) {
                        objT = newInstance(objTClass);
                        f.set(objParent, objT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                try {
                String className = parser.getAttributeValue(null,ATTR_CLASS);
                objTClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
                if (objTClass == null) { return null; }
                objT = newInstance(objTClass);
                if (objT == null) { return null; }
            }

            Method addMethod;
            try {
                addMethod = ArraySet.class.getDeclaredMethod("add", Object.class);
            } catch (Exception e) {
                e.printStackTrace();
                return objT;
            }

            final int innerDepth = parser.getDepth();
            int xmlType;
            while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (xmlType != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (xmlType == XmlPullParser.START_TAG && parser.getDepth() == innerDepth + 1) {
                    Object item = optArray[OBJ_CODE].parse(parser, null, null);
                    try {
                        addMethod.invoke(objT, item);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }

            return objT;
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
                } else {
                    serializer.attribute(null,ATTR_CLASS, ArraySet.class.getName());
                }

                serializer.attribute(null,ATTR_SIZE,""+size);
                for (int i = 0 ; i < size; i++) {
                    optArray[OBJ_CODE].serialize(serializer, set.valueAt(i), null);
                }
                serializer.endTag(null, Integer.toString(mClassCode));
        }
    }

    public static class MapEntryXmlOpt extends AbstractObjXmlOpt {
        public MapEntryXmlOpt() {
            super(MAPENTRY_CODE);
        }
        public Object parse(XmlPullParser parser, Object objParent, Field f) {
            return null;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {
        }
    }



    public static class ArrayMapXmlOpt extends AbstractObjXmlOpt {
        public ArrayMapXmlOpt() {
            super(ARRAYMAP_CODE);
        }
        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {

            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }
            Object objT = null;
            Class objTClass = null;
            //to do : convert to helper func
            if ( objParent != null && f != null ) {
                objTClass = f.getType();
                try {
                    objT = f.get(objParent);
                    if (objT == null) {
                        objT = newInstance(objTClass);
                        f.set(objParent, objT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                try {
                String className = parser.getAttributeValue(null,ATTR_CLASS);
                objTClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
                if (objTClass == null) { return null; }
                objT = newInstance(objTClass);
                if (objT == null) { return null; }
            }

            Method putMethod;
            try {
                putMethod = ArraySet.class.getDeclaredMethod("put", Object.class, Object.class);
            } catch (Exception e) {
                e.printStackTrace();
                return objT;
            }

            final int innerDepth = parser.getDepth();
            int xmlType;
            boolean keyFlag = true;
            Object key = null;
            Object value = null;
            while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (xmlType != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (xmlType == XmlPullParser.START_TAG && parser.getDepth() == innerDepth + 1) {
                    if (keyFlag) {
                        key = optArray[OBJ_CODE].parse(parser, null, null);
                    } else {
                        value = optArray[OBJ_CODE].parse(parser, null, null);
                    }
                    if (keyFlag) {
                        keyFlag = false;
                        continue;
                    }
                    keyFlag = true;
                    try {
                        putMethod.invoke(objT, key, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
            return objT;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {

                ArrayMap map = (ArrayMap) obj;
                Set keySet = map.keySet();
                if (keySet.size() == 0) {
                    return;
                }
                serializer.startTag(null, Integer.toString(mClassCode));
                Iterator iterator = keySet.iterator();
                while (iterator.hasNext()) {
                    Object key = iterator.next();
                    Object value = map.get(key);
                    optArray[OBJ_CODE].serialize(serializer, key, null);
                    optArray[OBJ_CODE].serialize(serializer, value, null);
                }
                serializer.endTag(null, Integer.toString(mClassCode));
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


        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {

            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }
            String classCodeStr = parser.getName();
            int classCode = Integer.valueOf(classCodeStr);
            if (classCode > OBJ_CODE ) {
                return optArray[classCode].parse(parser, objParent ,f);
            }

            Object objT = null;
            Class objTClass = null;
            if ( objParent != null && f != null) {
                objTClass = f.getType();
                try {
                    objT = f.get(objParent);
                    if (objT == null) {
                        objT = newInstance(objTClass);
                        f.set(objParent, objT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                try {
                String className = parser.getAttributeValue(null,ATTR_CLASS);
                objTClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
                if (objTClass == null) { return null; }
                objT = newInstance(objTClass);
                if (objT == null) { return null; }
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
            if (obj == null)  { return; }
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
