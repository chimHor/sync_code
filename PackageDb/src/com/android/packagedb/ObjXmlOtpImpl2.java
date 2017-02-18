package com.android.packagedb;
//import android.content.pm.PackageParser;

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

public class ObjXmlOtpImpl2 {

    static final boolean DEBUG = true;
    static final String TAG = "ObjXmlOtpImpl2";

    static final String ATTR_PARA = "p";
    static final String ATTR_SIZE = "s";
    static final String ATTR_CLASS = "c";

    static final String ATTR_OID = "d";
    //for map key type
    static final String ATTR_KCLASS = "kc";
    //for map value type
    static final String ATTR_VCLASS = "vc";



    //helper
   /*
   public static Object newInstance(Class c) {
        //to do
        Object o = null;
        try {
            o = c.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }
    */
    public static abstract class AbstractObjXmlOpt  {
        public int mClassCode;
        public String mTagName;
        public Class mClass;

        public abstract Object parse(XmlPullParser parser, Object obj, Field f) throws XmlPullParserException,IOException ;
        public abstract void serialize(XmlSerializer serializer, Object obj, Field f) throws XmlPullParserException,IOException ;
        public Object createInstance(String suggestClass) { return null; }
        public Object createInstance(Class suggestClass) { return null; }

        public static class Helper {

            public static ArrayMap<Integer, Object> mmap = new ArrayMap<Integer, Object>();

            public static void saveObjRefId(XmlSerializer serializer, Object o)
                throws XmlPullParserException,IOException {
                serializer.attribute(null,ATTR_OID ,Integer.toString(mmap.size()));
                mmap.put(mmap.size(), o);
            }
            public static int getRefIdByObj(Object obj) {
                Set<Integer> keySet = mmap.keySet();
                Iterator<Integer> iterator = keySet.iterator();
                while (iterator.hasNext()) {
                    Integer i = iterator.next();
                    if (obj == mmap.get(iterator.next()))
                        return i;
                }
                return -1;
            }
            public static int getObjRefIdFromParser(XmlPullParser parser) {
                String idStr = parser.getAttributeValue(null, ATTR_OID);
                if (idStr == null) {
                    return -1;
                }
                else {
                    return Integer.valueOf(idStr);
                }
            }

            public static void recordObjRef(Object obj, int id) {
                mmap.put(id, obj);
            }
            public static Object getObjByRefId(int id) {
                return mmap.get(id);
            }

            public static SerializableObject oo = new SerializableObject();
            public static SerializableObject getSerializableObject() {return oo;}

            /////////////////////////////////////////
            static final int BASE_POINT = 0xb0;

            public static int tagNameToClassCode(String tagName) {
                int code = Integer.parseInt(tagName,16);
                return code - BASE_POINT;
            }
            public static String classCodeToTagName(int classCode) {
                return Integer.toString(classCode + BASE_POINT, 16);
            }

