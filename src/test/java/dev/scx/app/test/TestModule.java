package dev.scx.app.test;


import dev.scx.app.*;
import dev.scx.app.base.BaseModelService;
import dev.scx.app.component.ScxAppComponentModule;
import dev.scx.app.cors.ScxAppCorsModule;
import dev.scx.app.crud.CRUDModule;
import dev.scx.app.fix_table.FixTableModule;
import dev.scx.app.fss.FSSModule;
import dev.scx.app.http.ScxAppHttpModule;
import dev.scx.app.logging.ScxAppLoggingModule;
import dev.scx.app.redirect.RedirectModule;
import dev.scx.app.scheduling.ScxAppSchedulingModule;
import dev.scx.app.sql.ScxAppSQLModule;
import dev.scx.app.static_server.ScxAppStaticServerModule;
import dev.scx.app.test.car.Car;
import dev.scx.app.test.car.CarColor;
import dev.scx.app.test.car.CarOwner;
import dev.scx.app.test.car.CarService;
import dev.scx.app.test.like.Like;
import dev.scx.app.test.like.LikeService;
import dev.scx.app.test.like.Order;
import dev.scx.app.test.person.Person;
import dev.scx.app.test.person.PersonService;
import dev.scx.app._util.FileUtils;
import dev.scx.app._util.NetUtils;
import dev.scx.app._util.StopWatch;
import dev.scx.app._util.zip.UnZipBuilder;
import dev.scx.app._util.zip.ZipBuilder;
import dev.scx.app._util.zip.ZipOptions;
import dev.scx.app.web.ScxAppWebModule;
import dev.scx.http.media.multi_part.MultiPart;
import dev.scx.http.routing.x.static_files.StaticFilesHandler;
import dev.scx.http.uri.ScxURI;
import dev.scx.http.x.HttpClient;
import dev.scx.random.ScxRandom;
import dev.scx.scheduling.ScxScheduling;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.scx.data.field_policy.FieldPolicyBuilder.*;
import static dev.scx.data.query.BuildControl.USE_EXPRESSION;
import static dev.scx.data.query.QueryBuilder.*;
import static dev.scx.http.method.HttpMethod.POST;
import static dev.scx.random.ScxRandom.NUMBER_AND_LETTER;
import static java.lang.System.Logger.Level.ERROR;
import static org.testng.AssertJUnit.assertEquals;

public class TestModule implements ScxAppModule {

