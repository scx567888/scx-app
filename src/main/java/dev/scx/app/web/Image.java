package dev.scx.app.web;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.exception.BadRequestException;
import dev.scx.http.routing.x.static_files.StaticFilesSupport;
import dev.scx.http.routing.x.static_files.cache_control.CacheControl;
import dev.scx.web.ScxWeb;
import dev.scx.web.result.WebResult;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.AbsoluteSize;
import net.coobird.thumbnailator.geometry.Position;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static dev.scx.http.headers.HttpHeaderName.CACHE_CONTROL;
import static dev.scx.web.result.Binary.getMediaTypeByFileName;

/// Image
///
/// @author scx567888
/// @version 0.0.1
public final class Image implements WebResult {

    private final File file;
    private final Integer width;
    private final Integer height;
    private final Position position;

    // 缓存
    private byte[] buffer;

    private Image(File file, Integer width, Integer height, Position position) {
        this.file = file;
        this.width = width;
        this.height = height;
        this.position = position;
    }

    public static Image of(File file) {
        return new Image(file, null, null, null);
    }

    public static Image of(File file, Integer width, Integer height, Position position) {
        return new Image(file, width, height, position);
    }

    /// 裁剪后的图片
    private static byte[] getBuffer(File file, Integer width, Integer height, Position position) {
        try (var out = new ByteArrayOutputStream()) {
            var image = Thumbnails.of(file).scale(1.0).asBufferedImage();
            var imageHeight = image.getHeight();
            var imageWidth = image.getWidth();

            var croppedHeight = (height == null || height > imageHeight || height <= 0) ? imageHeight : height;
            var croppedWidth = (width == null || width > imageWidth || width <= 0) ? imageWidth : width;

            var absoluteSize = new AbsoluteSize(croppedWidth, croppedHeight);
            if (position != null) {
                Thumbnails.of(file).sourceRegion(position, absoluteSize).size(croppedWidth, croppedHeight).keepAspectRatio(false).toOutputStream(out);
            } else {
                Thumbnails.of(file).size(croppedWidth, croppedHeight).keepAspectRatio(false).toOutputStream(out);
            }

            return out.toByteArray();
        } catch (Exception e) {
            throw new BadRequestException(e);
        }
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) throws BadRequestException {
        var mediaType = getMediaTypeByFileName(file.getName());
        var cacheControl = CacheControl.of("public", "immutable", "max-age=2628000").encode();

        var response = request.response();
        // 设置缓存 减少服务器压力
        response.contentType(mediaType);
        response.setHeader(CACHE_CONTROL, cacheControl);

        // 发送原图
        if (width == null && height == null) {
            StaticFilesSupport.sendFile(file, request);
        } else {
            if (buffer == null) {
                buffer = getBuffer(file, width, height, position);
            }
            response.send(buffer);
        }

    }

}
