package cn.yapeteam.yolbi.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yuxiangll
 * @since 2024/1/8 07:37
 * IntelliJ IDEA
 */
@AllArgsConstructor
@Setter@Getter
public class User {
    private String username;
    private String password;


    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
