package me.arunpadiyan.netaccess;

/**
 * Created by arunp on 27-Sep-15.
 */
public class Usage {
    String ip,amount,time,link;
    Boolean active;

    public Usage(String ip, String amount, String time, String link, Boolean active) {
        this.ip = ip;
        this.amount = amount;
        this.time = time;
        this.link = link;
        this.active = active;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
