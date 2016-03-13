package com.yzc.simplyweather.util;

import java.io.InputStream;

/**
 * Created by yzc on 2016/3/12.
 */
public interface HttpCallbackListener {
    void onFinish(InputStream in);

    void onError(Exception e);
}
