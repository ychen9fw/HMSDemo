package com.ychen9.demo.model;

public class PriceType {
    private String name;
    private int ID;

    public PriceType(String name, int ID){
        this.name = name;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public int getID(){
        return ID;
    }

    @Override
    public String toString(){
        return name;
    }

}
