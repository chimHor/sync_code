package com.android.packagedb.util2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import com.android.packagedb.util.PkgSerializer.AbstractObjXmlOpt.Helper;
import com.android.packagedb.util.SerializableObject.BytesWraper;

public class CertificateGhost implements Serializable {
	public byte[] content;
	
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
    
    public CertificateGhost(Certificate c) {
    	try {
    		if (c != null)
    			content = c.getEncoded();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public Certificate dumpFromGhost(){
    	if (content == null || content.length == 0) {
    		return null;
    	}
        final InputStream is = new ByteArrayInputStream(content);
        Certificate c = null;
        try {
        	c = (Certificate) t.newInstance(cf.generateCertificate(is), content);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return c;
    } 
}
