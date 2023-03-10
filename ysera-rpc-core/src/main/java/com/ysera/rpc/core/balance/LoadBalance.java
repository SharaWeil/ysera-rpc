package com.ysera.rpc.core.balance;

import com.ysera.rpc.core.registry.RegistryInfo;

import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.util.List;

/**
 * @author Administrator
 * @ClassName LoadBalance
 * @createTIme 2023年01月18日 21:11:11
 **/
public interface LoadBalance {

    /**
     *  负载均衡
     * @param list 服务列表
     * @return
     */
    RegistryInfo selectOne(List<RegistryInfo> list);
}
