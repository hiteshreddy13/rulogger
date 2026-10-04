package com.rulogger;

import java.nio.ByteBuffer;

/**
 * Utility for formatting log strings with "{}" placeholders directly into a DirectByteBuffer,
 * completely avoiding the creation of temporary Strings or byte arrays to keep the GC clean.
 */
public class ZeroCopyFormatter {

    // Use ThreadLocal to avoid synchronization and buffer reallocation overhead
    private static final ThreadLocal<ByteBuffer> BUFFER_POOL = ThreadLocal.withInitial(() ->
        ByteBuffer.allocateDirect(4096) // 4KB per thread should be enough for a log message
    );

    /**
     * Formats the template and arguments directly into the thread-local DirectByteBuffer.
     * @param template The log string template containing "{}" placeholders.
     * @param args The arguments to replace the placeholders.
     * @return The populated ByteBuffer ready for JNI. The position is 0, limit is the data length.
     */
    public static ByteBuffer format(String template, Object... args) {
        ByteBuffer buffer = BUFFER_POOL.get();
        buffer.clear();

        if (template == null) {
            writeAscii(buffer, "null");
            buffer.flip();
            return buffer;
        }

        int argIndex = 0;
        int len = template.length();
        for (int i = 0; i < len; i++) {
            char c = template.charAt(i);

            // Check for placeholder "{}"
            if (c == '{' && i + 1 < len && template.charAt(i + 1) == '}') {
                if (args != null && argIndex < args.length) {
                    writeObject(buffer, args[argIndex++]);
                } else {
                    writeAscii(buffer, "{}"); // Not enough arguments
                }
                i++; // Skip the '}'
            } else {
                // Ensure space
                if (buffer.remaining() < 1) {
                    break; // Truncate if buffer is full for simplicity in this demo
                }
                // Fast path for ASCII to avoid String.getBytes
                if (c < 128) {
                    buffer.put((byte) c);
                } else {
                    buffer.put((byte) '?'); // Fallback for non-ascii in this zero-alloc demo
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    private static void writeObject(ByteBuffer buffer, Object arg) {
        if (arg == null) {
            writeAscii(buffer, "null");
        } else if (arg instanceof Integer) {
            writeInt(buffer, (Integer) arg);
        } else if (arg instanceof Long) {
            writeLong(buffer, (Long) arg);
        } else if (arg instanceof String) {
            writeAscii(buffer, (String) arg);
        } else {
            // Fallback: will cause allocation via toString(), but handles unknown types.
            writeAscii(buffer, arg.toString());
        }
    }

    private static void writeAscii(ByteBuffer buffer, String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (buffer.remaining() == 0) break;
            char c = str.charAt(i);
            buffer.put((byte) (c < 128 ? c : '?'));
        }
    }

    // Zero-allocation integer writing logic directly to bytes
    private static void writeInt(ByteBuffer buffer, int value) {
        if (value == Integer.MIN_VALUE) {
            writeAscii(buffer, "-2147483648");
            return;
        }
        if (value < 0) {
            if (buffer.remaining() == 0) return;
            buffer.put((byte) '-');
            value = -value;
        }
        if (value == 0) {
            if (buffer.remaining() == 0) return;
            buffer.put((byte) '0');
            return;
        }

        // Find length
        int temp = value;
        int length = 0;
        while (temp > 0) {
            length++;
            temp /= 10;
        }

        if (buffer.remaining() < length) return;

        int startPos = buffer.position();
        buffer.position(startPos + length);

        int writePos = startPos + length - 1;
        while (value > 0) {
            buffer.put(writePos--, (byte) ('0' + (value % 10)));
            value /= 10;
        }
    }

    private static void writeLong(ByteBuffer buffer, long value) {
        if (value == Long.MIN_VALUE) {
            writeAscii(buffer, "-9223372036854775808");
            return;
        }
        if (value < 0) {
            if (buffer.remaining() == 0) return;
            buffer.put((byte) '-');
            value = -value;
        }
        if (value == 0) {
            if (buffer.remaining() == 0) return;
            buffer.put((byte) '0');
            return;
        }

        long temp = value;
        int length = 0;
        while (temp > 0) {
            length++;
            temp /= 10;
        }

        if (buffer.remaining() < length) return;

        int startPos = buffer.position();
        buffer.position(startPos + length);

        int writePos = startPos + length - 1;
        while (value > 0) {
            buffer.put(writePos--, (byte) ('0' + (value % 10)));
            value /= 10;
        }
    }
}
