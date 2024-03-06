package io.github.hubao.hbrpcdemoprovider;

import io.github.hubao.hbrpccore.annotation.HbProvider;
import io.github.hubao.hbrpcdemoapi.User;
import io.github.hubao.hbrpcdemoapi.UserService;
import org.springframework.stereotype.Component;

@Component
@HbProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(Integer id) {
        return new User(1, "hubao-" + System.currentTimeMillis());
    }
}
