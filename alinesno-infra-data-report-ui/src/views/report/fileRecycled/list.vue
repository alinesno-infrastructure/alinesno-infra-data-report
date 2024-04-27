<template>
  <!--
    【上报文件】 功能列表

    @author paul
    @date 2022-11-28 10:28:04
  -->
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px" @submit.native.prevent>
      <el-form-item label="文件名" prop="fileFullName">
        <el-input
          v-model="queryParams.fileFullName"
          placeholder="请输入文件名"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
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

    <el-table v-loading="loading" :data="FileReportList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文件名" align="left" prop="fileFullName" show-overflow-tooltip/>
      <el-table-column label="文件大小" align="left" prop="fileSize" show-overflow-tooltip width="100"/>
<!--      <el-table-column label="业务模型" align="left" prop="modelNameLabel" show-overflow-tooltip/>-->
<!--      <el-table-column label="校验状态" align="left" prop="ifCheck" width="90">-->
<!--        <template #default="scope">-->
<!--          <span>{{ reportStatusTrans(scope.row.ifCheck)   }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="上报状态" align="left" prop="reportStatus" width="80">-->
<!--        <template #default="scope">-->
<!--          <span>{{ statusTrans(scope.row.reportStatus)  }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="上报数据行数" align="left" prop="reportRow" width="100"show-overflow-tooltip/>-->
<!--      <el-table-column label="备注" align="left" prop="remark" show-overflow-tooltip/>-->
<!--      <el-table-column label="上报时间" align="left" prop="reportTime" width="160"/>-->
<!--      <el-table-column label="添加时间" align="center" prop="addTime" width="160">-->
<!--        <template #default="scope">-->
<!--          <span>{{ parseTime(scope.row.addTime) }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="操作" width="100" align="center" class-name="small-padding fixed-width">-->
<!--        <template #default="scope">-->
<!--          <el-button-->
<!--            size="mini"-->
<!--            type="text"-->
<!--            icon="Edit"-->
<!--            @click="handleUpdate(scope.row)"-->
<!--          >还原</el-button>-->
<!--          <el-button-->
<!--            size="mini"-->
<!--            type="text"-->
<!--            icon="Delete"-->
<!--            @click="handleDelete(scope.row)"-->
<!--          >删除</el-button>-->
<!--        </template>-->
<!--      </el-table-column>-->
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改【上报文件】对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">

        <el-form-item label="业务模型" prop="modelId">
          <el-select  v-model="form.modelId" placeholder="请选择业务模型" style="width:680px"  clearable size="small" filterable>
            <el-option
              v-for="item in modelNamelist"
              :key="item.id"
              :label="item.modelName"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件" prop="fileId">
          <el-row>
            <el-col :span="21">
              <el-input v-model="form.storageFileId" disabled placeholder="上传文件" style="width:580px"/>
            </el-col>
            <el-col :span="2">
              <el-button size="small" type="primary"  @click="handleUpFile()">点击上传</el-button>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="文件全名" prop="fileFullName">
          <el-input v-model="form.fileFullName" placeholder="请输入文件全名" :readonly="true"/>
        </el-form-item>
        <el-form-item label="文件大小" prop="fileSize">
          <el-input v-model="form.fileSize" placeholder="请输入文件大小" :readonly="true"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>


  </div>
</template>

<script  setup name="FileRecycled">
import { ref, reactive, onMounted} from 'vue';
import { useRouter, useRoute } from 'vue-router';
const { proxy } = getCurrentInstance();
import {parseTime} from "@/utils/ruoyi";
import {
  listFileReport,
  getFileReport,
  delFileReport,
  addFileReport,
  changeFileReportField,
  changeStatusFileReport,
  updateFileReport,
  exportFileReport,listDelFileReport,reductionFileReport,deleteFileReport } from "@/api/report/FileReport";
import  Condition  from "@/api/search/condition";

import  searchParam  from "@/api/search/searchform";

import { listBusinessModel } from "@/api/report/BusinessModel";

// 弹出层标题
const title = ref("");

// 总条数
const total = ref(0);

// 【上报文件】表格数据
const FileReportList = ref([]);

// 搜索参数
const searchParams = ref([]);

const modelNamelist = ref([]);

const searchParamTem = ref([]);

// 选中数组
const names = ref([]);

// 选中数组
const ids = ref([]);

// 状态
const statusOptions = ref([]);

// 是否显示弹出层
const open = ref(false);

// 遮罩层
const loading = ref(true);

// 非单个禁用
const single = ref(true);

// 非多个禁用
const multiple = ref(true);

// 显示搜索条件
const showSearch = ref(true);

const data = reactive({
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        hasDelete: 1,
        fileName: null,
        fileExtendName: null,
        fileFullName: null,
        fileSize: null,
        ifDir: null,
        storageType: null,
        storageFileId: null,
        storageFilePath: null,
        modelId: null,
        ifCheck: null,
        checkFileId: null,
        checkFileName: null,
        checkFileExtendName: null,
        checkFilePath: null,
        reportStatus: null,
        reportRow: null,
        reportTime: null,
      },

      // 查询参数配置对象
      queryParamsConfig: {
        fileName: Condition.like(),
        fileExtendName: Condition.like(),
        fileFullName: Condition.like(),
        fileSize: Condition.like(),
        ifDir: Condition.like(),
        storageType: Condition.like(),
        storageFileId: Condition.like(),
        storageFilePath: Condition.like(),
        modelId: Condition.like(),
        ifCheck: Condition.like(),
        checkFileId: Condition.like(),
        checkFileName: Condition.like(),
        checkFileExtendName: Condition.like(),
        checkFilePath: Condition.like(),
        reportStatus: Condition.like(),
        reportRow: Condition.like(),
        reportTime: Condition.like(),
        hasDelete: Condition.eq(),
      },

      // 表单参数
      form: {},

      // 表单校验
      rules: {
        fileName: [
          { required: false, message: "文件名称不能为空", trigger: "blur" }
        ],
        fileExtendName: [
          { required: false, message: "文件后缀不能为空", trigger: "blur" }
        ],
        fileFullName: [
          { required: false, message: "文件全名不能为空", trigger: "blur" }
        ],
        fileSize: [
          { required: false, message: "文件大小不能为空", trigger: "blur" }
        ],
        ifDir: [
          { required: true, message: "是否是目录;0-否,1-是不能为空", trigger: "blur" }
        ],
        storageType: [
          { required: true, message: "存储类型，默认是0，minIO不能为空", trigger: "blur" }
        ],
        storageFileId: [
          { required: true, message: "文件id,minIO中的id不能为空", trigger: "blur" }
        ],
        storageFilePath: [
          { required: true, message: "文件路径不能为空", trigger: "blur" }
        ],
        modelId: [
          { required: true, message: "模型id不能为空", trigger: "blur" }
        ],
        ifCheck: [
          { required: true, message: "校验是否通过;0-否,1-是不能为空", trigger: "blur" }
        ],
        checkFileId: [
          { required: true, message: "校验文件id不能为空", trigger: "blur" }
        ],
        checkFileName: [
          { required: true, message: "校验文件名称不能为空", trigger: "blur" }
        ],
        checkFileExtendName: [
          { required: true, message: "校验文件后缀不能为空", trigger: "blur" }
        ],
        checkFilePath: [
          { required: true, message: "校验文件路径不能为空", trigger: "blur" }
        ],
        reportStatus: [
          { required: true, message: "上报状态;0-否,1-是不能为空", trigger: "blur" }
        ],
        reportRow: [
          { required: true, message: "上报数据行数不能为空", trigger: "blur" }
        ],
        reportTime: [
          { required: true, message: "上报时间不能为空", trigger: "blur" }
        ],
        remark: [
          { required: false, message: "备注不能为空", trigger: "blur" }
        ]
      },

      Params: {
        pageNum : 1,
        pageSize: 1000,
        modelName: null,
      },

      ParamsConfig:{
        typeName:Condition.like()
      }
});

const { queryParams, queryParamsConfig, form, rules, Params, ParamsConfig} = toRefs(data);

// 页面加载后触发
onMounted(() => {
  getModelNameList()
  getList();
})



//增加业务模型父类下拉框功能
function  getModelNameList() {
  searchParamTem.value = searchParam(ParamsConfig.value, Params.value);
  listBusinessModel(searchParamTem.value).then(response => {
    modelNamelist.value = response.rows;
  });
}

/** 查询【上报文件】列表 */
function  getList() {
    searchParams.value = searchParam(queryParamsConfig.value, queryParams.value);
    loading.value = true;
    listDelFileReport(searchParams.value).then(response => {
      FileReportList.value = response.rows;
      total.value = response.total;
      loading.value = false;
    });
}

// 取消按钮
function  cancel() {
  open.value = false;
  reset();
}

// 表单重置
function  reset() {
  form.value = {
    fileName: null,
    fileExtendName: null,
    fileFullName: null,
    fileSize: null,
    ifDir: null,
    storageType: null,
    storageFileId: null,
    storageFilePath: null,
    modelId: null,
    ifCheck: null,
    checkFileId: null,
    checkFileName: null,
    checkFileExtendName: null,
    checkFilePath: null,
    reportStatus: null,
    reportRow: null,
    reportTime: null,
    remark: null
  };
  proxy.resetForm("formRef");
}

/** 搜索按钮操作 */
function  handleQuery() {
  // 获取参数
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function  resetQuery() {
  proxy.resetForm("queryForm");
  handleQuery();
}

// 多选框选中数据
function  handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  names.value = selection.map(item => item.fileFullName);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function  handleAdd() {
  reset();
  open.value = true;
  title.value = "添加【上报文件】";
}

/** 还原按钮操作 */
function  handleUpdate(row) {
  reset();
  reductionFileReport(row).then(() => {
    getList();
    proxy.$modal.msgSuccess("还原成功");
  });
}

/** 提交按钮 */
function  submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateFileReport(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addFileReport(form.value).then(response => {
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
  const fileNames = row.id || ids.value;
  let fileNameList = row.fileFullName || names.value;
  //避免弹出窗数据太长，只显示前15条数据
  if ( fileNameList.length > 15 ) {
    fileNameList = fileNameList.slice(0,15);
  }

  proxy.$confirm('是否确认删除【上报文件】文件名为"' + fileNameList + '"的数据项?', "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }).then(function() {
      return deleteFileReport(fileNames);
    }).then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    }).catch(error => {

    })
}

/** 状态修改 **/
function  handleStatusChange(row) {
  return changeStatusFileReport(row.id, row.status).then(response=>{
    if( response.code == 200 ){
      proxy.$modal.msgSuccess("操作成功");
    }
  });
}

/** 修改字段状态 **/
function  chanageFile(value , filed , id){
  return changeFileReportField(value , filed , id).then(response =>{
    if( response.code == 200 ){
      proxy.$modal.msgSuccess("操作成功");
    }
  }) ;
}

/** 导出按钮操作 */
function  handleExport() {
  const queryParams = queryParams.value;
  proxy.$confirm('是否确认导出所有【上报文件】数据项?', "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }).then(function() {
      return exportFileReport(queryParams);
    }).then(response => {
      download(response.msg);
    })
}


function  statusTrans(value) {
  if (value === null || value === '') {
    return '';
  }
  switch (value) {
    case 0:
      return "待上报";
    case 1:
      return "已上报";
    case 2:
      return "上报异常";
    case 3:
      return "发送中";
    default:
      return "未上报";
  }
}

function  reportStatusTrans(value) {
  if (value === null || value === '') {
    return '';
  }
  switch (value) {
    case 0:
      return "校验不通过";
    case 1:
      return "校验通过";
    case 2:
      return "未校验";
    default:
      return '';
  }
}


</script>
