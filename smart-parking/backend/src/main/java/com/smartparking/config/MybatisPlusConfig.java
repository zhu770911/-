package com.smartparking.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 * 注意：乐观锁等拦截器 Starter 不会自动配置，必须手动注册 MybatisPlusInterceptor，
 * 否则带 @Version 字段的实体执行 updateById 会报
 * "Parameter 'MP_OPTLOCK_VERSION_ORIGINAL' not found"
 */
@Configuration
@MapperScan("com.smartparking.mapper")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 乐观锁插件：配合 ParkingSlot 实体的 @Version 字段生效
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
