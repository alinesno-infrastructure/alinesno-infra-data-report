<template>
  <!--
    保存消息 功能列表

 * @author paul
 * @date 2022-05-09 09:33:44
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
          :default-time="['00:00:00', '23:59:59']" size="small">
        </el-date-picker>
      </el-col>
    </el-row>
    <br/>

    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="业务模型" prop="modelId">
        <el-select  v-model="queryParams.modelId" placeholder="请选择业务模型"  clearable size="small" filterable  @keyup.enter.native="handleQuery">
          <el-option
            v-for="item in modelList"
            :key="item.id"
            :label="item.modelName"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="上报文件" prop="fileId">
        <el-select  v-model="queryParams.fileId" placeholder="请选择上报文件"  clearable size="small" filterable  @keyup.enter.native="handleQuery">
          <el-option
            v-for="item in fileList"
            :key="item.id"
            :label="item.fileName"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="上报状态" prop="reportStatus">
        <el-select  v-model="queryParams.reportStatus" placeholder="请选择上报状态"  clearable size="small" filterable  @keyup.enter.native="handleQuery">
          <el-option
            v-for="item in reportStatusOptions"
            :key="item.key"
            :label="item.label"
            :value="item.key">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="目标库" prop="messageType">
        <el-select  v-model="queryParams.messageType" placeholder="请选择目标库"  clearable size="small" filterable  @keyup.enter.native="handleQuery">
          <el-option
            v-for="item in messageTypeOptions"
            :key="item.key"
            :label="item.label"
            :value="item.key">
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
      <el-table-column label="业务模型" align="left" width="130" prop="modelNameLabel" show-overflow-tooltip />
      <el-table-column label="消息主题" align="left" width="130" prop="kafkaTopiceLabel" show-overflow-tooltip />
      <el-table-column label="上报文件" align="left" prop="fileNameLabel" show-overflow-tooltip />
      <el-table-column label="目标库" align="left" prop="messageTypeLabel" show-overflow-tooltip />
      <el-table-column label="序号" align="left" prop="fileIndex" show-overflow-tooltip />
