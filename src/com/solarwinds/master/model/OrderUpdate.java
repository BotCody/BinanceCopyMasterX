package com.solarwinds.master.model;
import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class OrderUpdate implements Serializable{
    @SerializedName("e")
    @Expose
    private String e;
    @SerializedName("T")
    @Expose
    private Long t;
    @SerializedName("E")
    @Expose
    private Long exec;

    public O getO() {
        return o;
    }

    public void setO(O o) {
        this.o = o;
    }

    @SerializedName("o")
    @Expose
    private O o;

    private final static long serialVersionUID = 1390418850112885682L;

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public Long getT() {
        return t;
    }

    public void setT(Long t) {
        this.t = t;
    }

    public Long getExec() {
        return exec;
    }

    public void setExec(Long exec) {
        this.exec = exec;
    }

}
