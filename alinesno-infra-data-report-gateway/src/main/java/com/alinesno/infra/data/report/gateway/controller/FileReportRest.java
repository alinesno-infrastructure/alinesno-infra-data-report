package com.alinesno.infra.data.report.gateway.controller;

import cn.hutool.core.date.DateTime;
import com.alinesno.infra.common.core.constants.SpringInstanceScope;
import com.alinesno.infra.common.facade.pageable.DatatablesPageBean;
import com.alinesno.infra.common.facade.pageable.TableDataInfo;
import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.facade.response.ResultCodeEnum;
import com.alinesno.infra.common.web.adapter.plugins.TranslateCode;
import com.alinesno.infra.common.web.adapter.rest.BaseController;
import com.alinesno.infra.data.report.config.MinioConfig;
import com.alinesno.infra.data.report.dto.ReportInfoDTO;
import com.alinesno.infra.data.report.dto.SubmitCheckFileDTO;
import com.alinesno.infra.data.report.dto.SubmitReportFileDTO;
import com.alinesno.infra.data.report.entity.FileReportEntity;
import com.alinesno.infra.data.report.gateway.thread.ReportSubmitCallback;
import com.alinesno.infra.data.report.gateway.thread.ReportSubmitTask;
import com.alinesno.infra.data.report.gateway.thread.ReportSubmitTaskManager;
import com.alinesno.infra.data.report.gateway.thread.ReportSubmitThreadPool;
import com.alinesno.infra.data.report.service.IFileReportService;
import com.alinesno.infra.data.report.service.IMessageFailService;
import com.alinesno.infra.data.report.service.IMessageService;
import com.alinesno.infra.data.report.service.KafkaProducerService;
import com.alinesno.infra.data.report.util.MinioFileUtil;
import com.alinesno.infra.data.report.vo.ResponseBean;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import com.alinesno.infra.data.report.dto.ReportModelDTO;

/**
 * 【请填写功能名称】Rest
 *
 * @author paul
 * @date 2022-11-28 10:28:04
 */
@RestController
@Scope(SpringInstanceScope.PROTOTYPE)
@RequestMapping("/api/infra/data/report/FileReport")
public class FileReportRest extends BaseController<FileReportEntity, IFileReportService> {

    //日志记录
    private static final Logger log = LoggerFactory.getLogger(FileReportRest.class);

    @Autowired
    private IFileReportService fileReportService;

    @Autowired
    private MinioFileUtil minioFileUtil;

    @Value("${minio.local-storage-path}")
    private String local_path ;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private IMessageFailService messageFailService ;

    @Autowired
    private IMessageService messageService ;

    @Autowired
    private ReportSubmitThreadPool threadPool;

    @Autowired
    private ReportSubmitTaskManager taskManager;

    /**
     * 获取上报文件表格数据，与上回收站表格条件不同之处是has_delete=0
     * @param model
     * @param page
     */
//    @DataFilter
    @TranslateCode(plugin = "FileReportPlugin")
    @ResponseBody
//    @RequiresUser
    @PostMapping("/datatables")
    public TableDataInfo datatables(HttpServletRequest request, Model model, DatatablesPageBean page) {
        log.debug("page = {}", ToStringBuilder.reflectionToString(page));
        return this.toPage(model, this.getFeign() , page) ;
    }

    @Override
    public IFileReportService getFeign() {
        return this.fileReportService;
    }

