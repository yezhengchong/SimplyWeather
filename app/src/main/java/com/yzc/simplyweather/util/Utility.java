package com.yzc.simplyweather.util;

import android.util.JsonReader;

import com.yzc.simplyweather.db.SimplyWeatherDB;
import com.yzc.simplyweather.model.City;
import com.yzc.simplyweather.model.District;
import com.yzc.simplyweather.model.Province;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzc on 2016/3/12.
 */
public class Utility {

    private static SimplyWeatherDB simplyWeatherDB;

    //解析和处理服务器返回的省级数据
    public static boolean handleResponse(SimplyWeatherDB simplyWeatherDb, InputStream in) {
        LogUtil.d("Utility", "handleResponse");
        simplyWeatherDB = simplyWeatherDb;
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        boolean flag = false;
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String nodeName = reader.nextName();
                if (nodeName.equals("resultcode")) {
                    LogUtil.d("Utility", "resultcode = " + reader.nextString());
                    flag = true;
                } else if (nodeName.equals("result") && flag) {
                    saveAreaToDatabase(reader);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //保存到数据库
    private static boolean saveAreaToDatabase(JsonReader reader) {
        LogUtil.d("Utility", "saveAreaToDatabase");
        String provinceName = null;
        String cityName = null;
        String districtName = null;
        List<String> provinceNames = new ArrayList<>();
        List<String> cityNames = new ArrayList<>();
        boolean changedProvince = false;
        boolean changeCity = false;
        int provinceId = 0;
        int cityId = 0;
        int districtId = 0;
        Province previousProvince = new Province();
        City previousCity = new City();

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String nodeName = reader.nextName();
                    if (nodeName.equals("province")) {
                        provinceName = reader.nextString().trim();
                        if (!provinceNames.contains(provinceName)) {
                            provinceNames.add(provinceName);
                            changedProvince = true;
                            provinceId++;
                        }
                    } else if (nodeName.equals("city")) {
                        cityName = reader.nextString().trim();
                        if (!cityNames.contains(cityName)) {
                            cityNames.add(cityName);
                            changeCity = true;
                            cityId++;
                        }
                    } else if (nodeName.equals("district")) {
                        districtName = reader.nextString().trim();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                LogUtil.d("Utility", "\nprovince_name = " + provinceName +
                        "\ncity_name = " + cityName + "\ndistrict_name = " + districtName);

                if (changedProvince) {
                    Province province = new Province();
                    province.setId(provinceId);
                    province.setProvinceName(provinceName);
                    previousProvince = province;
                    simplyWeatherDB.saveProvince(province);
                    changedProvince = false;
                    LogUtil.d("Utility", "province_id = " + province.getId() +
                            "\t province_name = " + province.getProvinceName());
                }

                if (changeCity) {
                    City city = new City();
                    city.setId(cityId);
                    city.setCityName(cityName);
                    city.setProvinceId(previousProvince.getId());
                    previousCity = city;
                    simplyWeatherDB.saveCity(city);
                    changeCity = false;
                    LogUtil.d("Utility", "city_id = " + city.getId() +
                            "\tcity_name = " + city.getCityName() +
                            "\tprovince_id = " + city.getProvinceId());
                }

                District district = new District();
                districtId++;
                district.setId(districtId);
                district.setDistrictName(districtName);
                district.setCityId(previousCity.getId());
                simplyWeatherDB.saveDistrict(district);
                LogUtil.d("Utility", "district_id = " + district.getId() +
                        "\tdistrict_name = " + district.getDistrictName() +
                        "\tcity_id = " + district.getCityId());
            }
            reader.endArray();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
