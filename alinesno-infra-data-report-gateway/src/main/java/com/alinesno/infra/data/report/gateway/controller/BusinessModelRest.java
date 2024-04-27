package com.alinesno.infra.data.report.gateway.controller;

import com.alinesno.infra.common.core.constants.SpringInstanceScope;
import com.alinesno.infra.common.facade.pageable.ConditionDto;
import com.alinesno.infra.common.facade.pageable.DatatablesPageBean;
import com.alinesno.infra.common.facade.pageable.TableDataInfo;
import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.facade.response.ResultCodeEnum;
import com.alinesno.infra.common.facade.wrapper.RpcWrapper;
import com.alinesno.infra.common.web.adapter.plugins.TranslateCode;
import com.alinesno.infra.common.web.adapter.rest.BaseController;
import com.alinesno.infra.data.report.config.MinioConfig;
import com.alinesno.infra.data.report.dto.ModelDTO;
import com.alinesno.infra.data.report.entity.BusinessModelEntity;
import com.alinesno.infra.data.report.entity.FileReportEntity;
import com.alinesno.infra.data.report.entity.FileShareEntity;
import com.alinesno.infra.data.report.service.IBusinessModelService;
import com.alinesno.infra.data.report.service.IFileReportService;
import com.alinesno.infra.data.report.service.IFileShareService;
import com.alinesno.infra.data.report.util.MinioFileUtil;
import com.alinesno.infra.data.report.vo.ResponseBean;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 【请填写功能名称】Rest
 *
 * @author paul
 * @date 2022-11-28 10:28:04
 */
@RestController
@Scope(SpringInstanceScope.PROTOTYPE)
@RequestMapping("/api/infra/data/report/BusinessModel")
public class BusinessModelRest extends BaseController<BusinessModelEntity, IBusinessModelService> {

    //日志记录
    private static final Logger log = LoggerFactory.getLogger(BusinessModelRest.class);

    @Autowired
    private IBusinessModelService businessModelService;

    @Autowired
    private MinioFileUtil minioFileUtil;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private IFileShareService fileShareService;

    @Autowired
    private IFileReportService fileReportService;

//    @DataFilter
    @TranslateCode(plugin = "BusinessModelPlugin")
    @ResponseBody
//    @RequiresUser
    @PostMapping("/datatables")
    public TableDataInfo datatables(HttpServletRequest request, Model model, DatatablesPageBean page) {
        log.debug("page = {}", ToStringBuilder.reflectionToString(page));
        return this.toPage(model, this.getFeign() , page) ;
    }

    //    @DataFilter
    @TranslateCode(plugin = "BusinessModelPlugin")
    @ResponseBody
//    @RequiresUser ModelNameList
    @PostMapping("/getModelNameList")
    public TableDataInfo getModelNameList(HttpServletRequest request, Model model, DatatablesPageBean page) {
        log.debug("page = {}", ToStringBuilder.reflectionToString(page));
        return this.toPage(model, this.getFeign() , page) ;
    }

    //    @DataFilter
    @TranslateCode(plugin = "BusinessModelPlugin")
    @ResponseBody
//    @RequiresUser ModelNameList
    @PostMapping("/getModelTreeList")
    public TableDataInfo getModelTreeList(HttpServletRequest request, Model model, DatatablesPageBean page) {
        log.debug("page = {}", ToStringBuilder.reflectionToString(page));
        return this.toPage(model, this.getFeign() , page) ;
    }

