package entity;

import java.io.Serializable;

public class Result implements Serializable {

    private boolean flag;
    private String message;

    public Result(boolean flag, String message) {
        super();
        this.flag = flag;
        this.message = message;
    }


    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Result{" +
                "flag=" + flag +
                ", message='" + message + '\'' +
                '}';
    }
}