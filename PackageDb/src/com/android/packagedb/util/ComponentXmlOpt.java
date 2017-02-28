package com.android.packagedb.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageItemInfo;
import android.util.ArraySet;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import java.io.IOException;

import com.android.packagedb.util.PkgSerializer.ObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt.Helper;

public class ComponentXmlOpt<T extends Component, E extends PackageItemInfo> extends ObjXmlOpt {
    private Class<T> tClass;
    private Class<E> eClass;
    private Constructor tCons;
	
	static final String ATTR_ARG1 = "a1";

    static final ArraySet<String> keyFields = new ArraySet<String>();
    static final ArraySet<String> skipFields = new ArraySet<String>();
    
    
    int pkgRefId = -1;
    static {
    }

    public ComponentXmlOpt(Class<T> t , Class<E> e) {
    	tClass = t;
    	eClass = e;
        mClass = t;
        Constructor[] cs = t.getConstructors();
        for (Constructor c : cs) {
        	Class[] ccs = c.getParameterTypes();
        	for (Class cc : ccs) {
        		if (cc.equals(Package.class)) {
        			tCons = c;
        		}
        	}
        }
    }

    @Override
    public boolean needSaveRef() {
        return true;
    }

    @Override
    public Object createInstance(String suggestClass) {
        final Package pkg = (Package)Helper.getObjByRefId(pkgRefId);
        E e;
		try {
			e = eClass.newInstance();
			return tCons.newInstance(pkg, e);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        return null;
    }
    @Override
    public Object createInstance(Class suggestClass) {
        final Package pkg = (Package)Helper.getObjByRefId(pkgRefId);
        E e;
		try {
			e = eClass.newInstance();
			return tCons.newInstance(pkg, e);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        return null;
    }

    @Override
    public void collectInfoBeforeCreateInstance(XmlPullParser parser)
        throws XmlPullParserException,IOException {
        String pkgRefIdStr = parser.getAttributeValue(null, ATTR_ARG1);
        if (pkgRefIdStr != null) {
            pkgRefId = Integer.parseInt(pkgRefIdStr);
        } else {
            pkgRefId = -1;
        }
    }
    @Override
    public boolean serializeFieldsBefore(XmlSerializer serializer, Object obj)
        throws XmlPullParserException,IOException {
    	Component a = (Component) obj;
        int refId = Helper.getRefIdByObj(a.owner);
        serializer.attribute(null,ATTR_ARG1,Integer.toString(refId));
        return false;
    }

    @Override
    protected boolean handleSerializeSpField(Object o, Field field, String fieldName) {
        if (skipFields.contains(fieldName)) {
            return true;
        }
        return false;
    }
}
