package com.joe.spider.util.db;

import lombok.Data;

/**
 * Druid数据源配置
 *
 * @author joe
 * @version 2017.12.17 14:18
 */
@Data
public class DruidProp {
    private String  url;
    private String  username;
    private String  password;
    /*
     * 最大连接数量
     */
    private int     maxActive                 = 30;
    /*
     * 最小连接池数量
     */
    private int     minIdle                   = 10;
    /*
     * 初始化数量
     */
    private int     initialSize               = 10;
    /*
     * 验证语句
     */
    private String  validationQuery           = "SELECT 1";
    /*
     * 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
     */
    private boolean testOnBorrow              = false;
    /*
     * 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
     */
    private boolean testOnReturn              = false;
    /*
     * 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，
     * 执行validationQuery检测连接是否有效。
     */
    private boolean testWhileIdle             = true;
    /*
     * 获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，
     * 如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
     */
    private long    maxWait                   = 60000;
    private boolean poolPreparedStatements    = false;
    /*
     * 开启Druid的监控统计功能，同时采用slf4j日志
     */
    private String  filters                   = "stat,slf4j";
    /*
     * 该值大于0时会定期（间隔为该值，单位为毫秒）将监控保存到日志中
     */
    private long    timeBetweenLogStatsMillis = 60 * 1000;
}
