package com.alinesno.infra.common.web.adapter.plugins;


import com.alinesno.infra.data.report.entity.BusinessModelEntity;
import com.alinesno.infra.data.report.enums.messageTypeEnum;
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
@Component("BusinessModelPlugin")
public class BusinessModelPlugin implements TranslatePlugin {

    @Autowired
    IBusinessModelService businessModelService;

    private final String TYPE_PARENT_ID =    "modelParentId";
    private final String TYPE_NAME =  "modelParentName";
    private final String messageType =  "messageType";



    @Override
    public void translate(ArrayNode node, TranslateCode convertCode) {
        if (node.size() > 0 ) {
            //获取业务模型列表
            List<String> modelParentIds = this.extractIds(node, TYPE_PARENT_ID);
            List<BusinessModelEntity> modelParentList = this.businessModelService.listByIds(modelParentIds);
            Map<String, BusinessModelEntity> modelParentMap = this.toEntityMap(modelParentList);


            //转换逻辑
            node.forEach(jsonObject -> {
                ObjectNode rootNode = (ObjectNode) jsonObject;
                //从返回的列表中获取业务模型id
                String aTypeIdID = jsonObject.get(TYPE_PARENT_ID).asText();
                //从查找的列表里拿到对应的业务模型
                BusinessModelEntity businessModel = modelParentMap.get(aTypeIdID);
                if (businessModel != null) {
                    //设置返回值
                    rootNode.put(TYPE_NAME + LABEL_SUFFER, businessModel.getModelName());
                }

                //消息类型
                rootNode.put(messageType + LABEL_SUFFER, messageTypeEnum.getEnumDesc(Integer.valueOf(jsonObject.get(messageType).asText())));


            });
        }


    }
}
