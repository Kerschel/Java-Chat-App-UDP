import java.io.Serializable;

public class Users implements Serializable {
    public String name;
    public String ip;

    Users(String name,String ip){
        this.name = name;
        this.ip = ip;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }


    public void setIp(String ip) {
        this.ip = ip;
    }
}