    @Override
    public IBusinessModelService getFeign() {
        return this.businessModelService;
    }

//    数据大脑项目，需要将数据写入mysql表，在前端查看数据，确认数据后再发往kafka
//    @RequestMapping(value = "/uploadfile", method = RequestMethod.POST)
//    @ResponseBody
//    public AjaxResult uploadFile(HttpServletRequest request , ModelDTO modelDTO, MultipartFile file) throws Exception {
//
//        //如果消息类型为mysql数据库,则拼凑建表语句
//        if(modelDTO.getMessageType().equals("1"))
//        {
//            Map<String, String> tableMap = businessModelService.checkTableExistsWithShow(modelDTO.getKafkaTopice());
//            if (tableMap !=null && tableMap.size()> 0 ){
//                log.error(modelDTO.getKafkaTopice()+"表已存在!请联系管理员删除后再重新上传!");
//                return  new AjaxResult(415,modelDTO.getKafkaTopice()+"表已存在!请联系管理员删除后再重新上传!");
//
//            }
//        }
//        String storageFilePath ="";
//
//        Map<String , Object> dataMap = new HashMap<String , Object>();
//
//        StringBuffer createTableSQL=new StringBuffer();
//
//        int cellNum =0 ;// 导入文件的列数
//        StringBuffer columnCnName= new StringBuffer();
//        StringBuffer columnName= new StringBuffer();
//
//
//        if (file==null|| file.getSize()==0){
//            log.error("文件上传错误，重新上传");
//            return new AjaxResult(415,"文件上传错误，重新上传!");
//        }
//        String filename = file.getOriginalFilename();
//        if ( !( filename.endsWith(".xls") || filename.endsWith(".xlsx") ) ){
//            log.error("文件上传格式错误，请重新上传");
//            return  new AjaxResult(415,"文件上传格式错误，请重新上传");
//        }
//        //获取列数、中文字段列名、英文字段列名
//        try {
//            if ( filename.endsWith(".xls") ){
//                InputStream inputStream = file.getInputStream();
//                HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
//                //读取第一张sheet
//                HSSFSheet sheet = workbook.getSheetAt(0);
//
//                if (sheet.getLastRowNum()<7){
//                    return  new AjaxResult(415,"文件上传格式错误，请重新上传");
//                }
//
//                //第一行为字段中文名称
//                HSSFRow firstRow = sheet.getRow(0);
//                //第二行为字段英文名称
//                HSSFRow secondRow = sheet.getRow(1);
//                //第三行 字段含义
//                HSSFRow threeRow = sheet.getRow(2);
//                //第四行 是否必填
//                HSSFRow fourRow = sheet.getRow(3);
//                //第五行 数据类型
//                HSSFRow fiveRow = sheet.getRow(4);
//                //第六行 枚举值
//                HSSFRow sixRow = sheet.getRow(5);
//                //第七行 数据样例
//                HSSFRow sevenRow = sheet.getRow(6);
//                if(    !firstRow.getCell(0).getStringCellValue().equals("字段名称")
//                    || !secondRow.getCell(0).getStringCellValue().equals("字段英文名称")
//                    || !threeRow.getCell(0).getStringCellValue().equals("字段含义")
//                    || !fourRow.getCell(0).getStringCellValue().equals("是否必填")
//                    || !fiveRow.getCell(0).getStringCellValue().equals("数据类型")
//                    || !sixRow.getCell(0).getStringCellValue().equals("枚举值")
//                    || !sevenRow.getCell(0).getStringCellValue().equals("数据样例")
//                ){
//                    return  new AjaxResult(415,"文件上传格式错误，请重新上传");
//                }
//
//                //获取列数
//                cellNum=firstRow.getPhysicalNumberOfCells();
//                //遍历列，获取字段中文名称清单，字段英文名称清单
//                for(int i=1;i<cellNum;i++){
//                    columnCnName.append(firstRow.getCell(i).getStringCellValue()).append(",");
//                    columnName.append(secondRow.getCell(i).getStringCellValue()).append(",");
//                }
//                columnCnName.deleteCharAt(columnCnName.length() - 1);
//                columnName.deleteCharAt(columnName.length() - 1);
//                dataMap.put("cellNum",cellNum-1);
//                dataMap.put("columnCnName",columnCnName);
//                dataMap.put("columnName",columnName);
//
//                //如果消息类型为mysql数据库,则拼凑建表语句
//                if(modelDTO.getMessageType().equals("1")){
//
//
//                    createTableSQL.append("CREATE TABLE ").append(modelDTO.getKafkaTopice()).append(System.lineSeparator()).append("(");
//                    //遍历第一、二、三、四、五行的信息
//                    //获取列数
//                    cellNum=firstRow.getPhysicalNumberOfCells();
//                    //遍历列，获取字段中文名称清单，字段英文名称清单
//                    for(int i=1;i<cellNum;i++){
//                        createTableSQL.append(secondRow.getCell(i).getStringCellValue()).append(" ").append(fiveRow.getCell(i).getStringCellValue()).append(" ");
//                        if  (fourRow.getCell(i).getStringCellValue().equals("必填"))
//                        {
//                            createTableSQL.append(" NOT NULL ");
//                        }else
//                        {
//                            createTableSQL.append(" DEFAULT NULL ");
//                        }
//                        createTableSQL.append(" COMMENT '").append(firstRow.getCell(i).getStringCellValue()).append("' ,").append(System.lineSeparator());
//                    }
//                    createTableSQL.append(" id varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键'").append(" ,").append(System.lineSeparator());
//                    createTableSQL.append(" import_time varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL  COMMENT '导入时间'").append(" ,").append(System.lineSeparator());
//                    createTableSQL.append(" operator_id varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL  COMMENT '操作员id'").append(" ,").append(System.lineSeparator());
//                    createTableSQL.append(" user_file_id varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '导入文件ID,可做批次号'").append(" ,").append(System.lineSeparator());
//                    createTableSQL.append(" PRIMARY KEY (id) USING BTREE").append(System.lineSeparator());
//                    createTableSQL.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC ").append(";");
//                    log.info(createTableSQL.toString() );
//                }
//
//            }else {
//                InputStream inputStream = file.getInputStream();
//                XSSFWorkbook Workbook = new XSSFWorkbook(inputStream);
//                XSSFSheet sheet = Workbook.getSheetAt(0);
//
//                if (sheet.getLastRowNum()<7){
//                    return  new AjaxResult(415,"文件上传格式错误，请重新上传");
//                }
//
//                //第一行为字段中文名称
//                XSSFRow firstRow = sheet.getRow(0);
//                //第二行为字段英文名称
//                XSSFRow secondRow = sheet.getRow(1);
//                //第三行 字段含义
//                XSSFRow threeRow = sheet.getRow(2);
//                //第四行 是否必填
//                XSSFRow fourRow = sheet.getRow(3);
//                //第五行 数据类型
//                XSSFRow fiveRow = sheet.getRow(4);
//                //第六行 枚举值
//                XSSFRow sixRow = sheet.getRow(5);
//                //第七行 数据样例
//                XSSFRow sevenRow = sheet.getRow(6);
//                if(    !firstRow.getCell(0).getStringCellValue().equals("字段名称")
//                        || !secondRow.getCell(0).getStringCellValue().equals("字段英文名称")
//                        || !threeRow.getCell(0).getStringCellValue().equals("字段含义")
//                        || !fourRow.getCell(0).getStringCellValue().equals("是否必填")
//                        || !fiveRow.getCell(0).getStringCellValue().equals("数据类型")
//                        || !sixRow.getCell(0).getStringCellValue().equals("枚举值")
//                        || !sevenRow.getCell(0).getStringCellValue().equals("数据样例")
//                ){
//                    return  new AjaxResult(415,"文件上传格式错误，请重新上传");
//                }
//
//                //获取列数
//                cellNum=firstRow.getPhysicalNumberOfCells();
//                //遍历列，获取字段中文名称清单，字段英文名称清单
//                for(int i=1;i<cellNum;i++){
//                    columnCnName.append(firstRow.getCell(i).getStringCellValue()).append(",");
//                    columnName.append(secondRow.getCell(i).getStringCellValue()).append(",");
//                }
//                columnCnName.deleteCharAt(columnCnName.length() - 1);
//                columnName.deleteCharAt(columnName.length() - 1);
//                dataMap.put("cellNum",cellNum-1);
//                dataMap.put("columnCnName",columnCnName);
//                dataMap.put("columnName",columnName);
//
//                //如果消息类型为mysql数据库,则拼凑建表语句
//                if(modelDTO.getMessageType().equals("1")){
//                    Map<String, String> tableMap = businessModelService.checkTableExistsWithShow(modelDTO.getKafkaTopice()) ;
//
//                    createTableSQL.append("CREATE TABLE ").append(modelDTO.getKafkaTopice()).append(System.lineSeparator()).append("(");
//                    //遍历第一、二、三、四、五行的信息
//
//                    //获取列数
//                    cellNum=firstRow.getPhysicalNumberOfCells();
//                    //遍历列，获取字段中文名称清单，字段英文名称清单
//                    for(int i=1;i<cellNum;i++){
//                        createTableSQL.append(secondRow.getCell(i).getStringCellValue()).append(" ").append(fiveRow.getCell(i).getStringCellValue()).append(" ");
//                        if  (fourRow.getCell(i).getStringCellValue().equals("必填"))
//                        {
//                            createTableSQL.append(" NOT NULL  ");
//                        }else
//                        {
//                            createTableSQL.append(" DEFAULT NULL ");
//                        }
//                        createTableSQL.append(" COMMENT '").append(firstRow.getCell(i).getStringCellValue()).append("' ,").append(System.lineSeparator());
//                    }
//                    createTableSQL.append(" id          varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键'").append(" ,").append(System.lineSeparator());
//                    createTableSQL.append(" import_time varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL  COMMENT '导入时间'").append(" ,").append(System.lineSeparator());
//                    createTableSQL.append(" operator_id varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL  COMMENT '操作员id'").append(" ,").append(System.lineSeparator());
//                    createTableSQL.append(" user_file_id varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '导入文件ID,可做批次号'").append(" ,").append(System.lineSeparator());
//                    createTableSQL.append(" PRIMARY KEY (id) USING BTREE").append(System.lineSeparator());
//                    createTableSQL.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC ").append(";");
//                    log.info(createTableSQL.toString() );
//                    }
//            }
//        }catch (IOException e) {
//            e.printStackTrace();
//            log.error("文件内容读取失败，请重试");
//            return  new AjaxResult(500 ,"文件内容读取失败，请重试");
//        }
//
//
//        try {
//
//            storageFilePath =  minioFileUtil.uploadFile(file);
//            dataMap.put("storageFilePath",storageFilePath);
//            if(modelDTO.getMessageType().equals("1")) {
//                businessModelService.createTable(createTableSQL.toString());
//            }
//        } catch (Exception e) {
//           e.printStackTrace();
//           return AjaxResult.error();
//        }
//
//        if( storageFilePath.equals("the file is empty")|| storageFilePath.equals("upload failed")){
//            return AjaxResult.error();
//        }else {
//            return new AjaxResult(200,"成功!",dataMap);
//        }
//
//
//    }


