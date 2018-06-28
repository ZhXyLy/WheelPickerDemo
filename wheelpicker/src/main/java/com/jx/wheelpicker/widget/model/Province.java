package com.jx.wheelpicker.widget.model;


import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 * @date 2016/9/14 0014
 */
public class Province implements Serializable {
    public String name;
    public String code;
    public List<City> cities;

    public String getName() {
        return name;
    }

    public String getShortName() {
        return name.length() > 6 ? name.substring(0, 6) : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<City> getCity() {
        return cities;
    }

    public void setCity(List<City> city) {
        this.cities = city;
    }
}
