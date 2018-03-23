package com.joe.spider.util;

import com.joe.http.IHttpClientUtil;
import com.joe.utils.concurrent.LockService;
import com.joe.utils.pattern.PatternUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.regex.Pattern;

/**
 * 爬虫类，创建后直接添加任务即可，没有任务自动关闭
 *
 * @author joe
 * @version 2017.12.18 21:52
 */
@Slf4j
public class Spider {
    private IHttpClientUtil client;
    /**
     * 域名对应的执行器
     */
    private Map<String, SpiderExecutor> executors;
    /**
     * 特定域名对应的抓取时间间隔，单位毫秒
     */
    private Map<String, Long> intervals;
    /**
     * 默认执行时间间隔，单位毫秒
     */
    private Long interval;

    /**
     * 默认构造器
     *
     * @param interval 同一个域名两次抓取时间间隔，小于等于0时表示没有间隔，会作用与所有域名，单位毫秒
     */
    public Spider(long interval) {
        init(interval);
    }

    private void init(long interval) {
        log.info("初始化爬虫");
        this.interval = interval;
        this.client = new IHttpClientUtil();
        this.executors = new HashMap<>();
        this.intervals = new HashMap<>();
        log.info("爬虫初始化完毕");
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
        String host = getHost(url);
        log.debug("更新host[{}]对应的抓取时间间隔为：{}", host, interval);
        intervals.put(host, interval);
        SpiderExecutor executor = executors.get(host);
        if (executor != null) {
            executor.updateInterval(interval);
        }
    }


