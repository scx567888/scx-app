package dev.scx.app.test.util;

import dev.scx.app._old.util.os.OSHelper;
import org.testng.annotations.Test;

public class OSHelperTest {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        System.out.println(OSHelper.getOSInfo());
    }

}