    /**
     * 获取回收站表格数据，与上报文件表格条件不同之处是has_delete=1
     * @param model
     * @param page
     */
    @TranslateCode(plugin = "FileReportPlugin")
    @ResponseBody
//    @RequiresUser
    @PostMapping("/datatablesDel")
    public TableDataInfo datatablesDel(HttpServletRequest request, Model model, DatatablesPageBean page) {
        log.debug("page = {}", ToStringBuilder.reflectionToString(page));

        String operatorId = null ;
        // 设置用户
//        ManagerAccountEntity account = CurrentAccountJwt.get() ; // CurrentAccountSession.get(request);
//        if (account != null) {
//            operatorId = account.getId();
//
//        }

        String fileFullName =request.getParameter("condition[fileFullName|like]");
        Long  pageNum =Long.valueOf(request.getParameter("pageNum")) ;
        Long  pageSize =Long.valueOf(request.getParameter("pageSize"));
        IPage<FileReportEntity> fileList = fileReportService.recycleFileList(operatorId, pageNum, pageSize, fileFullName);
        TableDataInfo dInfo = new TableDataInfo();
        dInfo.setCode(200);
        dInfo.setMsg("查询成功");
        dInfo.setRows(fileList.getRecords());
        dInfo.setTotal((long)((int)fileList.getTotal()));
        return dInfo;
    }

    /**
     * 删除上报文件时，先进入回收站。还原文件，将文件中从回收站移到上报文件
     * @param fileReportEntity
     */
    @ResponseBody
    @PutMapping("/reduction")
    public TableDataInfo reductionFileReport(HttpServletRequest request,@RequestBody FileReportEntity fileReportEntity) {
        TableDataInfo dInfo = new TableDataInfo();

         try {
             fileReportService.reductionFileReport(String.valueOf(fileReportEntity.getId()));
             dInfo.setCode(200);
             dInfo.setMsg("还原成功");

         }
        catch (Exception e){
            log.info(e.getMessage());
            dInfo.setCode(500);
            dInfo.setMsg("服务器异常");
        }
        return dInfo;
    }

    /**
     * 删除回收站中的数据
     * @param request
     * @param request  ids 要删除的文件ids
     */
    @ResponseBody
    @GetMapping("/deleteFileReport")
    public TableDataInfo deleteFileReport(HttpServletRequest request, String ids) {
        List<String> idList = Arrays.asList( ids.split(",") );


        QueryWrapper<FileReportEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(FileReportEntity::getId,idList);


        List<FileReportEntity> fileReporList = this.getFeign().findByReportIds(queryWrapper);
        TableDataInfo dInfo = new TableDataInfo();
        for (FileReportEntity fileReportEntity : fileReporList) {
            try {
                fileReportService.deleteFileReport(String.valueOf(fileReportEntity.getId()));       //删除文件
                deleteMinioOject(fileReportEntity.getStorageFilePath());               //删除文件的附件

            }catch (Exception e){
                log.info(e.getMessage());
                dInfo.setCode(500);
                dInfo.setMsg("服务器异常");
            }

        }

        dInfo.setCode(200);
        dInfo.setMsg("删除成功");
        return dInfo;
    }

