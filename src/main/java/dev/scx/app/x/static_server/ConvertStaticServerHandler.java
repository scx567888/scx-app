package dev.scx.app.x.static_server;

import dev.scx.app.config.ScxConfigValueHandler;
import dev.scx.app.config.ScxEnvironment;
import dev.scx.app.config.handler.DefaultValueHandler;
import dev.scx.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * ConvertStaticServerHandler
 *
 * @author scx567888
 * @version 0.0.1
 */
record ConvertStaticServerHandler(ScxEnvironment scxEnvironment) implements ScxConfigValueHandler<List<StaticServer>> {

    @Override
    public List<StaticServer> handle(String keyPath, Node rawValue) {
        var arrayList = DefaultValueHandler.of(new ArrayList<Map<String, String>>()).handle(keyPath, rawValue);
        var tempList = new ArrayList<StaticServer>();
        for (var arg : arrayList) {
            try {
                tempList.add(new StaticServer(arg.get("location"), scxEnvironment.getPathByAppRoot(arg.get("root"))));
            } catch (Exception ignored) {

            }
        }
        return tempList;
    }

}
