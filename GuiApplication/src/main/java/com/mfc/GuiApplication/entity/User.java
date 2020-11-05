package com.mfc.GuiApplication.entity;


public class User {

    private CellTower connectedCellTower;
    private int userEdge = 1;


    public User(CellTower connectedCellTower, int userEdge) {
        super();
        this.connectedCellTower = connectedCellTower;
        this.userEdge = userEdge;
    }


    public CellTower getConnectedCellTower() {
        return connectedCellTower;
    }

    public void setConnectedCellTower(CellTower connectedCellTower) {
        this.connectedCellTower = connectedCellTower;
    }


    public int getUserEdge() {
        return userEdge;
    }


    public void setUserEdge(int userEdge) {
        this.userEdge = userEdge;
    }


    public User() {

    }
}
