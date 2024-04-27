package com.alinesno.infra.common.web.adapter.plugins;

import com.alibaba.fastjson.JSONObject;
import com.alinesno.infra.data.report.entity.BusinessModelEntity;
import com.alinesno.infra.data.report.enums.messageTypeEnum;
import com.alinesno.infra.data.report.enums.reportStatusEnum;
import com.alinesno.infra.data.report.service.IBusinessModelService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 业务模型转换
 *
 * @author LiuGB
 * @date 2023年5月9日
 */
@Component("FileReportPlugin")
public class FileReportPlugin implements TranslatePlugin {

    @Autowired
    IBusinessModelService businessModelService;

    private final String MODEL_ID =    "modelId";
    private final String MODEL_NAME =  "modelName";                //模型名称
    private final String MODEL_MESSAGE_TYPE   =   "messageType";   //消息类型
    private final String MODEL_KAFKATOPICE  =   "kafkaTopice";     //消息主题
    private final String MODEL_COLUMNNUM    =   "columnNum";       //列数
    private final String MODEL_COLUMNCNNAME =   "columnCnName";    //列中文名列表
    private final String MODEL_COLUMNNAME   =   "columnName";      //列英文名列表
    private final String STATUS_TYPE   =   "reportStatus";         //上报状态


    @Override
    public void translate(ArrayNode node, TranslateCode convertCode) {
        if (node.size() > 0 ) {
            //获取业务模型列表
            List<String> modelIds = this.extractIds(node, MODEL_ID);
            List<BusinessModelEntity> modelIdList = this.businessModelService.listByIds(modelIds);
            Map<String, BusinessModelEntity> modelIdMap = this.toEntityMap(modelIdList);


            //转换逻辑
            node.forEach(jsonObject -> {
                ObjectNode rootNode = (ObjectNode) jsonObject;

                //从返回的列表中获取业务模型id
                String aModelId = jsonObject.get(MODEL_ID).asText();
                //从查找的列表里拿到对应的业务模型
                BusinessModelEntity businessModel = modelIdMap.get(aModelId);
                if (businessModel != null) {
                    //设置模型名称
                    rootNode.put(MODEL_NAME + LABEL_SUFFER, businessModel.getModelName());
                    //消息类型
                    rootNode.put(MODEL_MESSAGE_TYPE, businessModel.getMessageType());
                    //消息类型
                    rootNode.put(MODEL_MESSAGE_TYPE + LABEL_SUFFER, messageTypeEnum.getEnumDesc(Math.toIntExact(businessModel.getMessageType())));
                    //设置消息主题
                    rootNode.put(MODEL_KAFKATOPICE, businessModel.getKafkaTopice());
                    //设置列数
                    rootNode.put(MODEL_COLUMNNUM + LABEL_SUFFER, businessModel.getColumnNum());
                    //设置列中文名列表
                    rootNode.put(MODEL_COLUMNCNNAME + LABEL_SUFFER, businessModel.getColumnCnName());
                    //设置列英文名列表
                    rootNode.put(MODEL_COLUMNNAME + LABEL_SUFFER, businessModel.getColumnName());
                }

                //消息类型
                rootNode.put(STATUS_TYPE + LABEL_SUFFER, reportStatusEnum.getEnumDesc(Integer.valueOf(jsonObject.get(STATUS_TYPE).asText())));


            });


        }


    }
}
