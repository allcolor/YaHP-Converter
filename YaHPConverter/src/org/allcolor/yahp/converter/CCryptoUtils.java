package org.allcolor.yahp.converter;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CCryptoUtils {
	public final static SecureRandom random = new SecureRandom();
	
	public static final String TOKENS = new String(new BigInteger(CBASE64Codec
			.decode("dEIITE5BTZ+VTElS")).xor(
			new BigInteger(new byte[] { 116, 111, 116, 111, 108, 101, 104, -61,
					-87, 114, 111, 115 })).toByteArray());

	public static List cutByBytePad(String in, int length) {
		return cutByBytePad(in.getBytes(),length);
	}
	
	public static List cutByBytePad(byte [] array, int length) {
		if (length > 256) throw new RuntimeException("Too long, padding not possible.");
		List result = new ArrayList();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		bout.write(1);
		for (int i = 0; i < array.length; i++) {
			bout.write(array[i]);
			if (bout.toByteArray().length == length) {
				result.add(bout.toByteArray());
				bout = new ByteArrayOutputStream();
				bout.write(1);
			}
		}
		if (bout.toByteArray().length > 0) {
			int nb = length-bout.toByteArray().length;
			for (int i=0;i<nb;i++) {
				bout.write(nb);
			}
			result.add(bout.toByteArray());
		}
		return result;
	}

	public static List cutByByteNoPad(byte [] array, int length) {
		List result = new ArrayList();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		for (int i = 0; i < array.length; i++) {
			bout.write(array[i]);
			if (bout.toByteArray().length == length) {
				result.add(bout.toByteArray());
				bout = new ByteArrayOutputStream();
			}
		}
		if (bout.toByteArray().length > 0) {
			result.add(bout.toByteArray());
		}
		return result;
	}
	public static byte [] generateUniquePassPhrase() {
		return generateUniquePassPhrase(16);
	}

	public static byte [] generateUniquePassPhrase(int length) {
		byte [] passArray = new byte[length];
		for (int i=0;i<length;i++) {
			passArray[i] = (byte)random.nextInt(256);
			if (i == 0 && passArray[i] == 0)
				passArray[i] = 1;
		}
		return passArray;
	}
	
	public static boolean comparByteSequence(byte [] a,byte [] b) {
		if (a.length != b.length) return false;
		for (int i=0;i<a.length;i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	public static String[] cutByChar(String in, int length) {
		char[] array = in.toCharArray();
		List result = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			buffer.append(array[i]);
			if (buffer.toString().length() >= length && i < (array.length - 1)) {
				result.add(buffer.toString());
				buffer = new StringBuffer();
			}
		}
		if (buffer.length() > 0) {
			result.add(buffer.toString());
		}
		String[] sresult = new String[result.size()];
		for (int i = 0; i < result.size(); i++) {
			sresult[i] = (String) result.get(i);
		}
		return sresult;
	}
	
	public static String convertFromBigInt(BigInteger i) {
		try {
			String dec = new String(CBASE64Codec.decode(new String(i.toByteArray(),
					"ascii")), "utf-8");
			return dec;
		} catch (Exception e) {
			return "";
		}
	}

	public static String convertToString(BigInteger i) {
		return CBASE64Codec.encode(i.toByteArray());
	}

	public static BigInteger convertFromString(String s) {
		try {
			return new BigInteger(CBASE64Codec.decode(s));
		} catch (Exception ioe) {
			return null;
		}
	}

	public static BigInteger convertToBigInt(String mystring) {
		try {
			byte[] array = CBASE64Codec.encode(mystring.getBytes("utf-8")).getBytes(
					"ascii");
			return new BigInteger(array);
		} catch (UnsupportedEncodingException e) {
			return BigInteger.ZERO;
		}
	}
}
