package com.android.packagedb.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

import android.os.BaseBundle;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.ArraySet;
import android.util.ArrayMap;
import android.util.Base64;

import android.util.Log;
import android.content.IntentFilter.AuthorityEntry;
import android.content.pm.ManifestDigest;
import android.content.pm.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.PublicKey;
import android.util.Base64;

public class TestCompareHelper {
	static ArrayList<String> comparePos = new ArrayList<String>();
	static AbsCmp[] notCmpMemberClasses = {
		new BundleCmp(),
		new PatternMatcherCmp(),
		new AuthorityEntryCmp(),
		new SignatureCmp(),
		new CertificateCmp(),
		new ManifestDigestCmp(),
		new PublicKeyCmp(),
	};

	
	static class ComparePair {
		Object obj1;
		Object obj2;
		public ComparePair(Object a, Object b) {
			obj1 = a;
			obj2 = b;
		}
		public boolean equals(Object o) {
			 if (!(o instanceof ComparePair)) {
				 return false;
			 }
			 ComparePair pair = (ComparePair) o;
			 if ((obj1 == pair.obj1) && (obj2 == pair.obj2)) {
				 return true;
			 }
			 if ((obj2 == pair.obj1) && (obj1 == pair.obj2)) {
				 return true;
			 }
			 return false;
		}
	}
	
	
	abstract static class AbsCmp {
		final Class mClass;
		public AbsCmp(Class c) {mClass = c;}
		public abstract boolean compare(Object obj1, Object obj2);
	}
	
	
	static class BundleCmp extends AbsCmp {
		public BundleCmp() {super(Bundle.class);}
		
