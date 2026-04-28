package dev.scx.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/// IOUtils
///
/// @author scx567888
/// @version 0.0.1
public final class IOUtils {

    public static byte[] trimBytes(byte[] bytes, int offset, int length) {
        if (offset == 0 && length == bytes.length) {
            return bytes;
        }
        var data = new byte[length];
        System.arraycopy(bytes, offset, data, 0, length);
        return data;
    }

    public static void writeFileToOut(Path path, OutputStream out) throws IOException {
        try (var inputStream = Files.newInputStream(path)) {
            inputStream.transferTo(out);
        }
    }

    public static void writeFileToOut(Path path, OutputStream out, long offset, long length) throws IOException {
        try (var raf = new RandomAccessFile(path.toFile(), "r")) {
            //先移动文件指针
            raf.seek(offset);
            //循环发送
            var buffer = new byte[8192];
            while (length > 0) {
                int i = raf.read(buffer, 0, (int) Math.min(buffer.length, length));
                if (i == -1) {
                    break; // 处理文件结束情况
                }
                out.write(buffer, 0, i);
                length -= i;
            }
        }
    }

    public static void readInToFile(InputStream in, Path path, OpenOption... options) throws IOException {
        try (var os = Files.newOutputStream(path, options)) {
            in.transferTo(os);
        }
    }

    public static void readInToFile(InputStream in, Path path, long offset, long length) throws IOException {
        try (var raf = new RandomAccessFile(path.toFile(), "rw")) {
            raf.seek(offset);

            var buffer = new byte[8192];
            while (length > 0) {
                int i = in.read(buffer, 0, (int) Math.min(buffer.length, length));
                if (i == -1) {
                    break;
                }
                raf.write(buffer, 0, i);
                length -= i;
            }
        }
    }

    public static int transferByteBuffer(ByteBuffer source, ByteBuffer dest) {
        int sourceRemaining = source.remaining();
        int destRemaining = dest.remaining();
        if (sourceRemaining > destRemaining) {
            // 设置源缓冲区的限制为当前 position 加上目标缓冲区的剩余容量
            int originalLimit = source.limit();
            // 限流复制
            source.limit(source.position() + destRemaining);
            dest.put(source);
            source.limit(originalLimit); // 恢复原始限制
            return destRemaining;
        } else {
            dest.put(source);
            return sourceRemaining;
        }
    }

}
