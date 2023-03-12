package ru.leonidm.ormm.utils;

public final class ArrayConverter {

    private ArrayConverter() {
    }

    public static byte[] toBytes(boolean[] array) {
        byte[] out = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = (byte) (array[i] ? 1 : 0);
        }
        return out;
    }

    public static byte[] toBytes(Boolean[] array) {
        byte[] out = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = (byte) (array[i] ? 1 : 0);
        }
        return out;
    }

    public static byte[] toBytes(Byte[] array) {
        byte[] out = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = array[i];
        }
        return out;
    }

    public static byte[] toBytes(short[] array) {
        byte[] out = new byte[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            out[2 * i] = (byte) ((array[i] >> 8) & 0xFF);
            out[2 * i + 1] = (byte) (array[i] & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(Short[] array) {
        byte[] out = new byte[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            out[2 * i] = (byte) ((array[i] >> 8) & 0xFF);
            out[2 * i + 1] = (byte) (array[i] & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(int[] array) {
        byte[] out = new byte[array.length * 4];
        for (int i = 0; i < array.length; i++) {
            out[4 * i] = (byte) ((array[i] >> 24) & 0xFF);
            out[4 * i + 1] = (byte) ((array[i] >> 16) & 0xFF);
            out[4 * i + 2] = (byte) ((array[i] >> 8) & 0xFF);
            out[4 * i + 3] = (byte) (array[i] & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(Integer[] array) {
        byte[] out = new byte[array.length * 4];
        for (int i = 0; i < array.length; i++) {
            out[4 * i] = (byte) ((array[i] >> 24) & 0xFF);
            out[4 * i + 1] = (byte) ((array[i] >> 16) & 0xFF);
            out[4 * i + 2] = (byte) ((array[i] >> 8) & 0xFF);
            out[4 * i + 3] = (byte) (array[i] & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(long[] array) {
        byte[] out = new byte[array.length * 8];
        for (int i = 0; i < array.length; i++) {
            out[8 * i] = (byte) ((array[i] >> 56) & 0xFF);
            out[8 * i + 1] = (byte) ((array[i] >> 48) & 0xFF);
            out[8 * i + 2] = (byte) ((array[i] >> 40) & 0xFF);
            out[8 * i + 3] = (byte) ((array[i] >> 32) & 0xFF);
            out[8 * i + 4] = (byte) ((array[i] >> 24) & 0xFF);
            out[8 * i + 5] = (byte) ((array[i] >> 16) & 0xFF);
            out[8 * i + 6] = (byte) ((array[i] >> 8) & 0xFF);
            out[8 * i + 7] = (byte) (array[i] & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(Long[] array) {
        byte[] out = new byte[array.length * 8];
        for (int i = 0; i < array.length; i++) {
            out[8 * i] = (byte) ((array[i] >> 56) & 0xFF);
            out[8 * i + 1] = (byte) ((array[i] >> 48) & 0xFF);
            out[8 * i + 2] = (byte) ((array[i] >> 40) & 0xFF);
            out[8 * i + 3] = (byte) ((array[i] >> 32) & 0xFF);
            out[8 * i + 4] = (byte) ((array[i] >> 24) & 0xFF);
            out[8 * i + 5] = (byte) ((array[i] >> 16) & 0xFF);
            out[8 * i + 6] = (byte) ((array[i] >> 8) & 0xFF);
            out[8 * i + 7] = (byte) (array[i] & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(float[] array) {
        byte[] out = new byte[array.length * 4];
        for (int i = 0; i < array.length; i++) {
            int intBits = Float.floatToIntBits(array[i]);
            out[4 * i] = (byte) ((intBits >> 24) & 0xFF);
            out[4 * i + 1] = (byte) ((intBits >> 16) & 0xFF);
            out[4 * i + 2] = (byte) ((intBits >> 8) & 0xFF);
            out[4 * i + 3] = (byte) (intBits & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(Float[] array) {
        byte[] out = new byte[array.length * 4];
        for (int i = 0; i < array.length; i++) {
            int intBits = Float.floatToIntBits(array[i]);
            out[4 * i] = (byte) ((intBits >> 24) & 0xFF);
            out[4 * i + 1] = (byte) ((intBits >> 16) & 0xFF);
            out[4 * i + 2] = (byte) ((intBits >> 8) & 0xFF);
            out[4 * i + 3] = (byte) (intBits & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(double[] array) {
        byte[] out = new byte[array.length * 8];
        for (int i = 0; i < array.length; i++) {
            long longBits = Double.doubleToLongBits(array[i]);
            out[8 * i] = (byte) ((longBits >> 56) & 0xFF);
            out[8 * i + 1] = (byte) ((longBits >> 48) & 0xFF);
            out[8 * i + 2] = (byte) ((longBits >> 40) & 0xFF);
            out[8 * i + 3] = (byte) ((longBits >> 32) & 0xFF);
            out[8 * i + 4] = (byte) ((longBits >> 24) & 0xFF);
            out[8 * i + 5] = (byte) ((longBits >> 16) & 0xFF);
            out[8 * i + 6] = (byte) ((longBits >> 8) & 0xFF);
            out[8 * i + 7] = (byte) (longBits & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(Double[] array) {
        byte[] out = new byte[array.length * 8];
        for (int i = 0; i < array.length; i++) {
            long longBits = Double.doubleToLongBits(array[i]);
            out[8 * i] = (byte) ((longBits >> 56) & 0xFF);
            out[8 * i + 1] = (byte) ((longBits >> 48) & 0xFF);
            out[8 * i + 2] = (byte) ((longBits >> 40) & 0xFF);
            out[8 * i + 3] = (byte) ((longBits >> 32) & 0xFF);
            out[8 * i + 4] = (byte) ((longBits >> 24) & 0xFF);
            out[8 * i + 5] = (byte) ((longBits >> 16) & 0xFF);
            out[8 * i + 6] = (byte) ((longBits >> 8) & 0xFF);
            out[8 * i + 7] = (byte) (longBits & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(char[] array) {
        byte[] out = new byte[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            int converted = array[i];
            out[2 * i] = (byte) ((converted >> 8) & 0xFF);
            out[2 * i + 1] = (byte) (converted & 0xFF);
        }
        return out;
    }

    public static byte[] toBytes(Character[] array) {
        byte[] out = new byte[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            int converted = array[i];
            out[2 * i] = (byte) ((converted >> 8) & 0xFF);
            out[2 * i + 1] = (byte) (converted & 0xFF);
        }
        return out;
    }

    public static boolean[] toBooleans(byte[] bytes) {
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("Given array must have 2*x bytes, but it has %s".formatted(bytes.length));
        }

        boolean[] out = new boolean[bytes.length];

        for (short i = 0; i < out.length; i++) {
            out[i] = bytes[i] > 0;
        }

        return out;
    }

    public static boolean[] toBooleans(Byte[] bytes) {
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("Given array must have 2*x bytes, but it has %s".formatted(bytes.length));
        }

        boolean[] out = new boolean[bytes.length];

        for (short i = 0; i < out.length; i++) {
            out[i] = bytes[i] > 0;
        }

        return out;
    }

    public static short[] toShorts(byte[] bytes) {
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("Given array must have 2*x bytes, but it has %s".formatted(bytes.length));
        }

        short[] out = new short[bytes.length / 2];

        for (int i = 0; i < out.length; i++) {
            out[i] = (short) (((bytes[2 * i] & 0xFF) << 8)
                    + (bytes[2 * i + 1] & 0xFF));
        }

        return out;
    }

    public static short[] toShorts(Byte[] bytes) {
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("Given array must have 2*x bytes, but it has %s".formatted(bytes.length));
        }

        short[] out = new short[bytes.length / 2];

        for (short i = 0; i < out.length; i++) {
            out[i] = (short) (((bytes[2 * i] & 0xFF) << 8)
                    + (bytes[2 * i + 1] & 0xFF));
        }

        return out;
    }

    public static int[] toInts(byte[] bytes) {
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("Given array must have 4*x bytes, but it has %s".formatted(bytes.length));
        }

        int[] out = new int[bytes.length / 4];

        for (int i = 0; i < out.length; i++) {
            out[i] = ((bytes[4 * i] & 0xFF) << 24)
                    + ((bytes[4 * i + 1] & 0xFF) << 16)
                    + ((bytes[4 * i + 2] & 0xFF) << 8)
                    + (bytes[4 * i + 3] & 0xFF);
        }

        return out;
    }

    public static int[] toInts(Byte[] bytes) {
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("Given array must have 4*x bytes, but it has %s".formatted(bytes.length));
        }

        int[] out = new int[bytes.length / 4];

        for (int i = 0; i < out.length; i++) {
            out[i] = ((bytes[4 * i] & 0xFF) << 24)
                    + ((bytes[4 * i + 1] & 0xFF) << 16)
                    + ((bytes[4 * i + 2] & 0xFF) << 8)
                    + (bytes[4 * i + 3] & 0xFF);
        }

        return out;
    }

    public static long[] toLongs(byte[] bytes) {
        if (bytes.length % 8 != 0) {
            throw new IllegalArgumentException("Given array must have 8*x bytes, but it has %s".formatted(bytes.length));
        }

        long[] out = new long[bytes.length / 8];

        for (int i = 0; i < out.length; i++) {
            out[i] = ((long) (bytes[8 * i] & 0xFF) << 56)
                    + ((long) (bytes[8 * i + 1] & 0xFF) << 48)
                    + ((long) (bytes[8 * i + 2] & 0xFF) << 40)
                    + ((long) (bytes[8 * i + 3] & 0xFF) << 32)
                    + ((long) (bytes[8 * i + 4] & 0xFF) << 24)
                    + ((bytes[8 * i + 5] & 0xFF) << 16)
                    + ((bytes[8 * i + 6] & 0xFF) << 8)
                    + (bytes[8 * i + 7] & 0xFF);
        }

        return out;
    }

    public static long[] toLongs(Byte[] bytes) {
        if (bytes.length % 8 != 0) {
            throw new IllegalArgumentException("Given array must have 8*x bytes, but it has %s".formatted(bytes.length));
        }

        long[] out = new long[bytes.length / 8];

        for (int i = 0; i < out.length; i++) {
            out[i] = ((long) (bytes[8 * i] & 0xFF) << 56)
                    + ((long) (bytes[8 * i + 1] & 0xFF) << 48)
                    + ((long) (bytes[8 * i + 2] & 0xFF) << 40)
                    + ((long) (bytes[8 * i + 3] & 0xFF) << 32)
                    + ((long) (bytes[8 * i + 4] & 0xFF) << 24)
                    + ((bytes[8 * i + 5] & 0xFF) << 16)
                    + ((bytes[8 * i + 6] & 0xFF) << 8)
                    + (bytes[8 * i + 7] & 0xFF);
        }

        return out;
    }

    public static float[] toFloats(byte[] bytes) {
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("Given array must have 4*x bytes, but it has %s".formatted(bytes.length));
        }

        float[] out = new float[bytes.length / 4];

        for (int i = 0; i < out.length; i++) {
            out[i] = Float.intBitsToFloat(((bytes[4 * i] & 0xFF) << 24)
                    + ((bytes[4 * i + 1] & 0xFF) << 16)
                    + ((bytes[4 * i + 2] & 0xFF) << 8)
                    + (bytes[4 * i + 3] & 0xFF));
        }

        return out;
    }

    public static float[] toFloats(Byte[] bytes) {
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("Given array must have 4*x bytes, but it has %s".formatted(bytes.length));
        }

        float[] out = new float[bytes.length / 4];

        for (int i = 0; i < out.length; i++) {
            out[i] = Float.intBitsToFloat(((bytes[4 * i] & 0xFF) << 24)
                    + ((bytes[4 * i + 1] & 0xFF) << 16)
                    + ((bytes[4 * i + 2] & 0xFF) << 8)
                    + (bytes[4 * i + 3] & 0xFF));
        }

        return out;
    }

    public static double[] toDoubles(byte[] bytes) {
        if (bytes.length % 8 != 0) {
            throw new IllegalArgumentException("Given array must have 8*x bytes, but it has %s".formatted(bytes.length));
        }

        double[] out = new double[bytes.length / 8];

        for (int i = 0; i < out.length; i++) {
            out[i] = Double.longBitsToDouble(((long) (bytes[8 * i] & 0xFF) << 56)
                    + ((long) (bytes[8 * i + 1] & 0xFF) << 48)
                    + ((long) (bytes[8 * i + 2] & 0xFF) << 40)
                    + ((long) (bytes[8 * i + 3] & 0xFF) << 32)
                    + ((long) (bytes[8 * i + 4] & 0xFF) << 24)
                    + ((bytes[8 * i + 5] & 0xFF) << 16)
                    + ((bytes[8 * i + 6] & 0xFF) << 8)
                    + (bytes[8 * i + 7] & 0xFF));
        }

        return out;
    }

    public static double[] toDoubles(Byte[] bytes) {
        if (bytes.length % 8 != 0) {
            throw new IllegalArgumentException("Given array must have 8*x bytes, but it has %s".formatted(bytes.length));
        }

        double[] out = new double[bytes.length / 8];

        for (int i = 0; i < out.length; i++) {
            out[i] = Double.longBitsToDouble(((long) (bytes[8 * i] & 0xFF) << 56)
                    + ((long) (bytes[8 * i + 1] & 0xFF) << 48)
                    + ((long) (bytes[8 * i + 2] & 0xFF) << 40)
                    + ((long) (bytes[8 * i + 3] & 0xFF) << 32)
                    + ((long) (bytes[8 * i + 4] & 0xFF) << 24)
                    + ((bytes[8 * i + 5] & 0xFF) << 16)
                    + ((bytes[8 * i + 6] & 0xFF) << 8)
                    + (bytes[8 * i + 7] & 0xFF));
        }

        return out;
    }

    public static char[] toChars(byte[] bytes) {
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("Given array must have 2*x bytes, but it has %s".formatted(bytes.length));
        }

        char[] out = new char[bytes.length / 2];

        for (int i = 0; i < out.length; i++) {
            out[i] = (char) (((bytes[2 * i] & 0xFF) << 8)
                    + (bytes[2 * i + 1] & 0xFF));
        }

        return out;
    }

    public static char[] toChars(Byte[] bytes) {
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("Given array must have 2*x bytes, but it has %s".formatted(bytes.length));
        }

        char[] out = new char[bytes.length / 2];

        for (short i = 0; i < out.length; i++) {
            out[i] = (char) (((bytes[2 * i] & 0xFF) << 8)
                    + (bytes[2 * i + 1] & 0xFF));
        }

        return out;
    }

    public static Boolean[] toBoxed(boolean[] array) {
        Boolean[] out = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = array[i];
        }
        return out;
    }

    public static Byte[] toBoxed(byte[] array) {
        Byte[] out = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = array[i];
        }
        return out;
    }

    public static Short[] toBoxed(short[] array) {
        Short[] out = new Short[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = array[i];
        }
        return out;
    }

    public static Integer[] toBoxed(int[] array) {
        Integer[] out = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = array[i];
        }
        return out;
    }

    public static Long[] toBoxed(long[] array) {
        Long[] out = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = array[i];
        }
        return out;
    }

    public static Float[] toBoxed(float[] array) {
        Float[] out = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = array[i];
        }
        return out;
    }

    public static Double[] toBoxed(double[] array) {
        Double[] out = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = array[i];
        }
        return out;
    }

    public static Character[] toBoxed(char[] array) {
        Character[] out = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = array[i];
        }
        return out;
    }
}
