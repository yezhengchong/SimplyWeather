package com.yzc.simplyweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yzc.simplyweather.model.City;
import com.yzc.simplyweather.model.District;
import com.yzc.simplyweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzc on 2016/3/12.
 */
public class SimplyWeatherDB {

    //数据库名
    public static final String DB_NAME = "simply_weather";

    //数据库版本
    public static final int VERSION = 1;

    private static SimplyWeatherDB simplyWeatherDB;

    private SQLiteDatabase db;

    //将构造方法初始化
    private SimplyWeatherDB(Context context) {
        SimplyWeatherOpenHelper dbHelper = new SimplyWeatherOpenHelper(context,
                DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    //获取CoolWeatherDB的实例
    public synchronized static SimplyWeatherDB getInstance(Context context) {
        if (simplyWeatherDB == null) {
            simplyWeatherDB = new SimplyWeatherDB(context);
        }
        return simplyWeatherDB;
    }

    //将Province实例存储到数据库
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            db.insert("Province", null, values);
        }
    }

    //从数据库读取全国所有的省份信息
    public List<Province> loadProvinces() {
        List<Province> list = new ArrayList<Province>();
        Cursor cursor = db
                .query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor
                        .getColumnIndex("province_name")));
                list.add(province);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    //将City实例存储到数据库
    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("province_id", city.getProvinceId());
            db.insert("City", null, values);
        }
    }

    //从数据库读取某省下所有的城市信息
    public List<City> loadCities(int provinceId) {
        List<City> list = new ArrayList<City>();
        Cursor cursor = db.query("City", null, "province_id = ?",
                new String[]{String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor
                        .getColumnIndex("city_name")));
                list.add(city);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public void saveDistrict(District district) {
        if (district != null) {
            ContentValues values = new ContentValues();
            values.put("district_name", district.getDistrictName());
            values.put("city_id", district.getCityId());
            db.insert("County", null, values);
        }
    }

    public List<District> loadDistricts(int cityId) {
        List<District> list = new ArrayList<District>();
        Cursor cursor = db.query("County", null, "city_id = ?",
                new String[] {String.valueOf(cityId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                District district = new District();
                district.setId(cursor.getInt(cursor.getColumnIndex("id")));
                district.setDistrictName(cursor.getString(cursor
                        .getColumnIndex("district_name")));
                district.setCityId(cityId);
                list.add(district);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

}
