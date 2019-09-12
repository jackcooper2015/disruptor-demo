package disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * 3、定义事件处理的具体实现
 * <pre>
 *     通过实现接口 com.lmax.disruptor.EventHandler<T> 定义事件处理的具体实现
 * </pre>
 */
public class DemoEventHandler implements EventHandler<EventModel> {

    @Override
    public void onEvent(EventModel model, long sequence, boolean endOfBatch) throws Exception {
        /**
         *  todo 在此写接收到事件的逻辑
         *  （此处需要判断数据完整性）
         */

        System.out.println("model = " + model+",sequence="+sequence+","+endOfBatch);
    }
}
