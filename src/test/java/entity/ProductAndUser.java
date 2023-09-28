package entity;

import java.util.Date;

public class ProductAndUser {

    private int pid;
    private String product_name;
    private java.util.Date product_time;
    private int uid;
    private String username;
    private String password;
    private String gender;
    private String addr;

    @Override
    public String toString() {
        return "ProductAndUser{" +
                "pid=" + pid +
                ", product_name='" + product_name + '\'' +
                ", product_time=" + product_time +
                ", uid=" + uid +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", gender='" + gender + '\'' +
                ", addr='" + addr + '\'' +
                '}';
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public Date getProduct_time() {
        return product_time;
    }

    public void setProduct_time(Date product_time) {
        this.product_time = product_time;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
