package dev.scx.app.test;

import dev.scx.app.util.zip.ZipBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class ZipTest {

    public static void main(String[] args) throws Exception {
        test1();
    }

    @Test
    public static void test1() throws Exception {
        var v = new ZipBuilder();
        v.put("\\空目录\\");
        v.put("///根目录\\scx.txt/", "scxscxscxscx".getBytes(StandardCharsets.UTF_8));
        v.put("根目录\\scx123.txt", "scxscxscxscx".getBytes(StandardCharsets.UTF_8));
        v.put("根目录\\第二层目录\\第三层目录\\\\\\\\\\文件.txt", "文件文件文件".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(v.toBytes().length, 615);
    }

}
