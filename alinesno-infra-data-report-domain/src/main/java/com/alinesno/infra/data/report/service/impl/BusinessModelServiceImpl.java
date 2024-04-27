package com.alinesno.infra.data.report.service.impl;

import com.alinesno.infra.common.core.service.impl.IBaseServiceImpl;
import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.data.report.config.MinioConfig;
import com.alinesno.infra.data.report.dto.ModelDTO;
import com.alinesno.infra.data.report.entity.BusinessModelEntity;
import com.alinesno.infra.data.report.mapper.BusinessModelMapper;
import com.alinesno.infra.data.report.service.IBusinessModelService;
import com.alinesno.infra.data.report.util.MinioFileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author paul
 * @date 2022-11-28 10:28:04
 */
@Service
public class BusinessModelServiceImpl extends IBaseServiceImpl< BusinessModelEntity, BusinessModelMapper> implements IBusinessModelService {
    //日志记录
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(BusinessModelServiceImpl.class);

    @Autowired
    private BusinessModelMapper businessModelMapper;

    @Autowired
    private MinioFileUtil minioFileUtil;

    @Autowired
    private MinioConfig minioConfig;


    @Override
    public void createTable(String createTableSql) {
        businessModelMapper.createTable(createTableSql);
    }

    @Override
    public Map<String, String> checkTableExistsWithShow(String tableName) {
        return  businessModelMapper.checkTableExistsWithShow(tableName);
    }

    //检查同一个用户下，业务模型名称是否存在，如已存在，则不允许保存。确同一个用户下业务模型名称唯一
    @Override
    public AjaxResult checkIfExist(BusinessModelEntity businessMode){

        QueryWrapper<BusinessModelEntity> wrapper = new QueryWrapper<>();

        //如果是修改，则查询其他记录，是否有同名
        if ( businessMode.getId() != null &&  !businessMode.getId().equals("") )
        {
            wrapper.ne("id", businessMode.getId());
        }
        wrapper.eq("model_name", businessMode.getModelName());
        wrapper.eq("operator_id", businessMode.getOperatorId());
        List<BusinessModelEntity> businessModeList = this.list(wrapper);
        if ( businessModeList != null && businessModeList.size() > 0 )	{
            return AjaxResult.error("已存在同名的业务模型!") ;
        } else {
            return AjaxResult.success() ;
        }

    };

