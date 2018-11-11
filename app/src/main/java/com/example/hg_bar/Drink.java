package com.example.hg_bar;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by Looten on 2015-05-08.
 */


public class Drink {
    private int _id;
    private String _name;
    //ArrayList<String> bottles = new ArrayList<>();
    JSONObject bottles;
    String amounts;
    int count;
    String info;
    String type;
    ArrayList<Integer> bottles_send = new ArrayList<>();

    public Drink() { /*Required empty bean constructor*/ }

    //public Drink(String name, ArrayList<String> bottles, String amounts, int count, String info, String type, ArrayList<Integer> bottles_send ) {
    public Drink(String name, JSONObject bottles, int count, String info, String type, ArrayList<Integer> bottles_send ) {
        this._name = name;
        this.bottles = bottles;
        //this.amounts = amounts;
        this.count = count;
        this.info = info;
        this.type = type;
        this.bottles_send = bottles_send;
    }

    public void setId(int id) {
        this._id = id;
    }

    public void setName(String name) {
        this._name = name;
    }

    public void setBottles(JSONObject bottles) {
        this.bottles = bottles;
    }

    public void setAmounts(String amounts) {
        this.amounts = amounts;
    }

    public void setCount(int count) {
        this.count = count;
    }
    public void setInfo(String info) {
        this.info = info;
    }

    public void setType(String type) {
        this.type = type;
    }


    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public JSONObject getBottles() {
        return bottles;
    }

    public int getCount() {
        return count;
    }
    public String getInfo() {
        return info;
    }

    public String getType() {
        return type;
    }
    public ArrayList<Integer> getBottlesSend() {
        return bottles_send;
    }

}
