package com.jx.wheelpicker.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jx.wheelpicker.util.AreaUtils;
import com.jx.wheelpicker.widget.model.Area;
import com.jx.wheelpicker.widget.model.AreaJsonPreviewData;
import com.jx.wheelpicker.widget.model.City;
import com.jx.wheelpicker.widget.model.Province;

import java.util.ArrayList;
import java.util.List;


/**
 * 区域选择器
 *
 * @author zhaoxl
 * @date 2018/6/26
 */
public class WheelAreaPicker extends LinearLayout implements IWheelAreaPicker {
    private static final String TAG = "WheelAreaPicker";

    private static final float ITEM_TEXT_SIZE = 20;
    private static final float ITEM_SPACE = 10;
    private static final String SELECTED_ITEM_COLOR = "#353535";
    private static final int PROVINCE_INITIAL_INDEX = 0;

    private Context mContext;

    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<String> mProvinceName, mCityName, mAreaName;

    private WheelPicker mWPProvince, mWPCity, mWPArea;

    public WheelAreaPicker(Context context) {
        this(context, null);
    }

    public WheelAreaPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView(context);

        mProvinceList = getJsonDataList();

        obtainProvinceData();

        addListenerToWheelPicker();

        //view重绘时回调
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                wheelScrollChanged();//初始化完毕，第一次显示时的回调
                return true;
            }
        });
    }

    private List<Province> getJsonDataList() {
        if (isInEditMode()) {
            //从assets文件中读取预览时乱码，
            String json = AreaJsonPreviewData.DATA;
            return new Gson().fromJson(json, new TypeToken<List<Province>>() {
            }.getType());
        }
        return AreaUtils.getInstance().getJsonData(getContext());
    }

    private void initView(Context context) {
        setOrientation(HORIZONTAL);

        mContext = context;

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
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 5, 5, 5);
        params.width = 0;
        params.weight = weight;
        wheelPicker.setItemTextSize(dip2px(mContext, ITEM_TEXT_SIZE));
        wheelPicker.setSelectedItemTextColor(Color.parseColor(SELECTED_ITEM_COLOR));
        wheelPicker.setCurved(true);
        wheelPicker.setVisibleItemCount(7);
        wheelPicker.setAtmospheric(true);
        wheelPicker.setItemSpace(dip2px(mContext, ITEM_SPACE));
        wheelPicker.setLayoutParams(params);
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
                wheelScrollChanged();
            }
        });

        mWPCity.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                //获取城市对应的城区的名字
                setArea(position);
                wheelScrollChanged();
            }
        });

        mWPArea.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                wheelScrollChanged();
            }
        });
    }

    private void wheelScrollChanged() {
        if (onWheelScrollChangeListener != null) {
            onWheelScrollChangeListener.onWheelScroll(this);
        }
    }

    private void setCityAndAreaData(int position) {
        if (mProvinceList == null || position < 0 || mProvinceList.size() <= position) {
            return;
        }
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
        if (mCityList == null || position < 0 || mCityList.size() <= position) {
            return;
        }
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
    public String getSSQ() {
        return getProvince().getName() + getCity().getName() + getArea().getName();
    }

    @Override
    public String getSSQCode() {
        return getArea().getCode();
    }

    @Override
    public void setItemTextSize(float size) {
        if (mWPProvince != null) {
            mWPProvince.setItemTextSize(size);
        }
        if (mWPCity != null) {
            mWPCity.setItemTextSize(size);
        }
        if (mWPArea != null) {
            mWPArea.setItemTextSize(size);
        }
    }

    @Override
    public void setItemTextSize(int unit, float value) {
        if (mWPProvince != null) {
            mWPProvince.setItemTextSize(unit, value);
        }
        if (mWPCity != null) {
            mWPCity.setItemTextSize(unit, value);
        }
        if (mWPArea != null) {
            mWPArea.setItemTextSize(unit, value);
        }
    }

    @Override
    public void setSelectPositionByCode(String code) {
        if (code == null || code.length() != 6) {
            Log.w(TAG, "setSelectPositionByCode: code 必须是省市区代码，长度为6位");
            return;
        }
        String provinceCode = code.substring(0, 2) + "0000";
        String cityCode = code.substring(0, 4) + "00";
        for (int p = 0; p < mProvinceList.size(); p++) {
            if (mProvinceList.get(p).getCode().equals(provinceCode)) {
                mWPProvince.setSelectedItemPosition(p);
                setCityAndAreaData(p);
                Province province = mProvinceList.get(p);
                List<City> cityList = province.getCity();
                for (int c = 0; c < cityList.size(); c++) {
                    if (cityList.get(c).getCode().equals(cityCode)) {
                        mWPCity.setSelectedItemPosition(c);
                        setArea(c);
                        City city = cityList.get(c);
                        List<Area> areaList = city.getArea();
                        for (int a = 0; a < areaList.size(); a++) {
                            if (areaList.get(a).getCode().equals(code)) {
                                mWPArea.setSelectedItemPosition(a);
                                return;
                            }
                        }
                        mWPArea.setSelectedItemPosition(0);
                        return;
                    }
                }
                mWPCity.setSelectedItemPosition(0);
                setArea(0);
                return;
            }
        }
        mWPProvince.setSelectedItemPosition(0);
        setCityAndAreaData(0);
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private OnWheelScrollChangeListener onWheelScrollChangeListener;

    public void setOnWheelScrollChangeListener(OnWheelScrollChangeListener listener) {
        this.onWheelScrollChangeListener = listener;
    }

    public interface OnWheelScrollChangeListener {
        /**
         * 停止滚动即回调
         *
         * @param wheelAreaPicker IWheelAreaPicker
         */
        void onWheelScroll(IWheelAreaPicker wheelAreaPicker);
    }
}