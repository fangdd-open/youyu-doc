package com.fangdd.tp.helper;

import com.fangdd.tp.dto.UserContent;
import com.fangdd.tp.entity.Site;
import com.fangdd.tp.entity.User;

/**
 * @author xuwenzhen
 * @date 18/11/28
 */
public class UserContextHelper {
    private static final ThreadLocal<UserContent> USER_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static UserContent getUserContext() {
        return USER_CONTEXT_THREAD_LOCAL.get();
    }

    public static void setUserContext(UserContent userContent) {
        USER_CONTEXT_THREAD_LOCAL.set(userContent);
    }

    public static User getUser() {
        UserContent content = USER_CONTEXT_THREAD_LOCAL.get();
        if (content == null) {
            return null;
        }
        return content.getUser();
    }

    public static Site getSite() {
        return USER_CONTEXT_THREAD_LOCAL.get().getSite();
    }
}
