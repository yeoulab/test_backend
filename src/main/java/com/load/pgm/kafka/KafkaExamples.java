package com.load.pgm.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

enum KafkaConfigExtra{
	TOPIC,
	THREAD_SLEEP_TIME,
	WORD_COUNT,
	TPS,
	SYNC_FLAG
}

@RestController
@RequestMapping(value="/kafka")
public class KafkaExamples {
	
	static final Logger logger = LoggerFactory.getLogger(KafkaExamples.class);
	static List<ConsumerThread> ConsumerThreadList = new ArrayList<ConsumerThread>();
	static List<ProducerThread> ProducerThreadList = new ArrayList<ProducerThread>();
	static Lorem lorem = LoremIpsum.getInstance();
	static int messageCnt = 1;
	
	@RequestMapping(value="/health")
	public String health() {
		return "kafka example connection success";
	}
	
	
	@PostMapping(value="/producer", produces="application/json; charset=utf8")
	public void makeProducerThread(@RequestBody HashMap<String, Object> params) {
		ProducerThread pt = new ProducerThread(params);
		ProducerThreadList.add(pt);
		
		ExecutorService producerExecutorService = Executors.newCachedThreadPool();
		producerExecutorService.execute(pt);
	}
	
	@GetMapping("/producer")
	@ResponseBody
	public ArrayList<HashMap<String, String>> getProducer(){
		ArrayList<HashMap<String, String>> rtnList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> threadId;
		String tmpThreadName = "";
		
		for( ProducerThread t : ProducerThreadList ) {
			threadId = new HashMap<String, String>();
			tmpThreadName = t.toString();
			String[] tmpSplitWord1 = tmpThreadName.split("\\[");
			String[] tmpSplitWord2 = tmpSplitWord1[1].split(",");
			threadId.put("threadName", tmpSplitWord2[0]);
			rtnList.add(threadId);
		}
		
		return rtnList;
	}
	