    /**
     * 上传上报文件，检查上报文件的格式与业务模板是否一致，如不一致，将进行提示
     * @param reportModelDTO
     * @param file
     */
    @RequestMapping(value = "/uploadfile", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult uploadFile(HttpServletRequest request , ReportModelDTO reportModelDTO, MultipartFile file) throws Exception {
        String storageFilePath ="";

        Map<String , Object> dataMap = new HashMap<String , Object>();

        int cellNum =0 ;// 导入文件的列数
        StringBuffer columnCnName= new StringBuffer();
        StringBuffer columnName= new StringBuffer();


        if ( file==null || file.getSize() == 0 ){
            log.error("文件上传错误，重新上传");
            return new AjaxResult(415,"文件上传错误，重新上传!");
        }
        String filename = file.getOriginalFilename();
        if (!(filename.endsWith(".xls")|| filename.endsWith(".xlsx"))){
            log.error("文件上传格式错误，请重新上传");
            return  new AjaxResult(415,"文件上传格式错误，请重新上传");
        }
        //获取列数、中文字段列名、英文字段列名
        try {
            if (filename.endsWith(".xls")){
                InputStream inputStream = file.getInputStream();
                HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
                //读取第一张sheet
                HSSFSheet sheet = workbook.getSheetAt(0);

                //检查文件

                if (sheet.getLastRowNum()<7){
                    return  new AjaxResult(415,"上传文件格式错误，请重新上传");
                }



                //第一行为字段中文名称
                HSSFRow firstRow = sheet.getRow(0);

                //判断上传文件的列数是否与导入模板的列数是否相等
                if(firstRow.getPhysicalNumberOfCells()-1 != reportModelDTO.getColumnNum()){
                    return  new AjaxResult(415,"上传文件列数与导入模板不一致!请检查");
                }
                //第二行为字段英文名称
                HSSFRow secondRow = sheet.getRow(1);
                //第三行 字段含义
                HSSFRow threeRow = sheet.getRow(2);
                //第四行 是否必填
                HSSFRow fourRow = sheet.getRow(3);
                //第五行 是否必填
                HSSFRow fiveRow = sheet.getRow(4);
                //第六行 数据类型
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
                cellNum=firstRow.getPhysicalNumberOfCells();
                //遍历列，获取字段中文名称清单，字段英文名称清单
                for(int i=1;i<cellNum;i++){
                    columnCnName.append(firstRow.getCell(i).getStringCellValue()).append(",");
                    columnName.append(secondRow.getCell(i).getStringCellValue()).append(",");
                }
                columnCnName.deleteCharAt(columnCnName.length() - 1);
                columnName.deleteCharAt(columnName.length() - 1);
                if ( !reportModelDTO.getColumnCnName().equals(columnCnName.toString()) || !reportModelDTO.getColumnName().equals(columnName.toString()) ) {
                    return  new AjaxResult(415,"上传文件列名与导入模板不一致!请检查");
                }
                dataMap.put("cellNum",cellNum-1);
                dataMap.put("columnCnName",columnCnName);
                dataMap.put("columnName",columnName);
                dataMap.put("reportRow",sheet.getPhysicalNumberOfRows()-7);

            }else {
                InputStream inputStream = file.getInputStream();
                XSSFWorkbook Workbook = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = Workbook.getSheetAt(0);

                if ( sheet.getLastRowNum() < 7 ){
                    return  new AjaxResult(415,"文件上传格式错误，请重新上传");
                }

                //第一行为字段中文名称
                XSSFRow firstRow = sheet.getRow(0);

                //判断上传文件的列数是否与导入模板的列数是否相等
                if( firstRow.getPhysicalNumberOfCells() -1 != reportModelDTO.getColumnNum() ){
                    return  new AjaxResult(415,"上传文件列数与导入模板不一致!请检查");
                }

                //第二行为字段英文名称
                XSSFRow secondRow = sheet.getRow(1);
                //第三行 字段含义
                XSSFRow threeRow = sheet.getRow(2);
                //第四行 是否必填
                XSSFRow fourRow = sheet.getRow(3);
                //第五行 是否必填
                XSSFRow fiveRow = sheet.getRow(4);
                //第六行 数据类型
                XSSFRow sixRow = sheet.getRow(5);
                //第七行 数据样例
                XSSFRow sevenRow = sheet.getRow(6);
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
                cellNum=firstRow.getPhysicalNumberOfCells();
                //遍历列，获取字段中文名称清单，字段英文名称清单
                for(int i=1;i<cellNum;i++){
                    columnCnName.append(firstRow.getCell(i).getStringCellValue()).append(",");
                    columnName.append(secondRow.getCell(i).getStringCellValue()).append(",");
                }
                columnCnName.deleteCharAt(columnCnName.length() - 1);
                columnName.deleteCharAt(columnName.length() - 1);
                if ( !reportModelDTO.getColumnCnName().equals(columnCnName.toString()) || !reportModelDTO.getColumnName().equals(columnName.toString()) ) {
                    return  new AjaxResult(415,"上传文件列名与导入模板不一致!请检查");
                }
                dataMap.put("cellNum",cellNum-1);
                dataMap.put("columnCnName",columnCnName);
                dataMap.put("columnName",columnName);
                dataMap.put("reportRow",sheet.getPhysicalNumberOfRows()-7);

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
            return AjaxResult.error(e.getMessage());
        }

       if ( storageFilePath.startsWith("/") ) {
           return new AjaxResult(200,"成功!",dataMap);
       } else {
           return AjaxResult.error(storageFilePath);

       }

    }

    /**
     * 在服务器上创建文件目录，在上报时，从minIO上下载文件到服务器
     *
     * @param path
     */
    public static void CreatLocalDir(String path)  {
        try {
            File file = new File(path);
            if(file.getParentFile().isDirectory()){//判断上级目录是否是目录
                if(!file.exists()){   //如果文件目录不存在
                    file.mkdirs();    //创建文件目录
                }
            }else{
                throw new Exception("传入非法的目录信息!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过传入的文件名、文件后缀、文件路径,从minIO上下载文件到服务器
     * @param fileName
     * @param extendName
     * @param fileUrl
     */
    @RequestMapping(value = "/getMinioOject", method = RequestMethod.GET)
    public ResponseBean getMinioOject( String fileName,String extendName,String fileUrl){

        ResponseBean result = new ResponseBean() ;

        ResponseBean checkMinioResult = minioFileUtil.checkMinioStatus();
        if ( checkMinioResult.getCode() == 400 ) {
            return checkMinioResult ;
        }

        CreatLocalDir(local_path);
        String saveFileName=local_path+ File.separator+fileName+'.'+extendName;
        File submissionFile=new File(saveFileName);

        if (submissionFile.exists()) {
            log.debug( "文件" + saveFileName+ "已存在。不需要重新下载!");
            result.setCode(ResultCodeEnum.SUCCESS);
            result.setMessage("文件" + saveFileName+ "已存在。不需要重新下载!") ;
            return  result ;

        }

        MinioClient minioClient = MinioClient.builder()
            .endpoint(minioConfig.getUrl())
            .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
            .build();

        try {
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileUrl)        // 格式为"/2022/12/06/2e69a530-60c2-453c-8b09-a8c0dfdc4e94.xlsx"
                    .filename(saveFileName) // 将文件保存到本地存储路径，用于检测文件及解析文件
                    .build());
            result.setCode(ResultCodeEnum.SUCCESS);
            result.setMessage("文件" + saveFileName+ "已成功下载到服务器!") ;


        } catch ( ErrorResponseException e ) {
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage(e.getMessage()) ;
            e.printStackTrace();
        } catch ( InsufficientDataException e ) {
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage(e.getMessage()) ;
            e.printStackTrace();
        } catch ( InternalException e ) {
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage(e.getMessage()) ;
            e.printStackTrace();
        } catch ( InvalidKeyException e ) {
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage(e.getMessage()) ;
            e.printStackTrace();
        } catch ( InvalidResponseException e ) {
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage(e.getMessage()) ;
            e.printStackTrace();
        } catch ( IOException e ) {
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage(e.getMessage()) ;
            e.printStackTrace();
        } catch ( NoSuchAlgorithmException e ) {
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage(e.getMessage()) ;
            e.printStackTrace();
        } catch ( ServerException e ) {
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage(e.getMessage()) ;
            e.printStackTrace();
        } catch ( XmlParserException e ) {
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage(e.getMessage()) ;
            e.printStackTrace();
        }

        return result ;
    }


    /**
     * 上报文件时，从本地存储中读取excel并解析成json，发送到kafka。如检查必填项等信息时，出现空值，则弹出下载检查结果窗
     * @param request
     * @param response
     * @param reportInfo
     */
    @PostMapping("/readExcel")
    public AjaxResult readExcel(HttpServletRequest request, HttpServletResponse response, @RequestBody ReportInfoDTO reportInfo) throws IOException {

        ResponseBean checkMinioStatus = minioFileUtil.checkMinioStatus();
        if ( checkMinioStatus.getCode() == 400 ) {
            return  AjaxResult.error(checkMinioStatus.getMessage()) ;

        }

        //2-刚上报,未校验, 3-发送中
        submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 2, 3, new DateTime(), reportInfo.getMessageType(), null));
        AjaxResult result = null;

        ResponseBean getResult = getMinioOject(reportInfo.getFileName(), reportInfo.getExtendName(), reportInfo.getFileUrl()) ;

        //检查minIO是否在正常工作
        if ( getResult.getCode() == 400 ){
            log.error(getResult.getMessage());
            //2-刚上报,未校验, 2-上报异常
            submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 2, 2, new DateTime(), reportInfo.getMessageType(), getResult.getMessage()));
            return AjaxResult.error(getResult.getMessage());
        }

        //检查kafka是否在正常工作
        ResponseBean responseBean = kafkaProducerService.checkWorkingProperly();
        if ( responseBean.getCode() != 200 ) {
            log.error("连接kafka失败!异常信息为:{}",responseBean.getMessage());
            //2-刚上报,未校验, 2-上报异常
            submitReportFile(new SubmitReportFileDTO(reportInfo.getId(), 2, 2, new DateTime(), reportInfo.getMessageType(), responseBean.getMessage()));
            return AjaxResult.error(500,responseBean.getMessage());
        }

        // 获取操作用户ID
//        ManagerAccountEntity account = CurrentAccountJwt.get() ; // CurrentAccountSession.get(request);
//        if ( account != null ) {
//            reportInfo.setOperatorId(account.getId());
//        }

        log.debug("minIO存放路径：{}",minioConfig.getLocalPath());
        reportInfo.setLocal_path(minioConfig.getLocalPath());

        result = submitReport(reportInfo);

        return result;
    }



