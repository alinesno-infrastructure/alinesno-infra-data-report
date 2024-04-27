import request from '@/utils/request';
import Clipboard from "clipboard";
import { ElMessage } from 'element-plus'

/**
 * 【数据上报】 接口文件
 *
 * @author paul
 * @date 2024年3月10日
 */

// 接口配置项
var prefix = '/api/infra/data/report/FileReport/' ;
var managerUrl = {
    datatables : prefix +"datatables" ,
    createUrl: prefix + 'add' ,
    saveUrl: prefix + 'save' ,
    updateUrl: prefix +"modify" ,
    statusUrl: prefix +"changeStatus" ,
    cleanUrl: prefix + "clean",
    detailUrl: prefix +"detail",
    removeUrl: prefix + "delete" ,
    exportUrl: prefix + "exportExcel",
    changeField: prefix + "changeField",
    datatablesDel: prefix + "datatablesDel",
    reduction: prefix + "reduction",
    deleteFileReport: prefix + "deleteFileReport",
    readExcel: prefix + "readExcel",
    DownloadChekFile: prefix + "DownloadChekFile",
    fileReportStats: prefix + "fileReportStats",
    fileProgress: prefix + "fileProgress",
    checkFileReportIfExist: prefix + "checkFileReportIfExist",
    checkFileReportIfUsed: prefix + "checkFileReportIfUsed"
}

// 查询【请填写功能名称】列表
export function listFileReport(query , data) {
  return request({
    url: managerUrl.datatables ,
    method: 'post',
    params: query ,
    data: data
  })
}

// 查询【请填写功能名称】详细
export function getFileReport(id) {
  return request({
    url: managerUrl.detailUrl + '/' + id ,
    method: 'get'
  })
}

// 新增【请填写功能名称】
export function addFileReport(data) {
  return request({
    url: managerUrl.saveUrl,
    method: 'post',
    data: data
  })
}

// 修改【请填写功能名称】
export function updateFileReport(data) {
  return request({
    url: managerUrl.updateUrl,
    method: 'put',
    data: data
  })
}

// 删除【请填写功能名称】
export function delFileReport(id) {
  return request({
    url: managerUrl.removeUrl + '/' + id ,
    method: 'delete'
  })
}

// 导出【请填写功能名称】
export function exportFileReport(query) {
  return request({
    url: managerUrl.exportUrl,
    method: 'get',
    params: query
  })
}

// 状态【请填写功能名称】修改
export function changeStatusFileReport(id , status) {
  const data = {
    id ,
    status
  }
  return request({
    url: managerUrl.statusUrl,
    method: 'put',
    data: data
  })
}

// 修改【请填写功能名称】单个字段值
export function changeFileReportField(value , field , id){
  const data = {
    value ,
    field ,
    id
  }
  return request({
    url: managerUrl.changeField ,
    method: 'post',
    data: data
  })
}


// 查询【请填写功能名称】列表
export function listDelFileReport(query , data) {
  return request({
    url: managerUrl.datatablesDel ,
    method: 'post',
    params: query ,
    data: data
  })
}

// 回收站还原【上报文件】
export function reductionFileReport(data) {
  return request({
    url: managerUrl.reduction,
    method: 'put',
    data: data
  })
}

// 回收站删除【上报文件】
export function deleteFileReport(ids) {
  return request({
    url: managerUrl.deleteFileReport + '/?ids=' + ids,
    method: 'get'
  })
}


//上报文件时，从本地存储中读取excel并解析成json，发送到kafka。如检查必填项等信息时，出现空值，则弹出下载检查结果窗
export function readExcel(data) {
  return request({
    url: managerUrl.readExcel,
    method: 'post',
    data: data ,
    timeout:999999
  })
}

//下载导入模板
export function DownloadChekFile(fileName, extendName, checkFilePath) {
  return request({
    url: managerUrl.DownloadChekFile+"?fileName="+fileName+"&extendName="+ extendName+"&checkFilePath="+ checkFilePath,
    method: 'get',
    responseType:'blob'
  })
}


// 查询【统计信息】列表
export function getFileReportStats(query , data) {
  return request({
    url: managerUrl.fileReportStats ,
    method: 'post',
    params: query ,
    data: data
  })
}

// 获取上报进度
export function getFileProgress(taskId) {
  return request({
    url: managerUrl.fileProgress+"?taskId="+taskId,
    method: 'get'
  })
}

//检查同一个用户下，上报文件名称是否存在，如已存在，则不允许保存。确同一个用户下上报文件名称唯一
export function checkFileReportIfExist(data) {
  return request({
    url: managerUrl.checkFileReportIfExist,
    method: 'post',
    data: data
  })
}


//检查上报文件是否已被引用，如被引用，则不能删除
export function checkFileReportIfUsed(ids) {
  return request({
    url: managerUrl.checkFileReportIfUsed + '?ids=' + ids ,
    method: 'get'
  })
}

//复制方法
export function copy() {
  const clipboard = new Clipboard('.tag-read');
  clipboard.on('success', e => {
    clipboard.destroy();
    ElMessage.success('复制成功');
  })
  clipboard.on('error', e => {
    // 不支持复制
    clipboard.destroy();
    ElMessage.error('该浏览器不支持复制');
  })
}
