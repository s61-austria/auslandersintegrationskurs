package com.s61.integration.model;

import java.io.Serializable;

public class InternationalCar implements Serializable {

    private String licencePlate;
    private com.s61.integration.connector.model.Countries country;
    private boolean isStolen;

    public InternationalCar(String licencePlate, com.s61.integration.connector.model.Countries country, boolean isStolen) {
        this.licencePlate = licencePlate;
        this.isStolen = isStolen;
        this.country = country;
    }

    /**
     * Checks if a licenseplate is a country code and then six characters
     *
     * @return Whether license plate is international
     */
    public boolean isInternationalPlate() {
        return licencePlate.replace("-", "").substring(1).length() == 6;
    }

    public com.s61.integration.connector.model.Countries getCountry() {
        return country;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public boolean isStolen() {
        return isStolen;
    }

    public void setStolen(boolean stolen) {
        isStolen = stolen;
    }
}
