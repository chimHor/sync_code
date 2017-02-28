package com.android.packagedb.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.IntentInfo;
import android.content.pm.PackageParser.Package;
import android.util.ArraySet;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import java.io.IOException;

import com.android.packagedb.util.PkgSerializer.ObjXmlOpt;
import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt.Helper;

public class IntentInfoXmlOpt<T extends IntentInfo, E extends Component> extends ObjXmlOpt{
    	private Class<T> ownClass;
    	private Class<E> componentClass;
        private Constructor tCons;
        private Field componentMember;
	    static final ArraySet<String> keyFields = new ArraySet<String>();
	    static final ArraySet<String> skipFields = new ArraySet<String>();

	    static final String ATTR_ARG1 = "a1";
	    int activityRefId = -1;
	    static {
	    	skipFields.add("activity");
	    	skipFields.add("service");
	    	skipFields.add("provider");
	    }

	    public IntentInfoXmlOpt(Class<T> t , Class<E> e, String member) {
	    	ownClass = t;
	    	componentClass = e;
	        mClass = t;
	        try {
				componentMember = t.getDeclaredField(member);
			} catch (NoSuchFieldException | SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        Constructor[] cs = t.getConstructors();
	        for (Constructor c : cs) {
	        	Class[] ccs = c.getParameterTypes();
	        	if (ccs.length == 1) {
	        		tCons = c;
	        	}
	        }
	    }
	    @Override
	    public Object createInstance(String suggestClass) { 
			try {
				final E component = (E)Helper.getObjByRefId(activityRefId);
				return tCons.newInstance(component);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
	    }
	    @Override
	    public Object createInstance(Class suggestClass) {
			try {
				final E component = (E)Helper.getObjByRefId(activityRefId);
				return tCons.newInstance(component);
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
	        String activityRefIdStr = parser.getAttributeValue(null, ATTR_ARG1);
	        if (activityRefIdStr != null) {
	            activityRefId = Integer.parseInt(activityRefIdStr);
	        } else {
	            activityRefId = -1;
	        }
	    }

	    @Override
	    public boolean serializeFieldsBefore(XmlSerializer serializer, Object obj)
	        throws XmlPullParserException,IOException {
	    	try {
	        int refId = Helper.getRefIdByObj(componentMember.get(obj));
	        serializer.attribute(null,ATTR_ARG1,Integer.toString(refId));
	    	} catch (IllegalAccessException e) {
	    		e.printStackTrace();
	    	}
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

