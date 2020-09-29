package com.cluo.mall.miaosha.mq;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.cluo.mall.miaosha.dao.ItemDOMapper;
import com.cluo.mall.miaosha.dao.ItemStockDOMapper;

@Component
public class MqConsumer {

	
	private DefaultMQPushConsumer consumer;
	
	@Value("${mq.nameserver.addr}")
	private String nameServerAddr;
	
	@Value("${mq.topicname}")
	private String topicName;
	
	@Autowired
	private ItemStockDOMapper itemStockDOMapper;
	
	@PostConstruct
	public void init() throws MQClientException {
		consumer = new DefaultMQPushConsumer("stock_consumer_group");
		consumer.setNamesrvAddr(nameServerAddr);
		consumer.subscribe(topicName, "*");
		consumer.registerMessageListener(new MessageListenerConcurrently() {

			@Override
			public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext arg1) {
				Message message = msgs.get(0);
				String joinString = new String(message.getBody());
				Map<String,Object> map = JSON.parseObject(joinString, Map.class);
				Integer itemId = (Integer)map.get("itemId");
				Integer amount = (Integer)map.get("amount");
				//实现库存到数据库
				itemStockDOMapper.decreaseStock(itemId, amount);
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			}
			
		});
	}
}
