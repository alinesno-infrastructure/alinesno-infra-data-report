import request from '@/utils/request';

/**
 * 【数据模型】 接口文件
 *
 * @author paul
 * @date 2024年3月10日
 */

// 接口配置项
var prefix = '/api/infra/data/report/BusinessModel/' ;
var managerUrl = {
    datatables : prefix +"datatables" ,
    ModelNameList : prefix +"getModelNameList" ,
    createUrl: prefix + 'add' ,
    saveUrl: prefix + 'save' ,
    updateUrl: prefix +"modify" ,
    statusUrl: prefix +"changeStatus" ,
    cleanUrl: prefix + "clean",
    detailUrl: prefix +"detail",
    removeUrl: prefix + "delete" ,
    exportUrl: prefix + "exportExcel",
    changeField: prefix + "changeField",
    downloadfile: prefix + "downloadfile",
    downloadTemplate: prefix + "downloadTemplate",
    changeModelInfo: prefix + "changeModelInfo",
    treeNavInfo: prefix + "listTreeNavInfo",
    checkBusinessModelIfExist: prefix + "checkBusinessModelIfExist",
    checkBusinessModelIfUsed: prefix + "checkBusinessModelIfUsed",
    checkMinioStatus: prefix + "checkMinioStatus"
}

// 查询【请填写功能名称】列表
export function listBusinessModel(query , data) {
  return request({
    url: managerUrl.datatables ,
    method: 'post',
    params: query ,
    data: data
  })
}

// 查询【请填写功能名称】列表
export function listModelName(query , data) {
  return request({
    url: managerUrl.ModelNameList ,
    method: 'post',
    params: query ,
    data: data
  })
}

// 查询【请填写功能名称】列表
export function listBusinessModel(query , data) {
  return request({
    url: managerUrl.datatables ,
    method: 'post',
    params: query ,
    data: data
  })
}

// 查询【请填写功能名称】详细
export function getBusinessModel(id) {
  return request({
    url: managerUrl.detailUrl + '/' + id ,
    method: 'get'
  })
}

// 新增【请填写功能名称】
export function addBusinessModel(data) {
  return request({
    url: managerUrl.saveUrl,
    method: 'post',
    data: data
  })
}

// 修改【请填写功能名称】
export function updateBusinessModel(data) {
  return request({
    url: managerUrl.updateUrl,
    method: 'put',
    data: data
  })
}

// 删除【请填写功能名称】
export function delBusinessModel(id) {
  return request({
    url: managerUrl.removeUrl + '/' + id ,
    method: 'delete'
  })
}

// 导出【请填写功能名称】
export function exportBusinessModel(query) {
  return request({
    url: managerUrl.exportUrl,
    method: 'get',
    params: query
  })
}

// 状态【请填写功能名称】修改
export function changeStatusBusinessModel(id , status) {
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
export function changeBusinessModelField(value , field , id){
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


// 下载模板
export function downloadfile(storageFilePath,fileName) {
  return request({
    url: managerUrl.downloadfile+"/?storageFilePath="+storageFilePath+"&fileName="+fileName,
    method: 'get'
  })
}

// 下载数据模型_样例
export function downloadTemplate(fileName) {
  return request({
    url: managerUrl.downloadTemplate+"?fileName="+fileName,
    method: 'get',
    responseType: 'blob' // 指定响应数据类型为二进制流（blob）
  })
}


// 上传导入模板后，补充业务模型的导入模板西悉尼
export function changeModelInfo(modelInfo){
  return request({
    url: managerUrl.changeModelInfo ,
    method: 'post',
    data: modelInfo
  })
}


//查询树形导航条当前节点和下级节点及符合查询条件的数据
export function listTreeNavInfo(ids,pageNum,pageSize, data){
  return request({
    url: managerUrl.treeNavInfo+"?ids=" +ids+"&pageNum="+pageNum+"&pageSize="+pageSize,
    method: 'post',
    params: data,
    data: data
  })
}

//检查同一个用户下，业务模型名称是否存在，如已存在，则不允许保存。确同一个用户下业务模型名称唯一
export function checkBusinessModelIfExist(data) {
  return request({
    url: managerUrl.checkBusinessModelIfExist,
    method: 'post',
    data: data
  })
}


//检查业务模型是否已被上报文件引用，如被引用，则不能删除
export function checkBusinessModelIfUsed(ids) {
  return request({
    url: managerUrl.checkBusinessModelIfUsed + '?ids=' + ids ,
    method: 'get'
  })
}

//检查minIO服务器是否正常
export function checkMinioStatus() {
  return request({
    url: managerUrl.checkMinioStatus ,
    method: 'get'
  })
}
