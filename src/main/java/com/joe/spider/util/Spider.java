package com.joe.spider.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.joe.http.IHttpClientUtil;
import com.joe.utils.common.Assert;
import com.joe.utils.concurrent.ConcurrentUtil;
import com.joe.utils.concurrent.LockService;
import com.joe.utils.concurrent.ThreadUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 爬虫类，创建后直接添加任务即可，没有任务自动关闭
 *
 * @author joe
 * @version 2017.12.18 21:52
 */
@Slf4j
public class Spider {

    /**
     * 空任务
     */
    private static final SpiderTask         NULL_SPIDER_TASK = new SpiderTask(0, null);

    private final IHttpClientUtil           client;

    /**
     * 任务线程池
     */
    private ExecutorService                 executor;

    /**
     * 默认执行时间间隔，单位毫秒
     */
    private volatile Long                   interval;

    /**
     * 指定域名最后一次的执行时间
     */
    private volatile Map<String, Long>      lastExecTimes;

    /**
     * 特定域名对应的抓取时间间隔，单位毫秒
     */
    private volatile Map<String, Long>      intervals;

    /**
     * 延迟队列
     */
    private volatile DelayQueue<SpiderTask> delayQueue;

    /**
     * 是否关闭，shutdown表示关闭
     */
    private volatile boolean                shutdown;

    /**
     * 关闭状态锁
     */
    private volatile Lock                   shutdownLock;

    /**
     * 主线程
     */
    private Thread                          mainThread;

    /**
     * 默认构造器
     *
     * @param interval 同一个域名两次抓取时间间隔，小于等于0时表示没有间隔，会作用与所有域名，单位毫秒
     */
    public Spider(long interval) {
        this(interval, null);
    }

    /**
     * 默认构造器
     *
     * @param interval 同一个域名两次抓取时间间隔，小于等于0时表示没有间隔，会作用与所有域名，单位毫秒
     */
    public Spider(long interval, ThreadPoolExecutor executor) {
        this.interval = interval;
        this.client = new IHttpClientUtil();
        this.shutdownLock = new ReentrantLock();
        this.shutdown = true;
        init(executor);
    }

    private void init(ThreadPoolExecutor executor) {
        ConcurrentUtil.execWithLock(shutdownLock, () -> {
            if (shutdown) {
                log.info("初始化爬虫");
                if (executor == null) {
                    this.executor = ThreadUtil.createPool(ThreadUtil.PoolType.IO);
                } else {
                    this.executor = executor;
                }
                this.lastExecTimes = new HashMap<>();
                this.intervals = new HashMap<>();
                this.delayQueue = new DelayQueue<>();
                this.shutdown = false;
                this.mainThread = new Thread(() -> {
                    try {
                        while (!shutdown || delayQueue.size() != 0) {
                            SpiderTask task = delayQueue.take();
                            // 表示要结束执行了
                            if (task == NULL_SPIDER_TASK) {
                                return;
                            }
                            // 执行任务
                            this.executor.execute(task);
                        }
                    } catch (InterruptedException e) {
                        // 忽略中断异常，直接返回
                        log.warn("爬虫线程被中断");
                    } finally {
                        // 执行结束清理，直接关闭
                        ConcurrentUtil.execWithLock(shutdownLock, () -> {
                            this.shutdown = true;
                            this.lastExecTimes.clear();
                            this.intervals.clear();
                            this.delayQueue.clear();
                            if (!this.executor.isShutdown()) {
                                this.executor.shutdown();
                            }
                        });
                    }
                });
                mainThread.start();
                log.info("爬虫初始化完毕");
            }
        });
    }

    /**
     * 关闭爬虫任务
     * @param now 是否立即关闭，true表示停止任务，立即关闭
     */
    public void shutdown(boolean now) {
        ConcurrentUtil.execWithLock(shutdownLock, () -> {
            if (shutdown) {
                return;
            }
            log.info("关闭爬虫任务");
            shutdown = true;
            if (now) {
                // 直到添加成功
                while (!delayQueue.add(NULL_SPIDER_TASK)) {

                }
                if (!this.executor.isShutdown()) {
                    executor.shutdownNow();
                }
            }
        });
    }

    /**
     * 当前是否关闭状态
     * @return true表示关闭
     */
    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * 设置特定域名对应的抓取间隔
     *
     * @param url      URL
     * @param interval 对应的抓取间隔，单位毫秒
     * @throws MalformedURLException URL错误抛出该异常
     */
    public void setInterval(String url, long interval) throws MalformedURLException {
        setInterval(new URL(url), interval);
    }

    /**
     * 设置特定域名对应的抓取间隔
     *
     * @param url      URL
     * @param interval 对应的抓取间隔，单位毫秒
     */
    public void setInterval(URL url, long interval) {
        if (url == null) {
            log.warn("设置特定域名对应的抓取时间间隔不能为null");
            return;
        }
        String host = url.getHost();
        log.debug("更新host[{}]对应的抓取时间间隔为：{}", host, interval);
        intervals.put(host, interval);
    }

    /**
     * 添加一个爬虫任务
     *
     * @param url      要爬取的URL
     * @param callback 该爬虫爬取成功后的回调
     * @return 添加结果，true表示添加成功
     * @throws MalformedURLException URL错误抛出该异常
     */
    public <T extends Callback> boolean addTask(String url,
                                                T callback) throws MalformedURLException {
        Assert.notBlank(url);
        Assert.notNull(callback);
        String host = new URL(url).getHost();
        log.debug("添加任务[{}],任务对应主机为：{}", url, host);

        Lock lock = LockService.getLock(host);
        try {
            return ConcurrentUtil.execWithLock(shutdownLock,
                () -> ConcurrentUtil.execWithLock(lock, () -> {
                    if (shutdown) {
                        return false;
                    }
                    long now = System.currentTimeMillis();
                    // 执行间隔
                    long interval = Math.max(intervals.getOrDefault(host, this.interval), 0);
                    // 获取本次执行时间
                    long lastExecTime = lastExecTimes.getOrDefault(host, now) + interval;

                    // 创建任务
                    SpiderTask task = new SpiderTask(lastExecTime - now, () -> {
                        try {
                            String result = client.executeGet(url);
                            log.debug("请求[{}]结果为：{}", url, result);
                            callback.exec(result);
                        } catch (Exception e) {
                            log.error("请求URL[{}]失败", url, e);
                        }
                    });

                    if (delayQueue.add(task)) {
                        // 刷新最后一次执行时间
                        lastExecTimes.put(host, lastExecTime);
                        return true;
                    } else {
                        return false;
                    }
                }));
        } catch (Exception e) {
            throw new SpiderException(e);
        }
    }

    /**
     * 爬虫任务
     */
    private static class SpiderTask implements Delayed, Runnable {

        /**
         * 延迟执行时间，单位毫秒
         */
        private long     delay;

        /**
         * 实际任务
         */
        private Runnable task;

        /**
         * 起始时间
         */
        private long     begin;

        public SpiderTask(long delay, Runnable task) {
            this.delay = delay;
            this.task = task;
            this.begin = System.currentTimeMillis();
        }

        @Override
        public void run() {
            task.run();
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(delay - (System.currentTimeMillis() - begin),
                TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            Assert.notNull(o, "o不能为null");
            if (o instanceof SpiderTask) {
                SpiderTask spiderTask = (SpiderTask) o;
                return (int) (this.delay - spiderTask.delay);
            }
            throw new SpiderException("不支持的类型：" + o.getClass());
        }
    }

}
