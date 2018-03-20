package model;

import java.io.Serializable;

public class InternationalCar implements Serializable {

    private String licencePlate;
    private Countries registerdInCountry;
    private Countries destinationCountry;
    private boolean isStolen;

    public InternationalCar(String licencePlate, Countries registerdInCountry, Countries destinationCountry, boolean isStolen) {
        this.licencePlate = licencePlate;
        this.registerdInCountry = registerdInCountry;
        this.destinationCountry = destinationCountry;
        this.isStolen = isStolen;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public Countries getOriginCountry() {
        return registerdInCountry;
    }

    public Countries getDestinationCountry() {
        return destinationCountry;
    }

    public boolean isStolen() {
        return isStolen;
    }

    public void setStolen(boolean stolen) {
        isStolen = stolen;
    }
}
