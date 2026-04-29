package dev.scx.app.test.website;

import dev.scx.app.util.HttpHelper;
import dev.scx.app.ScxAppContext;
import dev.scx.app.util.ScxHttpClientHelper;
import dev.scx.app.web.Template;
import dev.scx.app.util.zip.ZipBuilder;
import dev.scx.app.test.car.CarService;
import dev.scx.app.test.person.Person;
import dev.scx.app.test.person.PersonService;
import dev.scx.di.annotation.Inject;
import dev.scx.digest.ScxDigest;
import dev.scx.http.media.multi_part.MultiPartPart;
import dev.scx.http.media_type.FileFormat;
import dev.scx.http.method.HttpMethod;
import dev.scx.http.routing.RoutingContext;
import dev.scx.random.ScxRandom;
import dev.scx.web.ScxWeb;
import dev.scx.web.annotation.Part;
import dev.scx.web.annotation.QueryParam;
import dev.scx.web.annotation.Route;
import dev.scx.web.annotation.Routes;
import dev.scx.web.result.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import static dev.scx.io.ScxIO.byteOutputToOutputStream;
import static dev.scx.random.ScxRandom.NUMBER_AND_LETTER;

/// 简单测试
///
/// @author scx567888
/// @version 0.0.1
@Routes
public class WebSiteController {

    final CarService carService;

    @Inject
    public CarService carService1;

    public WebSiteController(CarService carService) {
        this.carService = carService;
    }

    @Route(value = "/test0", methods = HttpMethod.POST)
    public Object test0(@QueryParam String name,
                               @QueryParam Integer age,
                               @Part MultiPartPart content,
                               @Part MultiPartPart content1) {
        System.err.println("客户端 IP :" + HttpHelper.getRequestIP(ScxWeb.routingContext().request()));
        return Map.of("now", LocalDateTime.now(),
                "name", name,
                "age", age,
                "content", content.asString(),
                "content1", content1.asString());
    }

    @Route(value = "/test-transaction",methods = HttpMethod.GET)
    public Html TestTransaction(RoutingContext ctx) throws Exception {
        var personService = ScxAppContext.getBean(PersonService.class);
        var p1 = personService.add(new Person().setMoney(100));
        var p2 = personService.add(new Person().setMoney(200));
        var sb = new StringBuilder();
        sb.append("转账开始前: ").append("p1(").append(p1.money).append(") p2(").append(p2.money).append(")</br>");
        try {
            //模拟 转账
            ScxAppContext.autoTransaction(() -> {

                // todo 此处需要支持并发处理 及 同时执行 给 p1 扣钱 和 给 p2 加钱
                //给 p1 扣钱
                p1.money = p1.money - 50;
                var p11 = personService.update(p1);

                //给 p2 加钱
                p2.money = p2.money + 50;
                var p21 = personService.update(p2);

                sb.append("转账中: ").append("p1(")
                        .append(p11.money)
                        .append(") p2(")
                        .append(p21.money).append(")</br>");

                throw new RuntimeException("模拟发生异常 !!!");
            });
        } catch (Exception e) {
            var p11 = personService.get(p1.id);
            var p21 = personService.get(p2.id);
            sb.append("出错了 回滚后: ").append("p1(").append(p11.money).append(") p2(").append(p21.money).append(")</br>");
        }
        return Html.of(sb.toString());
    }

    /// 测试!!!
    ///
    /// @return 页面
    /// @throws java.io.IOException if any.
    @Route(methods = HttpMethod.GET, priority = 10)
    public Template TestIndex(RoutingContext c) throws IOException {
        System.err.println("最后一次匹配的路由" + c.request().path());
        var index = Template.of("index.html");
        index.add("name", c.data().get("name"));
        index.add("age", 22);
        return index;
    }

    /// 多个路由
    ///
    /// @param c a
    /// @throws IOException a
    @Route(methods = HttpMethod.GET, priority = 5)
    public void TestIndex1(RoutingContext c) throws Throwable {
        System.err.println("第二个匹配的路由" + c.request().path());
        c.data().put("name", "小明");
        c.next();
    }

    /// 这里如果 order 小于其他的 order 根据 其会因其路径为(模糊路径) 在最后进行才进行匹配
    ///
    /// @param c a
    /// @throws IOException a
    @Route(value = "/*", methods = HttpMethod.GET, priority = 1)
    public void TestIndex1a(RoutingContext c) throws Throwable {
        System.err.println("两个 carService 是否相等 " + (carService == carService1));
        System.err.println("第一个匹配的路由" + c.request().path());
        c.next();
    }

