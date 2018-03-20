package model;

import java.io.Serializable;
import java.util.Date;

public class Invoice implements Serializable {
    private String licencePlate;
    private Double price;
    private Double distance;
    private Date dueByDate;
    private Date createdDate;
    private Countries destinationCountry;

    public Invoice(String licencePlate, Double price, Double distance, Date dueByDate, Date createdDate, Countries destinationCountry) {
        this.licencePlate = licencePlate;
        this.price = price;
        this.distance = distance;
        this.dueByDate = dueByDate;
        this.createdDate = createdDate;
        this.destinationCountry = destinationCountry;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public Double getPrice() {
        return price;
    }

    public Double getDistance() {
        return distance;
    }

    public Date getDueByDate() {
        return dueByDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Countries getDestinationCountry() {
        return destinationCountry;
    }
}