    @RequestMapping(value = "/uploadfile", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult uploadFile(HttpServletRequest request , ModelDTO modelDTO, MultipartFile file) throws Exception {

        return businessModelService.importModel(modelDTO, file) ;

    }


    //获取minIO对象流
    public InputStream getObject(String bucketName, String objectName) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {

        MinioClient client = minioConfig.getMinioClient();
        return client.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }
    /**
     * 下载文件
     *
     * @param filePath  文件绝对路径
     * @param response
     * @throws IOException
     */
    @GetMapping("downloadFile")
    public void downloadFile(String fileName,String filePath, HttpServletResponse response) throws IOException {

        ResponseBean checkMinioStatus = minioFileUtil.checkMinioStatus();
        if ( checkMinioStatus.getCode() == 400 ) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = checkMinioStatus.getMessage();
            OutputStream ps = response.getOutputStream();
            ps.write(data.getBytes("UTF-8"));
            return ;

        }

        if (StringUtils.isBlank(filePath)) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "文件下载失败";
            OutputStream ps = response.getOutputStream();
            ps.write(data.getBytes("UTF-8"));
            return;
        }
        try {
            InputStream object = getObject(minioConfig.getBucketName(),filePath);
            byte buf[] = new byte[1024];
            int length = 0;
            response.reset();

            String storeFileName=filePath.substring(filePath.lastIndexOf("/") + 1);
            String storeFileExtendName=storeFileName.substring(storeFileName.lastIndexOf(".") + 1);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String timeValue = formatter.format(new Date());

            response.setHeader("Content-Disposition", "attachment;filename=" +URLEncoder.encode(fileName+timeValue+"."+storeFileExtendName, "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            OutputStream outputStream = response.getOutputStream();
            // 输出文件
            while ((length = object.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
            // 关闭输出流
            outputStream.close();
        } catch (Exception ex) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "文件下载失败。"+ex.getMessage();
            OutputStream ps = response.getOutputStream();
            ps.write(data.getBytes("UTF-8"));
        }
    }

    /**
     * 分享文件
     *
     * @param shareFileId  分享文件ID
     * @param response
     * @throws IOException
     */
    @GetMapping("share")
    public void share(String shareFileId, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(shareFileId)) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "shareFileId为空，文件分享失败";
            OutputStream ps = response.getOutputStream();
            ps.write(data.getBytes("UTF-8"));
            return;
        }

        RpcWrapper<FileShareEntity> rpcWrapper = RpcWrapper.build();
        rpcWrapper.eq("shareFileId", shareFileId);


        FileShareEntity fileShare = fileShareService.findOne(rpcWrapper);
        if (fileShare == null) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "分享ID不存在，文件分享失败";
            OutputStream ps = response.getOutputStream();
            ps.write(data.getBytes("UTF-8"));
            return;
        }else {
            if ( fileShare.getEndTime().after(new Date() ) ){
                downloadFile(fileShare.getShareFileName(),fileShare.getStorageFilePath(),response);
            }
            else{
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                String data = "已到失效时间，文件分享失败";
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes("UTF-8"));
                return;

            }
        }
    }

    /**
     * 数据模型_样例
     *
     * @param request
     * @param response
     * @param model
     * @throws IOException
     */
    @GetMapping("/downloadTemplate")
    public ResponseEntity<byte[]> exportTemplate(HttpServletRequest request, HttpServletResponse response,
                                                 Model model) throws  IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource("");
        // 获得服务器路径
        String srcTemplateFilePath = url.getPath();
        // 文件的存放路径
        String filePath=srcTemplateFilePath+ File.separator+"template" + File.separator + "studentSample.xlsx";

        byte[] body = null;

        InputStream is =  new FileInputStream(filePath);// 文件的存放路径
        body = IOUtils.toByteArray(is);

        // 设置响应头，告诉浏览器文件类型为 Excel 文件，并指定文件名
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("studentSample.xlsx").build());

        // 返回 ResponseEntity，包含文件字节数组和响应头
        return new ResponseEntity<byte[]>(body, headers, HttpStatus.OK);

    }


    @ApiOperation("补充业务模型的模板信息")
    @ResponseBody
    @PostMapping({"changeModelInfo"})
    public AjaxResult changeModelInfo(@RequestBody BusinessModelEntity businessModel) {
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
        boolean b = this.getFeign().update(updateWrapper);
        return b ? this.ok() : this.error();
    }


    /**
     * 查询树形导航条当前节点和下级节点及符合查询条件的数据
     *
     * @param request
     * @param model
     * @param page
     * @param ids         树形导航条当前节点和下级节点的id值
     * @param pageNum     预防查询面板没有设置查询条件时，page中没有值
     * @param pageSize    预防查询面板没有设置查询条件时，page中没有值
     */
