package io.github.hubao.hbrpc.demo.provider;

import io.github.hubao.hbrpc.core.annotation.HbProvider;
import io.github.hubao.hbrpc.demo.api.User;
import io.github.hubao.hbrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@HbProvider(version = "1.0.0")
public class UserServiceImpl implements UserService {

    @Autowired
    Environment environment;



    @Override
    public User findById(int id) {
        return new User(id, "KK-" + environment.getProperty("server.port") + "_" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "KK-" + name + "_" + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId().longValue();
    }

    @Override
    public long getId(float id) {
        return 1L;
    }

    @Override
    public String getName() {
        return "KK123";
    }

    @Override
    public String getName(int id) {
        return "Cola-" + id;
    }

    @Override
    public int[] getIds() {
        return new int[] {100,200,300};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{1,2,3};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }

}
