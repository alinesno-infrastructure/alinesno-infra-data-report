package com.alinesno.infra.data.report.service.impl;

import com.alinesno.infra.common.core.service.impl.IBaseServiceImpl;
import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.facade.response.ResultCodeEnum;
import com.alinesno.infra.common.facade.wrapper.RpcWrapper;
import com.alinesno.infra.data.report.config.MinioConfig;
import com.alinesno.infra.data.report.entity.*;
import com.alinesno.infra.data.report.mapper.FileReportMapper;
import com.alinesno.infra.data.report.service.*;
import com.alinesno.infra.data.report.vo.FileReportAbnlStats;
import com.alinesno.infra.data.report.vo.FileReportMsgStats;
import com.alinesno.infra.data.report.vo.FileReportTodayStats;
import com.alinesno.infra.data.report.vo.ResponseBean;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author paul
 * @date 2022-11-28 10:28:04
 */
@Service
public class FileReportServiceImpl extends IBaseServiceImpl< FileReportEntity, FileReportMapper> implements IFileReportService {
    //日志记录
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(FileReportServiceImpl.class);

    @Autowired
    private FileReportMapper fileReportMapper;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private IBusinessModelService businessModelService;

    @Autowired
    private IMessageHisService messageHisService ;

    @Autowired
    private IFileShareService fileShareService ;

    @Autowired
    private IMessageService messageService ;



    @Override
    public IPage<FileReportEntity> recycleFileList(String operatorId, Long currentPage, Long pageCount, String fileName) {
        Page<FileReportEntity> page = new Page<>(currentPage, pageCount);
        return fileReportMapper.recycleFileList(page, operatorId,  currentPage,  pageCount, fileName);
    }


    @Override
    public void reductionFileReport( String id){
        fileReportMapper.reductionFileReport(id);
    };

    @Override
    public void deleteFileReport( String id){
        fileReportMapper.deleteFileReport(id);
    };


    @Override
    public FileReportTodayStats FileReportTodayStats(String operatorId){
        return fileReportMapper.FileReportTodayStats(operatorId);
    };

    @Override
    public FileReportMsgStats FileReportMsgStats(String operatorId){
        return fileReportMapper.FileReportMsgStats(operatorId);
    };

    @Override
    public FileReportAbnlStats FileReportAbnlStats(String operatorId){
        return fileReportMapper.FileReportAbnlStats(operatorId);
    };

    //消息对象为表时，执行相关的SQL语句
    @Override
    public void insertTable(String tableName, String values) {
        fileReportMapper.insertTable(tableName, values);
    }

    //消息对象为表时，执行相关的SQL语句
    @Override
    public void executeSql(String sqlContent ) {
        fileReportMapper.executeSql(sqlContent);
    }

    //遍历minIO中的文件对象，并将文件对象转入checkDelete函数，如文件对象在业务模型、上报文件中不存在，则删除minio中的文件对象
    @Override
    public void timingDeleteFile(){
        try {
            //创建客户端
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioConfig.getUrl())
                    .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                    .build();

            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(minioConfig.getBucketName()).recursive(true).build());

