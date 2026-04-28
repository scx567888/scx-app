package dev.scx.app.test.util;

import dev.scx.app.util.IOUtils;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.testng.AssertJUnit.assertEquals;

public class IOUtilsTest {

    public static void main(String[] args) {
        testTransferByteBufferFullTransfer();
        testTransferByteBufferPartialTransfer();
    }

    @Test
    public static void testTransferByteBufferFullTransfer() {
        ByteBuffer source = ByteBuffer.allocate(10);
        ByteBuffer dest = ByteBuffer.allocate(10);
        for (int i = 0; i < 10; i = i + 1) {
            source.put((byte) i);
        }
        source.flip(); // Prepare source for reading

        int transferred = IOUtils.transferByteBuffer(source, dest);

        assertEquals(10, transferred);
        assertEquals(0, source.remaining());
        assertEquals(10, dest.position());
        dest.flip(); // Prepare destination for reading
        for (int i = 0; i < 10; i = i + 1) {
            assertEquals((byte) i, dest.get());
        }
    }

    @Test
    public static void testTransferByteBufferPartialTransfer() {
        ByteBuffer source = ByteBuffer.allocate(15);
        ByteBuffer dest = ByteBuffer.allocate(10);
        for (int i = 0; i < 15; i = i + 1) {
            source.put((byte) i);
        }
        source.flip(); // Prepare source for reading

        int transferred = IOUtils.transferByteBuffer(source, dest);

        assertEquals(10, transferred);
        assertEquals(5, source.remaining());
        assertEquals(10, dest.position());
        dest.flip(); // Prepare destination for reading
        for (int i = 0; i < 10; i = i + 1) {
            assertEquals((byte) i, dest.get());
        }
    }

}