    /**
     * 已成功发送消息到kafka主题|数据表并已设置上报状态, description = "设置上报状态"
     * @param submitReportFileDTO
     */
    @RequestMapping(value = "/submitReportFile", method = RequestMethod.POST)
    @ResponseBody
    public String submitReportFile(@RequestBody SubmitReportFileDTO submitReportFileDTO) {
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
            } else if (submitReportFileDTO.getMessageType().equals("1")) {
                result = "已成功发送消息到mysql数据表并已设置上报状态!";
            }
        } else{
            result = "上报异常!请联系管理员处理异常信息!";
        }

        return result;
    }


    /**
     * 设置上报文件的校验文件名称", description = "设置校验文件名称"
     * @param submitCheckFileDTO
     */
    @RequestMapping(value = "/submitCheckFileDTO", method = RequestMethod.POST)
    @ResponseBody
    public String submitCheckFileDTO(@RequestBody SubmitCheckFileDTO submitCheckFileDTO) {

        LambdaUpdateWrapper<FileReportEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(FileReportEntity::getCheckFileName, submitCheckFileDTO.getCheckFileName())
                .set(FileReportEntity::getCheckFileExtendName, submitCheckFileDTO.getCheckExtendName())
                .set(FileReportEntity::getIfCheck, 0)
                .set(FileReportEntity::getReportTime, submitCheckFileDTO.getReportTime())
                .set(FileReportEntity::getReportStatus, 0)
                .eq(FileReportEntity::getId, submitCheckFileDTO.getUserFileId());
        fileReportService.update(lambdaUpdateWrapper);
        return "已成功设置用户文件的检查文件名称";
    }

    /**
     * 下载校验文件
     *
     * @param request
     * @param response
     * @param model
     * @throws IOException
     */
    @GetMapping("/DownloadChekFile")
    public ResponseEntity<byte[]> DownloadChekFile(String fileName, String extendName, String checkFilePath, HttpServletRequest request, HttpServletResponse response, Model model) throws  IOException {

        getMinioOject(fileName, extendName , checkFilePath) ;

        String saveFileName=local_path+ File.separator+fileName+'.'+extendName;
        // 文件的存放路径
        File excelFile = new File(saveFileName);

        byte[] body = null;

        //InputStream is =  new FileInputStream(data.getSrcPath() + File.separator + fileName);// 文件的存放路径
        InputStream is =  new FileInputStream(excelFile);// 文件的存放路径
        body = is.readAllBytes();
        //设置头信息
        HttpHeaders headers = new HttpHeaders();
        //设置下载的附件 (myFileName必须处理中文名称!)
        String downFileName = fileName+'.'+extendName;
        //处理中文编码
        String myFileName = new String(downFileName.getBytes("utf-8"), "iso-8859-1");
        headers.setContentDispositionFormData("attachment", myFileName);
        //设置MIME类型
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        //用 FileUpload 组件的 FileUtils 读取文件，并构建成 ResponseEntity<byte[]>返回给浏览器

        excelFile.deleteOnExit();
        return new ResponseEntity<byte[]>(body, headers, HttpStatus.CREATED);
    }

    /**
     * 获取仪盘表统计数据
     * @param request
     * @param model
     * @param response
     */