    @Override
    public AjaxResult importModel(ModelDTO modelDTO, MultipartFile file){

        String storageFilePath ="";

        Map<String , Object> dataMap = new HashMap<String , Object>();

        int cellNum = 0 ;// 导入文件的列数
        StringBuffer columnCnName= new StringBuffer();
        StringBuffer columnName= new StringBuffer();


        if ( file == null|| file.getSize() == 0 ){
            log.error("文件上传错误，重新上传");
            return new AjaxResult(415,"文件上传错误，重新上传!");
        }
        String filename = file.getOriginalFilename();
        if ( !( filename.endsWith(".xls") || filename.endsWith(".xlsx") ) ){
            log.error("文件上传格式错误，请重新上传");
            return  new AjaxResult(415,"文件上传格式错误，请重新上传");
        }
        //获取列数、中文字段列名、英文字段列名
        try {
            if ( filename.endsWith(".xls") ){
                InputStream inputStream = file.getInputStream();
                HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
                //读取第一张sheet
                HSSFSheet sheet = workbook.getSheetAt(0);

                if (sheet.getLastRowNum()<7){
                    return  new AjaxResult(415,"文件上传格式错误，请重新上传");
                }

                //第一行为字段中文名称
                HSSFRow firstRow = sheet.getRow(0);
                //第二行为字段英文名称
                HSSFRow secondRow = sheet.getRow(1);
                //第三行 字段含义
                HSSFRow threeRow = sheet.getRow(2);
                //第四行 是否必填
                HSSFRow fourRow = sheet.getRow(3);
                //第五行 数据类型
                HSSFRow fiveRow = sheet.getRow(4);
                //第六行 枚举值
                HSSFRow sixRow = sheet.getRow(5);
                //第七行 数据样例
                HSSFRow sevenRow = sheet.getRow(6);
                if(    !firstRow.getCell(0).getStringCellValue().equals("字段名称")
                        || !secondRow.getCell(0).getStringCellValue().equals("字段英文名称")
                        || !threeRow.getCell(0).getStringCellValue().equals("字段含义")
                        || !fourRow.getCell(0).getStringCellValue().equals("是否必填")
                        || !fiveRow.getCell(0).getStringCellValue().equals("数据类型")
                        || !sixRow.getCell(0).getStringCellValue().equals("枚举值")
                        || !sevenRow.getCell(0).getStringCellValue().equals("数据样例")
                ){
                    return  new AjaxResult(415,"文件上传格式错误，请重新上传");
                }

                //获取列数
                cellNum = firstRow.getPhysicalNumberOfCells();
                //遍历列，获取字段中文名称清单，字段英文名称清单
                for(int i=1;i<cellNum;i++){
                    columnCnName.append(firstRow.getCell(i).getStringCellValue()).append(",");
                    columnName.append(secondRow.getCell(i).getStringCellValue()).append(",");
                }
                columnCnName.deleteCharAt(columnCnName.length() - 1);
                columnName.deleteCharAt(columnName.length() - 1);
                dataMap.put("cellNum",cellNum-1);
                dataMap.put("columnCnName",columnCnName);
                dataMap.put("columnName",columnName);

            }else {
//                InputStream inputStream = file.getInputStream();
//                XSSFWorkbook Workbook = new XSSFWorkbook(inputStream);
//                XSSFSheet sheet = Workbook.getSheetAt(0);

                Workbook wb = WorkbookFactory.create(file.getInputStream());
                Sheet sheet = wb.getSheetAt(0);
                if ( sheet == null ) {
                    return AjaxResult.error("文件不存在!") ;

                }

                if ( sheet.getLastRowNum() < 7 ){
                    return  new AjaxResult(415,"文件上传格式错误，请重新上传");
                }

                //第一行为字段中文名称
                Row firstRow = sheet.getRow(0);
                //第二行为字段英文名称
                Row secondRow = sheet.getRow(1);
                //第三行 字段含义
                Row threeRow = sheet.getRow(2);
                //第四行 是否必填
                Row fourRow = sheet.getRow(3);
                //第五行 数据类型
                Row fiveRow = sheet.getRow(4);
                //第六行 枚举值
                Row sixRow = sheet.getRow(5);
                //第七行 数据样例
                Row sevenRow = sheet.getRow(6);
                if(    !firstRow.getCell(0).getStringCellValue().equals("字段名称")
                        || !secondRow.getCell(0).getStringCellValue().equals("字段英文名称")
                        || !threeRow.getCell(0).getStringCellValue().equals("字段含义")
                        || !fourRow.getCell(0).getStringCellValue().equals("是否必填")
                        || !fiveRow.getCell(0).getStringCellValue().equals("数据类型")
                        || !sixRow.getCell(0).getStringCellValue().equals("枚举值")
                        || !sevenRow.getCell(0).getStringCellValue().equals("数据样例")
                ){
                    return  new AjaxResult(415,"文件上传格式错误，请重新上传");
                }

                //获取列数
                cellNum = firstRow.getPhysicalNumberOfCells();
                //遍历列，获取字段中文名称清单，字段英文名称清单
                for( int i=1; i<cellNum; i++ ){
                    columnCnName.append(firstRow.getCell(i).getStringCellValue()).append(",");
                    columnName.append(secondRow.getCell(i).getStringCellValue()).append(",");
                }
                columnCnName.deleteCharAt(columnCnName.length() - 1);
                columnName.deleteCharAt(columnName.length() - 1);
                dataMap.put("cellNum",cellNum-1);
                dataMap.put("columnCnName",columnCnName);
                dataMap.put("columnName",columnName);

            }
        }catch (IOException e) {
            e.printStackTrace();
            log.error("文件内容读取失败，请重试");
            return  new AjaxResult(500 ,"文件内容读取失败，请重试");
        }


        try {

            storageFilePath =  minioFileUtil.uploadFile(file);
            dataMap.put("storageFilePath",storageFilePath);

        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error();
        }

        if( storageFilePath.equals("the file is empty") || storageFilePath.equals("upload failed")){
            return AjaxResult.error();
        }else {
            BusinessModelEntity businessModel = new BusinessModelEntity();
            businessModel.setId(Long.valueOf(modelDTO.getId()));
            businessModel.setStorageFileId(modelDTO.getFileId());
            businessModel.setStorageFileFullName(modelDTO.getFileName() );
            businessModel.setStorageFileSize(Long.valueOf(modelDTO.getStorageFileSize()));
            businessModel.setStorageFilePath(storageFilePath);
            businessModel.setColumnNum((long) (cellNum - 1));
            businessModel.setColumnName(columnName.toString());
            businessModel.setColumnCnName(columnCnName.toString());
            changeModelInfo(businessModel);
            return new AjaxResult(200,"成功!",dataMap);

        }

    } ;


    public AjaxResult changeModelInfo(BusinessModelEntity businessModel) {
        log.debug("businessModel = {}", businessModel);
        Assert.isTrue(businessModel != null && businessModel.getId() != null, "实体对象为空");
        Assert.hasLength(String.valueOf(businessModel.getId()), "字段为空");
        UpdateWrapper<BusinessModelEntity> updateWrapper = new UpdateWrapper();

        String[] split = businessModel.getStorageFileFullName().split("\\.");
        log.debug("文件名:{},文件名后缀:{}",split[0],split[1]);
        if ( split.length > 1 ) {
            updateWrapper.set("storage_file_name", split[0]);
            updateWrapper.set("storage_file_extend_name", split[1]);

        }

        updateWrapper.set("storage_file_id",businessModel.getStorageFileId());
        updateWrapper.set("storage_file_full_name",businessModel.getStorageFileFullName());
        updateWrapper.set("storage_file_path",businessModel.getStorageFilePath());
        updateWrapper.set("storage_file_size",businessModel.getStorageFileSize());
        updateWrapper.set("column_num",businessModel.getColumnNum());
        updateWrapper.set("column_cn_name",businessModel.getColumnCnName());
        updateWrapper.set("column_name",businessModel.getColumnName());

        updateWrapper.eq("id", businessModel.getId());
        boolean b = this.update(updateWrapper);
//        return b ? this.ok() : this.error();
        return  null ;
    }


}
