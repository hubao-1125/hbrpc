package io.github.hubao.hbrpc.core.cluster;

import io.github.hubao.hbrpc.core.api.Router;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 功能描述: 灰度路由
 * @author hubao
 * @Date: 2024/3/31 21:48
 */
@Data
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {

    private int grayRatio;

    public void setGrayRatio(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    private final Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    public GrayRouter() {

    }


    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {

        if (providers == null || providers.size() <= 1) {
            return providers;
        }

        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> grayNodes = new ArrayList<>();

        providers.forEach(i->{
            if ("true".equals(i.getParameters().get("gray"))) {
                grayNodes.add(i);
            } else {
                normalNodes.add(i);
            }
        });

        if (normalNodes.isEmpty() || grayNodes.isEmpty()) {
            return providers;
        }

        log.debug(" grayRouter grayNodes/normalNodes,grayRatio ===> {}/{},{}",
                grayNodes.size(), normalNodes.size(), grayRatio);

        if (grayRatio <= 0) {
            return normalNodes;
        } else if (grayRatio >= 100) {
            return grayNodes;
        }

        if(random.nextInt(100) < grayRatio) {
            log.debug(" grayRouter grayNodes ===> {}", grayNodes);
            return grayNodes;
        } else {
            log.debug(" grayRouter normalNodes ===> {}", normalNodes);
            return normalNodes;
        }

    }
}
