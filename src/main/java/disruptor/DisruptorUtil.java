package disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jack-cooper
 * @version 1.0.0
 * @ClassName DisruptorConfig.java
 * @Description TODO
 * @createTime 2019年09月12日 17:28:00
 */
public class DisruptorUtil {
    private static RingBuffer<EventModel> ringBuffer;
    private static Disruptor<EventModel> disruptor;
    private static ExecutorService executorService;


    @PostConstruct
    @SuppressWarnings("unchecked")
    public static void start(){
        /**
         * 4、Disruptor 通过 java.util.concurrent.ExecutorService 提供的线程来触发 Consumer 的事件处理
         */
        executorService = Executors.newCachedThreadPool();
        /**
         * 5、指定等待策略
         * <pre>
         *     Disruptor 定义了 com.lmax.disruptor.WaitStrategy 接口用于抽象 Consumer 如何等待新事件，这是策略模式的应用。
         *     Disruptor 提供了多个 WaitStrategy 的实现，每种策略都具有不同性能和优缺点，根据实际运行环境的 CPU 的硬件特点
         *     选择恰当的策略，并配合特定的 JVM 的配置参数，能够实现不同的性能提升。
         *      (1) 、BlockingWaitStrategy 是最低效的策略，但其对CPU的消耗最小并且在各种不同部署环境中能提供更加一致的性能表现
         *     （2）、SleepingWaitStrategy 的性能表现跟 BlockingWaitStrategy 差不多，对 CPU 的消耗也类似，但其对生产者线程的影响最小，适合用于异步日志类似的场景；
         *     （3）、YieldingWaitStrategy 的性能是最好的，适合用于低延迟的系统。在要求极高性能且事件处理线数小于 CPU 逻辑核心数的场景中，推荐使用此策略；例如，CPU开启超线程的特性。
         * </pre>
         */
        // 多发送
        //Disruptor<DemoEvent> disruptor = new Disruptor<>(DemoEvent.EVENT_FACTORY, 1024, executorService);
        //单发送
        disruptor = new Disruptor<>(EventModel.EVENT_FACTORY, 1024, executorService, ProducerType.SINGLE,new YieldingWaitStrategy());
        /**
         * 6、注册事件消费处理器
         */
        final EventHandler<EventModel> handler = new DemoEventHandler();
        disruptor.handleEventsWith(handler);
        /**
         * 启动disruptor
         */
        ringBuffer = disruptor.start();
    }

    /**
     * 关闭disruptor
     */
    @PreDestroy
    public static void stop(){
        //关闭 disruptor，方法会堵塞，直至所有的事件都得到处理；
        disruptor.shutdown();
        //关闭 disruptor 使用的线程池；如果需要的话，必须手动关闭， disruptor 在 shutdown 时不会自动关闭；
        executorService.shutdown();
    }


    /**
     * 发布事件：
     * <pre>
     *   Disruptor 的事件发布过程是一个两阶段提交的过程
     *    （1）、先从 RingBuffer 获取下一个可以写入的事件的序号；
     *    （2）、获取对应的事件对象，将数据写入事件对象；
     *    （3）、将事件提交到 RingBuffer;
     *   事件只有在提交之后才会通知 EventProcessor 进行处理；
     *   注意: 最后的 ringBuffer.publish 方法必须包含在 finally 中以确保必须得到调用；如果某个请求的 sequence 未被提交，将会堵塞后续的发布操作或者其它的 producer。
     *
     *   此外，Disruptor 要求 RingBuffer.publish 必须得到调用的潜台词就是，如果发生异常也一样要调用 publish ，那么，很显然这个时候需要调用者在事件处理的实现上来判
     *   断事件携带的数据是否是正确的或者完整的，这是实现者应该要注意的事情。
     * </pre>
     */
    public static void sendMsg(EventModel eventModel){
        //请求下一个事件序号；
        final long seq = ringBuffer.next();
        try {
            //获取该序号对应的事件对象；
            final EventModel demoEvent = ringBuffer.get(seq);
            demoEvent.setId(eventModel.getId());
            demoEvent.setName(eventModel.getName());
        } finally {
            //发布事件；
            ringBuffer.publish(seq);
        }
    }
}
