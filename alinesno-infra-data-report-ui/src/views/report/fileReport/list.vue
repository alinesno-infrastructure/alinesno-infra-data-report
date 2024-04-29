<template>
  <!--
    【上报文件】 功能列表

    @author paul
    @date 2022-11-28 10:28:04
  -->
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文件名" prop="fileFullName">
        <el-input
          v-model="queryParams.fileFullName"
          placeholder="请输入文件名"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务模型" prop="modelId">
        <el-select v-model="queryParams.modelId" placeholder="请选择业务模型" clearable size="small"  filterable  @keyup.enter.native="handleQuery">
          <el-option
            v-for="item in modelNamelist"
            :key="item.id"
            :label="item.modelName"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="上报状态" prop="reportStatus">
        <el-select v-model="queryParams.reportStatus" placeholder="请选择上报状态" clearable size="small"  filterable  @keyup.enter.native="handleQuery">
          <el-option
            v-for="item in reportStatusOptions"
            :key="item.key"
            :label="item.label"
            :value="item.key">
          </el-option>
        </el-select>
      </el-form-item>
<!--      <el-form-item label="目标库" prop="reportStatus">-->
<!--        <el-select v-model="queryParams.reportStatus" placeholder="请选择目标库" clearable size="small"  filterable  @keyup.enter.native="handleQuery">-->
<!--          <el-option-->
<!--            v-for="item in reportStatusOptions"-->
<!--            :key="item.key"-->
<!--            :label="item.label"-->
<!--            :value="item.key">-->
<!--          </el-option>-->
<!--        </el-select>-->
<!--      </el-form-item>-->
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
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

    <el-table v-loading="loading" :data="FileReportList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文件名" align="left" prop="fileFullName" show-overflow-tooltip />
      <el-table-column label="文件大小" align="left" prop="fileSize" width="100" show-overflow-tooltip />
      <el-table-column label="数据行数" align="left" prop="reportRow" width="80"/>
      <el-table-column label="业务模型" align="left" prop="modelNameLabel" show-overflow-tooltip/>
      <el-table-column label="校验状态" align="left" prop="ifCheck"  width="90"  >
        <template #default="scope">
          <span>{{ reportStatusTrans(scope.row.ifCheck) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上报状态" align="left" width="80" >
        <template #default="scope">
          <span>{{ scope.row.reportStatusLabel  }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上报时间" align="left" prop="reportTime" width="155" />
      <el-table-column label="添加时间" align="center" prop="addTime" width="155" >
        <template #default="scope">
          <span>{{ parseTime(scope.row.addTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="350" align="center" class-name="small-padding fixed-width" >
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
            icon="el-icon-share"
            @click="handleShare(scope.row)"
          >分享</el-button>
          <el-button
            v-show="scope.row.reportStatus == 0 || scope.row.reportStatus == 2 "
            size="mini"
            type="text"
            icon="el-icon-upload"
            @click="submitReport(scope.row)"
          >上报</el-button>
          <el-button
            v-show="scope.row.ifCheck===0"
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="DownloadChekFileF(scope.row)"
          >校验文件</el-button>
          <el-button
            v-show="scope.row.reportStatus===2"
            size="mini"
            type="text"
            icon="el-icon-bell"
            @click="exceptionInfo(scope.row)"
          >异常信息</el-button>
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

    <!-- 添加或修改【上报文件】对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">

        <el-form-item label="业务模型" prop="modelId">
          <el-select  v-model="form.modelId" placeholder="请选择业务模型" style="width:680px"  clearable size="small" @change="modelChanged()" filterable>
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
              <el-input v-model="form.storageFileId" disabled placeholder="已按照导入模板格式填报数据" style="width:580px"/>
            </el-col>
            <el-col :span="2">
              <el-button size="small" type="primary" disable="true" style="margin-left: 15px" @click="handleUpFile(form.modelId)">点击上传</el-button>
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
          <el-input v-model="form.remark" type="textarea" :autosize="{minRows:3}" maxlength="4000" show-word-limit placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 上传文件对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
        ref="dataFormFile"
        :limit="1"
        accept=".xls,.xlsx"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :file-list="fileUpList"
        :on-progress="handleFileUploadProgress"
        :on-success="fileUpSuccess"
        :on-error="fileUpError"
        :auto-upload="false"
        :before-upload="onBeforeUpload"
        :data="reportParmeter"
        drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击选择文件</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <span>仅允许导入.xls,.xlsx格式文件。</span>
          <!--el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="downloadMode">下载业务模型</--el-link-->
          <a
            v-show="reportParmeter.storageFileFullName"
            target="_blank"
            style="text-decoration:none; font-size:12px;color:#005CD4;margin-left: 15px"
            :href="downloadMode()"
            :download="reportParmeter.storageFileFullName"
            class="downloadDataMode"
          ><i class="el-icon-download"></i>下载业务模型
          </a>
          <br>
          <div class="el-upload__tip" slot="tip" v-show="upload.replace" >
            将替换已经存在的业务模型导入模板
          </div>

        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="fileUpload()">上 传</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 【分享上报文件】对话框 -->
    <el-dialog title="分享上报文件" v-model="shareOpen" width="780px" append-to-body>
      <el-form 	v-show="!shareIsSuccess" ref="shareFormRef" :model="shareForm" :rules="shareRules" label-width="80px">

        <el-form-item label="链接有效期至" prop="endTime" label-width="120px">
          <el-date-picker clearable size="small"
                          v-model="shareForm.endTime"
                          type="date"
                          format="YYYY-MM-DD"
                          value-format="YYYY-MM-DD"
                          placeholder="选择失效时间"
                          style="width:580px"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="是否需要验证码" prop="ifCode"  label-width="120px">
          <el-radio-group v-model="shareForm.ifCode" class="myradiogroup" style="width:580px">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <el-form
        v-if="shareIsSuccess"
        class="share-success-form"
        :model="shareData"
        ref="shareSuccessForm"
        label-suffix="："
        label-width="90px"
      >
        <div class="success-tip">
          <i class="el-icon-success"></i>
          <span class="text" style="margin-right:20px">成功创建分享链接</span>
          <el-button size="mini" type="text" icon="el-icon-document-copy" class="tag-read" v-show="shareIsSuccess&&shareData.extractionCode"
                     :data-clipboard-text= "'分享链接: ' + ShareLink(shareData.shareBatchNum) + ' 提取码: ' + shareData.extractionCode" @click="copyF">复制到剪切板

          </el-button>
          <el-button size="mini" type="text" icon="el-icon-document-copy" class="tag-read" v-show="shareIsSuccess&&!shareData.extractionCode"
                     :data-clipboard-text= "'分享链接: ' + ShareLink(shareData.shareBatchNum) " @click="copyF">复制到剪切板

          </el-button>
          <br>
        </div>
        <el-form-item label="分享链接" prop="shareLink">
          <el-input
            v-model="shareData.shareLink"
            :readonly="true"
            type="textarea"
            size="small"
            autosize
          ></el-input>
        </el-form-item>
        <el-form-item label="提取码" prop="extractionCode" v-show="shareIsSuccess&&shareData.extractionCode">
          <el-input
            v-model="shareData.extractionCode"
            :readonly="true"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer"   v-show="!shareIsSuccess">
        <el-button type="primary" @click="submitShareForm">确 定</el-button>
        <el-button @click="cancelShare">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="异常消息内容"
      :visible.sync="contentVisible"
      width="980px">
      <span>
        <el-input
          type="textarea"
          autosize
          placeholder="请输入内容"
          v-model="exceptionContent">
      </el-input>

      </span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="contentVisible = false">关 闭</el-button>
      </span>
    </el-dialog>


  </div>
</template>

<script  setup name="FileReport">
import {ref, reactive, onMounted, onUnmounted} from 'vue';
import { useRouter, useRoute } from 'vue-router';
const { proxy } = getCurrentInstance();

import {
  listFileReport,
  getFileReport,
  delFileReport,
  addFileReport,
  changeFileReportField,
  changeStatusFileReport,
  updateFileReport,
  exportFileReport,readExcel,DownloadChekFile,getFileProgress,checkFileReportIfExist,checkFileReportIfUsed,copy } from "@/api/report/FileReport";
import  Condition  from "@/api/search/condition";
import  searchParam  from "@/api/search/searchform";
import {   listBusinessModel } from "@/api/report/BusinessModel";
// import uuid from  'vue3-uuid';
import {v4 as uuidv4} from "uuid";
import {addFileShareCustom,getShareLink} from "@/api/report/FileShare";
import { getToken } from "@/utils/auth";
import {parseTime} from "@/utils/ruoyi";

// 总条数
const total = ref(0);

// 弹出层标题
const title = ref("");

const exceptionContent = ref("");

// 初始化定时器id,用于上报文件后，刷新界面表格
const intervalId = ref(null);

const download_file_url = ref(import.meta.env.VITE_APP_BASE_API + '/api/infra/data/report/BusinessModel/downloadFile?filePath=');

const share_file_url = ref(import.meta.env.VITE_APP_BASE_API + '/api/infra/data/report/BusinessModel/share?&shareFileId=');

//  分享是否成功
const shareIsSuccess = ref(false);

//文件上传时显示窗
const fileuploading = ref(false);

const contentVisible = ref(false);

const shareOpen = ref(false);

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

// 【上报文件】表格数据
const FileReportList = ref([]);

// 状态
const statusOptions = ref([]);

// 搜索参数
const searchParams = ref([]);

// 选中数组
const names = ref([]);

// 选中数组
const ids = ref([]);

const modelNamelist = ref([]);

const modelNamelist_filter = ref([]);

const searchParamTem = ref([]);

// 上传文件参数
// 文件列表
const fileList = ref([]);

// 上传的文件列表
const fileUpList = ref([]);

const reportStatusOptions = ref(
     [
            {key: 0, label: "未上报",cantSelect: false},
            {key: 1, label: "已上报",cantSelect: false},
            {key: 2, label: "上报异常",cantSelect: false}
          ]
)


const data = reactive({
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
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
        hasStatus : 0,
        modelName: null,
      },

      ParamsConfig:{
        hasStatus :  Condition.eq(),
        modelName: Condition.like()
      },

      reportParmeter:{},

      shareForm:{},

      shareRules:{
        shareUrl: [
          { required: false, message: "分享文件URL不能为空", trigger: "blur" }
        ],
        endTime: [
          { required: false, message: "失效时间不能为空", trigger: "blur" }
        ],
        extractionCode: [
          { required: false, message: "提取码不能为空", trigger: "blur" }
        ],
        shareBatchNum: [
          { required: false, message: "分享批次号不能为空", trigger: "blur" }
        ],
        shareStatus: [
          { required: false, message: "分享状态(0正常,1已失效,2已撤销)不能为空", trigger: "blur" }
        ],
        shareType: [
          { required: false, message: "分享类型(0公共,1私密,2好友)不能为空", trigger: "blur" }
        ],
        shareUserId: [
          { required: false, message: "分享用户id不能为空", trigger: "blur" }
        ],
        shareFileType: [
          { required: false, message: "分享文件类型(0业务模型,1上报文件)不能为空", trigger: "blur" }
        ],
        shareFileId: [
          { required: true, message: "分享文件ID不能为空", trigger: "blur" }
        ],
        shareFileName: [
          { required: false, message: "文件名称不能为空", trigger: "blur" }
        ],
        shareFileExtendName: [
          { required: false, message: "文件后缀不能为空", trigger: "blur" }
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
        ],
        ifCode: [
          { required: true, message: "是否需要验证码", trigger: "blur" }
        ]
      },

      // 分享成功的数据
      shareData: {
        ifCode: false,
        shareLink: '',
        shareBatchNum: '',
        extractionCode: ''
      },

      //上报参数实体
      reportForm: {},

      // 用户导入参数
      upload: {
          // 是否显示弹出层
          open: false,
          // 是否显示替换提示
          replace: false,
          // 弹出层标题
          title: "上传文件",
          // 是否禁用上传
          isUploading: false,
          // 是否更新已经存在的用户数据
          updateSupport: 0,

          // 设置上传的请求头部
          headers: { Authorization: "Bearer " + getToken() },

          // 文件上传地址
          url: import.meta.env.VITE_APP_BASE_API + "/api/infra/data/report/FileReport/uploadfile"
      }
});

const { queryParams, queryParamsConfig, form, rules, Params, ParamsConfig, reportParmeter, shareForm, shareRules, shareData, reportForm,  upload } = toRefs(data);

// 页面加载后触发
onMounted(() => {
  getModelNameList();

  intervalId.value = setInterval(() => {
    getList()
  }, 10000)

  // getList();

})

onUnmounted( () =>{
  clearInterval(intervalId.value)
})


function  submitReport(row) {

  if( row.reportStatus == 1 ){
    proxy.$modal.msgSuccess("已上报，不需要重新上报！");
  } else{
      loading.value = true;
      let v_oldCheckFileName ="";
      let v_oldCheckExtendName ="";
      if ( row.checkFileName ){
        v_oldCheckFileName = row.checkFileName;
      } ;
      if (  row.checkFileExtendName) {
        v_oldCheckExtendName = row.checkFileExtendName;
      }
      reportForm.value = {} ;
      reportForm.value.id                 =  row.id ;
      reportForm.value.modelId            =  row.modelId ;
      reportForm.value.fileName           =  row.fileName ;
      reportForm.value.extendName         =  row.fileExtendName ;
      reportForm.value.fileUrl            =  row.storageFilePath  ;
      reportForm.value.messageType        =  row.messageType ;
      reportForm.value.kafkaTopic         =  row.kafkaTopice  ;
      reportForm.value.oldCheckFileName   =  v_oldCheckFileName ;
      reportForm.value.oldCheckExtendName =  v_oldCheckExtendName ;
      readExcel(reportForm.value).then((res) => {
        if ( res.code == 200 ) {
          proxy.$modal.msgSuccess(res.msg);
        } else{
          proxy.$modal.msgError(res.msg);
        }
        getList();
      })
     .catch(( ) => {
        getList();
        loading.value = false;
     })

  }

}

// 文件上传中处理
function  handleFileUploadProgress(event, file, fileList) {
  // this.upload.isUploading = true;
}

function  onBeforeUpload(file){
  var arrayList = file.name.split(".")
  if( arrayList.length >= 2 ) {
    form.value.fileName = arrayList[0];
    form.value.fileExtendName = arrayList[1];

  }
}

/**
* 文件上传弹窗显示
*/
function  handleUpFile(modelId) {
   upload.value.open = true;
   fileUpList.value = [];
}

/**
 * 文件上传成功时的钩子
 */
function  fileUpSuccess(res, file, fileList) {
  debugger
  console.log("文件成功!")
  fileuploading.value = false;
  if( res.code == 200 ) {
    proxy.$modal.notifySuccess({
      title: '成功',
      message: `文件上传成功`
    })
    form.value.storageFileId = file.uid;
    form.value.fileFullName = file.name;
    form.value.storageFilePath = res.data.storageFilePath;
    form.value.fileSize = file.size;
    form.value.reportRow = res.data.reportRow;
    fileUpList.value = [];
    upload.value.open = false;
  }else {
    proxy.$modal.notifyError({
      title: '错误',
      message: `文件上传失败!`+res.msg
    })
    // this.fileUpList = [];
  }
}

/**
 * 文件上传失败时的钩子
 */
function  fileUpError(err, file, fileList) {
  fileuploading.value = false;
  proxy.$modal.notifyError({
    title: '错误',
    message: `文件上传失败`
  })
}

/**
 * 点击上传按钮时操作
 */
function  fileUpload() {
fileuploading.value = true;
  proxy.$refs.dataFormFile.submit()
}

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
    listFileReport(searchParams.value).then(response => {
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
  resetForm("queryForm");
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
  form.value.ifCheck =2 ;       //2-刚上报,未校验
  form.value.reportStatus =0 ;  //0-待上报
  form.value.messageType =0 ;
  fileUpList.value = [];
  open.value = true;
  title.value = "添加【上报文件】";
}

/** 修改按钮操作 */
function  handleUpdate(row) {
  reset();
  const fileName = row.id || ids.value
  getFileReport(fileName).then(response => {
    form.value = response.data;
    open.value = true;
    title .value= "修改【上报文件】";
  });
}

/** 提交按钮 */
function  submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    checkFileReportIfExist(form.value).then( res => {
      if ( res.code == 200 ) {
        if ( form.value.id != null ) {
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
      } else {
        proxy.$modal.msgError(res.msg)
      }
    }).catch(error => {

    });
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

  checkFileReportIfUsed(fileNames).then(res => {
    if ( res.code == 200 && res.msg == "操作成功" ) {
      proxy.$confirm('是否确认删除【上报文件】文件名为"' + fileNameList + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return delFileReport(fileNames);
      }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
      }).catch(error => {

      })

    } else {
      proxy.$modal.msgError(res.msg)
    }
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
  const queryParams = queryParams.value ;
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

function  modelChanged(){
  console.log("新增窗选择模型" + form.value.modelId)
  for ( let i in modelNamelist.value ) {
    if ( form.value.modelId == modelNamelist.value[i].id ) {
      debugger
      console.log("新增窗选择模型：" +  modelNamelist.value[i].columnNum)
      reportParmeter.value.modelID       = modelNamelist.value[i].id
      reportParmeter.value.kafkaTopice   = modelNamelist.value[i].kafkaTopice
      reportParmeter.value.columnNum     = modelNamelist.value[i].columnNum
      reportParmeter.value.columnCnName  = modelNamelist.value[i].columnCnName
      reportParmeter.value.columnName    = modelNamelist.value[i].columnName
      reportParmeter.value.filePath    = modelNamelist.value[i].storageFilePath
      reportParmeter.value.fileName    = modelNamelist.value[i].storageFileName
      reportParmeter.value.storageFileFullName    = modelNamelist.value[i].storageFileFullName
    }
  }
}

function  DownloadChekFileF(row) {
  DownloadChekFile(row.checkFileName, row.checkFileExtendName, row.checkFilePath).then((res) => {
    const blob = new Blob([res]);
    const fileName = row.checkFileName + '.' + row.checkFileExtendName;
    if ("download" in document.createElement("a")) {
      // 非IE下载
      const _link = document.createElement("a");
      _link.download = fileName;
      _link.style.display = "none";
      _link.href = URL.createObjectURL(blob);
      document.body.appendChild(_link);
      _link.click();
      URL.revokeObjectURL(_link.href); // 释放URL 对象
      document.body.removeChild(_link);
    } else {
      // IE10+下载
      navigator.msSaveBlob(blob, fileName);
    }
  })

}

function  handleShare(row) {
  resetshareForm();
  shareIsSuccess.value = false;
  shareOpen.value = true;
  shareForm.value.ifCode = 0;
  var uuid = uuidv4();
  shareForm.value.shareBatchNum = uuid.replace(/-/g,"")
  shareForm.value.shareFileType = 1 ;
  shareForm.value.shareFileId = row.id;
  shareForm.value.shareFileName = row.fileName;
  shareForm.value.shareFileExtendName = row.fileExtendName;
  shareForm.value.shareFileFullName = row.fileFullName;
  shareForm.value.shareFileSize = row.fileSize;
  shareForm.value.storageFileId = row.storageFileId;
  shareForm.value.storageFilePath = row.storageFilePath;
}

function  cancelShare(){
  shareOpen.value = false;
}

function  submitShareForm(){
  debugger
  addFileShareCustom(shareForm.value).then(response => {
    debugger
    if( response.code == 200 ){
      shareIsSuccess.value = true ; //  分享是否成功
      shareData.value.ifCode = response.data.ifCode;
      shareData.value.shareBatchNum = response.data.shareBatchNum;
      shareData.value.extractionCode = response.data.extractionCode;
      shareData.value.shareLink = ShareLink(shareData.value.shareBatchNum)
    }
  });
}

// 分享表单重置
function  resetshareForm() {
  shareForm.value = {
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
    storageFilePath: null,
    ifCode: null
  };
  proxy.resetForm("shareFormRef");
}

function  ShareLink(shareBatchNum) {
  return getShareLink(shareBatchNum)
}

function  exceptionInfo(row){
  contentVisible.value = true ;
  exceptionContent.value = row.errorMsg ;
 }

function  downloadMode(){
  return download_file_url.value + reportParmeter.value.filePath + "&fileName=" + reportParmeter.value.fileName;
}

//复制方法
function  copyF() {
  copy();
}


function  statusTrans(value) {
  if (value === null || value === '') {
    return '';
  }
  switch (value) {
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
<style>
.success-tip{
  margin-bottom: 20px;
}
</style>
