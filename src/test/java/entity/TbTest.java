package entity;


import org.malred.annotations.table.*;

import java.util.Date;

@Entity("tb_test")
public class TbTest  {
    @ID()
    @AutoIncrement()
    int id;
    @SQLCharLen(50)
    String username;
    @SQLDefault("1@1.com")
    String email;
    @SQLDateNow()
    Date birthdate;
//    String birthdate;
    @NotNull()
    boolean is_active;

    @Override
    public String toString() {
        return "TbTest{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", birthdate=" + birthdate +
                ", is_active=" + is_active +
                '}';
    }

    public TbTest() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }
}