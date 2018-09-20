package testerq.core;

import java.io.Serializable;
import java.text.MessageFormat;

public class NetworkHash128 implements Serializable {

    public byte i0;
    public byte i1;
    public byte i2;
    public byte i3;
    public byte i4;
    public byte i5;
    public byte i6;
    public byte i7;
    public byte i8;
    public byte i9;
    public byte i10;
    public byte i11;
    public byte i12;
    public byte i13;
    public byte i14;
    public byte i15;

    public void Reset() {
        i0 = 0;
        i1 = 0;
        i2 = 0;
        i3 = 0;
        i4 = 0;
        i5 = 0;
        i6 = 0;
        i7 = 0;
        i8 = 0;
        i9 = 0;
        i10 = 0;
        i11 = 0;
        i12 = 0;
        i13 = 0;
        i14 = 0;
        i15 = 0;
    }

    public boolean IsValid() {
        return (i0 | i1 | i2 | i3 | i4 | i5 | i6 | i7 | i8 | i9 | i10 | i11 | i12 | i13 | i14 | i15) != 0;
    }

    static int HexToNumber(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return 0;
    }

    public static NetworkHash128 Parse(String text) {
        NetworkHash128 hash = new NetworkHash128();
        // add leading zeros if required
        int l = text.length();
        if (l < 32) {
            String tmp = "";
            for (int i = 0; i < 32 - l; i++) {
                tmp += "0";
            }
            text = (tmp + text);
        }
        char[] chars = text.toCharArray();
        hash.i0 = (byte) (HexToNumber(chars[0]) * 16 + HexToNumber(chars[1]));
        hash.i1 = (byte) (HexToNumber(chars[2]) * 16 + HexToNumber(chars[3]));
        hash.i2 = (byte) (HexToNumber(chars[4]) * 16 + HexToNumber(chars[5]));
        hash.i3 = (byte) (HexToNumber(chars[6]) * 16 + HexToNumber(chars[7]));
        hash.i4 = (byte) (HexToNumber(chars[8]) * 16 + HexToNumber(chars[9]));
        hash.i5 = (byte) (HexToNumber(chars[10]) * 16 + HexToNumber(chars[11]));
        hash.i6 = (byte) (HexToNumber(chars[12]) * 16 + HexToNumber(chars[13]));
        hash.i7 = (byte) (HexToNumber(chars[14]) * 16 + HexToNumber(chars[15]));
        hash.i8 = (byte) (HexToNumber(chars[16]) * 16 + HexToNumber(chars[17]));
        hash.i9 = (byte) (HexToNumber(chars[18]) * 16 + HexToNumber(chars[19]));
        hash.i10 = (byte) (HexToNumber(chars[20]) * 16 + HexToNumber(chars[21]));
        hash.i11 = (byte) (HexToNumber(chars[22]) * 16 + HexToNumber(chars[23]));
        hash.i12 = (byte) (HexToNumber(chars[24]) * 16 + HexToNumber(chars[25]));
        hash.i13 = (byte) (HexToNumber(chars[26]) * 16 + HexToNumber(chars[27]));
        hash.i14 = (byte) (HexToNumber(chars[28]) * 16 + HexToNumber(chars[29]));
        hash.i15 = (byte) (HexToNumber(chars[30]) * 16 + HexToNumber(chars[31]));

        return hash;
    }

    public String ToString() {
        return MessageFormat.format("{0:%s}{1:%s}{2:%s}{3:%s}{4:%s}{5:%s}{6:%s}{7:%s}{8:%s}{9:%s}{10:%s}{11:%s}{12:%s}{13:%s}{14:%s}{15:%s}",
                i0, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15);
    }
}
