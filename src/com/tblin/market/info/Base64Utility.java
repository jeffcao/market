package com.tblin.market.info;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Base64Utility {

	private String lineSeparator;
	private int lineLength;

	public Base64Utility() {
		lineSeparator = System.getProperty("line.separator");
		lineLength = 0;
	}

	public Drawable base64ToDrawable(String base64Str) {
		Bitmap b = base64ToBitmap(base64Str);
		if (b == null) {
			return null;
		}
		return new BitmapDrawable(b);
	}

	public Bitmap base64ToBitmap(String base64Str) {
		if (base64Str == null) {
			return null;
		}
		Base64Utility decoder = new Base64Utility();
		byte[] bytes = decoder.decode(base64Str);
		if (bytes == null) {
			return null;
		}
		Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return b;
	}

	public String encode(byte[] bin) {
		return encode(bin, 0, bin.length);
	}

	/**
	 * Encode an array of bytes as Base64. It will be broken into lines if the
	 * line length is not 0. If broken into lines, the last line is not
	 * terminated with a line separator.
	 * 
	 * param ba The byte array to encode.
	 */
	public String encode(byte[] bin, int str, int len) {
		int ol;
		StringBuffer sb;
		int lp;
		int el;
		int ll;
		ol = (((len + 2) / 3) * 4);
		if (lineLength != 0) {
			int lines = (((ol + lineLength - 1) / lineLength) - 1);
			if (lines > 0) {
				ol += (lines * lineSeparator.length());
			}
		}
		sb = new StringBuffer(ol);
		lp = 0;
		el = (len / 3) * 3;
		ll = (len - el);
		for (int xa = 0; xa < el; xa += 3) {
			int cv;
			int c0, c1, c2, c3;

			if (lineLength != 0) {
				lp += 4;
				if (lp > lineLength) {
					sb.append(lineSeparator);
					lp = 4;
				}
			}
			cv = (bin[xa + str + 0] & 0xFF);
			cv <<= 8;
			cv |= (bin[xa + str + 1] & 0xFF);
			cv <<= 8;
			cv |= (bin[xa + str + 2] & 0xFF);
			c3 = cv & 0x3F;
			cv >>>= 6;
			c2 = cv & 0x3F;
			cv >>>= 6;
			c1 = cv & 0x3F;
			cv >>>= 6;
			c0 = cv & 0x3F;
			sb.append(ENCODE[c0]);
			sb.append(ENCODE[c1]);
			sb.append(ENCODE[c2]);
			sb.append(ENCODE[c3]);
		}
		if (lineLength != 0 && ll > 0) {
			lp += 4;
			if (lp > lineLength) {
				sb.append(lineSeparator);
				lp = 4;
			}
		}
		if (ll == 1) {
			sb.append(
					encode(new byte[] { bin[el + str], 0, 0 }).substring(0, 2))
					.append("==");
		} else if (ll == 2) {
			sb.append(
					encode(new byte[] { bin[el + str], bin[el + str + 1], 0 })
							.substring(0, 3)).append("=");
		}
		if (ol != sb.length()) {
			throw new RuntimeException(
					"Error in Base64 encoding method: Calculated output length of "
							+ ol + " did not match actual length of "
							+ sb.length());
		}
		return sb.toString();
	}

	public byte[] decode(String b64) {
		return decode(b64, 0, b64.length());
	}

	/**
	 * Decode a Base64 string to an array of bytes. The string must have a
	 * length evenly divisible by 4 (not counting line separators and other
	 * ignorable characters, like whitespace).
	 */
	public byte[] decode(String b64, int str, int len) {
		if (b64 == null) {
			return null;
		}
		byte[] ba;
		int dc;
		int rv;
		int ol;
		int pc;
		ba = new byte[(len / 4) * 3];
		dc = 0;
		rv = 0;
		ol = 0;
		pc = 0;
		for (int xa = 0; xa < len; xa++) {
			int ch = b64.charAt(xa + str);
			int value = (ch <= 255 ? DECODE[ch] : IGNORE);
			if (value != IGNORE) {
				if (value == PAD) {
					value = 0;
					pc++;
				}
				switch (dc) {
				case 0: {
					rv = value;
					dc = 1;
				}
					break;

				case 1: {
					rv <<= 6;
					rv |= value;
					dc = 2;
				}
					break;

				case 2: {
					rv <<= 6;
					rv |= value;
					dc = 3;
				}
					break;

				case 3: {
					rv <<= 6;
					rv |= value;
					ba[ol + 2] = (byte) rv;
					rv >>>= 8;
					ba[ol + 1] = (byte) rv;
					rv >>>= 8;
					ba[ol] = (byte) rv;
					ol += 3;
					dc = 0;
				}
					break;
				}
			}
		}
		if (dc != 0) {
			return null;
		}
		ol -= pc;
		if (ba.length != ol) {
			byte[] b2 = new byte[ol];
			System.arraycopy(ba, 0, b2, 0, ol);
			ba = b2;
		}
		return ba;
	}

	/**
	 * Set maximum line length for encoded lines. Ignored by decode.
	 * 
	 * @param len
	 *            Length of each line. 0 means no newlines inserted. Must be a
	 *            multiple of 4.
	 */
	public void setLineLength(int len) {
		this.lineLength = (len / 4) * 4;
	}

	/**
	 * Set the line separator sequence for encoded lines. Ignored by decode.
	 * Usually contains only a combination of chars \n and \r, but could be any
	 * chars except 'A'-'Z', 'a'-'z', '0'-'9', '+' and '/'.
	 * 
	 * @param linsep
	 *            Line separator - may be "" but not null.
	 */
	public void setLineSeparator(String linsep) {
		this.lineSeparator = linsep;
	}

	static private final char[] ENCODE = new char[64];
	static private final int[] DECODE = new int[256];
	static private final int IGNORE = -1;
	static private final int PAD = -2;
	static private final Base64Utility BASE64 = new Base64Utility();
	static {
		for (int xa = 0; xa <= 25; xa++) {
			ENCODE[xa] = (char) ('A' + xa);
		}
		for (int xa = 0; xa <= 25; xa++) {
			ENCODE[xa + 26] = (char) ('a' + xa);
		}
		for (int xa = 0; xa <= 9; xa++) {
			ENCODE[xa + 52] = (char) ('0' + xa);
		}
		ENCODE[62] = '+';
		ENCODE[63] = '/';

		for (int xa = 0; xa < 256; xa++) {
			DECODE[xa] = IGNORE;
		}
		for (int xa = 0; xa < 64; xa++) {
			DECODE[ENCODE[xa]] = xa;
		}
		DECODE['='] = PAD;
	}

	static public String toString(byte[] dta) {
		return BASE64.encode(dta);
	}

	static public String toString(byte[] dta, int str, int len) {
		return BASE64.encode(dta, str, len);
	}

	static public byte[] toBytes(String b64) {
		return BASE64.decode(b64);
	}

	static public byte[] toBytes(String b64, int str, int len) {
		return BASE64.decode(b64, str, len);
	}

}
