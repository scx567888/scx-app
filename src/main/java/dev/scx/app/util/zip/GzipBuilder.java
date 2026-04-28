package dev.scx.app.util.zip;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;

/// 用来 压缩 GZIP
///
/// @author scx567888
/// @version 0.0.1
public class GzipBuilder extends SequenceInputStream {

    public GzipBuilder(InputStream inputStream) {
        super(createGzipInputStream(inputStream));
    }

    public static Enumeration<InputStream> createGzipInputStream(InputStream inputStream) {
        var crc = new CRC32();
        var def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);

        var headerInputStream = new GzipHeaderInputStream();
        var dataInputStream = new GzipDataInputStream(inputStream, crc, def);
        var trailerInputStream = new GzipTrailerInputStream(crc, def);

        //整合三个流
        var in = List.of(headerInputStream, dataInputStream, trailerInputStream);
        return Collections.enumeration(in);

    }


    public static class GzipHeaderInputStream extends ByteArrayInputStream {

        /// GZIP header magic number.
        private static final int GZIP_MAGIC = 0x8b1f;

        // Represents the default "unknown" value for OS header, per RFC-1952
        private static final byte OS_UNKNOWN = (byte) 255;

        // HEADER
        private static final byte[] HEADER = new byte[]{
                (byte) GZIP_MAGIC,         // Magic number (short)
                (byte) (GZIP_MAGIC >> 8),  // Magic number (short)
                Deflater.DEFLATED,         // Compression method (CM)
                0,                         // Flags (FLG)
                0,                         // Modification time MTIME (int)
                0,                         // Modification time MTIME (int)
                0,                         // Modification time MTIME (int)
                0,                         // Modification time MTIME (int)
                0,                         // Extra flags (XFLG)
                OS_UNKNOWN                 // Operating system (OS)
        };

        public GzipHeaderInputStream() {
            super(HEADER);
        }

    }

    public static class GzipDataInputStream extends DeflaterInputStream {

        public GzipDataInputStream(InputStream in, CRC32 crc, Deflater def) {
            super(new CheckedInputStream(in, crc), def);
        }

    }


    public static class GzipTrailerInputStream extends LazyInputStream {

        /// Trailer size in bytes.
        private static final int TRAILER_SIZE = 8;

        private final CRC32 crc;
        private final Deflater def;

        public GzipTrailerInputStream(CRC32 crc, Deflater def) {
            this.crc = crc;
            this.def = def;
        }

        @Override
        public InputStream inputStream0() {
            byte[] trailer = new byte[TRAILER_SIZE];
            writeTrailer(trailer, 0);
            return new ByteArrayInputStream(trailer);
        }

        /// Writes GZIP member trailer to a byte array, starting at a given
        /// offset.
        private void writeTrailer(byte[] buf, int offset) {
            writeInt((int) crc.getValue(), buf, offset); // CRC-32 of uncompr. data
            // RFC 1952: Size of the original (uncompressed) input data modulo 2^32
            int iSize = (int) def.getBytesRead();
            writeInt(iSize, buf, offset + 4);
        }

        /// Writes integer in Intel byte order to a byte array, starting at a
        /// given offset.
        private void writeInt(int i, byte[] buf, int offset) {
            writeShort(i & 0xffff, buf, offset);
            writeShort((i >> 16) & 0xffff, buf, offset + 2);
        }

        /// Writes short integer in Intel byte order to a byte array, starting
        /// at a given offset
        private void writeShort(int s, byte[] buf, int offset) {
            buf[offset] = (byte) (s & 0xff);
            buf[offset + 1] = (byte) ((s >> 8) & 0xff);
        }

    }


}
