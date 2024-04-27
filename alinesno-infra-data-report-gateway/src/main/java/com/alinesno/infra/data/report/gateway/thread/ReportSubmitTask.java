package com.alinesno.infra.data.report.gateway.thread;

import com.alibaba.fastjson.JSONObject;
import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.data.report.dto.ReportInfoDTO;
import com.alinesno.infra.data.report.dto.SubmitCheckFileDTO;
import com.alinesno.infra.data.report.dto.SubmitReportFileDTO;
import com.alinesno.infra.data.report.entity.FileReportEntity;
import com.alinesno.infra.data.report.entity.MessageEntity;
import com.alinesno.infra.data.report.service.IFileReportService;
import com.alinesno.infra.data.report.service.IMessageFailService;
import com.alinesno.infra.data.report.service.IMessageService;
import com.alinesno.infra.data.report.service.KafkaProducerService;
import com.alinesno.infra.data.report.util.MinioFileUtil;
import com.alinesno.infra.data.report.vo.ResponseBean;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jxl.write.WriteException;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


//上报文件任务类
public class ReportSubmitTask  implements Callable<Integer> {

    //日志记录
    private static final Logger log = LoggerFactory.getLogger(ReportSubmitTask.class);

    private ReportInfoDTO reportInfo;
    private AtomicInteger progress;
    private ReportSubmitCallback callback;
    private IFileReportService fileReportService;
    private KafkaProducerService kafkaProducerService;
    private IMessageFailService messageFailService ;
    private IMessageService messageService ;
    private MinioFileUtil minioFileUtil;

    public ReportSubmitTask(ReportInfoDTO reportInfo, IFileReportService fileReportService, KafkaProducerService kafkaProducerService, IMessageFailService messageFailService, IMessageService messageService, MinioFileUtil minioFileUtil, AtomicInteger progress, ReportSubmitCallback callback) {
        this.reportInfo = reportInfo;
        this.fileReportService = fileReportService;
        this.kafkaProducerService = kafkaProducerService;
        this.messageFailService = messageFailService;
        this.messageService = messageService ;
        this.minioFileUtil = minioFileUtil ;
        this.progress = progress;
        this.callback = callback;

    }

    @Override
    public Integer call() throws Exception {
        // 任务开始执行
        progress.set(1);

        try {
            // 具体的操作逻辑
            //xlsx处理方法
            if ( reportInfo.getExtendName().equals("xlsx") ) {
                reportXlsx(reportInfo);

            } //xls处理方法
            else if( reportInfo.getExtendName().equals("xls") )
            {
                reportXls(reportInfo);

            }
        } catch (Exception e) {
            // 任务发生异常
            progress.set(-1);
            callback.onException(e);
            log.error("文件{}过程中出现异常:{}",reportInfo.getFileName(),e.getMessage());
            return -1;
        }

        callback.onComplete();
        return 0;
    }

