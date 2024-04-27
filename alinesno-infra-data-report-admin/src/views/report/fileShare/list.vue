<template>
  <!--
    【数据分享】 功能列表

    @author paul
    @date 2022-11-28 10:28:04
  -->
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px" @submit.native.prevent>

      <el-form-item label="文件名" prop="shareFileFullName">
        <el-input
          v-model="queryParams.shareFileFullName"
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
          type="success"
          plain
          icon="Edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
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

    <el-table v-loading="loading" :data="FileShareList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="分享链接" align="left" prop="shareBatchNum"  :show-overflow-tooltip="true">
        <template #default="scope">
          <span>{{ ShareLink(scope.row.shareBatchNum)}}</span>
        </template>
      </el-table-column>
      <el-table-column label="文件名" align="left" prop="shareFileFullName" :show-overflow-tooltip="true"  />
      <el-table-column label="失效时间" align="left" prop="endTime"  width="100">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="添加时间" align="center" prop="addTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.addTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="mini" type="text" icon="el-icon-document-copy" class="tag-read"
            :data-clipboard-text= "'分享链接: ' + ShareLink(scope.row.shareBatchNum) "
            v-if="scope.row.ifCode==0"
            @click="copyF"
          >复制链接</el-button>
          <el-button
            size="mini" type="text" icon="el-icon-document-copy" class="tag-read"
            :data-clipboard-text= "'分享链接: ' + ShareLink(scope.row.shareBatchNum) + ' 提取码: ' + scope.row.extractionCode"
            @click="copyF"
            v-if="scope.row.ifCode==1"
          >复制链接</el-button>
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

    <!-- 添加或修改【数据分享】对话框 -->
    <el-dialog :title="title" v-model="open" width="580px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="失效时间" prop="endTime" label-width="100px">
          <el-date-picker clearable size="small"  style="width:400px"
            v-model="form.endTime"
            type="date"
            value-format="yyyy-MM-dd +08:00"
            placeholder="选择失效时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="是否需要验证码" prop="ifCode"  label-width="120px" >
          <el-radio-group v-model="form.ifCode" class="myradiogroup">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="提取码" prop="extractionCode" label-width="100px" v-show="form.ifCode">
          <el-input v-model="form.extractionCode" placeholder="请输入提取码" maxlength="10" show-word-limit style="width:400px" onkeyup="value=value.replace(/[^A-Za-z0-9]/g, '')" />
        </el-form-item>
        <el-form-item label="分享批次号" prop="shareBatchNum" label-width="100px">
          <el-input v-model="form.shareBatchNum" placeholder="请输入分享批次号"  style="width:400px" onkeyup="value=value.replace(/[^A-Za-z0-9]/g, '')" />
        </el-form-item>
        <el-form-item label="文件名称" prop="shareFileName" label-width="100px">
          <el-input v-model="form.shareFileName" placeholder="请输入文件名称"  style="width:400px" :readonly="true"/>
        </el-form-item>
        <el-form-item label="文件后缀" prop="shareFileExtendName" label-width="100px">
          <el-input v-model="form.shareFileExtendName" placeholder="请输入文件后缀"  style="width:400px" :readonly="true"/>
        </el-form-item>
        <el-form-item label="文件全名" prop="shareFileFullName" label-width="100px">
          <el-input v-model="form.shareFileFullName" placeholder="请输入文件全名"   style="width:400px" :readonly="true"/>
        </el-form-item>
        <el-form-item label="文件大小" prop="shareFileSize" label-width="100px">
          <el-input v-model="form.shareFileSize" placeholder="请输入文件大小"  style="width:400px"  :readonly="true"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script  setup name="FileShare">
import { ref, reactive, onMounted} from 'vue';
import { useRouter, useRoute } from 'vue-router';
const { proxy } = getCurrentInstance();

import {
  listFileShare,
  getFileShare,
  delFileShare,
  addFileShare,
  changeFileShareField,
  changeStatusFileShare,
  updateFileShare,
  exportFileShare, getShareLink
} from "@/api/report/FileShare";
import {copy}   from "@/api/report/FileReport";
import  Condition  from "@/api/search/condition";
import  searchParam  from "@/api/search/searchform";
import {parseTime} from "@/utils/ruoyi";
// 总条数
const total = ref(0);

// 弹出层标题
const title = ref("");

// 遮罩层
const loading = ref(true);

// 非单个禁用
const single = ref(true);

// 非多个禁用
const multiple = ref(true);

// 显示搜索条件
const showSearch = ref(true);

// 是否显示弹出层
const open = ref(false);

// 选中数组
const names = ref([]);

// 选中数组
const ids = ref([]);

// 状态
const statusOptions = ref([]);

// 【数据分享】表格数据
const FileShareList = ref([]);

// 搜索参数
const searchParams = ref([]);

