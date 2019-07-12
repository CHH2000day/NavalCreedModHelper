package com.CHH2000day.navalcreed.modhelper;

import com.google.gson.annotations.Expose;

public class DataBean {
    @Expose(serialize = false, deserialize = false)
    public static final int RESULT_FAIl = -1;
    @Expose(serialize = false, deserialize = false)
    public static final int RESULT_SUCCESS = 0;

    public Integer resultCode;
    public String message;

    public Integer getResultCode() {
        return resultCode;
    }

    public DataBean setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public DataBean setMessage(String message) {
        this.message = message;
        return this;
    }

}