    //文件格式为.xlsx的上报过程
    AjaxResult reportXlsx(ReportInfoDTO reportInfo)  {
        //progress.set(2);
        log.debug("文件:{}处理进度:{}",reportInfo.getFileName(),progress.get());

        String result = null;
        Boolean sendKafkaSuc = true ;
        int fileAllCellNum = 0;
        boolean checkRequiredResult = true;
        XSSFWorkbook book = null ;
        //检查数据文件时，记录错误信息
        Map erorMap = new HashMap();
        //local_path
        String saveFileName = reportInfo.getLocal_path()+ File.separator+reportInfo.getFileName()+'.'+reportInfo.getExtendName();
        File submissionFile = new File(saveFileName);
        //progress.set(3);
        log.debug("文件:{}处理进度:{}",reportInfo.getFileName(),progress.get());

        try {
            XSSFSheet sheet;
            XSSFRow row;
            XSSFRow secondRow;


            //获取.xlsx文件输入流
            FileInputStream is = new FileInputStream(submissionFile);
            book = new XSSFWorkbook(is);

            //从第一个分页中提取数据
            sheet = book.getSheetAt(0);

            //第一行 字段中文名称
            //第二行 字段英文名称
            secondRow = sheet.getRow(1);

            //总列数
            fileAllCellNum=secondRow.getPhysicalNumberOfCells();

            //检查必填项是否为空
            checkRequiredResult=checkRequiredEntry(sheet,erorMap);
            if ( !checkRequiredResult ) {
                StringBuilder checkMsg = new StringBuilder();
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String newfileName =reportInfo.getFileName()+sdf.format(date);
                String checkFileName=reportInfo.getLocal_path()+ File.separator+newfileName+'.'+reportInfo.getExtendName();

                FileOutputStream out = new FileOutputStream(checkFileName);
                Set set =erorMap.entrySet();
                Iterator it = set.iterator();
                while (it.hasNext())
                {
                    Map.Entry entry = (Map.Entry) it.next();
                    Integer id = (Integer) entry.getKey();
                    String value = (String) entry.getValue();
                    checkMsg.append(value).append(System.getProperty("line.separator"));
                    sheet.getRow(id).createCell(fileAllCellNum).setCellValue(value);
                }
                book.write(out);
                out.flush();
                out.close();
                is.close();
                //删除旧的校验文件

                String oldFile=reportInfo.getLocal_path()+ File.separator+reportInfo.getOldCheckFileName()+'.'+reportInfo.getOldCheckExtendName();
                File oldCheckFile=new File(oldFile);
                if (oldCheckFile.exists()) {
                    oldCheckFile.delete();
                }
                String checkFilePath = minioFileUtil.uploadFile(checkFileName, newfileName+'.'+reportInfo.getExtendName());

                submitCheckFileDTO(new SubmitCheckFileDTO(reportInfo.getId(), newfileName, reportInfo.getExtendName(), checkFilePath, new Date()));
                //0-否, 2-上报异常
                submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 0, 2, new Date(), reportInfo.getMessageType(), checkMsg.toString()));
                result="上报失败!必填项为空!请检查文件并填写!";
                progress.set(-1);
                log.debug("文件:{}处理进度:{}",reportInfo.getFileName(),progress.get());
                return  new AjaxResult(205,result);
            }

            //检查没有问题时，开始往kafka产生数据
            //收集发送kafka时异常信息
            StringBuffer errorMsg=new StringBuffer();

            //progress.set(4);
            log.debug("文件:{}处理进度:{}",reportInfo.getFileName(),progress.get());
            ArrayList<MessageEntity> messageList = new ArrayList<>();
            //从第八行第二列开始为上报数据区域
            for( int i = 7; i <= sheet.getLastRowNum(); i++ ) {
                row = sheet.getRow(i);


                /*获取处理进度时，根据当前行数与总行数的比例，设置进度值
                int curProgress = (int) Math.round( (float)i / sheet.getLastRowNum() * 100 ) - 1 ;
                if ( 4  <  curProgress  ) {
                    progress.set( curProgress );
                    log.debug("文件:{}处理进度:{}",reportInfo.getFileName(),progress.get());
                }
                */

                //测试获取进度
                //Thread.sleep(5000);

                if(row != null) {
                    JSONObject json = new JSONObject();
                    //获得当前行的开始列
                    int firstCellNum = row.getFirstCellNum();
                    //每行从第二列开始
                    firstCellNum=firstCellNum+1;

                    for (int cellNum=firstCellNum;cellNum < fileAllCellNum;cellNum++) {
                        json.put(secondRow.getCell(cellNum).getStringCellValue(), getCellValue(row.getCell(cellNum)));
                        if (cellNum == fileAllCellNum - 1) {
                            //发往数据总线
                            if (reportInfo.getMessageType().equals("0") ) {
                                //向kafka指定的主题发json消息
                                ResponseBean responseBean = kafkaProducerService.sendMessage(reportInfo.getKafkaTopic(), json.toJSONString());

                                if ( responseBean.getCode() != 200 ) {
                                    sendKafkaSuc = false;
                                    errorMsg.append("第"+i+"行发送异常!"+responseBean.getMessage()) ;
                                    //写入异常信息表
                                    messageFailService.addMessageFail(reportInfo, json.toJSONString(),responseBean.getMessage());
                                }
                                else {
                                    MessageEntity tmpMessage = new MessageEntity();
                                    tmpMessage.setModelId(reportInfo.getModelId());          //业务模型ID
                                    tmpMessage.setFileId(reportInfo.getId());                //上报文件ID
                                    tmpMessage.setTopice(reportInfo.getKafkaTopic());        //消息主题
                                    tmpMessage.setFileIndex( i -7 + 1 );                     //上报序号
                                    tmpMessage.setData(json.toJSONString());                 //上报内容
                                    tmpMessage.setReportStatus(1);                           //上报状态
                                    tmpMessage.setMessageType(0);                            //目标库
                                    tmpMessage.setOperatorId(reportInfo.getOperatorId());    //操作员
                                    tmpMessage.setIfSendBus(1);                              //发往数据总线
                                    tmpMessage.setAddTime(new Date());                       //新增时间
                                    messageList.add(tmpMessage);

                                }
                            }else if (reportInfo.getMessageType().equals("1")){
                                MessageEntity tmpMessage = new MessageEntity();
                                tmpMessage.setModelId(reportInfo.getModelId());        //业务模型ID
                                tmpMessage.setFileId(reportInfo.getId());              //上报文件ID
                                tmpMessage.setTopice(reportInfo.getKafkaTopic());      //消息主题
                                tmpMessage.setFileIndex( i -7 + 1 );                   //上报序号
                                tmpMessage.setData(json.toJSONString());               //上报内容
                                tmpMessage.setReportStatus(1);                         //上报状态
                                tmpMessage.setMessageType(1);                          //目标库
                                tmpMessage.setOperatorId(reportInfo.getOperatorId());  //操作员
                                tmpMessage.setIfSendBus(0);                            //发往数据总线
                                tmpMessage.setAddTime(new Date());                     //新增时间
                                messageList.add(tmpMessage);

                            }
                        }
                    }
                }
            }

