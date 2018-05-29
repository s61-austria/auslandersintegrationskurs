package com.s61.integration.model;

public class InternationalStolenCar {
    private String licencePlate;
    private Countries originCountry;
    private boolean isStolen;

    public InternationalStolenCar(String licencePlate, Countries originCountry, boolean isStolen) {
        this.licencePlate = licencePlate;
        this.originCountry = originCountry;
        this.isStolen = isStolen;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public Countries getOriginCountry() {
        return originCountry;
    }

    public boolean isStolen() {
        return isStolen;
    }

    public void setStolen(boolean stolen) {
        isStolen = stolen;
    }
}
