package org.allcolor.yahp.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CBASE64Codec {
	private static final String B64ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	private static int[] decode(byte[] buffer) {
		int b1 = buffer[0] & 0x000000FF;
		b1 = b1 << 16;
		int b2 = buffer[1] & 0x000000FF;
		b2 = b2 << 8;
		int b3 = buffer[2] & 0x000000FF;
		int bufferValue = b1 + b2 + b3;
		return new int[] { ((bufferValue & 0x00FC0000) >>> 18),
				((bufferValue & 0x0003F000) >>> 12),
				((bufferValue & 0x00000FC0) >>> 6), (bufferValue & 0x0000003F) };
	}

	private static int[] decode(char[] buffer) {
		return new int[] { CBASE64Codec.B64ARRAY.indexOf(buffer[0]),
				CBASE64Codec.B64ARRAY.indexOf(buffer[1]),
				CBASE64Codec.B64ARRAY.indexOf(buffer[2]),
				CBASE64Codec.B64ARRAY.indexOf(buffer[3]) };
	}

	public static byte[] decode(String in) {
		in = in.replaceAll("\n", "");
		in = in.replaceAll("\r", "");
		String[] sarray = CCryptoUtils.cutByChar(in, 4);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < sarray.length; i++) {
			int[] index = CBASE64Codec.decode(sarray[i].toCharArray());
			int b1 = index[0] << 18;
			int b2 = index[1] << 12;
			int b3 = index[2] > -1 ? index[2] << 6 : 0;
			int b4 = index[3] > -1 ? index[3] : 0;
			int value = b1 + b2 + b3 + b4;
			int b11 = value >>> 16;
			int b21 = (value << 8) >>> 16;
			int b31 = (value << 16) >>> 16;
			try {
				out.write(new byte[] { (byte) b11 });
				if (index[2] > -1) {
					out.write(new byte[] { (byte) b21 });
				}
				if (index[3] > -1) {
					out.write(new byte[] { (byte) b31 });
				}
			} catch (IOException ignore) {
			}
		}
		return out.toByteArray();
	}

	public static String encode(byte[] array) {
		StringBuffer result = new StringBuffer();
		byte[] buffer = new byte[3];
		int indexBuffer = 0;
		for (int i = 0; i < array.length; i++) {
			buffer[indexBuffer++] = array[i];
			if (indexBuffer == 3) {
				int[] index = CBASE64Codec.decode(buffer);
				result.append(CBASE64Codec.B64ARRAY.charAt(index[0]));
				result.append(CBASE64Codec.B64ARRAY.charAt(index[1]));
				result.append(CBASE64Codec.B64ARRAY.charAt(index[2]));
				result.append(CBASE64Codec.B64ARRAY.charAt(index[3]));
				indexBuffer = 0;
				buffer[0] = buffer[1] = buffer[2] = 0;
			}
		}
		if ((indexBuffer < 3) && (indexBuffer > 0)) {
			int[] index = CBASE64Codec.decode(buffer);
			result.append(CBASE64Codec.B64ARRAY.charAt(index[0]));
			result.append(CBASE64Codec.B64ARRAY.charAt(index[1]));
			if ((3 - indexBuffer) == 1) {
				result.append(CBASE64Codec.B64ARRAY.charAt(index[2]));
			} else {
				result.append("=");
			}
			result.append("=");
		}
		String[] sarray = CCryptoUtils.cutByChar(result.toString(), 76);
		result = new StringBuffer();
		for (int i = 0; i < sarray.length; i++) {
			result.append(sarray[i]);
			if (i < array.length - 1) {
				result.append("\n");
			}
		}
		return result.toString();
	}
}
