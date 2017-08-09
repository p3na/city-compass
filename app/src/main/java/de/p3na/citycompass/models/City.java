package de.p3na.citycompass.models;

import android.location.Location;

/**
 * Author: Christian Hansen
 * Date: 05.08.17
 * E-Mail: c_hansen@gmx.de
 * <p>
 * Class Description:
 * City model class containing not all, but most frequent, fields.
 */

public class City {

    private String name;
    private Location location;
    private String region;
    private String regionCode;
    private String regionCodeShort;
    private String country;
    private String countryCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;

        if (countryCode != null)
            regionCodeShort = regionCode.replace(countryCode, "");
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;

        if (regionCode != null)
            regionCodeShort = regionCode.replace(countryCode, "");
    }

    public String getRegionCodeShort() {
        return regionCodeShort;
    }
}
