package com.mfc.GuiApplication.entity;

public class CellTower {

    private double lat;
    private double lon;
    private int cid;

    public CellTower() {

    }

    public CellTower(int cid, double lat, double lon) {
        this.cid = cid;
        this.lat = lat;
        this.lon = lon;
    }


    public double getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    @Override
    public String toString() {
        return "CellTower [lat=" + lat + ", lon=" + lon + ", cid=" + cid + "]";
    }

    public boolean equals(CellTower c) {
        if (this.cid == c.cid && this.lat == c.lat && this.lon == c.lon) return true;
        return false;
    }

}
