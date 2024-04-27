package com.alinesno.infra.data.report.service;

import com.alinesno.infra.common.facade.response.ResultCodeEnum;
import com.alinesno.infra.data.report.config.KafkaConfig;
import com.alinesno.infra.data.report.vo.ResponseBean;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaProducerService {

    //日志记录
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaConfig kafkaConfig;

    private Properties props ;
    private Producer<String,String> producer ;


    private void initProducer(){

       if ( kafkaConfig.getBootstrap() == null  || kafkaConfig.getBootstrap().equals("") ) {
           log.error("请配置kafka bootstrap-servers信息!");
       }


       props = new Properties();
       // 必须
       props.put("bootstrap.servers",kafkaConfig.getBootstrap());
       // 被发送到broker的任何消息的格式都必须是字节数组
       props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
       props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");

       // 非必须参数配置
       // acks=0表明producer完全不管发送结果；
       // acks=all或-1表明producer会等待ISR所有节点均写入后的响应结果；
       // acks=1，表明producer会等待leader写入后的响应结果
       props.put("acks","all");
       // 发生可重试异常时的重试次数
       props.put("retries",1);
       // producer会将发往同一分区的多条消息封装进一个batch中，
       // 当batch满了的时候，发送其中的所有消息,不过并不总是等待batch满了才发送消息；
       props.put("batch.size",16384);
       // 控制消息发送延时，默认为0，即立即发送，无需关心batch是否已被填满。
       props.put("linger.ms",10);
       // 指定了producer用于缓存消息的缓冲区大小，单位字节，默认32MB
       // producer启动时会首先创建一块内存缓冲区用于保存待发送的消息，然后由另一个专属线程负责从缓冲区中读取消息执行真正的发送
       props.put("buffer.memory",33554432);
       // 设置producer能发送的最大消息大小
       props.put("max.request.size",10485760);
       // 设置是否压缩消息，默认none
       props.put("compression.type","lz4");
       // 设置消息发送后，等待响应的最大时间
       props.put("request.timeout.ms",30);

       // enable.idempotence 设置为 true， max.in.flight.requests.per.connection 设置为 1 来确保只有一个消息被传递，从而保证消息的幂等性
       props.put("enable.idempotence", "true");
       props.put("max.in.flight.requests.per.connection", 1);

       producer = new KafkaProducer<String, String>(props);
   }


    public ResponseBean sendMessage(String topic, String msg ){
        if (producer == null)
        {
            initProducer();
        }

        ResponseBean result = new ResponseBean() ;

        String messageId = getUniqueId(msg);

       Future<RecordMetadata> send =producer.send(new ProducerRecord<>(topic, messageId, msg));

        try {
            //发往kakfa时，如超过5分钟没有发送出去，则报异常，否则为发送成功
            send.get(5l, TimeUnit.MINUTES);
            result.setCode(ResultCodeEnum.SUCCESS);
            result.setMessage("成功发送到kafka!");
            return result;
        }catch ( ExecutionException | InterruptedException | TimeoutException e ){
            log.error("发送kafka异常!"+ e.getMessage());
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage("发送kafka异常!"+ e.getMessage());
            return   result;
        }

    }


    /**
     * <<<<<<< HEAD  检查kafka是否正常工作
     *
     * @return ResponseBean
     */
    public ResponseBean checkWorkingProperly()  {

        ResponseBean result = new ResponseBean() ;

        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrap());

        AdminClient adminClient = AdminClient.create(properties);
        DescribeClusterResult describeClusterResult = adminClient.describeCluster(new DescribeClusterOptions().timeoutMs(2000));
        KafkaFuture<Collection<Node>> nodes = describeClusterResult.nodes();


        try {
            if (nodes.get().size() > 0) {
                result.setCode(ResultCodeEnum.SUCCESS);

            }else {
                log.error("请联系管理员!Kafka brokers are not working");
                result.setCode(ResultCodeEnum.FAIL);
                result.setMessage("请联系管理员!Kafka brokers are not working");
            }
        } catch ( InterruptedException e ) {
            e.printStackTrace();
            log.error("请联系管理员!Kafka brokers are not working");
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage("请联系管理员!"+e.getMessage());
        } catch ( ExecutionException e ) {
            e.printStackTrace();
            log.error("请联系管理员!Kafka brokers are not working");
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage("请联系管理员!"+e.getMessage());
        }
        adminClient.close();
        return result;

    }


    // 获取消息的唯一标识符
    public static String getUniqueId(String message) {
        log.debug("数据:{},uuid:{}",message,UUID.nameUUIDFromBytes(message.getBytes()).toString());
        return UUID.nameUUIDFromBytes(message.getBytes()).toString();
    }


}