		static Field mapField;
		{
			try {
				mapField = BaseBundle.class.getDeclaredField("mMap");
				if (mapField != null) mapField.setAccessible(true);
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		@Override
		public boolean compare(Object obj1, Object obj2) {
			// TODO Auto-generated method stub
			if (mapField == null) {
				Log.w("Test", "Bundle mmap field is null!!!!!!");
				return false;
			}
			Bundle b1 = (Bundle) obj1;
			Bundle b2 = (Bundle) obj2;
			ArrayMap m1 = null;
			try {
				m1 = (ArrayMap) mapField.get(b1);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArrayMap m2 = null;
			try {
				m2 = (ArrayMap) mapField.get(b2);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (m1 == null && m2 ==null) {
				return true;
			} else if (m1 != null && m2 != null) {
				return m1.equals(m2);
			} else {
				return false;
			}
		}
	}
	
	
	static class PatternMatcherCmp extends AbsCmp {
		public PatternMatcherCmp() {super(PatternMatcher.class);}

		@Override
		public boolean compare(Object obj1, Object obj2) {
			// TODO Auto-generated method stub
			PatternMatcher p1 = (PatternMatcher) obj1;
			PatternMatcher p2 = (PatternMatcher) obj2;
			if (p1.getPath().equals(p2.getPath()) && (p1.getType()==p2.getType())) {
				return true;
			}
			return false;
		}
	}
	
	static class AuthorityEntryCmp extends AbsCmp {
		public AuthorityEntryCmp() {super(AuthorityEntry.class);}

		@Override
		public boolean compare(Object obj1, Object obj2) {
			// TODO Auto-generated method stub
			AuthorityEntry a1 = (AuthorityEntry) obj1;
			AuthorityEntry a2 = (AuthorityEntry) obj2;
			if ( a1.getHost().equals(a2.getHost()) && (a1.getPort()==a2.getPort())) {
				return true;
			}
			return false;
		}
	}
	
	static class SignatureCmp extends AbsCmp {
		public SignatureCmp() {super(Signature.class);}

		@Override
		public boolean compare(Object obj1, Object obj2) {
			// TODO Auto-generated method stub
			Signature s1 = (Signature) obj1;
			Signature s2 = (Signature) obj2;
			return s1.equals(s2);
		}
	}
	
	static class CertificateCmp extends AbsCmp {
		public CertificateCmp() {super(Certificate.class);}

		@Override
		public boolean compare(Object obj1, Object obj2) {
			// TODO Auto-generated method stub
			try {
				byte[] b1 = ((Certificate) obj1).getEncoded();
				byte[] b2 = ((Certificate) obj2).getEncoded();
				return Arrays.equals(b1, b2);
			} catch (CertificateEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
	}
	
	static class ManifestDigestCmp extends AbsCmp {
		public ManifestDigestCmp() {super(ManifestDigest.class);}

		@Override
		public boolean compare(Object obj1, Object obj2) {
			// TODO Auto-generated method stub
			ManifestDigest m1 = (ManifestDigest) obj1;
			ManifestDigest m2 = (ManifestDigest) obj2;
			return m1.equals(m2);
		}
	}
	static class PublicKeyCmp extends AbsCmp {
		public PublicKeyCmp() {super(PublicKey.class);}

		@Override
		public boolean compare(Object obj1, Object obj2) {
			// TODO Auto-generated method stub
			byte[] b1 = Base64.encode(((PublicKey) obj1).getEncoded(), 0);
			byte[] b2 = Base64.encode(((PublicKey) obj2).getEncoded(), 0);
			return Arrays.equals(b1, b2);
		}
	}
	
	static ArrayList<ComparePair> beginCmp = new ArrayList<ComparePair>();
	
	
	public static boolean compare(Object obj1, Object obj2) {
		beginCmp.clear();
		boolean res = compareInner(obj1, obj2);
		return res;
	}
	
	public static boolean compareInner(Object obj1, Object obj2) {
		ComparePair pair = new ComparePair(obj1, obj2);
		printFalseMsg("");
		if (beginCmp.contains(pair)) {
			return true;
		} else {
			beginCmp.add(pair);
		}
		if (obj1 == obj2)
			return true;
		if (obj1 != null && obj2 == null) {
			printFalseMsg("obj1 is not null, ob2 is null");
			return false;
		}
		if (obj1 == null && obj2 != null) {
			printFalseMsg("obj1 is null, ob2 is not null");
			return false;
		}

		Class c1 = obj1.getClass();
		Class c2 = obj2.getClass();
		if (!c1.equals(c2)) {
			printFalseMsg("obj1 "+c1.getSimpleName()+ "  obj2 "+ c2.getSimpleName());
			return false;
		}
		
        Class c = c1;
        boolean b = false;
        if (c.isArray()) { 
        	b = arrayDeepEqualsElement(obj1, obj2);
        	if (!b) {
        		printFalseMsg("array obj1 not equals obj2");
        	}
        	return b;
        }
        
        if (c.equals(ArrayList.class)) {
        	ArrayList l1 = (ArrayList) obj1;
        	ArrayList l2 = (ArrayList) obj2;
        	b = true;
        	if (l1.size()!=l2.size()) {
        		Log.w("Test","l1 size : " + l1.size() + "  l2 size: " + l2.size());
        		b = false;
        	}
        	if (b) {
	        	for (int i = 0; i<l1.size(); i++) {
	        		if (!compareInner(l1.get(i), l2.get(i))) {
	        			b =false;
	        			break;
	        		}
	        	}
        	}
        	if (!b) {
        		printFalseMsg("arraylist obj1 not equals obj2");
        	}
        	return b;
        }
        
        if (c.equals(ArraySet.class)) {
        	ArraySet set1 = (ArraySet) obj1;       	
        	ArraySet set2 = (ArraySet) obj2;
        	b = true;
        	if (set1.size() != set2.size()) {
        		Log.w("Test","s1 size : " + set1.size() + "  s2 size: " + set2.size());
        		b = false;
            }
        	if (b) {
	        	for (int i=0; i<set1.size(); i++) {
	        		Object sub1 = set1.valueAt(i);
	        		/*
	            	if (!set2.contains(sub1)) {
						b = false;
						break;
	               	}
	        		*/
	        		b = false;
	        		for (int j=0; j<set2.size(); j++) {
	        			Object sub2 = set2.valueAt(j);
	        			if (compareInner(sub1,sub2)) {
	        				b = true;
	        				break;
	        			}
	        		}
	        		if (!b) {
	        			break;
	        		}
	        	}
        	}
        	if (!b) {
        		printFalseMsg("arrayset obj1 not equals obj2");
        	}
        	return b;
        }
        
        if (c.equals(ArrayMap.class)) {
        	 ArrayMap map1 = (ArrayMap) obj1;
        	 ArrayMap map2 = (ArrayMap) obj2;
        	 b = true;
             if (map1.size() != map2.size()) {
            	 Log.w("Test","m1 size : " + map1.size() + "  m2 size: " + map2.size());
                 b = false;
             }

             if (b) {
                 for (int i=0; i<map1.size(); i++) {
                     Object key = map1.keyAt(i);
                     Object mine = map1.valueAt(i);
                     Object theirs = map2.get(key);
                     if (mine == null) {
                         if (theirs != null || !map2.containsKey(key)) {
                             b = false;
                             break;
                         }
                     } else if (!compareInner(mine,theirs)) { 
                    	 b = false;
                    	 break;
                     }
                 }
             }
         	 if (!b) {
         		 printFalseMsg("arraymap obj1 not equals obj2");
         	 }
             return b;
        }
        
        for (int i = 0; i< notCmpMemberClasses.length; i++) {
        	if(notCmpMemberClasses[i].mClass.isInstance(obj1)) {
        		comparePos.add(c1.getSimpleName());
        		printFalseMsg("");
        		b = notCmpMemberClasses[i].compare(obj1, obj2);
        		if (!b) {
    				printFalseMsg("sphandle class obj1 not equals obj2");
    			}
        		comparePos.remove(comparePos.size()-1);
        		return b;
        	}
        }
        if (c.getName().startsWith("java")) {
        	comparePos.add(c1.getSimpleName());
        	b = obj1.equals(obj2);
        	comparePos.remove(comparePos.size()-1);
    		if (!b) {
				printFalseMsg("java class obj1 not equals obj2");
			}
    		return b;
        }
		
        for(; c != Object.class; c=c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();

            for (Field f : fields) {
                f.setAccessible(true);
                //base type
                int mode = f.getModifiers();
                if (Modifier.isStatic(mode)) {
                    continue;
                }
                Class subClass = f.getType();
                String fieldName = f.getName();
                comparePos.add(fieldName);
                b = true;
                try {
                	if (subClass == int.class) {
                		b = (f.getInt(obj1) == f.getInt(obj2)); 
                	} else if (subClass == boolean.class) {
                		b = (f.getBoolean(obj1) == f.getBoolean(obj2)); 
                	} else if (subClass == long.class) {
                		b = (f.getLong(obj1) == f.getLong(obj2)); 
                	} else if (subClass == float.class) {
                		b = (f.getFloat(obj1) == f.getFloat(obj2));
                	} else if (subClass == byte.class) {
                		b = (f.getByte(obj1) == f.getByte(obj2)); 
                	} else if (subClass == char.class) {
                		b = (f.getChar(obj1) == f.getChar(obj2));
                	} else if (subClass == short.class) {
                		b = (f.getShort(obj1) == f.getShort(obj2));
                	} else if (subClass == double.class) {
                		b = (f.getDouble(obj1) == f.getDouble(obj2));
                	} else {
						Object subObj1 = f.get(obj1);
		                Object subObj2 = f.get(obj2);
		                b=compareInner(subObj1, subObj2);
                	}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					b=false;
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					b=false;
					e.printStackTrace();
				}
                if (!b) {
                	printFalseMsg("subobj1 not equals subobj2");
                }
                comparePos.remove(comparePos.size()-1);
				if (!b) {
					return false;
				}
            }
        }
		return true;
	}
	
	private static boolean arrayEquals(Object e1, Object e2){
        boolean eq;
        if (e1 instanceof Object[] && e2 instanceof Object[])
            eq = arrayDeepEquals ((Object[]) e1, (Object[]) e2);
        else if (e1 instanceof byte[] && e2 instanceof byte[])
            eq = Arrays.equals((byte[]) e1, (byte[]) e2);
        else if (e1 instanceof short[] && e2 instanceof short[])
            eq = Arrays.equals((short[]) e1, (short[]) e2);
        else if (e1 instanceof int[] && e2 instanceof int[])
            eq = Arrays.equals((int[]) e1, (int[]) e2);
        else if (e1 instanceof long[] && e2 instanceof long[])
            eq = Arrays.equals((long[]) e1, (long[]) e2);
        else if (e1 instanceof char[] && e2 instanceof char[])
            eq = Arrays.equals((char[]) e1, (char[]) e2);
        else if (e1 instanceof float[] && e2 instanceof float[])
            eq = Arrays.equals((float[]) e1, (float[]) e2);
        else if (e1 instanceof double[] && e2 instanceof double[])
            eq = Arrays.equals((double[]) e1, (double[]) e2);
        else if (e1 instanceof boolean[] && e2 instanceof boolean[])
            eq = Arrays.equals((boolean[]) e1, (boolean[]) e2);
        else
            eq = false;
        return eq;
	}
	
    public static boolean arrayDeepEquals(Object[] array1, Object[] array2) {
        if (array1 == array2) {
            return true;
        }    
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }    
        for (int i = 0; i < array1.length; i++) {
            Object e1 = array1[i], e2 = array2[i];

            if (!arrayDeepEqualsElement(e1, e2)) {
                return false;
            }    
        }    
        return true;
    }    

    private static boolean arrayDeepEqualsElement(Object e1, Object e2) {
        Class<?> cl1, cl2; 

        if (e1 == e2) {
            return true;
        }    

        if (e1 == null || e2 == null) {
            return false;
        }    

        cl1 = e1.getClass().getComponentType();
        cl2 = e2.getClass().getComponentType();

        if (cl1 == null && cl2 == null) {
            return compareInner(e1,e2);
        }    

        /*
         * compare as arrays
         */
        if (cl1 == cl2) {
        if (cl1 == int.class) {
            return Arrays.equals((int[]) e1, (int[]) e2);
        } else if (cl1 == char.class) {
            return Arrays.equals((char[]) e1, (char[]) e2);
        } else if (cl1 == boolean.class) {
            return Arrays.equals((boolean[]) e1, (boolean[]) e2);
        } else if (cl1 == byte.class) {
            return Arrays.equals((byte[]) e1, (byte[]) e2);
        } else if (cl1 == long.class) {
            return Arrays.equals((long[]) e1, (long[]) e2);
        } else if (cl1 == float.class) {
            return Arrays.equals((float[]) e1, (float[]) e2);
        } else if (cl1 == double.class) {
            return Arrays.equals((double[]) e1, (double[]) e2);
        } else if (cl1 == short.class) {
            return Arrays.equals((short[]) e1, (short[]) e2);
        } else if (e1 instanceof Object[]){
        	return arrayDeepEquals((Object[]) e1, (Object[]) e2);
        }
        } else if (e1 instanceof Object[]) {
            return arrayDeepEquals((Object[]) e1, (Object[]) e2);
        }
        
        	Log.wtf("Test", " shoult not be here");
        	printFalseMsg("wtf");
        	return false;
    }

	
	
	private static void printFalseMsg(String msg) {
		int depth = comparePos.size();
		StringBuilder pos = new StringBuilder();
		for (int i = 0 ; i < depth; i++) {
			pos.append(comparePos.get(i));
			pos.append(" : ");
		}
		///*
		if (!(msg == null || msg.equals(""))) {
			Log.w("Test", "false in "+depth+", "+pos.toString());
			Log.w("Test", msg);
		} else {
			//Log.w("Test", "compare in "+depth+", "+pos.toString());
		}
		//*/
	}

}
