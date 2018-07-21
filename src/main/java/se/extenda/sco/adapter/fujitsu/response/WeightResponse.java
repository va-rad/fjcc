package se.extenda.sco.adapter.fujitsu.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.extenda.sco.adapter.fujitsu.response.util.ZonedDateTimeDeserializer;
import se.extenda.sco.adapter.fujitsu.response.util.ZonedDateTimeSerializer;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeightResponse {

    private long value;

    private Long quantity;

    private long frequencyCounter;

    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    private ZonedDateTime updateTime;

    public ZonedDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(ZonedDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public long getFrequencyCounter() {
        return frequencyCounter;
    }

    public void setFrequencyCounter(long frequencyCounter) {
        this.frequencyCounter = frequencyCounter;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "WeightResponse{" +
                "value=" + value +
                ", quantity=" + quantity +
                ", frequencyCounter=" + frequencyCounter +
                ", updateTime=" + updateTime +
                '}';
    }
}