<!--      <el-table-column label="消息内容" align="left" prop="data">-->
<!--        <template #default="scope">-->
<!--          <el-button-->
<!--            type="primary"-->
<!--            plain-->
<!--            icon="el-icon-s-promotion"-->
<!--            size="mini"-->
<!--            @click="showContent(scope.row)">查看内容</el-button>-->
<!--        </template>-->
<!--      </el-table-column>-->
      <el-table-column label="上报时间" align="center" prop="addTime" width="170">
        <template #default="scope">
          <span>{{ parseDatetime(scope.row.addTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上报状态" align="left" prop="reportStatusLabel" show-overflow-tooltip width="80" />
      <el-table-column label="已发总线" align="left" prop="ifSendBusLabel" show-overflow-tooltip width="80" />
      <el-table-column label="操作" width="100" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-document"
            @click="showContent(scope.row)"
          >内容</el-button>
          <el-button
            size="mini"
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
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

    <!-- 添加或修改消息对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="width: 70%;">

        <el-form-item label="业务模型" prop="modelId" >
          <el-select v-model="form.modelId" placeholder="请选择业务模型"   clearable>
            <el-option
              v-for="item in modelList"
              :key="item.id"
              :label="item.modelName"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="上报文件" prop="fileId" >
          <el-select v-model="form.fileId" placeholder="请选择上报文件"   clearable>
            <el-option
              v-for="item in fileList"
              :key="item.id"
              :label="item.fileName"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="序号" prop="fileIndex" >
          <el-input v-model="form.fileIndex" placeholder="请输入序号" />
        </el-form-item>

        <el-form-item label="消息内容" prop="data">
          <el-input v-model="form.data" type="textarea" placeholder="请输入内容" />
        </el-form-item>

        <el-form-item label="消息状态" prop="reportStatus">
          <el-select v-model="form.reportStatus" placeholder="请选择消息状态" label="消息状态" clearable>
            <el-option
              v-for="item in reportStatusOptions"
              :key="item.key"
              :label="item.label"
              :value="item.key">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="重试次数" prop="retryCount">
          <el-input-number v-model="form.retryCount" :min="1" :max="100" ></el-input-number>
        </el-form-item>


        <el-form-item label="超时时间" prop="timeout">
          <el-input-number v-model="form.timeout" :min="1" :max="100000" ></el-input-number>
        </el-form-item>


        <el-form-item label="异常消息" prop="errorMsg">
          <el-input v-model="form.errorMsg" type="textarea" placeholder="请输入异常消息" />
        </el-form-item>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="消息内容" v-model="contentVisible"  width="1280px">
      <el-form :model="jsonData" ref="jsonData"  label-width="68px">
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

<script  setup name="MqMessage">
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
  exportMqMessage} from "@/api/report/MessageHis";
import  Condition  from "@/api/search/condition";
import  searchParam  from "@/api/search/searchform";
import {listBusinessModel } from "@/api/report/BusinessModel";
import {listFileReport } from "@/api/report/FileReport";
import {parseTime} from "@/utils/ruoyi";

// 总条数
const total = ref(0);

const selectedTimeRange = ref(1);

// 弹出层标题
const title = ref("");

// 消息内容
const messageContent = ref("");

// 遮罩层
const loading = ref(true);

// 非单个禁用
const single = ref(true);

// 非多个禁用
const multiple = ref(true);

// 显示搜索条件
const showSearch = ref(true);

// 消息内容可见性
const contentVisible = ref(false);

// 是否显示弹出层
const open = ref(false);

const isEdit = ref(false);

// 队列消息表格数据
const MqMessageList  = ref([]);

// 搜索参数
const searchParams  = ref([]);

// 选中数组
const ids  = ref([]);

// 状态
const statusOptions  = ref([]);

const selectedTime  = ref([]);

const modelList  = ref([]);

const fileList  = ref([]);

const searchParamTem  = ref([]);

const messageTypeOptions  = ref(
[
        {
        key: 0, label: '数据总线'
        },
        {
        key: 1, label: '数据上报'
        }
      ]
);

const reportStatusOptions  = ref(
[
        {
        key: 0, label: '待上报'
        },
        {
        key: 1, label: '上报成功'
        },
        {
        key: 2, label: '上报异常'
        },
        {
        key: 3, label: '发送中'
        }
      ]
);

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
        modelId: null,
        fileId: null,
        fileIndex: null,
        data: null,
        reportStatus: null,
        retryCount: null,
        timeout: null,
        errorMsg: null,
        messageType: null,
        addTime: []
      },

      // 查询参数配置对象
      queryParamsConfig: {
        modelId: Condition.eq(),
        fileId: Condition.eq(),
        fileIndex: Condition.like(),
        data: Condition.like(),
        reportStatus: Condition.eq(),
        retryCount: Condition.like(),
        timeout: Condition.like(),
        errorMsg: Condition.like(),
        messageType:Condition.eq(),
        addTime:Condition.rangeDate()
      },

      // 表单参数
      form: {},

      // 表单校验
      rules: {
        modelId: [
          { required: true, message: "业务模型不能为空", trigger: "blur" },
        ],
        fileId: [
          { required: true, message: "上报文件不能为空", trigger: "blur" }
        ],
        fileIndex: [
          { required: true, message: "上报序号不能为空", trigger: "blur" }
        ],
        data: [
          { required: true, message: "消息内容不能为空", trigger: "blur" }
        ],
        reportStatus: [
          { required: true, message: "状态不能为空", trigger: "blur" }
        ],
        retryCount: [
          { required: false, message: "重试次数不能为空", trigger: "blur" }
        ],
        timeout: [
          { required: false, message: "超时时间不能为空", trigger: "blur" }
        ],
        errorMsg: [
          { required: false, message: "异常消息不能为空", trigger: "blur" }
        ]
      },

      Params: {
        pageNum : 1,
        pageSize: 1000,
        topic: null,
      },

      ParamsConfig:{
        topic:Condition.like()
      },

      // 消息内容
      jsonData:{}
});

