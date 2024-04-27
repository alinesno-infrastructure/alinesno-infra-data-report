package com.alinesno.infra.data.report.gateway.controller;

import cn.hutool.core.util.RandomUtil;
import com.alinesno.infra.common.core.constants.SpringInstanceScope;
import com.alinesno.infra.common.facade.pageable.DatatablesPageBean;
import com.alinesno.infra.common.facade.pageable.TableDataInfo;
import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.facade.wrapper.RpcWrapper;
import com.alinesno.infra.common.web.adapter.plugins.TranslateCode;
import com.alinesno.infra.common.web.adapter.rest.BaseController;
import com.alinesno.infra.data.report.entity.FileShareEntity;
import com.alinesno.infra.data.report.service.IFileShareService;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 【请填写功能名称】Rest
 *
 * @author paul
 * @date 2022-11-28 10:28:04
 */
@RestController
@Scope(SpringInstanceScope.PROTOTYPE)
@RequestMapping("/api/infra/data/report/FileShare")
public class FileShareRest extends BaseController<FileShareEntity, IFileShareService> {

    //日志记录
    private static final Logger log = LoggerFactory.getLogger(FileShareRest.class);

    @Autowired
    private IFileShareService fileShareService;

//    @DataFilter
    @TranslateCode
    @ResponseBody
//    @RequiresUser
    @PostMapping("/datatables")
    public TableDataInfo datatables(HttpServletRequest request, Model model, DatatablesPageBean page) {
        log.debug("page = {}", ToStringBuilder.reflectionToString(page));
        return this.toPage(model, this.getFeign() , page) ;
    }

    @TranslateCode
    @ResponseBody
    @PostMapping("/datatableNew")
    public TableDataInfo datatableNew(HttpServletRequest request, Model model, DatatablesPageBean page) {
        log.debug("page = {}", ToStringBuilder.reflectionToString(page));
        return this.toPage(model, this.getFeign() , page) ;
    }

    @Override
    public IFileShareService getFeign() {
        return this.fileShareService;
    }

    @ApiOperation("保存实体")
    @ResponseBody
    @PostMapping({"saveShare"})
    public AjaxResult saveShare(HttpServletRequest request, @RequestBody FileShareEntity fileShareEntity) throws Exception {
        log.debug("===> save Entity:{}", ToStringBuilder.reflectionToString(fileShareEntity));
//        ManagerAccountEntity e = CurrentAccountJwt.get();
//        if (e != null) {
//            fileShareEntity.setOperatorId(e.getId());
//            fileShareEntity.setTenantId(e.getTenantId());
//            fileShareEntity.setDepartmentId(e.getDepartmentId());
//        }

        if (fileShareEntity.getIfCode() == 1) {
            String extractionCode = RandomUtil.randomNumbers(6);
            fileShareEntity.setExtractionCode(extractionCode);
        }
        this.getFeign().save(fileShareEntity);

        Map<String , Object> dataMap = new HashMap<String , Object>();
        dataMap.put("ifCode",fileShareEntity.getIfCode());                  //是否需要验证码
        dataMap.put("shareBatchNum",fileShareEntity.getShareBatchNum());    //分享文件ID
        dataMap.put("extractionCode",fileShareEntity.getExtractionCode());  //分享文件验证码
        return new AjaxResult(200,"成功保存分享文件!",dataMap) ;

    }


    /**
     * 获取分享详情
     *
     * @param response
     */
    @GetMapping("shareInfo")
    public AjaxResult getShareInfo(HttpServletRequest request, HttpServletResponse response){
        String shareBatchNum  =request.getParameter("shareBatchNum");
        RpcWrapper<FileShareEntity> rpcWrapper = new RpcWrapper();
        rpcWrapper.eq("shareBatchNum",shareBatchNum);
        FileShareEntity fileShare = fileShareService.findOne(rpcWrapper);
        if (fileShare == null){
            return AjaxResult.error(502,"分享批次号错误!");
        }else{
            if (fileShare.getEndTime().after(new Date()) ){
                Map<String , Object> dataMap = new HashMap<String , Object>();
                dataMap.put("ifCode",fileShare.getIfCode());                  //是否需要验证码
                dataMap.put("extractionCode",fileShare.getExtractionCode());  //分享文件验证码
                return new AjaxResult(200,"分享文件进行中!",dataMap);
            }else {
                return AjaxResult.error(501,"分享链接已失效!");
            }
        }
    }
}