const data = reactive({
  // 查询参数
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    endTime: null,
    extractionCode: null,
    shareBatchNum: null,
    shareStatus: null,
    shareType: null,
    shareUserId: null,
    shareFileType: null,
    shareFileId: null,
    shareFileName: null,
    shareFileExtendName: null,
    shareFileFullName: null,
    shareFileSize: null,
    storageFileId: null,
    storageFilePath: null
  },

  // 查询参数配置对象
  queryParamsConfig: {
    endTime: Condition.like(),
    extractionCode: Condition.like(),
    shareBatchNum: Condition.like(),
    shareStatus: Condition.like(),
    shareType: Condition.like(),
    shareUserId: Condition.like(),
    shareFileType: Condition.like(),
    shareFileId: Condition.like(),
    shareFileName: Condition.like(),
    shareFileExtendName: Condition.like(),
    shareFileFullName: Condition.like(),
    shareFileSize: Condition.like(),
    storageFileId: Condition.like(),
    storageFilePath: Condition.like(),
  },

  // 表单参数
  form: {},

  // 表单校验
  rules: {
    endTime: [
      { required: true, message: "失效时间不能为空", trigger: "blur" }
    ],
    extractionCode: [
      { required: true, message: "提取码不能为空", trigger: "blur" }
    ],
    shareBatchNum: [
      { required: true, message: "分享批次号不能为空", trigger: "blur" }
    ],
    shareStatus: [
      { required: true, message: "分享状态(0正常,1已失效,2已撤销)不能为空", trigger: "blur" }
    ],
    shareType: [
      { required: true, message: "分享类型(0公共,1私密,2好友)不能为空", trigger: "blur" }
    ],
    shareUserId: [
      { required: true, message: "分享用户id不能为空", trigger: "blur" }
    ],
    shareFileType: [
      { required: true, message: "分享文件类型(0业务模型,1上报文件)不能为空", trigger: "blur" }
    ],
    shareFileId: [
      { required: true, message: "分享文件ID不能为空", trigger: "blur" }
    ],
    shareFileName: [
      { required: true, message: "文件名称不能为空", trigger: "blur" }
    ],
    shareFileExtendName: [
      { required: true, message: "文件后缀不能为空", trigger: "blur" }
    ],
    shareFileFullName: [
      { required: true, message: "文件全名不能为空", trigger: "blur" }
    ],
    shareFileSize: [
      { required: true, message: "文件大小不能为空", trigger: "blur" }
    ],
    storageFileId: [
      { required: true, message: "文件id,minIO中的id不能为空", trigger: "blur" }
    ],
    storageFilePath: [
      { required: true, message: "文件路径不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, queryParamsConfig, form, rules } = toRefs(data);

// 页面加载后触发
onMounted(() => {

  getList();
})


/** 查询【数据分享】列表 */
function  getList() {
    searchParams.value = searchParam(queryParamsConfig.value, queryParams.value);
    loading.value = true;
    listFileShare(searchParams.value).then(response => {
      FileShareList.value = response.rows;
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
    endTime: null,
    extractionCode: null,
    shareBatchNum: null,
    shareStatus: null,
    shareType: null,
    shareUserId: null,
    shareFileType: null,
    shareFileId: null,
    shareFileName: null,
    shareFileExtendName: null,
    shareFileFullName: null,
    shareFileSize: null,
    storageFileId: null,
    storageFilePath: null
  };
  resetForm("formRef");
}

/** 搜索按钮操作 */
function  handleQuery() {
  // 获取参数
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function  resetQuery() {
  resetForm("queryForm");
  handleQuery();
}

// 多选框选中数据
function  handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  names.value = selection.map(item => item.shareFileFullName);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function  handleAdd() {
  reset();
  open.value = true;
  title.value = "添加【数据分享】";
}

/** 修改按钮操作 */
function  handleUpdate(row) {
  reset();
  const shareUrl = row.id || ids.value
  getFileShare(shareUrl).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改【数据分享】";
  });
}

/** 提交按钮 */
function  submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if ( form.value.id != null ) {
        updateFileShare(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addFileShare(form.value).then(response => {
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
  const shareUrls = row.id || ids.value;
  let shareUrlList = row.shareFileFullName || names.value;
  //避免弹出窗数据太长，只显示前15条数据
  if ( shareUrlList.length > 15 ) {
    shareUrlList = shareUrlList.slice(0,15);
  }

  proxy.$confirm('是否确认删除【数据分享】文件名为："' + shareUrlList + '"的分享链接?', "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }).then(function() {
      return delFileShare(shareUrls);
    }).then(() => {
      getList();
    proxy.$modal.msgSuccess("删除成功");
    }).catch(error => {

    })
}

/** 状态修改 **/
function  handleStatusChange(row) {
  return changeStatusFileShare(row.id, row.status).then(response=>{
    if(response.code == 200){
      proxy.$modal.msgSuccess("操作成功");
    }
  });
}

/** 修改字段状态 **/
function  chanageFile(value , filed , id){
  return changeFileShareField(value , filed , id).then(response =>{
    if(response.code == 200){
      proxy.$modal.msgSuccess("操作成功");
    }
  }) ;
}

/** 导出按钮操作 */
function  handleExport() {
  const queryParams = queryParams.value;
  proxy.$confirm('是否确认导出所有【数据分享】数据项?', "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }).then(function() {
      return exportFileShare(queryParams);
    }).then(response => {
      download(response.msg);
    })
}

function  ShareLink(shareBatchNum) {
  return getShareLink(shareBatchNum)
}

//复制方法
function  copyF() {
  copy();
}

</script>
