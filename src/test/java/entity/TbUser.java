package entity;


import org.malred.annotations.table.Entity;

import java.io.Serializable;

@Entity("tb_user")
//public class TbUser implements Serializable {
public class TbUser  {

    // can't cast Integer to Long
    private int id;
    private String username;
    private String password;
    private String gender;
    private String addr;

    public TbUser(int id, String username, String password, String gender, String addr) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.addr = addr;
    }

    public TbUser() {
    }

    public TbUser(String username, String password, String gender, String addr) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.addr = addr;
    }

    @Override
    public String toString() {
        return "TbUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", gender='" + gender + '\'' +
                ", addr='" + addr + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
