package com.yxc.websocketclientdemo.util.chatObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author : YFL  is Creating a porject in PC$
 * @Email : yufeilong92@163.com
 * @Time :2019/9/9 09:55
 * @Purpose :
 */
public class ChatSubject {
    private List<ChatInterface> observers = new ArrayList<>();

    public void attach(ChatInterface observer) {
        if (!observers.isEmpty())
            observers.clear();
        observers.add(observer);

    }

    public void notifyAllChange(String msg) {
        if (observers == null || observers.isEmpty()) return;
        for (int i = 0; i < observers.size(); i++) {
            ChatInterface observer = observers.get(i);
            observer.updataMsg(msg);
        }
    }

    public void deleteAll() {
        if (observers != null) {
            observers.clear();
        }
    }
}