            //将上报内容入库
            messageService.saveBatch(messageList) ;

            book.close();

            if ( sendKafkaSuc ) {
                result = submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 1, 1, new Date(), reportInfo.getMessageType(), null));
                return AjaxResult.success(result);
            } else  {
                if (reportInfo.getMessageType().equals("0") ) {
                    result = "发送数据到消息总线失败!" +errorMsg.toString();
                } else if (reportInfo.getMessageType().equals("1") ) {
                    result = "发送数据到数据上报失败!" ;
                }
                submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 1, 2, new Date(), reportInfo.getMessageType(), result));
                return AjaxResult.error(500,result);

            }



        }
        catch ( FileNotFoundException e ) {
            log.error("找不到文件!"+e.getMessage() );
            submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 2, 2, new Date(), reportInfo.getMessageType(), "找不到文件!"+e.getMessage()));
            return AjaxResult.error(500,"找不到文件!"+e.getMessage() );
        } catch ( IOException e) {
            log.error("数组越界!检测到有"+fileAllCellNum+"列!"+e.getMessage());
            submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 2, 2, new Date(), reportInfo.getMessageType(), "数组越界!检测到有"+fileAllCellNum+"列!"+e.getMessage()));
            return AjaxResult.error(500,"数组越界!检测到有"+fileAllCellNum+"列!"+e.getMessage());
        }


    }

    //文件格式为.xls的上报过程
    AjaxResult reportXls(ReportInfoDTO reportInfo)  {
        //progress.set(2);
        String result = null ;
        Boolean sendKafkaSuc = true ;
        int fileAllCellNum = 0;
        boolean checkRequiredResult = true;
        //检查数据文件时，记录错误信息
        Map  erorMap = new HashMap();

        //获取.xls文件输入流
        String saveFileName = reportInfo.getLocal_path() + File.separator+reportInfo.getFileName() + '.' + reportInfo.getExtendName();
        StringBuffer tableValues = new StringBuffer();

        try {
            FileInputStream is = new FileInputStream(saveFileName);
            HSSFWorkbook workbook = new HSSFWorkbook(is);
            //从文件里的第一个分页中提取数据
            HSSFSheet sheet = workbook.getSheetAt(0);
            //第一行为字段名称
            //第二行为字段英文名称,取出第二行的所有列
            HSSFRow secondRow = sheet.getRow(1);

            //获取总列数
            fileAllCellNum = secondRow.getPhysicalNumberOfCells();

            //检查必填项是否为空
            checkRequiredResult = checkRequiredEntryXls( workbook,erorMap);

            //如果必填项没有值，则输出校验文件
            if ( !checkRequiredResult ) {

                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String newfileName=reportInfo.getFileName()+sdf.format(date);
                String checkFileName=reportInfo.getLocal_path()+ File.separator+newfileName+'.'+reportInfo.getExtendName();
                StringBuilder checkMsg = new StringBuilder();

                FileOutputStream out = new FileOutputStream(checkFileName);
                Set set =erorMap.entrySet();
                Iterator it = set.iterator();
                while ( it.hasNext() )
                {
                    Map.Entry entry = (Map.Entry) it.next();
                    Integer id = (Integer) entry.getKey();
                    String value = (String) entry.getValue();
                    checkMsg.append(value).append(System.getProperty("line.separator"));
                    sheet.getRow(id).createCell(fileAllCellNum).setCellValue(value);
                }
                workbook.write(out);
                out.flush();
                out.close();
                is.close();

                //删除旧的校验文件
                String oldFile=reportInfo.getLocal_path()+ File.separator+reportInfo.getOldCheckFileName()+'.'+reportInfo.getOldCheckExtendName();
                File oldCheckFile = new File(oldFile);
                if ( oldCheckFile.exists() ) {
                    oldCheckFile.delete();
                }

                File CheckFile = new File(checkFileName);
                if ( CheckFile.exists() ) {
                    CheckFile.delete();
                }

                submitCheckFileDTO(new SubmitCheckFileDTO(reportInfo.getId(),newfileName,reportInfo.getExtendName(),new Date()));
                submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 0, 2, new Date(), reportInfo.getMessageType(), checkMsg.toString()));
                return  new AjaxResult(205,"上报失败!必填项为空!请检查文件并填写!");
            }


            //如果通过必填项检查，则开始读取数据并发送kafka
            //收集发送kafka时异常信息
            StringBuffer errorMsg=new StringBuffer();

            ArrayList<MessageEntity> messageList = new ArrayList<>();
            //从第八行第二列开始为上报数据区域
            for( int i = 7; i <= sheet.getLastRowNum(); i++ ) {
                HSSFRow row= sheet.getRow(i) ;
                if(row != null) {
                    JSONObject json = new JSONObject();

                    //每行从第二列开始
                    int firstCellNum=1;

                    for ( int cellNum=firstCellNum; cellNum < fileAllCellNum; cellNum++ ){
                        json.put(secondRow.getCell(cellNum).toString(),row.getCell(cellNum).toString());
                        if ( cellNum == fileAllCellNum-1 ) {

                            //发往数据总线
                            if ( reportInfo.getMessageType().equals("0") ) {

                                //向kafka指定的主题发json消息
                                ResponseBean responseBean = kafkaProducerService.sendMessage( reportInfo.getKafkaTopic(), json.toJSONString() );
                                //累加json消息,用于调试及后续的保存
//                                jsons.add(json);

                                if ( responseBean.getCode() != 200 ) {
                                    sendKafkaSuc = false;
                                    errorMsg.append("第"+i+"行发送异常!"+responseBean.getMessage()) ;
                                    //写入异常信息表
                                    messageFailService.addMessageFail( reportInfo, json.toJSONString(), responseBean.getMessage() );
                                }else {
                                    MessageEntity tmpMessage = new MessageEntity();
                                    tmpMessage.setModelId(reportInfo.getModelId());     //业务模型ID
                                    tmpMessage.setFileId(reportInfo.getId());           //上报文件ID
                                    tmpMessage.setTopice(reportInfo.getKafkaTopic());   //消息主题
                                    tmpMessage.setFileIndex( i -7 + 1 );                //上报序号
                                    tmpMessage.setData(json.toJSONString());            //上报内容
                                    tmpMessage.setReportStatus(1);                      //上报状态
                                    tmpMessage.setMessageType(0);                       //目标库
                                    tmpMessage.setOperatorId(reportInfo.getOperatorId());  //操作员
                                    tmpMessage.setIfSendBus(1);                            //发往数据总线
                                    tmpMessage.setAddTime(new Date());                     //新增时间
                                    messageList.add(tmpMessage);

                                }
                            }else if ( reportInfo.getMessageType().equals("1") ){
                                MessageEntity tmpMessage = new MessageEntity();
                                tmpMessage.setModelId(reportInfo.getModelId());     //业务模型ID
                                tmpMessage.setFileId(reportInfo.getId());           //上报文件ID
                                tmpMessage.setTopice(reportInfo.getKafkaTopic());   //消息主题
                                tmpMessage.setFileIndex( i -7 + 1 );                //上报序号
                                tmpMessage.setData(json.toJSONString());            //上报内容
                                tmpMessage.setReportStatus(1);                      //上报状态
                                tmpMessage.setMessageType(1);                       //目标库
                                tmpMessage.setOperatorId(reportInfo.getOperatorId()); //操作员
                                tmpMessage.setIfSendBus(0);                            //发往数据总线
                                tmpMessage.setAddTime(new Date());                     //新增时间
                                messageList.add(tmpMessage);

                            }

                        }
                    }
                }
            }

            workbook.close();
            //将上报内容入库
            messageService.saveBatch(messageList) ;

            if ( sendKafkaSuc ) {
                result = submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 1, 1,new Date(),reportInfo.getMessageType(),null));
                return AjaxResult.success(result);
            } else {
                if (reportInfo.getMessageType().equals("0") ) {
                    result = "发送数据到消息总线失败!" +errorMsg.toString();
                } else if (reportInfo.getMessageType().equals("1") ) {
                    result = "发送数据到数据上报失败!" ;
                }
                submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 1, 2, new Date(), reportInfo.getMessageType(), result));
                return AjaxResult.error(500,result);
            }
        } catch (IOException e) {
            log.error("数组越界!检测到有"+fileAllCellNum+"列!"+e.getMessage());
            submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 1, 2, new Date(), reportInfo.getMessageType(), "数组越界!检测到有"+fileAllCellNum+"列!"+e.getMessage()));
            return AjaxResult.error(500,"数组越界!检测到有"+fileAllCellNum+"列!"+e.getMessage());
        } catch ( WriteException e) {
            log.error("写失败!"+e.getMessage());
            submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 1, 2, new Date(), reportInfo.getMessageType(), "写失败!"+e.getMessage()));
            return AjaxResult.error(500,"写失败!"+e.getMessage());
        }


    }

    //提交文件上报的信息
    public String submitReportFile(SubmitReportFileDTO submitReportFileDTO) {
        String result ="";
        LambdaUpdateWrapper<FileReportEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(FileReportEntity::getReportStatus, submitReportFileDTO.getReportStatus())
                .set(FileReportEntity::getReportTime, submitReportFileDTO.getReportTime())
                .set(FileReportEntity::getErrorMsg, submitReportFileDTO.getErrorMsg())
                .set(FileReportEntity::getIfCheck, submitReportFileDTO.getIfCheck())
                .eq(FileReportEntity::getId, submitReportFileDTO.getUserFileId());
        fileReportService.update(lambdaUpdateWrapper);

        if ( submitReportFileDTO.getReportStatus() == 1 ) {
            if (submitReportFileDTO.getMessageType().equals("0")) {
                result = "已成功发送消息到kafka主题并已设置上报状态!";
            } else if ( submitReportFileDTO.getMessageType().equals("1") ) {
                result = "已成功发送消息到mysql数据表并已设置上报状态!";
            }
        } else{
            result = "上报异常!请联系管理员处理异常信息!";
        }

        return result;
    }

    //获取上报文件的单元格的信息
    public static String getCellValue(XSSFCell cell){
        String cellValue = "";
        if( cell == null ){
            return cellValue;
        }
        //把数字当成String来读，避免出现1读成1.0的情况
        if( cell.getCellType() == CellType.NUMERIC ){
            cell.setCellType(CellType.STRING);
        }
        //判断数据的类型
        switch (cell.getCellType()){
            case NUMERIC: //数字
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case STRING: //字符串
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case BOOLEAN: //Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA: //公式
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case BLANK: //空值
                cellValue = "";
                break;
            case ERROR: //故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }

    //检查上报文件的必填项是否有值
    public boolean checkRequiredEntry(XSSFSheet sheet,Map erorMap){
        boolean result =true ;

        //表格第四行为必填项
        XSSFRow fourRow =sheet.getRow(3) ;

        //获取总列数
        int fileAllCellNum  =fourRow.getPhysicalNumberOfCells();

        XSSFRow row;

        //从第八行第二列开始为上报数据区域,对单元格进行遍历检查数据
        for( int i = 7; i <= sheet.getLastRowNum(); i++ ) {

            row = sheet.getRow(i);
            if( row != null ) {
                //每行从第二列开始
                int firstCellNum = 2;
                StringBuffer cellTestResult= new StringBuffer();
                for (int cellNum=firstCellNum;cellNum < fileAllCellNum;cellNum++){

                    //第四行中某一列为必填时，遍历每行的这一列是否有值，如没有值，则异常
                    if ( fourRow.getCell(cellNum).getStringCellValue().equals("必填") ){

                        String cellValue = "";

                        //把数字当成String来读，避免出现1读成1.0的情况
                        if( row.getCell(cellNum).getCellType()  == CellType.NUMERIC ){
                            row.getCell(cellNum).setCellType(CellType.STRING);
                        }

                        //判断数据的类型
                        switch ( row.getCell(cellNum).getCellType() ){
                            case NUMERIC: //数字
                                break;
                            case STRING: //字符串
                                break;
                            case BOOLEAN: //Boolean
                                break;
                            case FORMULA: //公式
                                cellValue = String.valueOf(row.getCell(cellNum).getCellFormula());
                                if ( cellValue.equals("null") || cellValue=="" )
                                {
                                    cellTestResult.append("第"+cellNum+"列，公式值为空，请检查公式是否正确!");
                                    result =false ;
                                }
                                break;
                            case BLANK: //空值
                                cellTestResult.append("第"+cellNum+"列，必填项不能为空!");
                                result =false ;
                                break;
                            case ERROR: //故障
                                cellTestResult.append("第"+cellNum+"列，含有非法字符");
                                result =false ;
                                break;
                            default:
                                cellTestResult.append("未知类型");
                                result =false ;
                                break;
                        }
                    }
                }
                if (cellTestResult.toString().length()>0){
                    erorMap.put(i,cellTestResult.toString());
                };
            }
        }

        return result;
    }

    //检查上报文件的必填项是否有值
    public boolean checkRequiredEntryXls( HSSFWorkbook workbook,Map erorMap) throws WriteException {
        boolean result =true ;

        //从文件里的第一个分页中提取数据
        HSSFSheet sheet = workbook.getSheetAt(0);

        //第四行标识字段为必填项
        HSSFRow fourRow= sheet.getRow(3);

        //获取总列数
        int fileAllCellNum =fourRow.getLastCellNum();

        //从第八行第二列开始为上报数据区域
        for( int i = 7; i <= sheet.getLastRowNum(); i++ ) {

            //获取当前行的所有列
            HSSFRow row = sheet.getRow(i);

            if( row != null ) {
                //每行从第二列开始
                int firstCellNum = 1 ;
                StringBuffer cellTestResult= new StringBuffer();
                for ( int cellNum=firstCellNum; cellNum < fileAllCellNum; cellNum++ )
                {
                    //第四行中某一列为必填时，遍历每行的这一列是否有值，如没有值，则异常
                    if ( fourRow.getCell(cellNum).toString().equals("必填") )
                    {

                        String cellValue = "";

                        //判断数据的类型
                        switch ( row.getCell(cellNum).getCellType()){
                            case NUMERIC: //数字
                                break;
                            case STRING: //字符串
                                break;
                            case BOOLEAN: //Boolean
                                break;
                            case FORMULA: //公式
                                cellValue = String.valueOf(row.getCell(cellNum).getCellFormula());
                                if (cellValue.equals("null") || cellValue=="")
                                {
                                    cellTestResult.append("第"+cellNum+"列，公式值为空，请检查公式是否正确!");
                                    result =false ;
                                }
                                break;
                            case BLANK: //空值
                                cellTestResult.append("第"+cellNum+"列，必填项不能为空!");
                                result =false ;
                                break;
                            case ERROR: //故障
                                cellTestResult.append("第"+cellNum+"列，含有非法字符");
                                result =false ;
                                break;
                            default:
                                cellTestResult.append("未知类型");
                                result =false ;
                                break;
                        }
                    }
                }
                if ( cellTestResult.toString().length() > 0 ){
                    erorMap.put(i,cellTestResult.toString());
                };
            }
        }
        return result;
    }

    //提交文件上报的校验信息
    public String submitCheckFileDTO(SubmitCheckFileDTO submitCheckFileDTO) {
        LambdaUpdateWrapper<FileReportEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(FileReportEntity::getCheckFileName, submitCheckFileDTO.getCheckFileName())
                .set(FileReportEntity::getCheckFileExtendName, submitCheckFileDTO.getCheckExtendName())
                .set(FileReportEntity::getCheckFilePath, submitCheckFileDTO.getCheckFileFath())
                .set(FileReportEntity::getIfCheck, 0)
                .set(FileReportEntity::getReportTime, submitCheckFileDTO.getReportTime())
                .set(FileReportEntity::getReportStatus, 0)
                .eq(FileReportEntity::getId, submitCheckFileDTO.getUserFileId());
        fileReportService.update(lambdaUpdateWrapper);
        return "已成功设置用户文件的检查文件名称";
    }

    public AtomicInteger getProgress() {
        return progress;
    }

    public void setProgress(AtomicInteger progress) {
        this.progress = progress;
    }
}
