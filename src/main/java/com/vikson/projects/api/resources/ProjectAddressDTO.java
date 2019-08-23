package com.vikson.projects.api.resources;

import com.vikson.projects.model.values.ProjectAddress;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Project Address Resource")
public class ProjectAddressDTO {

    @ApiModelProperty(value = "Street Address")
    private String streetAddress;
    @ApiModelProperty(value = "City")
    private String city;
    @ApiModelProperty(value = "Zip Code")
    private String zipCode;
    @ApiModelProperty(value = "Country Code")
    private String cc;

    public ProjectAddressDTO() {
    }

    public ProjectAddressDTO(ProjectAddress address) {
        this.streetAddress = address.getStreetAddress();
        this.city = address.getCity();
        this.zipCode = address.getZipCode();
        this.cc = address.getCc();
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectAddressDTO that = (ProjectAddressDTO) o;

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
}
