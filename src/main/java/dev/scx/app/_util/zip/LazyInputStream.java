package dev.scx.app._util.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/// 懒加载输入流
///
/// @author scx567888
/// @version 0.0.1
public abstract class LazyInputStream extends InputStream {

    private InputStream inputStream;

    public InputStream inputStream() {
        if (inputStream == null) {
            inputStream = inputStream0();
        }
        return inputStream;
    }

    protected abstract InputStream inputStream0();

    @Override
    public int read() throws IOException {
        return inputStream().read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream().read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream().read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return inputStream().readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return inputStream().readNBytes(len);
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return inputStream().readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream().skip(n);
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        inputStream().skipNBytes(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream().available();
    }

    @Override
    public void close() throws IOException {
        inputStream().close();
    }

    @Override
    public void mark(int readlimit) {
        inputStream().mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        inputStream().reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream().markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return inputStream().transferTo(out);
    }

}
