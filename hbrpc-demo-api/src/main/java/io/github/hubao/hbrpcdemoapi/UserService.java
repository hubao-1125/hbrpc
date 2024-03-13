package io.github.hubao.hbrpcdemoapi;

public interface UserService {

    User findById(int id);

    int getId(int id);

    String getName();

//    User findById(long id);
}
