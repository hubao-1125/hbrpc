package io.github.hubao.hbrpcdemoprovider;

import io.github.hubao.hbrpccore.annotation.HbProvider;
import io.github.hubao.hbrpcdemoapi.User;
import io.github.hubao.hbrpcdemoapi.UserService;
import org.springframework.stereotype.Component;

@Component
@HbProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(int id) {
        return new User(id, "KK-" + System.currentTimeMillis());
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
