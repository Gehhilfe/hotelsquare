package tk.internet.praktikum.foursquare.api.bean;

import java.util.List;



public class PlaceAutoComplete {
    private String status;
    private List<Prediction> predictions;
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }
}