    @Override
    public ScxAppModuleDefinition init(ScxAppInitContext context) {
        List<Class<?>> classListByScxModule = null;
        try {
            classListByScxModule = ScxAppHelper.findClassListByScxModule(this.getClass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ScxAppModuleDefinition.of()
            .candidate(classListByScxModule.toArray(Class[]::new));
    }

    public static void main(String[] args) throws IOException {
        runModule();
        test0();
        test1();
        test2();
        test3();
        test4();
        test5();
    }

    @BeforeTest
    public static void runModule() {
        //模拟外部参数
        var args = new String[]{"--scx.port=8888", "--scx.config.path=AppRoot:scx-config.json"};
        ScxApp.builder()
                .setMainClass(TestModule.class)

            .module(new ScxAppLoggingModule())
            .module(new ScxAppComponentModule())
            .module(new ScxAppHttpModule())
            .module(new ScxAppCorsModule())
            .module(new ScxAppWebModule())
            .module(new ScxAppStaticServerModule())
            .module(new ScxAppSQLModule())
            .module(new ScxAppSchedulingModule())
            // 以下是 偏业务的模块
            .module(new FixTableModule())
            .module(new FSSModule())
            .module(new RedirectModule())
            .module(new CRUDModule())
            .module(new TestModule())
                .setArgs(args)
//                .configure(ScxAppFeature.SHOW_BANNER, true)
//                .configure(ScxAppFeature.SHOW_OPTIONS_INFO, true)
//                .configure(ScxAppFeature.SHOW_MODULE_LIFE_CYCLE_INFO, true)
//                .configure(ScxAppFeature.SHOW_START_UP_INFO, true)
//                .configure(ScxAppFeature.USE_DEVELOPMENT_ERROR_PAGE, true)
//                .configure(ScxAppFeature.ENABLE_SCHEDULING_WITH_ANNOTATION, true)
//                .configure(ScxAppFeature.USE_SPY, true)
                .run();
        //修复表
        try {
//            ScxAppContext.sqlClient().execute(SQL.sql("drop database if exists scx_test; create database scx_test; use scx_test"));
        } catch (Exception ignored) {

        }
//        ScxAppContext.scx().getComponent(ScxFix).fixTable();
    }

    @Test
    public static void test0() {
        var carService = ScxAppContext.getBean(CarService.class);
        var carService1 = new BaseModelService<>(Car.class);
        //纯表达式插入
        var name = carService.add(assignField("name", "RAND()"));
        try {
            if (carService1.count() < 1500) {
                System.err.println("开始: 方式1 (批量) 插入");
                //插入数据 方式1
                StopWatch.start("save1");
                var l = new ArrayList<Car>();
                for (int i = 0; i < 99; i = i + 1) {
                    var c = new Car();
                    c.name = ScxRandom.randomString(10, NUMBER_AND_LETTER) + "🤣";
                    c.color = CarColor.values()[ScxRandom.randomInt(4)];
                    c.owner = new CarOwner("Jack", i, new String[]{"123456789", "666666666"});
                    c.tags = new String[]{"fast", "beautiful", "small", "big"};
                    l.add(c);
                }
                carService.add(l);
                System.err.println("完成: 方式1 (批量) 插入 99条数据时间 :" + StopWatch.stopToMillis("save1"));

                System.err.println("开始: 方式2 (循环单次) 插入");
                //插入数据 方式2
                StopWatch.start("save2");
                for (int i = 0; i < 99; i = i + 1) {
                    var c = new Car();
                    c.name = ScxRandom.randomString(10, NUMBER_AND_LETTER) + "😢";
                    c.color = CarColor.values()[ScxRandom.randomInt(4)];
                    c.owner = new CarOwner("David", i, new String[]{"987654321"});
                    carService1.add(c);
                }
                System.err.println("方式2 (循环单次) 插入 99条数据时间 :" + StopWatch.stopToMillis("save2"));
            }

            System.err.println("将 id 大于 200 的 name 设置为空 !!!");
            //方式1
            var c = new Car();
            c.name = null;
            carService.update(c, include("name").ignoreNull(false), query().where(gt("id", 200)));

            //方式2
            carService.update(ignoreNull("name", false), query().where(gt("id", 200)));

            //方式3
            carService.update(assignField("name", "NULL"), query().where(gt("id", 200)));

            System.err.println("查询所有数据条数 !!! : " + carService.find().size());
            System.err.println("查询所有 id 大于 200 条数 !!! : " + carService.find(gt("id", 200)).size());
            System.err.println("查询所有 name 为空 条数 !!! : " + carService.find(eq("name", null)).size());
            System.err.println("查询所有 车主为 Jack 的条数 !!! : " + carService.find(eq("JSON_EXTRACT(owner,'$.name')", "Jack", USE_EXPRESSION)).size());
            System.err.println("查询所有 车主年龄大于 18 的条数 !!! : " + carService.find(gt("JSON_EXTRACT(owner,'$.age')", 18, USE_EXPRESSION)).size());
            System.err.println("查询所有 拥有 fast 和 big 标签的条数 !!! : " + carService.find(whereClause("JSON_CONTAINS(tags, ?)", "[\"fast\",\"big\"]")).size());
            System.err.println("查询所有 汽车 中 车主 的 电话号 中 包含 666666666 的条数 !!! : " + carService.find(whereClause("JSON_CONTAINS(owner,?,'$.phoneNumber')", "[\"666666666\"]")).size());

            System.err.println("------------------------- 测试事务 --------------------------------");
            // 测试事务
            //插入数据 方式2
            System.err.println("事务开始前数据库中 数据条数 : " + carService.count());

            ScxAppContext.autoTransaction(() -> {
                System.err.println("现在插入 1 数据条数");
                var bb = new Car();
                bb.name = "唯一ID";
                bb.color = CarColor.values()[ScxRandom.randomInt(4)];
                carService.add(bb);
                System.err.println("现在数据库中数据条数 : " + carService.count());
                System.err.println("现在在插入 1 错误数据");
                carService.add(bb);
            });
        } catch (Exception e) {
            System.err.println("出错了 后滚后数据库中数据条数 : " + carService.count());
        }
        //测试虚拟字段
        carService.update(assignField("name", "REVERSE(name)"), whereClause("1 = 1"));
        var list = carService.find(assignField("reverseName", "REVERSE(name)"));
        System.out.println(list.get(0).reverseName);

    }

    @Test
    public static void test1() throws IOException {
        FileUtils.write(ScxAppContext.getTempPath("test.txt"), "内容2内容2内容2内容2😂😂😂!!!".getBytes(StandardCharsets.UTF_8));
        var ip = Arrays.stream(NetUtils.getLocalIPAddress()).filter(i -> i instanceof Inet4Address).toList().getFirst();
        var logger = System.getLogger(TestModule.class.getName());
        var httpClient=new HttpClient();
        //测试 URIBuilder
        for (int i = 0; i < 10; i = i + 1) {
            var s = "http://" + ip.getHostAddress() + ":8888/test0";
            var stringHttpResponse = httpClient.request()
                    .method(POST)
                    .uri(
                        ScxURI.of(s)
                            .addQuery("name", "小明😊123?!@%^&**()_特-殊 字=符")
                            .addQuery("age", 18)
                    )
                    .send(
                        MultiPart.of()
                            .add("content", "内容内容内容内容内容".getBytes(StandardCharsets.UTF_8))
                            .add("content1", ScxAppContext.getTempPath("test.txt").toFile())
                    );
            logger.log(ERROR, "测试请求[{0}] : {1}", i, stringHttpResponse.asString());
        }
    }

    @Test
    public static void test2() {
//        var car = new Car();
//        ScxAppContext.eventBus().consumer("test-event-bus", (c) -> {
//            assertEquals(c, car);
//        });
//        ScxAppContext.eventBus().publish("test-event-bus", car);
    }

    @Test
    public static void test3() {
        var personService = ScxAppContext.getBean(PersonService.class);
        var carService = ScxAppContext.getBean(CarService.class);
        if (personService.count() < 200) {
            List<Car> list = carService.find();
            var ps = new ArrayList<Person>();
            for (int i = 0; i < list.size(); i = i + 1) {
                var p = new Person();
                p.carID = list.get(i).id;
                p.age = i;
                ps.add(p);
            }
            personService.add(ps);
        }
        //根据所有 person 表中年龄小于 100 的 carID 查询 car 表中的数据
        var cars = carService.find(query().where(in("id",
                personService.buildListSQL(query().where(lt("age", 100)), include("carID"))
        )));
        var logger = System.getLogger(TestModule.class.getName());
        logger.log(ERROR, "根据所有 person 表中年龄小于 100 的 carID 查询 car 表中的数据 总条数 {0}", cars.size());
        //根据所有 person 表中年龄小于 100 的 carID 查询 car 表中的数据
        var cars1 = carService.find(query().where(in("id",
                personService.buildListSQL(query().where(lt("age", 100)), include("carID"))
        )));
        logger.log(ERROR, "第二种方式 (whereSQL) : 根据所有 person 表中年龄小于 100 的 carID 查询 car 表中的数据 总条数 {0}", cars1.size());
    }

    @Test
    public static void test4() {

        try {
            //创建一个压缩文件先
            var zipBuilder = new ZipBuilder();
            zipBuilder.put("第一个目录/第二个目录/第二个目录中的文件.txt", "文件内容".getBytes(StandardCharsets.UTF_8));
            zipBuilder.put("第一个目录/这是一系列空目录/这是一系列空目录/这是一系列空目录/这是一系列空目录/这是一系列空目录");
            zipBuilder.put("第一个目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录");
            zipBuilder.put("第一个目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/这不是一系列空目录/一个文本文件.txt", "一些内容,一些内容,一些内容,一些内容 下😊😂🤣❤😍😒👌😘".getBytes(StandardCharsets.UTF_8));
            zipBuilder.toFile(ScxAppContext.getTempPath("aaaaa.zip"));

            //解压再压缩
            new UnZipBuilder(ScxAppContext.getTempPath("aaaaa.zip")).toFile(ScxAppContext.getTempPath("hhhh"));
            new ZipBuilder(ScxAppContext.getTempPath("hhhh")).toFile(ScxAppContext.getTempPath("bbbbb.zip"));
            //重复一次
            new UnZipBuilder(ScxAppContext.getTempPath("bbbbb.zip")).toFile(ScxAppContext.getTempPath("gggggg"), new ZipOptions().setIncludeRoot(true));
            new ZipBuilder(ScxAppContext.getTempPath("gggggg"), new ZipOptions().setIncludeRoot(true)).toFile(ScxAppContext.getTempPath("ccccc.zip"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public static void test5() {
        //测试使用关键字 作为表名和列名
        LikeService bean = ScxAppContext.getBean(LikeService.class);
        var z = new Like();
        z.order = new Order();
        z.order.where = "123";
        var a = bean.add(z);
        var b = bean.update(a);
        var c = bean.find(eq("JSON_EXTRACT(`order`, '$.where')", "123", USE_EXPRESSION));
        var d = bean.delete(b.id);
        System.out.println(b);
    }

    @Override
    public void start(ScxApp scx) {
        scx.getComponent(ScxAppHttpModule.class).router()
            .route(
                "/static/*",
                StaticFilesHandler.of(scx.scxEnvironment().getPathByAppRoot("AppRoot:c\\static"))
        );
        var logger = System.getLogger(TestModule.class.getName());
        //测试定时任务
        ScxScheduling.fixedRate()
                .startTime(Instant.now().plusSeconds(3))
                .interval(Duration.of(1, ChronoUnit.SECONDS))
                .maxRunCount(10)
                .start((a) -> {
                    //测试
                    logger.log(ERROR, "这是通过 ScxContext.scheduleAtFixedRate() 打印的 : 一共 10 次 , 这时第 " + a.currentRunCount() + " 次执行 !!!");
                });

        ScxScheduling.cron()
                .cronExpression("*/1 * * * * ?")
                .start((a) -> {
                    //测试
                    logger.log(ERROR, "这是通过 ScxContext.scheduler() 使用 Cron 表达式 打印的 : 这时第 " + a.currentRunCount() + " 次执行 !!!");
                });

        ScxScheduling.fixedRate()
                .startTime(Instant.now().plusSeconds(3))
                .interval(Duration.of(1, ChronoUnit.SECONDS))
                .start((a) -> {
                    logger.log(ERROR, "这是通过 ScxContext.scheduleAtFixedRate() 打印的 : 不限次数 不过到 第 10 次手动取消 , 这是第 " + a.currentRunCount() + " 次执行 !!!");
                    if (a.currentRunCount() >= 10) {
                        a.cancelSchedule();
                    }
                });

        System.out.println("CarModule-Start");
    }

}
