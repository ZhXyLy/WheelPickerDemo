package com.jx.wheelpicker.widget;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jx.wheelpicker.widget.model.Area;
import com.jx.wheelpicker.widget.model.AreaJsonPreviewData;
import com.jx.wheelpicker.widget.model.City;
import com.jx.wheelpicker.widget.model.Province;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * 区域选择器
 *
 * @author Administrator
 * @date 2016/9/14 0014
 */
public class WheelAreaPicker extends LinearLayout implements IWheelAreaPicker {
    private static final float ITEM_TEXT_SIZE = 20;
    private static final float ITEM_SPACE = 10;
    private static final String SELECTED_ITEM_COLOR = "#353535";
    private static final int PROVINCE_INITIAL_INDEX = 0;

    private Context mContext;

    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<String> mProvinceName, mCityName, mAreaName;

    private AssetManager mAssetManager;

    private LayoutParams mLayoutParams;

    private WheelPicker mWPProvince, mWPCity, mWPArea;

    public WheelAreaPicker(Context context) {
        this(context, null);
    }

    public WheelAreaPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        initLayoutParams();

        initView(context);

        mProvinceList = getJsonDataFromAssets(mAssetManager);

        obtainProvinceData();

        addListenerToWheelPicker();
    }

    @SuppressWarnings("unchecked")
    private List<Province> getJsonDataFromAssets(AssetManager assetManager) {
        List<Province> provinceList = null;
        if (isInEditMode()) {
            //从assets文件中读取预览时乱码，
            String json = AreaJsonPreviewData.DATA;
            provinceList = new Gson().fromJson(json, new TypeToken<List<Province>>() {
            }.getType());
            return provinceList;
        }
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = assetManager.open("RegionJsonData.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String json = stringBuilder.toString();
            provinceList = new Gson().fromJson(json, new TypeToken<List<Province>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return provinceList;
    }

    private void initLayoutParams() {
        mLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLayoutParams.setMargins(5, 5, 5, 5);
        mLayoutParams.width = 0;
    }

    private void initView(Context context) {
        setOrientation(HORIZONTAL);

        mContext = context;

        mAssetManager = mContext.getAssets();

        mProvinceName = new ArrayList<>();
        mCityName = new ArrayList<>();
        mAreaName = new ArrayList<>();

        mWPProvince = new WheelPicker(context);
        mWPCity = new WheelPicker(context);
        mWPArea = new WheelPicker(context);
        mWPProvince.setItemAlign(WheelPicker.ALIGN_RIGHT);
        mWPCity.setItemAlign(WheelPicker.ALIGN_CENTER);
        mWPArea.setItemAlign(WheelPicker.ALIGN_LEFT);

        initWheelPicker(mWPProvince, 1f);
        initWheelPicker(mWPCity, 1f);
        initWheelPicker(mWPArea, 1f);
    }

    private void initWheelPicker(WheelPicker wheelPicker, float weight) {
        mLayoutParams.weight = weight;
        wheelPicker.setItemTextSize(dip2px(mContext, ITEM_TEXT_SIZE));
        wheelPicker.setSelectedItemTextColor(Color.parseColor(SELECTED_ITEM_COLOR));
        wheelPicker.setCurved(true);
        wheelPicker.setVisibleItemCount(7);
        wheelPicker.setAtmospheric(true);
        wheelPicker.setItemSpace(dip2px(mContext, ITEM_SPACE));
        wheelPicker.setLayoutParams(mLayoutParams);
        addView(wheelPicker);
    }

    private void obtainProvinceData() {
        for (Province province : mProvinceList) {
            mProvinceName.add(province.getShortName());
        }
        mWPProvince.setData(mProvinceName);
        setCityAndAreaData(PROVINCE_INITIAL_INDEX);
    }

    private void addListenerToWheelPicker() {
        //监听省份的滑轮,根据省份的滑轮滑动的数据来设置市跟地区的滑轮数据
        mWPProvince.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                //获得该省所有城市的集合
                mCityList = mProvinceList.get(position).getCity();
                setCityAndAreaData(position);
            }
        });

        mWPCity.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                //获取城市对应的城区的名字
                setArea(position);
            }
        });

    }

    private void setCityAndAreaData(int position) {
        //获得该省所有城市的集合
        mCityList = mProvinceList.get(position).getCity();
        //获取所有city的名字
        //重置先前的城市集合数据
        mCityName.clear();
        for (City city : mCityList) {
            mCityName.add(city.getShortName());
        }
        mWPCity.setData(mCityName);
        mWPCity.setSelectedItemPosition(0);
        //获取第一个城市对应的城区的名字
        //重置先前的城区集合的数据
        setArea(0);
    }

    private void setArea(int position) {
        List<Area> areas = mCityList.get(position).getArea();
        mAreaName.clear();
        for (Area area : areas) {
            mAreaName.add(area.getShortName());
        }
        mWPArea.setData(mAreaName);
        mWPArea.setSelectedItemPosition(0);
    }

    @Override
    public Province getProvince() {
        return mProvinceList.get(mWPProvince.getCurrentItemPosition());
    }

    @Override
    public City getCity() {
        return mCityList.get(mWPCity.getCurrentItemPosition());
    }

    @Override
    public Area getArea() {
        return mCityList.get(mWPCity.getCurrentItemPosition()).getArea().get(mWPArea.getCurrentItemPosition());
    }

    @Override
    public void setItemTextSize(int textSize) {
        int textSizePx = dip2px(mContext, textSize);
        if (mWPProvince != null) {
            mWPProvince.setItemTextSize(textSizePx);
        }
        if (mWPCity != null) {
            mWPCity.setItemTextSize(textSizePx);
        }
        if (mWPArea != null) {
            mWPArea.setItemTextSize(textSizePx);
        }
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}