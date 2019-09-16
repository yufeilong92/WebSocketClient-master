package com.yxc.websocketclientdemo.util;

import android.content.Context;
import android.os.Handler;
import android.util.Log;


import com.yxc.websocketclientdemo.util.chatObserver.ChatInterface;
import com.yxc.websocketclientdemo.util.chatObserver.ChatSubject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * @Author : YFL  is Creating a porject in PC$
 * @Email : yufeilong92@163.com
 * @Time :2019/9/16 09:07
 * @Purpose :
 */
public class WsNewManager {

    private volatile static WsNewManager singleton;
    private static Context mContext;
    private JWebSocketClient client;
    private final ChatSubject mSubject;

    private WsNewManager(Context mContext) {
        this.mContext = mContext;
        mSubject = new ChatSubject();

    }

    public static WsNewManager getSingleton(Context mContext) {
        if (singleton == null) {
            synchronized (WsNewManager.class) {
                if (singleton == null) {
                    singleton = new WsNewManager(mContext);
                }
            }
        }
        return singleton;
    }

    public void attchSubject(ChatInterface chatActivity) {
        mSubject.attach(chatActivity);
    }

    public void init() {
        //初始化websocket
        initSocketClient();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//开启心跳检测
    }

    //
    public void onClose() {
        closeConnect();
        mSubject.deleteAll();
    }


    private int mId;

    public void regithServciet(int id) {
        mId = id;
        String message = "{\"getway\":\"member_login\",\"key\":\"" + getkey() + "\",\"record_id\":\"" + id + "\"}";
        client.send(message);
    }

    public void sendServiceText(String comdata, boolean isImage, String path) {
        String message = "{\"getway\":\"member_send_msg\",\"key\":\"" + getkey() + "\",\"record_id\":\"" + mId + "\"," +
                "\"type\":" + (isImage ? "2" : "1") + ",\"content\":\"" + (isImage ? path : comdata) + "\"}";
        client.send(message);
    }

    /**
     * Boss 消息列表页
     *
     * @return
     */
    public void regithstBossList() {
        String message = "{\"getway\":\"boss_lists_login\",\"key\":\"" + getkey() + "\"}";
        client.send(message);
    }

    public void registerBoss(int id) {
        mId = id;
        String message = "{\"getway\":\"boss_talk_login\",\"key\":\"" + getkey() + "\",\"record_id\":\"" + id + "\"}";
        client.send(message);

    }

    public void sendBossText(String comdata, boolean isImage, String path) {
        String message = "{\"getway\":\"boss_talk_msg\",\"key\":\"" + getkey() + "\",\"record_id\":\"" + mId + "\"," +
                "\"type\":" + (isImage ? "2" : "1") + ",\"content\":\"" + (isImage ? path : comdata) + "\"}";
        client.send(message);
    }

    /**
     * 求职消息列表
     *
     * @return
     */
    public void registerJobList() {
        String message = "{\"getway\":\"employee_lists_login\",\"key\":\"" + getkey() + "\"}";
        client.send(message);
    }

    public void registerJobId(int id) {
        mId = id;
        String message = "{\"getway\":\"employee_talk_login\",\"key\":\"" + getkey() + "\",\"record_id\":\"" + mId + "\"}\n";
        client.send(message);
    }

    public void sendJobText(String comdata, boolean isImage, String path) {
        String message = "{\"getway\":\"employee_talk_msg\",\"key\":\"" + getkey() + "\",\"record_id\":\"" + mId + "\"," +
                "\"type\":" + (isImage ? "2" : "1") + ",\"content\":\"" + (isImage ? path : comdata) + "\"}";
        client.send(message);
    }

    private String getkey() {
//        UserInfomVo infom = SaveUserInfomUtilJave.getInstance().getUserInfom();
//        UserInfomVo.ResultBean use = infom.getResult();
//        return use.getKey();
        return "";
    }

    /**
     * 连接websocket
     */
    private void connect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * 断开连接
     */
    private void closeConnect() {
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }


    /**
     * 初始化websocket连接
     */
    private void initSocketClient() {
        URI uri = URI.create("");
        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                mSubject.notifyAllChange(message);
                Log.e("JWebSocketClientService", "收到的消息：" + message);

            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                Log.e("JWebSocketClientService", "websocket连接成功");
            }
        };
        connect();
    }

    private static final long HEART_BEAT_RATE = 10 * 1000;//每隔10秒进行一次对长连接的心跳检测
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("JWebSocketClientService", "心跳包检测websocket连接状态");
            if (client != null) {
                if (client.isClosed()) {
                    reconnectWs();
                }
            } else {
                //如果client已为空，重新初始化连接
                client = null;
                initSocketClient();
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    public void isConnect() {
        if (client != null) {
            if (client.isClosed()) {
                reconnectWs();
            }
        }
    }

    /**
     * 开启重连
     */
    private void reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable);
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.e("JWebSocketClientService", "开启重连");
                    client.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public class JWebSocketClient extends WebSocketClient {
        public JWebSocketClient(URI serverUri) {
            super(serverUri, new Draft_6455());
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.e("JWebSocketClient", "onOpen()");
        }

        @Override
        public void onMessage(String message) {
            Log.e("JWebSocketClient", "onMessage()");
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.e("JWebSocketClient", "onClose()");
        }

        @Override
        public void onError(Exception ex) {
            Log.e("JWebSocketClient", "onError()");
        }
    }

}

