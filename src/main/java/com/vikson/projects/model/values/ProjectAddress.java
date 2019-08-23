package com.vikson.projects.model.values;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ProjectAddress {

    @Column(nullable = false, name = "street_address")
    private String streetAddress = "";
    @Column(nullable = false, name = "city")
    private String city = "";
    @Column(nullable = false, name = "zip_code")
    private String zipCode = "";
    @Column(nullable = false, name = "cc", length = 2)
    private String cc = "";

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        if(StringUtils.isEmpty(streetAddress))
            streetAddress = "";
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if(StringUtils.isEmpty(city))
            city = "";
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        if(StringUtils.isEmpty(zipCode))
            zipCode = "";
        this.zipCode = zipCode;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        if(StringUtils.isEmpty(cc))
            cc = "";
        Assert.isTrue(cc.length() <= 2, "ProjectAddress.cc.length must be equal or less than 2!");
        this.cc = cc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectAddress that = (ProjectAddress) o;

        if (streetAddress != null ? !streetAddress.equals(that.streetAddress) : that.streetAddress != null)
            return false;
        if (city != null ? !city.equals(that.city) : that.city != null) return false;
        if (zipCode != null ? !zipCode.equals(that.zipCode) : that.zipCode != null) return false;
        return cc != null ? cc.equals(that.cc) : that.cc == null;
    }

    @Override
    public int hashCode() {
        int result = streetAddress != null ? streetAddress.hashCode() : 0;
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
        result = 31 * result + (cc != null ? cc.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectAddress{" +
                "streetAddress='" + streetAddress + '\'' +
                ", city='" + city + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", cc='" + cc + '\'' +
                '}';
    }
}
