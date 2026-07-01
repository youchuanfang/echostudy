package com.echostudy.utils;

import com.echostudy.enums.UserRole;
import com.echostudy.exception.BusinessException;
import com.echostudy.security.UserContext;

public class AuthUtils {

    private AuthUtils() {
    }

    public static void requireRole(UserRole role) {
        if (!role.name().equals(UserContext.getRole())) {
            throw new BusinessException(403, "没有操作权限");
        }
    }
}
