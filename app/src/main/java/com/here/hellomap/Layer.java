package com.here.hellomap;

public class Layer {
    private int layerID;
    private String layerName;

    public Layer(int layerID, String layerName) {
        this.layerID = layerID;
        this.layerName = layerName;
    }

    public int getLayerID() {
        return layerID;
    }

    public void setLayerID(int layerID) {
        this.layerID = layerID;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
}