            public static void saveTag(XmlSerializer serializer, String name, String value, String paraName) {
                if (value != null && value.length()!=0) {
                    try {
                        serializer.startTag(null, name);
                        if (paraName != null) {
                            serializer.attribute(null,ATTR_PARA,paraName);
                        }
                        serializer.text(value).endTag(null, name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            public static int parseInt(XmlPullParser parser)
                throws XmlPullParserException,IOException {
                    return Integer.valueOf(parser.nextText());
                }
            public static boolean parseBoolean(XmlPullParser parser)
                throws XmlPullParserException,IOException {
                    return Boolean.valueOf(parser.nextText());
                }
            public static long parseLong(XmlPullParser parser)
                throws XmlPullParserException,IOException {
                    return Long.valueOf(parser.nextText());
                }
            public static float parseFloat(XmlPullParser parser)
                throws XmlPullParserException,IOException {
                    return Float.intBitsToFloat(Integer.valueOf(parser.nextText()));
                }

            public static Object getInstance(AbstractObjXmlOpt opt, XmlPullParser parser, Object objParent, Field f) throws XmlPullParserException {

                Object objT = null;
                Class objTClass = null;
                if ( objParent != null && f != null ) {
                    objTClass = f.getType();
                    try {
                        objT = f.get(objParent);
                        if (objT == null && (!Modifier.isFinal(f.getModifiers()))) {
                            objT = opt.createInstance(objTClass);
                            f.set(objParent, objT);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    String className = parser.getAttributeValue(null,ATTR_CLASS);
                    if (className != null) {
                        objT = opt.createInstance(className);
                    }
                }
                return objT;
            }
            public static int classToCode(Class c) {
                if (c == null)
                    return Integer.MIN_VALUE;

                if (c.equals(int.class)) {
                    return INT_CODE;
                } else if (c.equals(boolean.class)) {
                    return BOOLEAN_CODE;
                } else if (c.equals(long.class)) {
                    return LONG_CODE;
                } else if (c.equals(float.class)) {
                    return FLOAT_CODE;
                } else if (c.isArray()) {
                    return ARRAY_CODE;
                }

                if (c.equals(CharSequence.class)) {
                    c = String.class;
                }

                for (int i = 2; i < optArray.length; i++) {
                    if (c.equals(optArray[i].mClass))
                        return optArray[i].mClassCode;
                }
                return OBJ_CODE;
            }

        }

    }


    static final int OBJ_CODE = 0;
    //todo : better way?
    static final int ARRAY_CODE = 1;

    /*
    static final int ARRAYLIST_CODE = 2;
    static final int ARRAYSET_CODE = 3;
    static final int ARRAYMAP_CODE = 4;
    static final int STRING_CODE = 5;
    */

    static final int INT_CODE = OBJ_CODE - 1;
    static final int BOOLEAN_CODE = OBJ_CODE - 2;
    static final int LONG_CODE = OBJ_CODE - 3;
    static final int FLOAT_CODE = OBJ_CODE - 4;


    static final AbstractObjXmlOpt[] optArray = {
        new ObjXmlOpt(),   //0, not allow move
        new ArrayXmlOpt(), //1, not allow move
        new ArrayListXmlOpt(),
        new ArraySetXmlOpt(),
        new ArrayMapXmlOpt(),
        new StringXmlOpt(),
        new IntegerXmlOpt(),
        new FloatXmlOpt(),
        new LongXmlOpt(),
        new BooleanXmlOpt(),
        new ApplicationInfoOpt(),

        // AbstractObjXmlOpt.Helper.BASE_POINT is 0xb0 , so the optArray size must less than 0xff- 0xb0
        // if BASE_POINT is 0xb00, optArray size must less than 0xfff- 0xb00
        };

    static {
        Log.w(TAG, "init optArray size:"+optArray.length);
        for (int i = 0; i < optArray.length ; i++) {
            optArray[i].mClassCode = i;
            optArray[i].mTagName = AbstractObjXmlOpt.Helper.classCodeToTagName(i);
        }
    }


    public static class ArrayXmlOpt extends AbstractObjXmlOpt {
        private ArrayList<Integer> arrayDim = new ArrayList<Integer>();
        private Class arrayItemType = null;

        public ArrayXmlOpt() {
            mClass = null;
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
                            int classCode = Helper.tagNameToClassCode(classCodeStr);
                            if (classCode == ARRAY_CODE) {
                                collectArrayInfo(parser);
                            } else if (classCode == INT_CODE) {
                                arrayItemType = int.class;
                            } else if (classCode == BOOLEAN_CODE) {
                                arrayItemType = boolean.class;
                            } else if (classCode == LONG_CODE) {
                                arrayItemType = long.class;
                            } else if (classCode == FLOAT_CODE) {
                                arrayItemType = float.class;
                            } else {
                                arrayItemType = optArray[classCode].mClass;
                                if (arrayItemType == null) {
                                    arrayItemType = getClass(parser.getAttributeValue(null, ATTR_CLASS));
                                }
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
                            int classCode = Helper.tagNameToClassCode(classCodeStr);
                            if (classCode == ARRAY_CODE) {
                                return checkClassAndSize(parser,Array.get(obj, 0))+1;
                            } else if (classCode == INT_CODE) {
                                arrayItemType = int.class;
                            } else if (classCode == BOOLEAN_CODE) {
                                arrayItemType = boolean.class;
                            } else if (classCode == LONG_CODE) {
                                arrayItemType = long.class;
                            } else if (classCode == FLOAT_CODE) {
                                arrayItemType = float.class;
                            } else {
                                Object subObject = Array.get(obj, 0);
                                if (subObject != null && subObject.getClass().isArray()) {
                                    break;
                                }
                                // compare type?
                                Class itemClass = obj.getClass().getComponentType();
                                arrayItemType = optArray[classCode].mClass;
                                if (arrayItemType == null) {
                                    arrayItemType = getClass(parser.getAttributeValue(null, ATTR_CLASS));
                                }
                                if (arrayItemType != null && itemClass.equals(arrayItemType)) {
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
                    xmlType = parser.getEventType();
                    int depth = parser.getDepth();
                    boolean b = false;
                    while (xmlType != XmlPullParser.END_DOCUMENT
                            && (xmlType != XmlPullParser.END_TAG || depth > lowDepth)) {
                        if (xmlType == XmlPullParser.START_TAG && depth == lowDepth) {
                            if (b)
                                break;
                            String subClassCodeStr = parser.getName();
                            int subClassCode = Helper.tagNameToClassCode(subClassCodeStr);
                            if (subClassCode == INT_CODE) {
                                Array.setInt(obj, i, Helper.parseInt(parser));
                            } else if (subClassCode == BOOLEAN_CODE) {
                                Array.setBoolean(obj, i, Helper.parseBoolean(parser));
                            } else if (subClassCode == LONG_CODE) {
                                Array.setLong(obj, i, Helper.parseLong(parser));
                            } else if (subClassCode == FLOAT_CODE) {
                                Array.setFloat(obj, i, Helper.parseFloat(parser));
                            } else {
                                Object item = optArray[OBJ_CODE].parse(parser, null, null);
                                Array.set(obj, i, item);
                            }
                            b = true;
                        }
                        parser.next();
                        xmlType = parser.getEventType();
                        depth = parser.getDepth();
                    }
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
            if ( objParent != null && f != null ) {
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

                    if ( objParent != null && f != null ) {
                        try {
                            f.set(objParent, objT);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
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

            serializer.startTag(null, mTagName);
            serializer.attribute(null, ATTR_SIZE, Integer.toString(length));
            if (f != null) {
                serializer.attribute(null, ATTR_PARA, f.getName());
            }

            for (int i = 0; i < length; i++) {
                int itemClassCode = Helper.classToCode(itemC);
                if (itemClassCode < OBJ_CODE) {
                    if (itemClassCode == INT_CODE) {
                        Helper.saveTag(serializer, Helper.classCodeToTagName(INT_CODE), Integer.toString(Array.getInt(obj,i)),null);
                    } else if (itemClassCode == BOOLEAN_CODE) {
                        Helper.saveTag(serializer, Helper.classCodeToTagName(BOOLEAN_CODE), Boolean.toString(Array.getBoolean(obj,i)),null);
                    } else if (itemClassCode == LONG_CODE) {
                        Helper.saveTag(serializer, Helper.classCodeToTagName(LONG_CODE), Long.toString(Array.getLong(obj,i)),null);
                    } else if (itemClassCode == FLOAT_CODE) {
                        Helper.saveTag(serializer, Helper.classCodeToTagName(FLOAT_CODE), Integer.toString(Float.floatToRawIntBits(Array.getFloat(obj,i))),null);
                    }
                } else {
                    optArray[OBJ_CODE].serialize(serializer, Array.get(obj, i), null);
                }

            }
            serializer.endTag(null, mTagName);
        }
    }

    public static class IntegerXmlOpt extends AbstractObjXmlOpt {
        public IntegerXmlOpt() {
            mClass = Integer.class;
        }

        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {
            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }
            int i = Helper.parseInt(parser);
            Object objT = null;
            if ( objParent != null && f != null ) {
                try {
                    objT = f.get(objParent);
                    if (objT == null && (!Modifier.isFinal(f.getModifiers()))) {
                        objT = new Integer(i);
                        f.set(objParent, objT);
                    } else if(objT != null) {
                        Integer ii = (Integer) objT;
                        if (ii.intValue() != i) {
                            objT = new Integer(i);
                            f.set(objParent, objT);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                objT = new Integer(i);
            }

            return objT;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {
                if ( obj instanceof Integer ) {
                    Integer i = (Integer) obj;
                    Helper.saveTag(serializer, mTagName, i.toString(), (f!=null)?f.getName():null);
                }
        }
    }


    public static class BooleanXmlOpt extends AbstractObjXmlOpt {
        public BooleanXmlOpt() {
            mClass = Boolean.class;
        }

        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {
            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }
            boolean b = Helper.parseBoolean(parser);
            Object objT = null;
            if ( objParent != null && f != null ) {
                try {
                    objT = f.get(objParent);
                    if (objT == null && (!Modifier.isFinal(f.getModifiers()))) {
                        objT = new Boolean(b);
                        f.set(objParent, objT);
                    } else if(objT != null) {
                        Boolean bb = (Boolean) objT;
                        if (bb.booleanValue() != b) {
                            objT = new Boolean(b);
                            f.set(objParent, objT);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                objT = new Boolean(b);
            }
            return objT;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {
                if ( obj instanceof Boolean ) {
                    Boolean b = (Boolean) obj;
                    Helper.saveTag(serializer, mTagName, b.toString(), (f!=null)?f.getName():null);
                }
        }
    }

    public static class LongXmlOpt extends AbstractObjXmlOpt {
        public LongXmlOpt() {
            mClass = Long.class;
        }

        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {
            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }
            long i = Helper.parseLong(parser);
            Object objT = null;
            if ( objParent != null && f != null ) {
                try {
                    objT = f.get(objParent);
                    if (objT == null && (!Modifier.isFinal(f.getModifiers()))) {
                        objT = new Long(i);
                        f.set(objParent, objT);
                    } else if(objT != null) {
                        Long ii = (Long) objT;
                        if (ii.longValue() != i) {
                            objT = new Long(i);
                            f.set(objParent, objT);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                objT = new Long(i);
            }

            return objT;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {
                if ( obj instanceof Long ) {
                    Long i = (Long) obj;
                    Helper.saveTag(serializer, mTagName, i.toString(), (f!=null)?f.getName():null);
                }
        }
    }


    public static class FloatXmlOpt extends AbstractObjXmlOpt {
        public FloatXmlOpt() {
            mClass = Float.class;
        }

        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {
            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }
            float i = Helper.parseFloat(parser);
            Object objT = null;
            if ( objParent != null && f != null ) {
                try {
                    objT = f.get(objParent);
                    if (objT == null && (!Modifier.isFinal(f.getModifiers()))) {
                        objT = new Float(i);
                        f.set(objParent, objT);
                    } else if(objT != null) {
                        Float ii = (Float) objT;
                        if (ii.longValue() != i) {
                            objT = new Float(i);
                            f.set(objParent, objT);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                objT = new Float(i);
            }

            return objT;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {
                if ( obj instanceof Float ) {
                    Float i = (Float) obj;
                    Helper.saveTag(serializer, mTagName, Integer.toString(Float.floatToIntBits(i.floatValue())), (f!=null)?f.getName():null);
                }
        }
    }


    public static class ArrayListXmlOpt extends AbstractObjXmlOpt {
        //public static final Class mClass = java.util.ArrayList.class;
        public ArrayListXmlOpt() {
            mClass = java.util.ArrayList.class;
        }

        @Override
        public Object createInstance(String suggestClass) {
            Object obj = null;
            try {
                obj = mClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return obj;
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

            Object objT = Helper.getInstance(this, parser, objParent, f);
            if (objT == null) { return null;}

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

                serializer.startTag(null, mTagName);
                if (f != null) {
                    serializer.attribute(null,ATTR_PARA,f.getName());
                } else {
                    serializer.attribute(null,ATTR_CLASS, ArrayList.class.getName());
                }

                serializer.attribute(null,ATTR_SIZE,""+size);
                for (int i = 0 ; i < size; i++) {
                    optArray[OBJ_CODE].serialize(serializer, list.get(i), null);
                }
                serializer.endTag(null, mTagName);
        }
    }


    public static class ArraySetXmlOpt extends AbstractObjXmlOpt {

        //public static final Class mClass = android.util.ArraySet.class;
        public ArraySetXmlOpt() {
            mClass = android.util.ArraySet.class;
        }
        @Override
        public Object createInstance(String suggestClass) {
            Object obj = null;
            try {
                obj = mClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return obj;
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

            Object objT = Helper.getInstance(this, parser, objParent, f);
            if (objT == null) { return null;}

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
                serializer.startTag(null, mTagName);
                if (f != null) {
                    serializer.attribute(null,ATTR_PARA,f.getName());
                } else {
                    serializer.attribute(null,ATTR_CLASS, ArraySet.class.getName());
                }

                serializer.attribute(null,ATTR_SIZE,""+size);
                for (int i = 0 ; i < size; i++) {
                    optArray[OBJ_CODE].serialize(serializer, set.valueAt(i), null);
                }
                serializer.endTag(null, mTagName);
        }
    }

    public static class StringXmlOpt extends AbstractObjXmlOpt {
        public StringXmlOpt() {
            mClass = String.class;
        }

        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {
            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }
            Object objT = (Object)parser.nextText();
            if ( objParent != null && f != null ) {
                try {
                    f.set(objParent, objT);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return objT;
        }

        public void serialize(XmlSerializer serializer, Object obj, Field f)
            throws XmlPullParserException,IOException {
            serializer.startTag(null, mTagName);
            if (f != null){
                serializer.attribute(null,ATTR_PARA,f.getName());
            } else {
                serializer.attribute(null,ATTR_CLASS, String.class.getName());
            }
            serializer.text(obj.toString()).endTag(null, mTagName);
        }
    }

    public static class ArrayMapXmlOpt extends AbstractObjXmlOpt {

        //public static final Class mClass = android.util.ArrayMap.class;
        public ArrayMapXmlOpt() {
            mClass = android.util.ArrayMap.class;
        }
        @Override
        public Object createInstance(String suggestClass) {
            Object obj = null;
            try {
                obj = mClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return obj;
        }

        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {

            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }

            Object objT = Helper.getInstance(this, parser, objParent, f);
            if (objT == null) { return null;}

            Method putMethod;
            try {
                putMethod = ArrayMap.class.getDeclaredMethod("put", Object.class, Object.class);
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
            serializer.startTag(null, mTagName);
            if (f != null) {
                serializer.attribute(null,ATTR_PARA,f.getName());
            } else {
                serializer.attribute(null,ATTR_CLASS, ArrayList.class.getName());
            }

            Iterator iterator = keySet.iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = map.get(key);
                optArray[OBJ_CODE].serialize(serializer, key, null);
                optArray[OBJ_CODE].serialize(serializer, value, null);
            }
            serializer.endTag(null, mTagName);
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
            mClass = null;
        }
        public boolean needSaveRef() {
            return false;
        }
        //todo
        @Override
        public Object createInstance(String suggestClass) {
            Object o = null;
            Class c = null;
            try {
                c = Class.forName(suggestClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                o = c.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return o;
        }
        @Override
        public Object createInstance(Class suggestClass) { return null; }

        protected boolean handleSerializeSpField(Object o, Field field, String fieldName) {return false;}

        protected Field getField(Object obj, String fieldName) {
            Class c = obj.getClass();
            Field f = null;
            try {
                for(; c != Object.class; c=c.getSuperclass()) {
                    f = c.getDeclaredField(fieldName);
                    if (f != null) {
                        f.setAccessible(true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return f;
        }

        public void collectInfoBeforeCreateInstance(XmlPullParser parser)
            throws XmlPullParserException,IOException {
                return;
            }

        public void defaultparseFields(XmlPullParser parser, Object objT, final int innerDepth)
            throws XmlPullParserException,IOException {
                int xmlType;
                while ((xmlType = parser.next()) != XmlPullParser.END_DOCUMENT
                        && (xmlType != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {

                    if (xmlType == XmlPullParser.START_TAG && (parser.getDepth() == innerDepth + 1)) {
                        String subClassCodeStr = parser.getName();
                        int subClassCode = Helper.tagNameToClassCode(subClassCodeStr);
                        String name = parser.getAttributeValue(null,ATTR_PARA);
                        if (name == null) {
                            continue;
                        }
                        Field subField = null;
                        try {
                            subField = getField(objT, name);
                            if (subField == null) {
                                continue;
                            }

                            if (subClassCode == INT_CODE) {
                                subField.setInt(objT, Helper.parseInt(parser));
                            } else if (subClassCode == BOOLEAN_CODE) {
                                subField.setBoolean(objT, Helper.parseBoolean(parser));
                            } else if (subClassCode == LONG_CODE) {
                                subField.setLong(objT, Helper.parseLong(parser));
                            } else if (subClassCode == FLOAT_CODE) {
                                subField.setFloat(objT, Helper.parseFloat(parser));
                            } else {
                                optArray[subClassCode].parse(parser, objT ,subField);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

        }
        public Object parse(XmlPullParser parser, Object objParent, Field f)
            throws XmlPullParserException,IOException {

            if ( parser.getEventType() != XmlPullParser.START_TAG ) {
                return null;
            }
            Object objT = null;
            String classCodeStr = parser.getName();
            int classCode = Helper.tagNameToClassCode(classCodeStr);
            if (classCode > OBJ_CODE ) {
                objT = optArray[classCode].parse(parser, objParent ,f);
                return objT;
            }

            final int innerDepth = parser.getDepth();
            collectInfoBeforeCreateInstance(parser);
            if (objT == null) {
                objT = Helper.getInstance(this, parser, objParent, f);
            }
            if (objT == null) { return null;}
            if (needSaveRef()) {
                int objId = Helper.getObjRefIdFromParser(parser);
                Helper.recordObjRef(objT, objId);
            }
            defaultparseFields(parser, objT, innerDepth);
            return objT;
        }

        private void serializeNullObj(XmlSerializer serializer)
            throws XmlPullParserException,IOException {
            serializer.startTag(null, mTagName).endTag(null, mTagName);
        }

        public void serializeFieldsBefore(XmlSerializer serializer, Object obj)
            throws XmlPullParserException,IOException {
        }
        public void defaultSerializeFields(XmlSerializer serializer, Object obj)
            throws XmlPullParserException,IOException {
                Class c = obj.getClass();
                for(; c != Object.class; c=c.getSuperclass()) {
                    //Field[] fs = c.getDeclaredFields();
                    Field[] fields = c.getFields();

                    for (Field f : fields) {
                        f.setAccessible(true);
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
                        int subClassCode = Helper.classToCode(subClass);
                        String fieldName = f.getName();
                        if (handleSerializeSpField(obj, f, fieldName)) {
                            continue;
                        }
                        try {
                        if (subClassCode < OBJ_CODE) {
                            if (subClassCode == INT_CODE) {
                                Helper.saveTag(serializer,
                                        Helper.classCodeToTagName(INT_CODE), Integer.toString(f.getInt(obj)),fieldName);
                            } else if (subClassCode == BOOLEAN_CODE) {
                                Helper.saveTag(serializer,
                                        Helper.classCodeToTagName(BOOLEAN_CODE), Boolean.toString(f.getBoolean(obj)),fieldName);
                            } else if (subClassCode == LONG_CODE) {
                                Helper.saveTag(serializer,
                                        Helper.classCodeToTagName(LONG_CODE), Long.toString(f.getLong(obj)),fieldName);
                            } else if (subClassCode == FLOAT_CODE) {
                                Helper.saveTag(serializer,
                                        Helper.classCodeToTagName(FLOAT_CODE),
                                        Integer.toString(Float.floatToIntBits(f.getFloat(obj))),fieldName);
                            }
                        } else {

                            Object subObj = f.get(obj);
                            if (subObj == null) {
                                continue;
                            }
                            optArray[subClassCode].serialize(serializer, subObj, f);
                        }
                        } catch(IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }

        public void serialize(XmlSerializer serializer, Object obj, Field field)
            throws XmlPullParserException,IOException {
            //todo: record null obj?
            if (obj == null && field == null) {
                serializeNullObj(serializer);
                return;
            }


            if (obj == null)  { return; }

            try {
                //choose right opt to handle it
                Class c = obj.getClass();
                int classCode = Helper.classToCode(c);
                if (classCode > OBJ_CODE) {
                    optArray[classCode].serialize(serializer, obj ,field);
                } else if (classCode < OBJ_CODE) {
                    return;
                } else {
                    // default handler
                    serializer.startTag(null, mTagName);

                    if (field != null){
                        serializer.attribute(null,ATTR_PARA,field.getName());
                    } else {
                        serializer.attribute(null,ATTR_CLASS,c.getName());
                    }
                    if (needSaveRef()) {
                        Helper.saveObjRefId(serializer, obj);
                    }
                    serializeFieldsBefore(serializer, obj);
                    defaultSerializeFields(serializer, obj);
                    serializer.endTag(null, mTagName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////////// wrapper
    XmlPullParserFactory xppFactory;
    XmlPullParser mParser;
    XmlSerializer mSerializer;


    public Object parsePkg(String value) {
        //PackageParser.Package pkg = new PackageParser.Package("");

        //todo read serializerobj
        //todo clear Helper mmap

        Object obj = null;
        try {
        if (mParser == null) {
            if (xppFactory == null) {
                xppFactory = XmlPullParserFactory.newInstance();
            }
            mParser = xppFactory.newPullParser();
        }
        java.io.StringReader reader = new StringReader(value);
        mParser.setInput(reader);

        mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        while (true) {
            int xmlType = mParser.next();
            if (xmlType == XmlPullParser.START_TAG) {
                obj = optArray[OBJ_CODE].parse(mParser, null, null);
                break;
            }
            if (xmlType == XmlPullParser.END_DOCUMENT) {
                break;
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //todo clear Helper mmap
        return obj;
    }


    public String serializerPkg(Object pkg) {
        StringWriter writer = new StringWriter();
        try {
        if (mSerializer == null) {
            mSerializer = new FastXmlSerializer();
            /*
            if (xppFactory == null) {
                xppFactory = XmlPullParserFactory.newInstance();
            }

            mSerializer = xppFactory.newSerializer();
            */
        }
        mSerializer.setOutput(writer);
        mSerializer.startDocument(null,true);
        //todo clear Helper mmap and serializerobj
        optArray[OBJ_CODE].serialize(mSerializer, (Object)pkg, null);
        mSerializer.flush();
        //todo save serializerobj
        } catch (Exception e) {
            e.printStackTrace();
        }

        //todo clear Helper mmap
        return writer.toString();
    }

}
