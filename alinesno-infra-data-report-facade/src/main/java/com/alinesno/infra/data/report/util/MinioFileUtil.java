package com.alinesno.infra.data.report.util;


import com.alinesno.infra.common.facade.response.ResultCodeEnum;
import com.alinesno.infra.common.web.adapter.utils.IdUtils;
import com.alinesno.infra.data.report.config.MinioConfig;
import com.alinesno.infra.data.report.vo.ResponseBean;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import jline.internal.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@Component
public  class MinioFileUtil {

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private MinioClient client;

    /**
     *
     * @param file 上传的文件
     * @return 访问地址
     */
    public String uploadFile(MultipartFile file)
    {
        if (file.isEmpty() || file.getSize() == 0) {
            return "the file is empty";
        }

        try {
            String fileName = extractFilename(file);       //对文件名进行编码
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())   //储存桶名称
                    .object(fileName)                      //文件路径加文件名
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            client.putObject(args);                        //上传文件
            //返回上传的路径 格式为  http://192.168.204.130:19000/alinesnodatareport/2022/12/06/c4652387-7e32-4b14-b9bb-a844816b60b7.xlsx
            //return minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + fileName;

           //返回上传的路径   格式为"/2022/12/06/2e69a530-60c2-453c-8b09-a8c0dfdc4e94.xlsx"
            return  "/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return  "minIO异常信息:"+e.getMessage() ;
        }
    }


    //获取文件名
    public static final String extractFilename(MultipartFile file)
    {
        String fileName = file.getOriginalFilename();
        //tring extension = getExtension(file);
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();;

        Date now = new Date();
        String datePath =  DateFormatUtils.format(now, "yyyy/MM/dd");

        fileName = datePath + "/" + IdUtils.fastUUID() + "." + extension;
        return fileName;
    }

   //删除对象
    public boolean removeObject(String bucketName, String filename) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Boolean result = false;

        RemoveObjectArgs.Builder builder = RemoveObjectArgs.builder();
        builder.bucket(bucketName);
        builder.object(filename);

        RemoveObjectArgs removeObjectArgs = builder.build();
        try {
            client.removeObject(removeObjectArgs);
            result = true;
        } catch (Exception e) {
           e.printStackTrace();
        }
        return result;
    }


    /**
     *
     * @param filePath 上传的文件路径
     * @return 访问地址
     */
    public String uploadFile(String filePath,String fileName)
    {
        if (filePath.isEmpty() || filePath == null ) {
            return "the file is empty";
        }

        try {
            File inFile = new File(filePath) ;
            InputStream inputStream = FileUtils.openInputStream(inFile);
            long fileSize = FileUtils.sizeOf(inFile);
            Tika tika = new Tika();
            String contentType = tika.detect(inFile);



            String minioFileName = extractFilename(fileName);       //对文件名进行编码
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())   //储存桶名称
                    .object(minioFileName)                      //要存储的对象名称
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType)
                    .build();
            client.putObject(args);                        //上传文件
            //返回上传的路径 格式为  http://192.168.204.130:19000/alinesnodatareport/2022/12/06/c4652387-7e32-4b14-b9bb-a844816b60b7.xlsx
            //return minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + fileName;

            //返回上传的路径   格式为"/2022/12/06/2e69a530-60c2-453c-8b09-a8c0dfdc4e94.xlsx"
            return  "/" + minioFileName;

        } catch (Exception e) {
            e.printStackTrace();
            return  "minIO异常信息:"+e.getMessage() ;
        }
    }

    //获取文件名
    public static final String extractFilename(String fileName)
    {

        String extension = FilenameUtils.getExtension(fileName).toLowerCase();;

        Date now = new Date();
        String datePath =  DateFormatUtils.format(now, "yyyy/MM/dd");

        fileName = datePath + "/" + IdUtils.fastUUID() + "." + extension;
        return fileName;
    }

    public ResponseBean checkMinioStatus(){

        ResponseBean checkResult = new ResponseBean() ;
        checkResult.setCode(ResultCodeEnum.SUCCESS);
        //minIO中桶名是否存在
        boolean bucketExist = false ;
        try {

            // 创建MinioClient对象并设置自定义配置
            MinioClient Client = MinioClient.builder()
                    .endpoint(minioConfig.getUrl())
                    .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                    .build();

            //列出所有存储桶
            List<Bucket> buckets = Client.listBuckets();
            for (Bucket bucket : buckets) {
                Log.debug("minIO bucketName:{}",bucket.name());
                if ( bucket.name().equals(minioConfig.getBucketName()) ) {
                    bucketExist = true ;
                }
            }

            if ( !bucketExist ) {
                checkResult.setCode(ResultCodeEnum.FAIL);
                checkResult.setMessage(String.format("MinIO服务器中桶名:%s不存在" ,minioConfig.getBucketName())) ;

            }

        } catch (Exception e) {
            Log.error("无法连接到 MinIO 服务器,错误信息:{}" , e.getMessage());
            checkResult.setCode(ResultCodeEnum.FAIL);
            checkResult.setMessage(String.format("无法连接到MinIO服务器，请联系管理员!错误信息:%s" , e.getMessage())) ;
            e.printStackTrace();
        }


        return checkResult ;

    }


}
