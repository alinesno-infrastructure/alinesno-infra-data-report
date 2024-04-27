import request from '@/utils/request';

/**
 * 【文件分享】 接口文件
 *
 * @author paul
 * @date 2024年3月10日
 */

// 接口配置项
var prefix = '/api/infra/data/report/FileShare/' ;
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
    saveShare: prefix + "saveShare",
    shareInfo: prefix + "shareInfo",
    datatableNew : prefix +"datatableNew" ,

}

// 查询【数据分享，包含用户过滤条件】列表
export function listFileShare(query , data) {
  return request({
    url: managerUrl.datatables ,
    method: 'post',
    params: query ,
    data: data
  })
}

// 查询【数据分享，不包含用户过滤条件】列表
export function listFileShareNew(query , data) {
  return request({
    url: managerUrl.datatableNew ,
    method: 'post',
    params: query ,
    data: data
  })
}

// 查询【请填写功能名称】详细
export function getFileShare(id) {
  return request({
    url: managerUrl.detailUrl + '/' + id ,
    method: 'get'
  })
}

// 新增【请填写功能名称】
export function addFileShare(data) {
  return request({
    url: managerUrl.saveUrl,
    method: 'post',
    data: data
  })
}

// 修改【请填写功能名称】
export function updateFileShare(data) {
  return request({
    url: managerUrl.updateUrl,
    method: 'put',
    data: data
  })
}

// 删除【请填写功能名称】
export function delFileShare(id) {
  return request({
    url: managerUrl.removeUrl + '/' + id ,
    method: 'delete'
  })
}

// 导出【请填写功能名称】
export function exportFileShare(query) {
  return request({
    url: managerUrl.exportUrl,
    method: 'get',
    params: query
  })
}

// 状态【请填写功能名称】修改
export function changeStatusFileShare(id , status) {
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
export function changeFileShareField(value , field , id){
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


// 新增【请填写功能名称】
export function addFileShareCustom(data) {
  return request({
    url: managerUrl.saveShare,
    method: 'post',
    data: data
  })
}

// 新增【通过分享批次号获取分享链接】
export function getShareLink(shareBatchNum) {
  return `${location.protocol}//${location.host}/share/${shareBatchNum}`
}

// 查询【通过分享批次号获取分享详情】详细
export function getShareInfo(shareBatchNum) {
  return request({
    url: managerUrl.shareInfo+"?shareBatchNum="+ shareBatchNum,
    method: 'get'
  })
}
