package com.alinesno.infra.common.web.adapter.plugins;

import com.alinesno.infra.data.report.entity.BusinessModelEntity;
import com.alinesno.infra.data.report.entity.FileReportEntity;
import com.alinesno.infra.data.report.enums.messageSendBusEnum;
import com.alinesno.infra.data.report.enums.messageTypeEnum;
import com.alinesno.infra.data.report.enums.reportStatusEnum;
import com.alinesno.infra.data.report.service.IBusinessModelService;
import com.alinesno.infra.data.report.service.IFileReportService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * 数据管理转换
 *
 * @author LiuGB
 * @date 2023年5月9日
 */
@Component("MessagePlugin")
public class MessagePlugin implements TranslatePlugin {

    @Autowired
    IBusinessModelService businessModelService;

    @Autowired
    IFileReportService fileReportService;

    private final String MODEL_ID =    "modelId";
    private final String MODEL_NAME =  "modelName";                //模型名称
    private final String MODEL_MESSAGE_TYPE   =   "messageType";   //消息类型
    private final String MODEL_KAFKATOPICE  =   "kafkaTopice";     //消息主题
    private final String FILE_ID =    "fileId";
    private final String FILE_NAME =  "fileName";                  //模型名称
    private final String STATUS   =   "reportStatus";              //上报状态
    private final String IFSENDBUS   =   "ifSendBus";              //上报状态


    @Override
    public void translate(ArrayNode node, TranslateCode convertCode) {
        if (node.size() > 0 ) {

            //获取业务模型列表
            List<String> modelIds = this.extractIds(node, MODEL_ID);
            List<BusinessModelEntity> modelIdList = this.businessModelService.listByIds(modelIds);
            Map<String, BusinessModelEntity> modelIdMap = this.toEntityMap(modelIdList);


            //获取上报文件列表
            List<String> fileIds = this.extractIds(node, FILE_ID);
            List<FileReportEntity> fileIdList = this.fileReportService.listByIds(fileIds);
            Map<String, FileReportEntity> fileIdMap = this.toEntityMap(fileIdList);


            //转换逻辑
            node.forEach(jsonObject -> {
                ObjectNode rootNode = (ObjectNode) jsonObject;

                //从返回的列表中获取业务模型id
                String aModelId = jsonObject.get(MODEL_ID).asText();
                //从查找的列表里拿到对应的业务模型
                BusinessModelEntity businessModel = modelIdMap.get(aModelId);
                if ( businessModel != null ) {
                    //设置模型名称
                    rootNode.put(MODEL_NAME + LABEL_SUFFER, businessModel.getModelName());
                    //目标库
                    rootNode.put(MODEL_MESSAGE_TYPE + LABEL_SUFFER, messageTypeEnum.getEnumDesc(Math.toIntExact(businessModel.getMessageType())));
                    //设置消息主题
                    rootNode.put(MODEL_KAFKATOPICE + LABEL_SUFFER, businessModel.getKafkaTopice());

                }

                //从返回的列表中获取上报文件id
                String afileId = jsonObject.get(FILE_ID).asText();
                //从查找的列表里拿到对应的上报文件
                FileReportEntity fileReport = fileIdMap.get(afileId);
                if ( fileReport != null ) {
                    //设置上报文件名称
                    rootNode.put(FILE_NAME + LABEL_SUFFER, fileReport.getFileName());

                }

                //上报状态
                rootNode.put(STATUS + LABEL_SUFFER, reportStatusEnum.getEnumDesc(Integer.valueOf(jsonObject.get(STATUS).asText())));
                //是否已发数据总线
                rootNode.put(IFSENDBUS + LABEL_SUFFER, messageSendBusEnum.getEnumDesc(Integer.valueOf(jsonObject.get(IFSENDBUS).asText())));


            });


        }


    }
}
