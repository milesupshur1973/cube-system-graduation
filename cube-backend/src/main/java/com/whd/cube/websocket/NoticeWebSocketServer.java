package com.whd.cube.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.EOFException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/notice")
@Component
public class NoticeWebSocketServer {

    // 存放所有在线的客户端
    private static final CopyOnWriteArraySet<Session> sessionSet = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessionSet.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessionSet.remove(session);
    }

    // 错误拦截
    @OnError
    public void onError(Session session, Throwable error) {
        // 如果是浏览器直接关闭/刷新导致的 EOFException，直接静默处理
        if (error instanceof EOFException) {
            // 可以选择什么都不输出，或者只输出一行提示，保持控制台整洁
            System.out.println("选手页面刷新或关闭，WebSocket正常断开");
        } else {
            // 只打印简短原因，不打印堆栈
            System.err.println("WebSocket发生异常: " + error.getMessage());
        }

        // 发生异常时，确保把这个已经失效的连接从集合中踢出去
        sessionSet.remove(session);
    }

    // 群发消息逻辑
    public static void sendAllMessage(String message) {
        for (Session session : sessionSet) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                System.err.println("WebSocket发送消息失败: " + e.getMessage());
            }
        }
    }
}