	@RequestMapping(value="/producer/{id}", method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteProducer(@PathVariable String id) {
		logger.debug("id : {}", id);
		try {
			for( ProducerThread t : ProducerThreadList ) {
				if( t.toString().contains(id) ) {
					try {
						t.setStop();
					}catch( Exception e ) {
						logger.debug(e.getMessage());
						break;
					}
					
					ProducerThreadList.remove(t);
				}
			}
		}catch( Exception e ) {
			System.out.println(e.getMessage());
		}
	}
	
	@PostMapping(value="/consumer", produces="application/json; chaset=utf8")
	public void makeConsumerThread(@RequestBody HashMap<String, Object> params) {
		ConsumerThread ct = new ConsumerThread(params);
		ConsumerThreadList.add(ct);
		
		ExecutorService consumerExecutorService = Executors.newCachedThreadPool();
		consumerExecutorService.execute(ct);
	}
	
	@GetMapping("/consumer")
	@ResponseBody
	public ArrayList<HashMap<String, String>> getConsumer(@RequestBody HashMap<String, Object> params) {
		ArrayList<HashMap<String, String>> rtnList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> threadId;
		String tmpThreadName = "";
		
		for( ConsumerThread t : ConsumerThreadList ) {
			threadId = new HashMap<String, String>();
			tmpThreadName = t.toString();
			String[] tmpSplitWord1 = tmpThreadName.split("\\[");
			String[] tmpSplitWord2 = tmpSplitWord1[1].split(",");
			threadId.put("threadName", tmpSplitWord2[0]);
			rtnList.add(threadId);
		}
		
		return rtnList;		
	}
	
	@RequestMapping(value="/consumer/{id}", method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteConsumer(@PathVariable String id) {
		logger.debug("id : {}", id);
		try {
			for( ConsumerThread t : ConsumerThreadList ) {
				if( t.toString().contains(id) ) {
					try {
						t.setStop();
					}catch( Exception e ) {
						logger.debug(e.getMessage());
						break;
					}
					
					ConsumerThreadList.remove(t);
				}
			}
		}catch( Exception e ) {
			System.out.println(e.getMessage());
		}
	}
	
	class ConsumerThread extends Thread {
		int memberId = (int) new Date().getTime();
		private HashMap<String, Object> config;
		private boolean stop = false;
		private String topicName = "";
		private int threadSleepMilliSec = 500; // 기본 쓰레드 슬립 0.5s
		
		public ConsumerThread() {}
		
		public ConsumerThread(HashMap<String, Object> params) {
			this.config = params;
		}
		
		public void setStop() {
			logger.debug("Thread Stop");
			this.stop = true;
		}
		
		@Override
		public void run() {
			Properties props = new Properties();
			
			//devault value
			props.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
			props.put("group.id", "ringo-consumer");
			props.put("enable.auto.commit", "false");
			props.put("auto.offset.reset", "earliest");
			props.put("max.poll.interval.ms", "10000");
			props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
			props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
			
			//토픽
			topicName = (String)config.get("TOPIC");
			// 쓰레드 슬립 타임(TPS 조절용)
			threadSleepMilliSec = Integer.parseInt((String)config.get("THREAD_SLEEP_TIME"));
			
			// 화면에서 받는 값 세팅
			Set configSet = config.entrySet();
			Iterator configIterator = configSet.iterator();
			while( configIterator.hasNext() ) {
				Entry<String, String> configEntry = (Entry) configIterator.next();
				String key = (String)configEntry.getKey();
				String value = (String)configEntry.getValue();
				logger.debug("key : {} / value : {}", key, value);
				
				props.put(key, value);
			}
			
			KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
			consumer.subscribe(Arrays.asList(topicName));
			
			try {
				while( true ) {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
					logger.debug("records.count : {}", records.count());
					for( ConsumerRecord<String, String> record : records ) {
						Thread.sleep(threadSleepMilliSec);
						logger.debug(String.format("Topic : %s, partition : %s, offset : %d, key : %s, values : %s \n"
								, record.topic(), record.partition(), record.offset(), record.key(), record.value()));
					}
					consumer.commitSync();
					if( this.stop ) {
						logger.debug("Consumer Thread Stop");
						break;
					}
				}
				
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				System.out.println("shutdown");
				consumer.commitAsync();
				consumer.close();
			}
			
		}
	
	}
	
	class ProducerThread extends Thread {
		private HashMap<String, Object> config;
		private boolean stop = false;
		
		private String topicName = "";
		private int threadSleepMilliSec = 1000; // 기본 스레드 슬립 1초
		private int wordCount = 100; // 기본 100자
		private boolean syncFlag = true;
		
		public ProducerThread() {}
		
		public ProducerThread(HashMap<String, Object> params) {
			this.config = params;
		}
		
		public void setStop() {
			this.stop = true;
		}
		
		@Override
		public void run() {
			Properties props = new Properties();
			props.put("acks", "1");
			props.put("compression.type", "gzip");
			props.put("bootstrap.servers", "localhost:9200,localhost:9093,localhost:9094");
			props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
			props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
			
			topicName = (String)config.get("TOPIC");
			threadSleepMilliSec = 1000 / Integer.parseInt((String)config.get("TPS"));
			wordCount = Integer.parseInt((String)config.get("WORD_COUNT"));
			syncFlag = Boolean.parseBoolean((String)config.get("SYNC_FLAG"));
			
			logger.debug("topicName : {}, threadSleepMilliSec : {}, wordCount : {}, syncFlag : {}",
					topicName, threadSleepMilliSec, wordCount, syncFlag);
			
			Set configSet = config.entrySet();
			Iterator configIterator = configSet.iterator();
			while( configIterator.hasNext() ) {
				Entry<String, String> configEntry = (Entry) configIterator.next();
				String key = (String)configEntry.getKey();
				String value = (String)configEntry.getValue();
				logger.debug("key : {} / value : {}", key, value);
				
				props.put(key, value);
			}
			
			Producer<String, String> producer = new KafkaProducer<>(props);
			String message = "";
			
			if( syncFlag ) {
				try {
					while( true ) {
						message = String.format("messageCount:%d, Meesage:%s", messageCnt, lorem.getWords(wordCount));
						RecordMetadata metadata = producer.send(new ProducerRecord<String, String>((String)config.get("TOPIC"), message)).get();
						logger.debug("partition : " + metadata.partition() + " offset : " + metadata.offset());
						Thread.sleep(threadSleepMilliSec);
						
						if( this.stop ) {
							break;
						}
						
						messageCnt++;
					}
				}catch( Exception e) {
					e.printStackTrace();
				}finally {
					producer.close();
				}
			}else {
				try {
					while( true ) {
						message = String.format("messageCount:%d, Meesage:%s", messageCnt, lorem.getWords(wordCount));
						producer.send(new ProducerRecord<String, String>((String)config.get("TOPIC"), message), new KafkaCallback());
						System.out.println(System.currentTimeMillis());
						Thread.sleep(threadSleepMilliSec);
						
						if( this.stop ) {
							break;
						}
						
						messageCnt++;
					}
				}catch(Exception e) {
					e.printStackTrace();
				}finally {
					producer.close();
				}
			}
			
		}
	}
	
	class KafkaCallback implements Callback{
		public void onCompletion(RecordMetadata metadata, Exception e) {
			if( metadata != null ) {
				logger.debug("partition : " + metadata.partition() + " offset : " + metadata.offset());
			}else {
				e.printStackTrace();
			}
		}
	}

}
