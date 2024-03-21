package io.github.hubao.hbrpc.demo.provider;

import io.github.hubao.hbrpc.core.annotation.HbProvider;
import io.github.hubao.hbrpc.demo.api.Order;
import io.github.hubao.hbrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

/**
 * 功能描述: -类
 *
 * @author hubao
 * @Date: 2024/3/14$ 15:39$
 */
@Component
@HbProvider(version = "hb_v_1.0.0")
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findById(Integer id) {

        if(id == 404) {
            throw new RuntimeException("404 exception");
        }

        return new Order(id.longValue(), 15.6f);
    }
}