    /**
     * 添加一个爬虫任务
     *
     * @param url      要爬取的URL
     * @param callback 该爬虫爬取成功后的回调
     * @throws InterruptedException  线程中断异常
     * @throws MalformedURLException URL错误
     */
    public <T extends Callback> void addTask(String url, T callback) throws InterruptedException,
            MalformedURLException {
        log.debug("添加任务[{}]", url);
        URL u = new URL(url);
        String host = getHost(u);
        log.debug("任务对应主机为：{}", host);

        SpiderExecutor executor = executors.get(host);

        if (executor == null) {
            Lock lock = LockService.getLock(host);
            lock.lock();
            try {
                executor = executors.get(host);
                if (executor == null) {
                    executor = new SpiderExecutor(host, intervals.getOrDefault(url, this.interval));
                    executors.put(host, executor);
                }
            } finally {
                lock.unlock();
            }
        }

        Runnable task = () -> {
            try {
                String result = client.executeGet(url);
                log.debug("请求[{}]结果为：{}", url, result);
                callback.exec(result);
            } catch (Exception e) {
                log.error("请求URL[{}]失败", url, e);
            }
        };

        log.debug("添加task到执行器");
        if (!executor.addTask(task)) {
            log.debug("当前执行器[{}]已经被关闭，重新创建一个执行器", host);
            Lock lock = LockService.getLock(host);
            lock.lock();
            try {
                executor = executors.get(host);
                if (executor.isShutdown()) {
                    executor = new SpiderExecutor(host, intervals.getOrDefault(url, this.interval));
                    executors.put(host, executor);
                } else {
                    log.debug("已经有其他线程创建过新的执行器[{}]，无需创建新执行器[{}]", host, host);
                }
                executor.addTask(task);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 获取域名（例如www.baidu.com的域名是baidu.com而不是www.baidu.com，使用URL.getHost只能得到www.baidu.com）
     *
     * @param url URL
     * @return URL对应的域名
     */
    private String getHost(URL url) {
        String host = url.getHost();
        if (host.equals("localhost") || PatternUtils.isIp(host)) {
            return host;
        }
        String suffix;
        int index;
        if (host.endsWith("com.cn") || host.endsWith("net.cn") || host.endsWith("org.cn") || host.endsWith("gov.cn")) {
            index = host.length() - 7;
            suffix = host.substring(index);
        } else {
            index = host.lastIndexOf(".");
            if (index <= 0) {
                log.info("无法获取[{}]的域名", host);
                suffix = host;
            } else {
                suffix = host.substring(index);
            }
        }

        host = host.substring(0, index);
        String pre = host.substring(host.lastIndexOf(".") + 1);
        return pre + suffix;
    }


    private static class SpiderExecutor extends Thread {
        private static AtomicLong count = new AtomicLong(0);
        /**
         * 该爬虫执行器对应的主机名
         */
        private String host;
        /**
         * 该爬虫的两次请求间的间隔，当小于等于0时表示请求完毕立即执行下一个
         */
        private volatile long interval;
        /**
         * 该爬虫对应的任务队列
         */
        private BlockingDeque<Runnable> deque;
        /**
         * 最后一次请求的时间
         */
        private long lastTime = 0;
        /**
         * 最大空闲时间，超过该时间没有任务将会关闭，单位为毫秒
         */
        private long idle;
        /**
         * 当前爬虫是否关闭
         */
        private volatile boolean shutdown = true;
        /**
         * 关闭锁，保证线程正确被关闭
         */
        private CountDownLatch lock;


        /**
         * 默认构造器
         *
         * @param host     执行器对应的host
         * @param interval 执行器的执行间隔，小于等于0表示没有间隔
         */
        SpiderExecutor(String host, long interval) {
            super("爬虫线程-" + host);
            this.host = host;
            this.interval = interval;
            this.deque = new LinkedBlockingDeque<>();
            this.idle = 1000 * 10;
            this.idle = idle > interval ? idle : interval + 1;
            this.start();
        }

        /**
         * 更新执行时间间隔
         *
         * @param interval 新的执行时间间隔
         */
        public void updateInterval(long interval) {
            this.interval = interval;
            this.idle = idle > interval ? idle : interval + 1;
        }

        /**
         * 往该执行器中加入新的任务
         *
         * @param task 任务
         * @return 加入成功返回true，加入失败返回false（当执行器关闭时加入将会失败）
         * @throws InterruptedException 异常中断
         */
        public boolean addTask(Runnable task) throws InterruptedException {
            if (isShutdown()) {
                return false;
            } else {
                deque.putLast(task);
                return true;
            }
        }

        /**
         * 获取该执行器对应的HOST
         *
         * @return 该执行器对应的HOST
         */
        public String getHost() {
            return this.host;
        }

        @Override
        public synchronized void start() {
            this.shutdown = false;
            super.start();
        }

        /**
         * 获取当前执行器是否关闭
         *
         * @return 如果关闭返回true，反之
         * @throws InterruptedException 异常中断
         */
        public boolean isShutdown() throws InterruptedException {
            if (lock == null) {
                return shutdown;
            } else {
                lock.await();
                //重置锁
                lock = null;
                return shutdown;
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Runnable task = deque.pollFirst(idle, TimeUnit.MILLISECONDS);
                    if (task == null) {
                        //加锁，下面语句执行期间外部不能获得线程的状态
                        lock = new CountDownLatch(1);
                        //防止在上面两行执行的时候有新数据插入，同时快速的执行
                        task = deque.pollFirst(1, TimeUnit.MILLISECONDS);
                        if (task == null) {
                            //如果仍然没有，那么关闭线程，打开锁，废弃该线程，同时外部得到的状态为线程已关闭
                            shutdown = true;
                            lock.countDown();
                            log.info("当前超过[{}]毫秒没有任务，关闭线程", idle);
                            return;
                        } else {
                            //说明此时有新任务进来了，不必关闭当前线程，此时只需要释放锁即可
                            lock.countDown();
                        }
                    }
                    log.debug("将要执行task[{}]", task);
                    //判断是否需要等待执行
                    if (this.interval > 0) {
                        long interval = this.interval - System.currentTimeMillis() + lastTime;
                        if (interval > 0) {
                            Thread.sleep(interval);
                        }
                    }
                    task.run();
                } catch (InterruptedException e) {
                    log.warn("爬虫线程被异常中断", e);
                    break;
                } catch (Throwable e) {
                    log.warn("爬虫线程执行异常", e);
                } finally {
                    //更新最后一次执行时间
                    lastTime = System.currentTimeMillis();
                }
            }
        }
    }
}
