package com.whd.cube.common;

/**
 * 线程隔离的全局容器
 * 用于在 Service 或 Controller 中随时获取当前登录用户的 ID
 */
public class UserContext {
    // ThreadLocal 保证了不同用户的请求互不干扰
    private static final ThreadLocal<Long> userHolder = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        userHolder.set(userId);
    }

    public static Long getUserId() {
        return userHolder.get();
    }

    public static void remove() {
        userHolder.remove();
    }
}