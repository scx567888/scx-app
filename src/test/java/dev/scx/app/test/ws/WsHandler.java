package dev.scx.app.test.ws;

import dev.scx.scheduling.ScxScheduling;
import dev.scx.web.annotation.Route;
import dev.scx.web.annotation.Routes;
import dev.scx.websocket.event.ScxEventWebSocket;
import dev.scx.websocket.x.ScxServerWebSocketHandshakeRequest;

import java.time.LocalDateTime;

@Routes("/now")
public class WsHandler  {


    @Route(kind = Route.RouteKind.WEBSOCKET_UPGRADE)
    public void onHandshakeRequest(dev.scx.websocket.x.ScxServerWebSocketHandshakeRequest request) throws Exception {
        var context = ScxEventWebSocket.of(request.upgrade());
        System.out.println("连接了");
        var scheduleContext = ScxScheduling.setInterval(() -> {
            System.out.println("发送消息");
            context.send(LocalDateTime.now().toString());
        }, 500);
        context.onText(c -> {
            System.out.println("收到消息 : " + c + " ");
        });
        context.onClose(c -> {
            System.out.println("Close " + c.code() + " " + c.reason());
            scheduleContext.cancel();
        });
        context.onError(error -> {
            System.out.println("Error " + error);
            scheduleContext.cancel();
        });
        context.start();
    }
}
