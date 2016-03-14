package com.yzc.simplyweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.yzc.simplyweather.R;
import com.yzc.simplyweather.service.AutoUpdateService;
import com.yzc.simplyweather.util.HttpCallbackListener;
import com.yzc.simplyweather.util.HttpUtil;
import com.yzc.simplyweather.util.Utility;

import java.io.InputStream;
import java.net.URLEncoder;

/**
 * Created by yzc on 2016/3/14.
 */
public class WeatherActivity extends Activity implements View.OnClickListener{

    //用于显示城市名
    private TextView cityNameText;

    //用于显示天气描述信息
    private TextView weatherDespText;

    //用于显示气温
    private TextView temperature;

    //用于显示当前日期
    private TextView currentDateText;

    //切换城市按钮
    private Button switchCity;

    //更新天气按钮
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        //初始化控件
        cityNameText = (TextView) findViewById(R.id.city_name);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temperature = (TextView) findViewById(R.id.temperature);
        currentDateText = (TextView) findViewById(R.id.current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);

        String districtName = getIntent().getStringExtra("district_name");
        if (!TextUtils.isEmpty(districtName)) {
            weatherDespText.setText("同步中...");
            queryWeather(districtName);
        } else {
            showWeather();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                weatherDespText.setText("同步中");
                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(this);
                String districtName = preferences.getString("district_name", "");
                queryWeather(districtName);
                break;
            default:
                break;
        }
    }

    private void queryWeather(String name) {
        try {
            String address = "http://v.juhe.cn/weather/index?format=2&cityname=" +
                    URLEncoder.encode(name, "UTF-8") + "&key=ac4be214437e3be0352cb75ea9254634";
            HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
                @Override
                public void onFinish(InputStream in) {
                    Utility.handleWeatherResponse(WeatherActivity.this, in);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            weatherDespText.setText("同步失败");
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name", ""));
        weatherDespText.setText(prefs.getString("weather", ""));
        temperature.setText(prefs.getString("temperature", ""));
        currentDateText.setText(prefs.getString("date", ""));
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