//    @DataFilter
    @TranslateCode(plugin = "BusinessModelPlugin")
    @ResponseBody
//    @RequiresUser
    @PostMapping({"listTreeNavInfo"})
    public TableDataInfo listTreeNavInfo(HttpServletRequest request, Model model, DatatablesPageBean page,String[] ids ,int pageNum,int pageSize ) {

        RpcWrapper<BusinessModelEntity> wrapper =   new RpcWrapper();
        wrapper.in("id",ids) ;
//        wrapper.builderCondition( page.getCondition());
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);

        Page<BusinessModelEntity> pageableResult = null;

        if (pageableResult == null) {
            pageableResult = new Page((long)page.getPageNum(), (long)page.getPageSize());
            pageableResult = this.getFeign().pageRpc(pageableResult, wrapper);
        }

        TableDataInfo dInfo = new TableDataInfo();
        dInfo.setCode(200);
        dInfo.setMsg("查询成功");
        dInfo.setRows(pageableResult.getRecords());
        dInfo.setTotal((long)((int)pageableResult.getTotal()));
        return dInfo;

    }

    /**
     * 检查同一个用户下，业务模型名称是否存在，如已存在，则不允许保存。确同一个用户下业务模型名称唯一
     *
     */
    @ResponseBody
    @PostMapping("/checkBusinessModelIfExist")
    public AjaxResult checkBusinessModelIfExist(HttpServletRequest request, @RequestBody BusinessModelEntity businessMode)  {
        // 设置用户
//        ManagerAccountEntity account = CurrentAccountJwt.get() ;
//        if ( account != null ) {
//            businessMode.setOperatorId(account.getId());
//        }

        return businessModelService.checkIfExist(businessMode);
    }


    /**
     * 检查业务模型是否已被上报文件引用，如被引用，则不能删除
     *
     */
    @ResponseBody
    @GetMapping("/checkBusinessModelIfUsed")
    public AjaxResult checkBusinessModelIfUsed(HttpServletRequest request, String ids)  {
        if ( ids == null ) {
            return AjaxResult.error("请求删除的id为空!");
        }

        List<String> idList = Arrays.asList(ids.split(","));

        StringBuffer msg = new StringBuffer();

        String operatorId = null ;
        // 设置用户
//        ManagerAccountEntity account = CurrentAccountJwt.get() ;
//        if ( account != null ) {
//            operatorId = account.getId();
//        }

        //检查是否有子业务模型，如有子业务模型，则不允许删除
        ResponseBean responseBean = checkHasChildren(idList, operatorId);
        if ( responseBean.getCode() == 400 ){
            return AjaxResult.error(responseBean.getMessage()) ;
        }

        //检查是否被数据目录引用，如有记录，则不允许删除
        QueryWrapper<FileReportEntity> wrapper = new QueryWrapper<>();
        wrapper.in("model_id",idList);
        wrapper.eq("operator_id", operatorId);
        List<FileReportEntity> fileReportList = fileReportService.list(wrapper);
        if ( fileReportList != null && fileReportList.size() > 0 )	{
            int i = 0 ;
            for ( FileReportEntity fileReport : fileReportList ) {
                //避免提示信息太长，只取前4个的名称
                if ( i == 3 ) {
                    msg.deleteCharAt( msg.length() - 1 ) ;
                    msg.append("等");
                    break;
                }
                msg.append(fileReport.getFileFullName());
                msg.append(",");
                i = i + 1 ;

            }
            if (   msg.lastIndexOf(",") == msg.length() - 1   ) {
                msg.deleteCharAt( msg.length() - 1 ) ;
            }

            return AjaxResult.error("业务模型已被上报文件\"" + msg.toString() + "\"引用,不能删除!") ;
        } else {
            return AjaxResult.success() ;
        }

    }

    private ResponseBean checkHasChildren(List<String> idList, String operatorId){
        ResponseBean result = new ResponseBean() ;
        StringBuffer msg = new StringBuffer();
        //检查是否存在子业务模型
        QueryWrapper<BusinessModelEntity> classifyWrapper = new QueryWrapper<>();
        classifyWrapper.in("model_parent_id",idList);
        classifyWrapper.eq("operator_id", operatorId);
        List<BusinessModelEntity> modelList = businessModelService.list(classifyWrapper);
        if ( modelList != null && modelList.size() > 0 )	{
            Set<String> modelSet = new HashSet<String>() ;

            for ( BusinessModelEntity  modelEntity : modelList ) {
                modelSet.add( modelEntity.getModelName() ) ;
            }

            int i = 0 ;
            for ( String model : modelSet ) {
                //避免提示信息太长，只取前4个的名称
                if ( i == 3 ) {
                    msg.deleteCharAt( msg.length() - 1 ) ;
                    msg.append("等");
                    break;
                }
                msg.append(model);
                msg.append(",");
                i = i + 1 ;

            }

            if (   msg.lastIndexOf(",") == msg.length() - 1   ) {
                msg.deleteCharAt( msg.length() - 1 ) ;
            }
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage("业务模型已被子模型\"" +  msg.toString() + "\"引用,不能删除!" );

        } else {
            result.setCode(ResultCodeEnum.SUCCESS);
        }
        return result ;

    }

    @RequestMapping(value = "/checkMinioStatus", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult checkMinioStatus(HttpServletRequest request , HttpServletResponse response )   {
        ResponseBean checkMinioStatus = minioFileUtil.checkMinioStatus();
        if ( checkMinioStatus.getCode() == 400 ) {
            return  AjaxResult.error(checkMinioStatus.getMessage()) ;

        }else{
            return  AjaxResult.success();
        }

    }
}