//    @DataFilter
    @ResponseBody
//    @RequiresUser
    @PostMapping("/fileReportStats")
    public AjaxResult fileReportStats(HttpServletRequest request, Model model, HttpServletResponse response) {
        String operatorId = "" ;

        // 设置用户
//        ManagerAccountEntity account = CurrentAccountJwt.get() ; // CurrentAccountSession.get(request);
//        if (account != null) {
//            operatorId = account.getId() ;
//        }

        Map<String , Object> dataMap = new HashMap<String , Object>();
        dataMap.put("todayStats",fileReportService.FileReportTodayStats(operatorId));        //今日上报统计
        dataMap.put("msgStats",fileReportService.FileReportMsgStats(operatorId));            //今日消息分布
        dataMap.put("abnlStats",fileReportService.FileReportAbnlStats(operatorId));          //异常情况推送
        return new AjaxResult(200,"成功获取统计信息!",dataMap);
    }


    /**
     * 删除文件的minIO附件
     * @param fileUrl 存储在minio中的文件对象，/路径/文件名
     */
    public void deleteMinioOject(String fileUrl){
         fileReportService.deleteMinioOject(fileUrl);
    }



    /**
     * 通过线程池上报文件
     * @param reportInfo
     */
    public AjaxResult submitReport(ReportInfoDTO reportInfo) {

        // 如果文件已提交上报线程，则提示文件正在上报中
        if ( taskManager.containsTask(reportInfo.getId()) ) {
            return AjaxResult.success("文件:{}正在上报中......",reportInfo.getFileName());
        }

        // 创建一个新的Callable任务，并添加到线程池中执行
        AtomicInteger progress = new AtomicInteger(0);
        ReportSubmitTask task = new ReportSubmitTask(reportInfo, fileReportService, kafkaProducerService, messageFailService, messageService, minioFileUtil, progress, new ReportSubmitCallback() {
            @Override
            public void onComplete() {
                // 如果任务执行完成，则从任务列表中删除该任务
                taskManager.removeTask(reportInfo.getId());
                progress.set(100);
                log.debug("文件:{}处理进度:{}",reportInfo.getFileName(),progress.get());
            }

            @Override
            public void onException(Exception e) {
                // 如果任务发生异常，则从任务列表中删除该任务
                taskManager.removeTask(reportInfo.getId());
                progress.set(-1);
                log.debug("文件:{}处理进度:{}",reportInfo.getFileName(),progress.get());
            }
        });
        threadPool.submit(task);
        taskManager.addTask(reportInfo.getId(), task);
        HashMap<Object, Object> result = new HashMap<>();
        result.put("fileName",reportInfo.getFileName());
        result.put("progress",progress.get());
        log.debug("文件:{}处理进度:{}",reportInfo.getFileName(),progress.get());

        return AjaxResult.success(String.format("文件:%s已提交上报线程......",reportInfo.getFileName()),result);
    }


    /**
     * 通过文件id获取当前上报进度
     * @param request
     * @param taskId  文件ID(提交文件时，以文件ID作为任务ID)
     */
    @ResponseBody
    @GetMapping("/fileProgress")
    public AjaxResult getFileProgress(HttpServletRequest request, String taskId) {

        //文件上报进度信息
        Map  resultMap = new HashMap();
        // 如果文件已提交上报线程，则提示文件正在上报中
        if ( taskManager.containsTask(taskId) ) {
            resultMap.put("status",3);
            resultMap.put("progress",taskManager.getTask(taskId).getProgress());
            return AjaxResult.success("文件正在上报中......",resultMap);
        }else{
            resultMap.put("status",1);
            resultMap.put("progress",100);
            return AjaxResult.success("已上报",resultMap);
        }
    }


    /**
     * 检查同一个用户下，上报文件名称是否存在，如已存在，则不允许保存。确同一个用户下上报文件名称唯一
     *
     */
    @ResponseBody
    @PostMapping("/checkFileReportIfExist")
    public AjaxResult checkFileReportIfExist(HttpServletRequest request, @RequestBody FileReportEntity fileReport)  {
        // 设置用户
//        ManagerAccountEntity account = CurrentAccountJwt.get() ;
//        if ( account != null ) {
//            fileReport.setOperatorId(account.getId());
//        }

        return fileReportService.checkIfExist(fileReport);
    }


    /**
     * 检查上报是否已被引用，如被引用，则不能删除
     *
     */
    @ResponseBody
    @GetMapping("/checkFileReportIfUsed")
    public AjaxResult checkFileReportIfUsed(HttpServletRequest request, String ids)  {
        if ( ids == null ) {
            return AjaxResult.error("请求删除的id为空!");
        }

        List<String> idList = Arrays.asList(ids.split(","));

        String operatorId = null ;
        // 设置用户
//        ManagerAccountEntity account = CurrentAccountJwt.get() ;
//        if ( account != null ) {
//            operatorId = account.getId();
//        }

        //检查是否被数据管理引用，如有记录，则不允许删除
        ResponseBean messageCheckResult = fileReportService.checkHasMessage(idList, operatorId);
        if ( messageCheckResult.getCode() == 400 )	{
            return AjaxResult.error(messageCheckResult.getMessage()) ;
        }

        //检查是否被历史数据引用，如有记录，则不允许删除
        ResponseBean messageHisCheckResult = fileReportService.checkHasMessageHis(idList, operatorId);
        if ( messageHisCheckResult.getCode() == 400 )	{
            return AjaxResult.error(messageHisCheckResult.getMessage()) ;
        }

        //检查是否被数据分享引用，如有记录，则不允许删除
        ResponseBean fileShareCheckResult = fileReportService.checkHasFileShare(idList, operatorId);
        if ( fileShareCheckResult.getCode() == 400 )	{
            return AjaxResult.error(fileShareCheckResult.getMessage()) ;
        }


        //检查是否被历史异常引用，如有记录，则不允许删除
        ResponseBean messageFailCheckResult = messageFailService.checkHasMessageFail(idList, operatorId);
        if ( messageFailCheckResult.getCode() == 400 )	{
            Set<String> fileReportSet = (Set<String>) messageFailCheckResult.getData();

            List<FileReportEntity> fileReportList = fileReportService.findByIds((List<String>) fileReportSet);

            List<String> fileNameList=fileReportList.stream().map(FileReportEntity::getFileFullName).collect(Collectors.toList());

            String msg = String.format( "上报文件\" %s \"在数据分享界面存在数据,不能删除!",fileNameList);

            return AjaxResult.error(msg) ;
        }

        return AjaxResult.success() ;

    }


}