const { pickerOptions, queryParams, queryParamsConfig, form, rules, Params, ParamsConfig, jsonData } = toRefs(data);

// 页面加载后触发
onMounted(() => {

  //查询业务模型
  getModelList();

  //查询业务模型
  getFileList();

  selectedTimeRangeChange();

  getList();

})


function  getModelList(){
  searchParamTem.value = searchParam(ParamsConfig.value, Params.value);
  listBusinessModel(searchParamTem.value).then(res => {
    modelList.value = res.rows ;
  }) ;
}

function  getFileList(){
  searchParamTem.value = searchParam(ParamsConfig.value, Params.value);
  listFileReport(searchParamTem.value).then(res => {
    fileList.value = res.rows ;
  }) ;
}

/** 查询队列消息列表 */
function  getList() {
  // 判断是否搜索按钮触发
   var startDate = null;
   var endDate = null;

   // if (startDate.value) {
   //   startDate = startDate.value;
   //   endDate = endDate.value;
   // } else {
   //   startDate = selectedTime[0].value;
   //   endDate = selectedTime[1].value;
   // }

  startDate = selectedTime[0].value;
  endDate = selectedTime[1].value;

  //处理时间过滤
  queryParams.value.addTime[0] = parseTime(startDate);
  queryParams.value.addTime[1] = parseTime(endDate);


  searchParams.value = searchParam(queryParamsConfig.value, queryParams.value);
  loading.value = true;
  listMqMessage(searchParams.value).then(response => {
    MqMessageList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function  resetJsonData(){
  jsonData.value = {
    messageContent: null,
    messageContentFormat: null
  };
  proxy.resetForm("jsonData");
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
    reportStatus: null,
    retryCount: null,
    shardingId: null,
    timeout: null,
    effectTime: null,
    log: null
  };
  proxy.resetForm("formRef");
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
      startDate.value = new Date().setMonth((new Date().getMonth()-1))
      break;
    case 2:
      startDate.value = new Date().setMonth((new Date().getMonth()-2))
      break;
    case 3:
      startDate.value = new Date().setMonth((new Date().getMonth()-3))
      break;
    case 6:
      startDate.value = new Date().setMonth((new Date().getMonth()-6))
      break;
    case 12:
      startDate.value = new Date().setMonth((new Date().getMonth()-12))
      break;
  }
  getList();
}

function  selectedTimeRangeChangeNotQuery() {
  // 重置时间选择器
  selectedTime.value = [];

  endDate.value = Date.now();
  switch (selectedTimeRange.value) {
    case 1:
      startDate.value = new Date().setMonth((new Date().getMonth()-1))
      break;
    case 2:
      startDate.value = new Date().setMonth((new Date().getMonth()-2))
      break;
    case 3:
      startDate.value = new Date().setMonth((new Date().getMonth()-3))
      break;
    case 6:
      startDate.value = new Date().setMonth((new Date().getMonth()-6))
      break;
    case 12:
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
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function  handleAdd() {
  isEdit.value = false;
  reset();
  open.value = true;
  title.value = "添加队列消息";
}

/** 修改按钮操作 */
function  handleUpdate(row) {
  isEdit.value = true;
  reset();
  const topic = row.id || ids.value
  getMqMessage(topic).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改队列消息";
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
  proxy.$confirm('是否确认删除选中的消息?', "警告", {
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
    if(response.code == 200){
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

</script>
<style scoped lang="scss">
::v-deep .el-table .cell{
  text-align: center;
}
</style>
