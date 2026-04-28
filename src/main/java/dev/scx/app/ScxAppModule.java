package dev.scx.app;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static dev.scx.app.ScxAppHelper.findClassListByScxModule;
import static dev.scx.app.ScxAppHelper.findRootPathByScxModule;

/// Scx 模块接口 , 自定义模块必须实现此接口
/// 当自定义的模块实现此接口之后 , 会根据 自定义模块的 根 package 进行扫描 , 所以功能代码请放在自定义模块的包或子包下
/// 生命周期请参阅方法说明
///
/// @author scx567888
/// @version 0.0.1
public abstract class ScxAppModule {

    /// 模块中所有的 class
    protected final List<Class<?>> classList;

    /// 模块根路径
    /// 如果模块是 jar 就获取 jar 所在目录
    /// 如果 模块不是 jar 就获取 所在 class 的目录
    protected final Path rootPath;

    /// 默认名称
    protected final String defaultName = this.getClass().getSimpleName();

    /// 在模块创建时 加载所有包含的 class
    public ScxAppModule() {
        try {
            //这里使用 ArrayList 重新包装一下 以便后续可以修改
            this.classList = new ArrayList<>(findClassListByScxModule(this.getClass()));
            this.rootPath = findRootPathByScxModule(this.getClass());
        } catch (IOException e) {
            throw new RuntimeException("ScxModule 加载失败 !!!", e);
        }
    }

    /// 核心模块初始化完成调用
    /// 注意请不要阻塞此方法
    public void start(ScxApp scx) {

    }

    /// 项目停止或结束时调用
    /// 注意请不要阻塞此方法
    public void stop(ScxApp scx) {

    }

    /// 模块名称
    ///
    /// @return name
    public String name() {
        return this.defaultName;
    }

    /// 每次都返回一个 新的 list 防止外部修改
    ///
    /// @return r
    public List<Class<?>> classList() {
        return new ArrayList<>(this.classList);
    }

    /// 所处路径
    ///
    /// @return a
    public Path rootPath() {
        return this.rootPath;
    }

}
