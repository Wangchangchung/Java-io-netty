package com.wcc.bio2;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by charse on 17-6-19.
 */
public class TimeServerHandlerExecutePool {

    // 线程池
    private ExecutorService  executor;

    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize){
        //创建一个线程池
        this.executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                maxPoolSize, 120L,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize));
    }

    public void  execute(Runnable task){

        executor.execute(task);
    }
}
