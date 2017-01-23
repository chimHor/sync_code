package com.android.packagedb;


import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

interface IObjXmlOpt  {
    public Object parse(XmlPullParser parser, Object t, Class c);
    public void serialize(XmlSerializer serializer, Object t, Field f);
}
