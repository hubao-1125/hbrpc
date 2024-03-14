package io.github.hubao.hbrpcdemoprovider;

import io.github.hubao.hbrpcdemoapi.Order;
import io.github.hubao.hbrpcdemoapi.OrderService;

/**
 * 功能描述: -类
 *
 * @author hubao
 * @Date: 2024/3/14$ 15:39$
 */
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findById(Integer id) {

        if(id == 404) {
            throw new RuntimeException("404 exception");
        }

        return new Order(id.longValue(), 15.6f);
    }
}
