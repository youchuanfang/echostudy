package com.echostudy.security;

public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static String getRole() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getRole();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
