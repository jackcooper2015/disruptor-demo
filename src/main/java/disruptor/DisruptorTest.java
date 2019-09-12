package disruptor;

import java.util.UUID;

/**
 * @author jack-cooper
 * @version 1.0.0
 * @ClassName DisruptorTest.java
 * @Description TODO
 * @createTime 2019年09月12日 16:25:00
 */
public class DisruptorTest {

    /**
     *  参考：https://www.cnblogs.com/haiq/p/4112689.html
     * @param args
     */
    public static void main(String[] args) {
        //spring 项目下不用此行代码，给init发放加@PostConstruct即可
        DisruptorUtil.start();
        //测试代码
        for (long i = 1; i < 2000; i++) {
            String uuid = UUID.randomUUID().toString();
            DisruptorUtil.sendMsg(new EventModel(i+"",uuid));
        }

        DisruptorUtil.stop();
    }


}



