import request from '@/utils/request';

/**
 * 【异常消息】 接口文件
 *
 * @author paul
 * @date 2024年3月10日
 */

// 接口配置项
var prefix = '/api/infra/data/report/MqMessageFail/' ;
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
    retry: prefix + "retrySend"
}

// 查询【异常消息】列表
export function listMqMessage(query , data) {
  return request({
    url: managerUrl.datatables ,
    method: 'post',
    params: query ,
    data: data
  })
}

// 查询【异常消息】详细
export function getMqMessage(id) {
  return request({
    url: managerUrl.detailUrl + '/' + id ,
    method: 'get'
  })
}

// 新增【异常消息】
export function addMqMessage(data) {
  return request({
    url: managerUrl.saveUrl,
    method: 'post',
    data: data
  })
}

// 修改【异常消息】
export function updateMqMessage(data) {
  return request({
    url: managerUrl.updateUrl,
    method: 'put',
    data: data
  })
}

// 删除【异常消息】
export function delMqMessage(id) {
  return request({
    url: managerUrl.removeUrl + '/' + id ,
    method: 'delete'
  })
}

// 导出【异常消息】
export function exportMqMessage(query) {
  return request({
    url: managerUrl.exportUrl,
    method: 'get',
    params: query
  })
}

// 状态【异常消息】修改
export function changeStatusMqMessage(id , status) {
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

// 修改【异常消息】单个字段值
export function changeMqMessageField(value , field , id){
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


//重发【异常消息】
export function retrySend(data) {
  return request({
    url: managerUrl.retry ,
    method: 'post',
    data: data
  })
}