    /// 测试!!!
    ///
    /// @return a
    @Route(value = "/baidu", methods = HttpMethod.GET)
    public Html TestHttpUtils() throws IOException, InterruptedException {
        var baiduHtml = ScxHttpClientHelper.get("https://www.baidu.com/").asString();
        return Html.of(baiduHtml);
    }

    /// 测试!!!
    ///
    /// @return a
    @Route(value = "/download", methods = HttpMethod.GET)
    public Binary TestDownload() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 9999; i = i + 1) {
            s.append("这是文字 ").append(i).append(", ");
        }
        return Binary.download(s.toString().getBytes(StandardCharsets.UTF_8), "测试中 + - ~!文 a😊😂 🤣 ghj ❤😍😒👌.txt");
    }

    /// 测试!!!
    ///
    /// @return a [BaseVo] object
    @Route(value = "/raw", methods = HttpMethod.GET)
    public WebResult TestRaw() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 9999; i = i + 1) {
            s.append("这是文字 ").append(i).append(", ");
        }
        return Binary.inline(s.toString().getBytes(StandardCharsets.UTF_8), FileFormat.TXT);
    }

    /// 测试!!!
    ///
    /// @return a [java.lang.String] object
    @Route(value = "/md5", methods = HttpMethod.GET)
    public String TestMd5() {
        return ScxDigest.md5Hex("123");
    }

    /// 测试!!!
    ///
    /// @return a [java.lang.String] object
    @Route(value = "/get-random-code", methods = HttpMethod.GET)
    public String getRandomCode() {
        return ScxRandom.randomString(9999, NUMBER_AND_LETTER);
    }

    /// 测试!!!
    ///
    /// @return a [BaseVo] object
    @Route(value = "/big-json", methods = HttpMethod.GET)
    public WebResult bigJson() {
        var users = carService1.find();
        return Json.of(users);
    }

    @Route(value = "/big-xml", methods = HttpMethod.GET)
    public WebResult bigXml() {
        var users = carService1.find();
        return Xml.of(users);
    }

    /// 测试!!!
    ///
    /// @return a [BaseVo] object
    @Route(value = "/a",methods = HttpMethod.GET)
    public Object a() {
        return Map.of("items", "a");
    }

    /// 测试!!!
    ///
    /// @return a [BaseVo] object
    @Route(value = "/a", methods = HttpMethod.GET)
    public Object b() {
        return Map.of("items", "b");
    }

    /// 测试 重复路由 !!!
    ///
    /// @return a [BaseVo] object
    @Route(value = "/v/:aaa", methods = {HttpMethod.GET, HttpMethod.POST})
    public Object c() {
        return Map.of("items", "aaa");
    }

    /// 测试 重复路由 !!!
    ///
    /// @return a [BaseVo] object
    @Route(value = "/v/:bbb", methods = HttpMethod.GET)
    public Object d() {
        return Map.of("items", "bbb");
    }

    /// 测试 ZIP
    ///
    /// @return a [BaseVo] object
    @Route(value = "/zip", methods = HttpMethod.GET)
    public Binary zip() throws Exception {
        var zipBuilder = new ZipBuilder();
        zipBuilder.put("第一个目录/第二个目录/第二个目录中的文件.txt", "文件内容".getBytes(StandardCharsets.UTF_8));
        zipBuilder.put("第一个目录/这是一系列空目录/这是一系列空目录/这是一系列空目录/这是一系列空目录/这是一系列空目录");
        zipBuilder.put("第一个目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录");
        zipBuilder.put("第一个目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/一个文本文件.txt", "一些内容,一些内容,一些内容,一些内容 下😊😂🤣❤😍😒👌😘".getBytes(StandardCharsets.UTF_8));
        zipBuilder.put("第三个目录/子目录");
        zipBuilder.remove("第三个目录");

        // 大型文件请使用这种方法下载
        return Binary.download(byteOutput -> {
            try (var zos = new ZipOutputStream(byteOutputToOutputStream(byteOutput))){
                zos.setLevel(0);
                zipBuilder.writeToZipOutputStream(zos);
            }
        }, "测试压缩包.zip");

    }

}
