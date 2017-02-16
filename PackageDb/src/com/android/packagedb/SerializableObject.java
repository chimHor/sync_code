

import java.util.ArrayList;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;

public class SerializableObject implements Serializable {
    public ArrayList<BytesWraper> wraperList = new ArrayList<BytesWraper>();

    public class BytesWraper implements Serializable {
        public byte[] content;
    }

    public static byte[] toBytes(SerializableObject me) {
        try {
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outBytes);
            out.writeObject(me);
            out.close();
            return outBytes.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SerializableObject generateFromBytes(byte[] bytes) {
        try {
            ByteArrayInputStream inBytes = new ByteArrayInputStream(bytes);
            SerializableObject obj = null;
            ObjectInputStream in = new ObjectInputStream(inBytes);
            obj = (SerializableObject)in.readObject();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }



}
