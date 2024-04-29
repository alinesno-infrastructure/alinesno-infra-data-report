<template>
  <!--
    历史异常 功能列表

    @author paul
    @date 2022-03-04 15:33:44
  -->
  <div class="app-container">
    <el-row v-show="showSearch">
      <el-col :span="24">
        <el-radio-group v-model="selectedTimeRange" @change="selectedTimeRangeChange" size="small">
          <el-radio-button :label="1">近1个月</el-radio-button>
          <el-radio-button :label="2">近2个月</el-radio-button>
          <el-radio-button :label="3">近3个月</el-radio-button>
          <el-radio-button :label="6">近6个月</el-radio-button>
          <el-radio-button :label="12">近12个月</el-radio-button>
        </el-radio-group>

        <el-date-picker
          style="margin-left: 30px"
          v-model="selectedTime"
          type="datetimerange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :picker-options="pickerOptions"
          @change="onPickerChange"
          :default-time="['00:00:00', '23:59:59']" size="small" >
        </el-date-picker>
      </el-col>
    </el-row>
    <br/>

    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="业务模型" prop="modelId">
        <el-select  v-model="queryParams.modelId" placeholder="请选择业务模型"  clearable size="small" filterable  @keyup.enter.native="handleQuery">
          <el-option
            v-for="item in businessModelList"
            :key="item.id"
            :label="item.modelName"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="上报文件" prop="fileReportId">
        <el-select  v-model="queryParams.fileReportId" placeholder="请选择上报文件名称"  clearable size="small" filterable  @keyup.enter.native="handleQuery">
          <el-option
            v-for="item in fileReportList"
            :key="item.id"
            :label="item.fileName"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>


      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status" placeholder="请选择状态" clearable size="small"  filterable @keyup.enter.native="handleQuery">
          <el-option
            v-for="item in hasStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>


      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="MqMessageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="上报文件" align="left"  prop="fileReportNameLabel" show-overflow-tooltip />
      <el-table-column label="业务模型" align="left" prop="modelNameLabel" />
      <el-table-column label="消息主题" align="left" prop="topic" />
      <el-table-column label="异常信息" align="left" prop="errorMsg" show-overflow-tooltip/>
      <el-table-column label="状态" align="left" prop="status"  width="90" >
        <template #default="scope">
          <el-tag v-if="scope.row.status==1" type="info">预发送</el-tag>
          <el-tag v-else-if="scope.row.status=='2'" type="warning">可发送</el-tag>
          <el-tag v-else-if="scope.row.status=='3'" type="danger">发送中</el-tag>
          <el-tag v-else-if="scope.row.status=='4'" type="success">已发送</el-tag>
          <el-tag v-else-if="scope.row.status=='5'" type="success">完成发送</el-tag>
          <el-tag v-else-if="scope.row.status=='6'" type="warning">超时</el-tag>
          <el-tag v-else-if="scope.row.status=='7'" type="success">已接收</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="重试次数" align="left" prop="retryCount" width="90"  />
      <el-table-column label="创建时间" align="center" prop="addTime"  width="170">
        <template #default="scope">
          <span>{{ parseDatetime(scope.row.addTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="mini"
            type="text"
            icon="Edit"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-document"
            @click="showContent(scope.row)"
          >内容</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-refresh"
            v-show="scope.row.status==6"
            @click="handleRetry(scope.row)"
          >重发</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />


    <el-dialog title="消息内容" v-model="contentVisible"  width="1280px">
      <el-form :model="jsonData" ref="jsonDataRef"  label-width="68px">
        <el-form-item label="内容" prop="messageContent">
          <el-input
            v-model="jsonData.messageContent"
            placeholder="请输入文件名"
            type="textarea"
            autosize
            size="small"
          />
        </el-form-item>
        <el-form-item label="格式化" prop="messageContentFormat">
          <el-input
            v-model="jsonData.messageContentFormat"
            placeholder="请输入文件名"
            type="textarea"
            autosize
            size="small"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="contentVisible = false">关 闭</el-button>
      </span>
    </el-dialog>


  </div>
</template>

<script  setup name="MessageFail">
import { ref, reactive, onMounted} from 'vue';
import { useRouter, useRoute } from 'vue-router';
const { proxy } = getCurrentInstance();

import {
  listMqMessage,
  getMqMessage,
  delMqMessage,
  addMqMessage,
  changeMqMessageField,
  changeStatusMqMessage,
  updateMqMessage,
  exportMqMessage,retrySend } from "@/api/report/MessageFail";
import  Condition  from "@/api/search/condition";
import  searchParam  from "@/api/search/searchform";
import {listBusinessModel } from "@/api/report/BusinessModel";
import {listFileReport } from "@/api/report/FileReport";
import {parseTime} from "@/utils/ruoyi";

// 弹出层标题
const title = ref("");

const selectedTimeRange = ref(1);

// 总条数
const total = ref(0);

const startDate = ref(null);
const endDate = ref(null);

// 是否显示弹出层
const open = ref(false);

const isEdit = ref(false);

// 消息内容可见性
const contentVisible = ref(false);

// 显示搜索条件
const showSearch = ref(true);

// 遮罩层
const loading = ref(true);

// 非单个禁用
const single = ref(true);

// 非多个禁用
const multiple = ref(true);

// 选中数组
const ids  = ref([]);

// 状态
const statusOptions  = ref([]);

const selectedTime  = ref([]);

const hasStatusOptions  = ref([
  {
    value: '2'
    , label: '可发送'
  },
  {
    value: '3'
    , label: '发送中'
  },
  {
    value: '4'
    , label: '已发送'
  },
  {
    value: '5'
    , label: '完成发送'
  },
  {
    value: '6'
    , label: '超时'
  },
  {
    value: '7'
    , label: '已接收'
  }]
);

// 发送kafka异常消息表格数据
const MqMessageList  = ref([]);

// 搜索参数
const searchParams  = ref([]);

const businessModelList  = ref([]);

const fileReportList  = ref([]);

const searchParamTem  = ref([]);

const data = reactive({
      // 日期时间选择器选项
      pickerOptions: {
        disabledDate: (time) => {
          return time.getTime() - 8.64e7 > Date.now();
        }
      },

      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        fileReportId: null,
        modelId: null,
        topic: null,
        data: null,
        status: null,
        retryCount: null,
        errorMsg: null,
        addTime: []
      },

      // 查询参数配置对象
      queryParamsConfig: {
        fileReportId: Condition.eq(),
        modelId: Condition.eq(),
        topic: Condition.like(),
        data: Condition.like(),
        status: Condition.eq(),
        retryCount: Condition.eq(),
        errorMsg: Condition.like(),
        addTime:Condition.rangeDate()
      },

      // 表单参数
      form: {
        shardingId: -1
      },

      Params: {
        pageNum : 1,
        pageSize: 1000,
        modelId: null,
      },

      ParamsConfig:{
        modelId:Condition.like()
      },

      // 消息内容
      jsonData:{}
});

const { pickerOptions, queryParams, queryParamsConfig, form, Params, ParamsConfig, jsonData } = toRefs(data);

// 页面加载后触发
onMounted(() => {
  selectedTimeRangeChange();

  getList();

  // 查询业务模型
  getBusinessModelList();

  // 查询上报文件
  getFileReportList();

})

function  getBusinessModelList(){
    searchParamTem.value = searchParam(ParamsConfig.value, Params.value);
    listBusinessModel(searchParamTem.value).then(res => {
      businessModelList.value = res.rows ;
    }) ;
}

function  getFileReportList(){
  searchParamTem.value = searchParam(ParamsConfig.value, Params.value);
  listFileReport(searchParamTem.value).then(res => {
    fileReportList.value = res.rows ;
  }) ;
}

/** 查询队列消息列表 */
function  getList() {
  // // 判断是否搜索按钮触发
  // var startDateTmp = null;
  // var endDateTmp = null;
  //
  // if ( startDate.value ) {
  //   startDateTmp = startDate.value;
  //   endDateTmp = endDate.value;
  // } else {
  //   startDateTmp = selectedTime[0].value;
  //   endDateTmp = selectedTime[1].value;
  // }
  //
  //
  //
  // //时间过滤
  // queryParams.value.addTime[0] = parseTime(startDateTmp);
  // queryParams.value.addTime[1] = parseTime(endDateTmp);
  //
  // searchParams.value = searchParam(queryParamsConfig.value, queryParams.value);
  loading.value = true;
  debugger
  // listMqMessage(searchParams.value).then(response => {
    listMqMessage(queryParams.value).then(response => {
    MqMessageList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 显示消息内容
function  showContent(row){
  contentVisible.value = true ;
  resetJsonData();
  jsonData.value.messageContent       = row.data ;
  jsonData.value.messageContentFormat = JSON.stringify(JSON.parse(row.data), null, '\t')  ;
}

// 取消按钮
function  cancel() {
  open.value = false;
  reset();
}

// 表单重置
function  reset() {
  form.value = {
    topic: null,
    mqGroup: null,
    data: null,
    status: null,
    retryCount: null,
    shardingId: null,
    timeout: null,
    effectTime: null,
    log: null
  };
  proxy.resetForm("formRef");
}

function  resetJsonData(){
  jsonData.value = {
    messageContent: null,
    messageContentFormat: null
  };
  proxy.resetForm("jsonDataRef");
}

/** 搜索按钮操作 */
function  handleQuery() {
  // 获取参数
  queryParams.value.pageNum = 1;
  getList();
}

function  selectedTimeRangeChange() {
  // 重置时间选择器
  selectedTime.value = [];

  endDate.value = Date.now();
  switch (selectedTimeRange.value) {
    case 1:
      // this.startDate = Date.now() - 2.592e9;
      startDate.value = new Date().setMonth((new Date().getMonth()-1))
      break;
    case 2:
      // this.startDate = Date.now() - 5.184e9;
      startDate.value = new Date().setMonth((new Date().getMonth()-2))
      break;
    case 3:
      // this.startDate = Date.now() - 7.776e9;
      startDate.value = new Date().setMonth((new Date().getMonth()-3))
      break;
    case 6:
      // this.startDate = Date.now() - 1.5552e10;
      startDate.value = new Date().setMonth((new Date().getMonth()-6))
      break;
    case 12:
      // this.startDate = Date.now() - 3.1104e10;
      startDate.value = new Date().setMonth((new Date().getMonth()-12))
      break;
  }
  getList();
}

function  selectedTimeRangeChangeNotQuery() {
  // 重置时间选择器
  selectedTime.value = [];

  var endDate = Date.now();
  switch (selectedTimeRange.value) {
    case 1:
      // this.startDate = Date.now() - 2.592e9;
      startDate.value = new Date().setMonth((new Date().getMonth()-1))
      break;
    case 2:
      // this.startDate = Date.now() - 5.184e9;
      startDate.value = new Date().setMonth((new Date().getMonth()-2))
      break;
    case 3:
      // this.startDate = Date.now() - 7.776e9;
      startDate.value = new Date().setMonth((new Date().getMonth()-3))
      break;
    case 6:
      // this.startDate = Date.now() - 1.5552e10;
      startDate.value = new Date().setMonth((new Date().getMonth()-6))
      break;
    case 12:
      // this.startDate = Date.now() - 3.1104e10;
      startDate.value = new Date().setMonth((new Date().getMonth()-12))
      break;
  }
}

// 确认选择时间选择器的回调事件
function  onPickerChange() {
  // 重置选择时间
  selectedTimeRange.value = 0;
  startDate.value = null;
}

/** 重置按钮操作 */
function  resetQuery() {
  proxy.resetForm("queryForm");
  selectedTimeRange.value = 1 ;
  selectedTimeRangeChangeNotQuery();
  handleQuery();
}

// 多选框选中数据
function  handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  names.value = selection.map(item => item.topic);
  addTimes.value = selection.map(item => item.addTime);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function  handleAdd() {
  isEdit.value = false;
  reset();
  open.value = true;
  title.value = "添加异常消息";
}

/** 修改按钮操作 */
function  handleUpdate(row) {
  isEdit.value = true;
  reset();
  const topic = row.id || ids.value
  getMqMessage(topic).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改异常消息";
  });
}

/** 提交按钮 */
function  submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateMqMessage(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addMqMessage(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function  handleDelete(row) {
  const id = row.id || ids.value;
  proxy.$confirm('是否确认删除选中的异常消息?', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(function() {
    return delMqMessage(id);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(error => {

  })
}

/** 状态修改 **/
function  handleStatusChange(row) {
  return changeStatusMqMessage(row.id, row.status).then(response=>{
    if(response.code == 200){
      proxy.$modal.msgSuccess("操作成功");
    }
  });
}

/** 修改字段状态 **/
function  chanageFile(value , filed , id){
  return changeMqMessageField(value , filed , id).then(response =>{
    if( response.code == 200 ){
      proxy.$modal.msgSuccess("操作成功");
    }
  }) ;
}

/** 导出按钮操作 */
function  handleExport() {
  const queryParams = queryParams.value;
  proxy.$confirm('是否确认导出所有队列消息数据项?', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(function() {
    return exportMqMessage(queryParams);
  }).then(response => {
    download(response.msg);
  })
}

/** 重发按钮操作 */
function  handleRetry(row) {
  debugger
  var sendObject = {};
  sendObject.id = row.id ;
  sendObject.fileReportId = row.fileReportId ;
  sendObject.modelId = row.modelId ;
  sendObject.topic = row.topic ;
  sendObject.data = row.data ;
  row.status = "3" ;
  retrySend(sendObject).then(response => {
    if ( response.code != 200 ) {
      proxy.$modal.msgError("重发失败！")
    }else {
      getList();
    }
  });
}

function  jsonFormat(){
  // messageContent.value = JSON.stringify(JSON.parse(messageContent.value), null, '\t')
}


</script>
<style scoped lang="scss">
::v-deep .el-table .cell{
  text-align: center;
}
</style>