             //开始遍历
            for (Iterator<Result<Item>> iterator = results.iterator(); iterator.hasNext(); ) {
                Item item = iterator.next().get();
                 //如果对象是目录，获取目录下的所有对象
                if (item.isDir()) {
                    log.debug("当前目录名称:{}",item.objectName());

                    //获取目录下的所有对象
                    Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder().bucket(minioConfig.getBucketName()).prefix(item.objectName()).recursive(true).build());
                    for (Iterator<Result<Item>> objectsIterator = objects.iterator(); objectsIterator.hasNext(); ) {
                        Item object = objectsIterator.next().get();
                        //minFilelist.add(object.objectName());
                        log.debug("File:{} ,size:{} bytes", object.objectName(), object.size());
                        checkDelete("/"+object.objectName());
                    }
                } else { // 如果对象不是目录
                    //minFilelist.add(item.objectName());
                    log.debug("File:{} ,size:{} bytes", item.objectName(), item.size());
                    checkDelete("/"+item.objectName());

                }

            }

        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | MinioException e) {
            log.debug("发生错误:{} " + e.getMessage());
        }

    }

    //删除minio中的文件对象，参数格式如：/2023/04/25/020df267-0570-4b2f-b4fa-1b474917ac17.xlsx
    public void deleteMinioOject(String fileUrl){
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioConfig.getUrl())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();

        // 指定要删除的bucket名称和文件名称
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(fileUrl)
                .build();

        try {
            // 删除文件
            minioClient.removeObject(removeObjectArgs);
        } catch ( InvalidKeyException e ) {
            e.printStackTrace();
        } catch ( NoSuchAlgorithmException e ) {
            e.printStackTrace();
        } catch ( ErrorResponseException e ) {
            e.printStackTrace();
        } catch ( InvalidResponseException e ) {
            e.printStackTrace();
        } catch ( ServerException e ) {
            e.printStackTrace();
        } catch ( InsufficientDataException e ) {
            e.printStackTrace();
        } catch ( XmlParserException e ) {
            e.printStackTrace();
        } catch ( InternalException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    };

    //如文件对象在业务模型、上报文件中不存在，则删除minio中的文件对象，参数格式如：/2023/04/25/020df267-0570-4b2f-b4fa-1b474917ac17.xlsx
    public void checkDelete(String fileUrl){

        RpcWrapper<BusinessModelEntity> businessModelWrapper = new RpcWrapper<>();
        businessModelWrapper.eq("storage_file_path",fileUrl);
        List<BusinessModelEntity> businessModelList = businessModelService.findAll(businessModelWrapper);

        RpcWrapper<FileReportEntity> fileReporWrapper = new RpcWrapper<>();
        fileReporWrapper.eq("storage_file_path",fileUrl);
        List<FileReportEntity> fileReportList = this.findAll(fileReporWrapper);

        RpcWrapper<FileReportEntity> fileReporWrap = new RpcWrapper<>();
        fileReporWrap.eq("check_file_path",fileUrl);
        List<FileReportEntity> fileReportListA = this.findAll(fileReporWrap);

        if ( businessModelList.size() == 0  &&   fileReportList.size() == 0 && fileReportListA.size() == 0 ) {
            log.debug("File:{}在业务模型和上报文件中没有找到记录，将从minIO中删除!", fileUrl);
            deleteMinioOject(fileUrl);
        }

    }

    //根据ID查询回收站中的数据，findByIdS方法过滤了hasDelete= 1的数据
    public List<FileReportEntity> findByReportIds( QueryWrapper<FileReportEntity> queryWrapper ){
        return  fileReportMapper.selectReportIds(queryWrapper);
    };


    //检查同一个用户下，上报文件名称是否存在，如已存在，则不允许保存。确同一个用户下上报文件名称唯一
    @Override
    public AjaxResult checkIfExist(FileReportEntity fileReport){

        QueryWrapper<FileReportEntity> wrapper = new QueryWrapper<>();

        //如果是修改，则查询其他记录，是否有同名
        if ( fileReport.getId() != null &&  !fileReport.getId().equals("") )
        {
            wrapper.ne("id", fileReport.getId());
        }
        wrapper.eq("file_full_name", fileReport.getFileFullName());
        wrapper.eq("operator_id", fileReport.getOperatorId());
        List<FileReportEntity> fileReportList = this.list(wrapper);
        if ( fileReportList != null && fileReportList.size() > 0 )	{
            return AjaxResult.error("已存在同名的上报文件!") ;
        } else {
            return AjaxResult.success() ;
        }

    };


    @Override
    public ResponseBean checkHasMessage(List<String> idList, String operatorId){
        ResponseBean result = new ResponseBean() ;
        StringBuffer msg = new StringBuffer();
        //检查是否存在数据管理数据
        QueryWrapper<MessageEntity> messageWrapper = new QueryWrapper<>();
        messageWrapper.in("file_id",idList);
        messageWrapper.eq("operator_id", operatorId);
        List<MessageEntity> messageList = messageService.list(messageWrapper);
        if ( messageList != null && messageList.size() > 0 )	{
            Set<String> fileReportSet = new HashSet<String>() ;

            for ( MessageEntity  messageEntity : messageList ) {
                fileReportSet.add( messageEntity.getFileId() ) ;
                if ( fileReportSet.size() == 3 ){
                    break;
                }
            }

            List<FileReportEntity> fileReportList = this.listByIds(fileReportSet);


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
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage("上报文件\"" +  msg.toString() + "\"在数据管理界面存在数据,不能删除!") ;

        } else {
            result.setCode(ResultCodeEnum.SUCCESS);
        }
        return result ;

    }

    //检查是否被历史数据引用，如有记录，则不允许删除
    @Override
    public ResponseBean checkHasMessageHis(List<String> idList, String operatorId){
        ResponseBean result = new ResponseBean() ;
        StringBuffer msg = new StringBuffer();
        //检查是否存在数据管理数据
        QueryWrapper<MessageHisEntity> messageWrapper = new QueryWrapper<>();
        messageWrapper.in("file_id",idList);
        messageWrapper.eq("operator_id", operatorId);
        List<MessageHisEntity> messageHisList = messageHisService.list(messageWrapper);
        if ( messageHisList != null && messageHisList.size() > 0 )	{
            Set<String> fileReportSet = new HashSet<String>() ;

            for ( MessageHisEntity  messageHisEntity : messageHisList ) {
                fileReportSet.add( messageHisEntity.getFileId() ) ;
                if ( fileReportSet.size() == 3 ){
                    break;
                }
            }

            List<FileReportEntity> fileReportList = this.listByIds(fileReportSet);

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
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage("上报文件\"" +  msg.toString() + "\"在历史数据界面存在数据,不能删除!") ;

        } else {
            result.setCode(ResultCodeEnum.SUCCESS);
        }
        return result ;

    }


    //检查是否被数据分享引用，如有记录，则不允许删除
    @Override
    public ResponseBean checkHasFileShare(List<String> idList, String operatorId){
        ResponseBean result = new ResponseBean() ;
        StringBuffer msg = new StringBuffer();
        //检查是否存在数据管理数据
        QueryWrapper<FileShareEntity> fileShareWrapper = new QueryWrapper<>();
        fileShareWrapper.in("share_file_id",idList);
        fileShareWrapper.eq("operator_id", operatorId);
        List<FileShareEntity> fileShareList = fileShareService.list(fileShareWrapper);
        if ( fileShareList != null && fileShareList.size() > 0 )	{
            Set<String> fileReportSet = new HashSet<String>() ;

            for ( FileShareEntity  fileShareEntity : fileShareList ) {
                fileReportSet.add( fileShareEntity.getShareFileFullName() ) ;
                if ( fileReportSet.size() == 3 ){
                    break;
                }
            }

            int i = 0 ;
            for ( String fileReport : fileReportSet ) {
                //避免提示信息太长，只取前4个的名称
                if ( i == 3 ) {
                    msg.deleteCharAt( msg.length() - 1 ) ;
                    msg.append("等");
                    break;
                }
                msg.append(fileReport);
                msg.append(",");
                i = i + 1 ;

            }

            if (   msg.lastIndexOf(",") == msg.length() - 1   ) {
                msg.deleteCharAt( msg.length() - 1 ) ;
            }
            result.setCode(ResultCodeEnum.FAIL);
            result.setMessage("上报文件\"" +  msg.toString() + "\"在数据分享界面存在数据,不能删除!") ;
        } else {
            result.setCode(ResultCodeEnum.SUCCESS);

        }
        return  result ;

    }



}

