package dev.scx.app.test.util;

import dev.scx.app._util.zip.URIUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class URIUtilsTest {

    public static void main(String[] args) {
        test1();
        test2();
        test3();
    }

    @Test
    public static void test1() {
        var a = "";
        var b = "////\\\\\\///\\\\";
        var c = " a/s/d/s/d/s/d/s/d/s/d/s/d/ad/f ";
        var d = "\\ff\\ff✌👏  🤷g\\fgb\\fga🎶 😢 💕‍♀️b\\fb\\fgb\\fgb\\fgb\\ff\\//a//🤷‍♀️a//a//////fgbhfbhj/dvhdbfvhbdbbjhbjh//fnjdfvk❤🎶🤦‍♂️😢👍🎉jdv";
        var e = "666666";
        var f = "           ";
        var g = "从扽记 者闹adf jvnkdvj nkddvj nkdfn kdf n卡框反 正那 狂风只能//\\////\\";

        var aa = "";
        var bb = "/";
        var cc = " a/s/d/s/d/s/d/s/d/s/d/s/d/ad/f ";
        var dd = "/ff/ff✌👏  🤷g/fgb/fga🎶 😢 💕‍♀️b/fb/fgb/fgb/fgb/ff/a/🤷‍♀️a/a/fgbhfbhj/dvhdbfvhbdbbjhbjh/fnjdfvk❤🎶🤦‍♂️😢👍🎉jdv";
        var ee = "666666";
        var ff = "           ";
        var gg = "从扽记 者闹adf jvnkdvj nkddvj nkdfn kdf n卡框反 正那 狂风只能/";

        var a1 = URIUtils.normalize(a);
        var b1 = URIUtils.normalize(b);
        var c1 = URIUtils.normalize(c);
        var d1 = URIUtils.normalize(d);
        var e1 = URIUtils.normalize(e);
        var f1 = URIUtils.normalize(f);
        var g1 = URIUtils.normalize(g);

        Assert.assertEquals(a1, aa);
        Assert.assertEquals(b1, bb);
        Assert.assertEquals(c1, cc);
        Assert.assertEquals(d1, dd);
        Assert.assertEquals(e1, ee);
        Assert.assertEquals(f1, ff);
        Assert.assertEquals(g1, gg);
    }

    @Test
    public static void test2() {
        var a = "";
        var b = "////\\\\\\///\\\\";
        var c = " a/s/d/s/d/s/d/s/d/s/d/s/d/ad/f ";
        var d = "\\ff\\ff✌👏  🤷g\\fgb\\fga🎶 😢 💕‍♀️b\\fb\\fgb\\fgb\\fgb\\ff\\//a//🤷‍♀️a//a//////fgbhfbhj/dvhdbfvhbdbbjhbjh//fnjdfvk❤🎶🤦‍♂️😢👍🎉jdv";
        var e = "666666";
        var f = "           ";
        var g = "从扽记 者闹adf jvnkdvj nkddvj nkdfn kdf n卡框反 正那 狂风只能//\\////\\";

        var aa = "/ a/s/d/s/d/s/d/s/d/s/d/s/d/ad/f /ff/ff✌👏  🤷g/fgb/fga🎶 😢 💕‍♀️b/fb/fgb/fgb/fgb/ff/a/🤷‍♀️a/a/fgbhfbhj/dvhdbfvhbdbbjhbjh/fnjdfvk❤🎶🤦‍♂️😢👍🎉jdv/666666/从扽记 者闹adf jvnkdvj nkddvj nkdfn kdf n卡框反 正那 狂风只能/";

        var a1 = URIUtils.join(a, b, c, d, e, f, g);

        Assert.assertEquals(a1, aa);
    }

    @Test
    public static void test3() {
        var a = "\\\\\\\\///////a/💃////////\\\\\\\\\\";
        var a1 = URIUtils.trimSlash(a);
        var a2 = URIUtils.trimSlashEnd(a);
        var a3 = URIUtils.trimSlashStart(a);
        var a4 = URIUtils.addSlashStart(a);
        var a5 = URIUtils.addSlashEnd(a);
        Assert.assertEquals(a1, "a/💃");
        Assert.assertEquals(a2, "\\\\\\\\///////a/💃");
        Assert.assertEquals(a3, "a/💃////////\\\\\\\\\\");
        Assert.assertEquals(a4, "/a/💃////////\\\\\\\\\\");
        Assert.assertEquals(a5, "\\\\\\\\///////a/💃/");
    }

}
