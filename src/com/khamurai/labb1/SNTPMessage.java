package com.khamurai.labb1;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class SNTPMessage {
    private byte leapIndicator = 0;
    private byte versionNumber = 4;
    private byte mode = 0;

    private short stratum = 0;
    private short pollInterval = 0;
    private byte precision = 0;

    private double rootDelay = 0;
    private double rootDispersion = 0;

    private byte[] referenceIdentifier = {0, 0, 0, 0};



    private double referenceTimeStamp = 0;
    private double originateTimeStamp = 0;
    private double receiveTimeStamp = 0;
    private double transmitTimeStamp = 0;

    public SNTPMessage(byte[] buf) {
        byte b = buf[0];

        leapIndicator = (byte) ((b>>6) & 0x3);

        versionNumber = (byte) ((b>>3) & 0x7);

        mode = (byte) (b & 0x7);

        stratum = unsignedByteToShort(buf[1]);

        pollInterval = unsignedByteToShort(buf[2]);

        precision = buf[3];

        rootDelay = (buf[4] * 256.0)
                + unsignedByteToShort(buf[5])
                + (unsignedByteToShort(buf[6]) / (0xff + 1.0))
                + (unsignedByteToShort(buf[7]) / (0xffff + 1.0));

        rootDispersion = (buf[8] * 256.0)
                + unsignedByteToShort(buf[9])
                + (unsignedByteToShort(buf[10]) / (0xff + 1.0))
                + (unsignedByteToShort(buf[11]) / (0xffff + 1.0));

        referenceIdentifier[0] = buf[12];
        referenceIdentifier[1] = buf[13];
        referenceIdentifier[2] = buf[14];
        referenceIdentifier[3] = buf[15];

        referenceTimeStamp = byteArrayToDouble(buf, 16);
        originateTimeStamp = byteArrayToDouble(buf, 24);
        receiveTimeStamp = byteArrayToDouble(buf, 32);
        transmitTimeStamp = byteArrayToDouble(buf, 40);
    }

    public SNTPMessage() {
        mode = 3;
        transmitTimeStamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;
    }

    private double byteArrayToDouble(byte[] buf, int index) {
        double result = 0.0;

        for(int i = 0; i < 8; i++) {
            result += unsignedByteToShort(buf[i+index]) * Math.pow(2, (3 - i) * 8);
        }

        return result;
    }

    private short unsignedByteToShort(byte b) {
        if((b & 0x80) == 0x80) {
            return (short) (128 + (b & 0x7F));
        }
        return (short) b;
    }

    public byte[] toByteArray() {
        byte[] array = new byte[48];

        array[0] = (byte)(leapIndicator<<6 | (versionNumber<<3) | mode);
        array[1] = (byte) stratum;
        array[2] = (byte) pollInterval;
        array[3] = precision;

        int data = (int)(rootDelay * (0xff + 1));
        array[4] = (byte) ((data >> 24) & 0xff);
        array[5] = (byte) ((data >> 16) & 0xff);
        array[6] = (byte) ((data >> 8) & 0xff);
        array[7] = (byte) (data & 0xff);

        int rd = (int)(rootDispersion * (0xff + 1));
        array[8] = (byte) ((rd >> 24) & 0xff);
        array[9] = (byte) ((rd >> 16) & 0xff);
        array[10] = (byte) ((rd >> 8) & 0xff);
        array[11] = (byte) (rd & 0xff);

        array[12] = referenceIdentifier[0];
        array[13] = referenceIdentifier[1];
        array[14] = referenceIdentifier[2];
        array[15] = referenceIdentifier[3];

        doubleToByteArray(array, 16, referenceTimeStamp);
        doubleToByteArray(array, 24, originateTimeStamp);
        doubleToByteArray(array, 32, receiveTimeStamp);
        doubleToByteArray(array, 40, transmitTimeStamp);

        return array;
    }

    private void doubleToByteArray(byte[] array, int index, double data) {
        for(int i = 0; i < 8; i++) {
            array[index + i] = (byte) (data / Math.pow(2, (3 - i) * 8));
            data -= (double) (unsignedByteToShort(array[index + i]) * Math.pow(2, (3 - i) * 8));
        }
    }

    @Override
    public String toString() {
        return "SNTPMess: " + "\n"+
                "leapIndicator= " + leapIndicator + "\n"+
                "versionNumber= " + versionNumber + "\n"+
                "mode= " + mode + "\n"+
                "stratum= " + stratum + "\n"+
                "pollInterval= " + pollInterval + "\n"+
                "precision= " + precision + "\n"+
                "rootDelay= " + rootDelay + "\n"+
                "rootDispersion= " + new DecimalFormat("0.00").format(rootDispersion*1000) + "\n"+
                "referenceIdentifier= " + referenceIdentifierToString(referenceIdentifier, stratum, versionNumber) + "\n"+
                "referenceTimestamp= " + timestampToString(referenceTimeStamp) + "\n"+
                "originateTimestamp= " + timestampToString(originateTimeStamp) + "\n"+
                "receiveTimestamp= " + timestampToString(receiveTimeStamp) + "\n"+
                "transmitTimestamp= " + timestampToString(transmitTimeStamp) + "\n";

    }

    public double getReferenceTimeStamp() {
        return referenceTimeStamp;
    }

    public double getOriginateTimeStamp() {
        return originateTimeStamp;
    }

    public double getReceiveTimeStamp() {
        return receiveTimeStamp;
    }

    public double getTransmitTimeStamp() {
        return transmitTimeStamp;
    }

    public static String timestampToString(double timestamp)
    {
        if(timestamp==0) return "0";

        double utc = timestamp - (2208988800.0);

        // milliseconds
        long ms = (long) (utc * 1000.0);

        // date/time
        String date = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date(ms));

        // fraction
        double fraction = timestamp - ((long) timestamp);
        String fractionSting = new DecimalFormat(".000").format(fraction);

        return date + fractionSting;
    }

    public String referenceIdentifierToString(byte[] ref, short stratum, byte version)
    {
        if(stratum==0 || stratum==1)
        {
            return new String(ref);
        }
        else if(version==3)
        {
            return unsignedByteToShort(ref[0]) + "." +
                    unsignedByteToShort(ref[1]) + "." +
                    unsignedByteToShort(ref[2]) + "." +
                    unsignedByteToShort(ref[3]);
        }

        else if(version==4)
        {
            return "" + ((unsignedByteToShort(ref[0]) / 256.0) +
                    (unsignedByteToShort(ref[1]) / 65536.0) +
                    (unsignedByteToShort(ref[2]) / 16777216.0) +
                    (unsignedByteToShort(ref[3]) / 4294967296.0));
        }

        return "";
    }


}
