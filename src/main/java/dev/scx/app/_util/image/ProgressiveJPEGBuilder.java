package dev.scx.app._util.image;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/// 将图片转换为 渐进式 JPEG
///
/// @author scx567888
/// @version 0.0.1
public final class ProgressiveJPEGBuilder {

    public static ImageWriter getImageWriter(String formatName) {
        var writerIterator = ImageIO.getImageWritersByFormatName(formatName.trim().toLowerCase());
        if (!writerIterator.hasNext()) {
            throw new IllegalArgumentException("未找到对应格式的 ImageWriter , formatName : " + formatName);
        }
        return writerIterator.next();
    }

    public static byte[] toProgressiveJPEG(InputStream inputStream) throws IOException {
        var b = new ByteArrayOutputStream();
        toProgressiveJPEG(inputStream, b);
        return b.toByteArray();
    }

    public static void toProgressiveJPEG(InputStream inputStream, OutputStream outputStream) throws IOException {
        //源文件
        var sourceImage = ImageIO.read(inputStream);
        //图片 writer
        var jpegWriter = getImageWriter("jpeg");
        //参数
        var writeParam = jpegWriter.getDefaultWriteParam();
        //使用默认参数以启用渐进式图片
        writeParam.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);

        //开始转换
        try (var m = new MemoryCacheImageOutputStream(outputStream)) {
            jpegWriter.setOutput(m);
            jpegWriter.write(null, new IIOImage(sourceImage, null, null), writeParam);
            jpegWriter.dispose();
        }
    }

}
