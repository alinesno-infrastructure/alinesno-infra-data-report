package com.alinesno.infra.common.web.adapter.plugins;

import com.alinesno.infra.data.report.entity.BusinessModelEntity;
import com.alinesno.infra.data.report.entity.FileReportEntity;
import com.alinesno.infra.data.report.service.IBusinessModelService;
import com.alinesno.infra.data.report.service.IFileReportService;
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
@Component("MessageFailPlugin")
public class MessageFailPlugin implements TranslatePlugin {

    @Autowired
    IBusinessModelService businessModelService;

    @Autowired
    IFileReportService fileReportService;

    private final String MODEL_ID =    "modelId";                  //模型id
    private final String MODEL_NAME =  "modelName";                //模型名称
    private final String FILE_REPORT_ID   =   "fileReportId";      //上报文件id
    private final String FILE_REPORT_NAME  =   "fileReportName";   //上报文件名称

    @Override
    public void translate(ArrayNode node, TranslateCode convertCode) {
        if (node.size() > 0 ) {
            //获取业务模型列表
            List<String> modelIds = this.extractIds(node, MODEL_ID);
            List<BusinessModelEntity> modelIdList = this.businessModelService.listByIds(modelIds);
            Map<String, BusinessModelEntity> modelIdMap = this.toEntityMap(modelIdList);

            //获取上报文件列表
            List<String> fileIds = this.extractIds(node, FILE_REPORT_ID);
            List<FileReportEntity> fileIdList = this.fileReportService.listByIds(fileIds);
            Map<String, FileReportEntity> fileIdMap = this.toEntityMap(fileIdList);


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

                }

                //从返回的列表中获取上报文件id
                String aFileId = jsonObject.get(FILE_REPORT_ID).asText();
                //从查找的列表里拿到对应的业务模型
                FileReportEntity fileReport = fileIdMap.get(aFileId);
                if (fileReport != null) {
                    //设置模型名称
                    rootNode.put(FILE_REPORT_NAME + LABEL_SUFFER, fileReport.getFileFullName());

                }

            });
        }


    }
}
