package se.extenda.sco.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemResponse {

    private String itemCode;

    private Long tolerance;

    @JsonProperty("isLightItem")
    private boolean isLightItem;

    private long weightsCount;

    @JsonProperty("weights")
    private WeightResponse[] weights;

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Long getTolerance() {
        return tolerance;
    }

    public void setTolerance(Long tolerance) {
        this.tolerance = tolerance;
    }

    public boolean isLightItem() {
        return isLightItem;
    }

    public void setLightItem(boolean lightItem) {
        isLightItem = lightItem;
    }

    public long getWeightsCount() {
        return weightsCount;
    }

    public void setWeightsCount(long weightsCount) {
        this.weightsCount = weightsCount;
    }

    public WeightResponse[] getWeights() {
        return weights;
    }

    public void setWeights(WeightResponse[] weights) {
        this.weights = weights;
    }

    @Override
    public String toString() {
        return "ItemResponse{" +
                "itemCode='" + itemCode + '\'' +
                ", tolerance=" + tolerance +
                ", isLightItem=" + isLightItem +
                ", weightsCount=" + weightsCount +
                ", weights=" + Arrays.toString(weights) +
                '}';
    }
}
