package com.yzc.simplyweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yzc.simplyweather.R;
import com.yzc.simplyweather.db.SimplyWeatherDB;
import com.yzc.simplyweather.model.City;
import com.yzc.simplyweather.model.District;
import com.yzc.simplyweather.model.Province;
import com.yzc.simplyweather.util.HttpCallbackListener;
import com.yzc.simplyweather.util.HttpUtil;
import com.yzc.simplyweather.util.MyApplication;
import com.yzc.simplyweather.util.Utility;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzc on 2016/3/13.
 */
public class ChooseAreaActivity extends Activity{

    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_DISTRICT = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private SimplyWeatherDB simplyWeatherDB;
    private List<String> lists;

    //省列表
    private List<Province> provinceList;

    //市列表
    private List<City> cityList;

    //县列表
    private List<District> districtList;

    //选中的省份
    private Province selectedProvince;

    //选中的城市
    private City selectedCity;

    //当前选中的级别
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        if (preferences.getBoolean("city_selected", false)) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.choose_area);
        titleText = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);
        lists = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,lists);
        simplyWeatherDB = SimplyWeatherDB.getInstance(this);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(index);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(index);
                    queryDistricts();
                } else if (currentLevel == LEVEL_DISTRICT) {
                    String districtName = districtList.get(index).getDistrictName();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("district_name", districtName);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces(); //加载省数据
    }

    //查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
    private void queryProvinces() {
        provinceList = simplyWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            lists.clear();
            for (Province province : provinceList) {
                lists.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer("Province");
        }
    }

    //查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
    private void queryCities() {
        cityList = simplyWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            lists.clear();
            for (City city : cityList) {
                lists.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer("City");
        }
    }

    //查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
    private void queryDistricts() {
        districtList = simplyWeatherDB.loadDistricts(selectedCity.getId());
        if (districtList.size() > 0) {
            lists.clear();
            for (District district : districtList) {
                lists.add(district.getDistrictName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_DISTRICT;
        }
    }

    //根据传入的类型从服务器上查询省市县数据
    private void queryFromServer(final String type) {
        showProgressDialog();
        HttpUtil.sendHttpRequest("http://v.juhe.cn/weather/citys?key=ac4be214437e3be0352cb75ea9254634",
                new HttpCallbackListener() {
                    @Override
                    public void onFinish(InputStream in) {
                        boolean result = Utility.handleResponse(SimplyWeatherDB
                                .getInstance(MyApplication.getContext()), in);
                        if (result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    if (type.equals("Province")) {
                                        queryProvinces();
                                    } else if (type.equals("City")) {
                                        queryCities();
                                    } else if (type.equals("District")) {
                                        queryDistricts();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                Toast.makeText(ChooseAreaActivity.this, "加载失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

    }

    //显示进度对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    //关闭进度对话框
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    //捕获Back按键，根据当前的级别来判断，此时应该返回哪级列表，还是直接退出
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_DISTRICT) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            finish();
        }
    }
}
