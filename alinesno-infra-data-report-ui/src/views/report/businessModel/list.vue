<template>
  <!--
    【业务模型】 功能列表
    @author paul
    @date 2022-11-28 10:28:04
  -->
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="3" :xs="24">
        <div class="head-container" style="height: calc(100vh - 150px);overflow: hidden;">
          <el-scrollbar :vertical="true" style="height:100%">
            <el-tree
              :data="modelTreeList"
              :props="defaultProps"
              :expand-on-click-node="false"
              :filter-node-method="filterNode"
              show-checkbox:true
              ref="tree"
              @node-click="handleNodeClick"
            />
          </el-scrollbar>
        </div>
      </el-col>

      <el-col :span="21" :xs="24">
          <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
            <el-form-item label="名称" prop="modelName">
              <el-input
                v-model="queryParams.modelName"
                placeholder="请输入名称"
                clearable
                size="small"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="简称" prop="modelShortName">
              <el-input
                v-model="queryParams.modelShortName"
                placeholder="请输入简称"
                clearable
                size="small"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="父模型" prop="modelParentId">
              <el-select  v-model="queryParams.modelParentId" placeholder="请选择业务模型父类"  clearable size="small" filterable  @keyup.enter.native="handleQuery">
                <el-option
                  v-for="item in modelNamelist"
                  :key="item.id"
                  :label="item.modelName"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="文件名" prop="storageFileFullName">
              <el-input
                v-model="queryParams.storageFileFullName"
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
      <!--      <el-col :span="1.5">-->
      <!--        <el-button-->
      <!--          type="warning"-->
      <!--          plain-->
      <!--          icon="el-icon-download"-->
      <!--          size="mini"-->
      <!--          @click="handleExport"-->
      <!--        >导出</el-button>-->
      <!--      </el-col>-->
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </el-row>

          <el-table v-loading="loading" :data="BusinessModelList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="名称" align="left" prop="modelName" show-overflow-tooltip/>
            <el-table-column label="简称" align="left" prop="modelShortName" show-overflow-tooltip/>
            <el-table-column label="父模型" align="left" prop="modelParentNameLabel" show-overflow-tooltip/>
            <el-table-column label="导入模板" align="left" prop="storageFileFullName"  width="350"/>
            <el-table-column label="目标库" align="left" prop="messageType" width="90">
              <template #default="scope">
                <span>{{ messageTypeLabel(scope.row.messageType) }}</span>
              </template>
            </el-table-column>
<!--            <el-table-column label="状态" prop="hasStatus" width="70">-->
<!--              <template #default="scope">-->
<!--              <el-switch-->
<!--                  v-model="scope.row.hasStatus"-->
<!--                  :active-value="0"-->
<!--                  :inactive-value="1"-->
<!--                  @change="handleStatusChange(scope.row)"-->
<!--                ></el-switch>-->
<!--               </template>-->
<!--            </el-table-column>-->
            <el-table-column label="操作" width="340" align="left" class-name="small-padding fixed-width">
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
                  icon="ChatDotRound"
                  @click="handleDesc(scope.row)"
                >详情</el-button>
      <!--          <el-button-->
      <!--            size="mini"-->
      <!--            type="text"-->
      <!--            icon="el-icon-share"-->
      <!--            @click="handleShare(scope.row)"-->
      <!--            v-show="scope.row.storageFileSize"-->
      <!--          >分享</el-button>-->
                <el-button
                  size="mini"
                  type="text"
                  icon="Upload"
                  @click="handleUpload(scope.row)"
                >上传</el-button>
                <a
                  v-show="getIfShowLink(scope.row.columnNum)"
                  target="_blank"
                  style="text-decoration:none; font-size:12px;color:#005CD4;margin-left: 15px"
                  :href="getDownloadFilePathlocal(scope.row)"
                  :download="scope.storageFileFullName"
                  class="downloadDataMode"
                ><i class="Download"></i>下载
                </a>

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
      </el-col>
    </el-row>

    <!-- 添加或修改【业务模型】对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="请输入名称" maxlength="80" show-word-limit/>
        </el-form-item>
        <el-form-item label="简称" prop="modelShortName">
          <el-input v-model="form.modelShortName" placeholder="请输入简称" maxlength="40" show-word-limit/>
        </el-form-item>
        <el-form-item label="父模型" prop="modelParentId">
          <el-select  v-model="form.modelParentId" placeholder="请选择业务模型父类" style="width:680px"  clearable size="small" filterable>
            <el-option
              v-for="item in modelNamelist"
              :key="item.id"
              :label="item.modelName"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="主题" prop="kafkaTopice">
          <el-input v-model="form.kafkaTopice" placeholder="请输入保存数据的消息主题,只能录入字母、数字、下划线" maxlength="64" show-word-limit onkeyup="this.value=this.value.replace(/[^\w]/g,'')" onpaste="this.value=this.value.replace(/[^\w]/g,'')"/>
        </el-form-item>
        <el-form-item label="目标库" prop="messageType"  >
          <el-radio-group v-model="form.messageType" class="myradiogroup"  @change="$forceUpdate()">
            <el-radio :label="1">数据上报</el-radio>
            <el-radio :label="0">数据总线</el-radio>
          </el-radio-group>
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


    <!-- 业务模型详情弹出框 -->
    <el-dialog title="业务模型详情" v-model="detailOpen" width="800px" append-to-body >
      <el-form ref="form" :model="modelDscForm"   label-width="80px">
        <el-form-item label="模型名称" prop="modelName" label-width="150px">
          <el-input v-model="modelDscForm.modelName"  readonly: true />
        </el-form-item>
        <el-form-item label="模型简称" prop="modelShortName" label-width="150px">
          <el-input v-model="modelDscForm.modelShortName"   readonly: true/>
        </el-form-item>
        <el-form-item label="目标库" prop="messageTypeLabel" label-width="150px">
          <el-input v-model="modelDscForm.messageTypeLabel" readonly: true />
        </el-form-item>
        <el-form-item label="消息主题" prop="kafkaTopice" label-width="150px">
          <el-input v-model="modelDscForm.kafkaTopice"  readonly: true />
        </el-form-item>
        <el-form-item label="导入模板文件名" prop="storageFileFullName" label-width="150px">
          <el-input v-model="modelDscForm.storageFileFullName"   readonly: true/>
        </el-form-item>
        <el-form-item label="文件大小(字节)" prop="storageFileSize" label-width="150px">
          <el-input v-model="modelDscForm.storageFileSize" readonly: true/>
        </el-form-item>
        <el-form-item label="列数" prop="columnNum" label-width="150px"  >
          <el-input v-model="modelDscForm.columnNum"  readonly: true />
        </el-form-item>
        <el-form-item label="中文字段列表" prop="columnCnName" label-width="150px">
          <el-input v-model="modelDscForm.columnCnName"  autosize readonly: true/>
        </el-form-item>
        <el-form-item label="英文字段列表" prop="columnName" label-width="150px">
          <el-input v-model="modelDscForm.columnName"  autosize readonly: true />
        </el-form-item>
        <el-form-item label="备注" prop="remark" label-width="150px">
          <el-input v-model="modelDscForm.remark" type="textarea" autosize readonly: true />
        </el-form-item>
        <el-form-item label="新增时间" prop="addTime" label-width="150px">
          <el-input v-model="modelDscForm.addTime"  readonly: true />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="detailCancel">关 闭</el-button>
      </div>
    </el-dialog>


    <!-- 【分享业务模型】对话框 -->
    <el-dialog title="分享业务模型" v-model="shareOpen" width="800px" append-to-body>
      <el-form 	v-show="!shareIsSuccess" ref="shareFormRef" :model="shareForm" :rules="shareRules" label-width="80px">

        <el-form-item label="链接有效期至" prop="endTime" label-width="120px">
          <el-date-picker clearable size="small"
                          v-model="shareForm.endTime"
                          type="date"
                          value-format="yyyy-MM-dd"
                          placeholder="选择失效时间"
                          style="width:280px"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="是否需要验证码" prop="ifCode"  label-width="120px">
          <el-radio-group v-model="shareForm.ifCode" class="myradiogroup">
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
          <span class="text">成功创建分享链接</span>
          <br>
        </div>
        <el-form-item label="分享链接" prop="shareBatchNum">
          <el-input
            :value="ShareLink(shareData.shareBatchNum)"
            :readonly="true"
            type="textarea"
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

    <!--业务模型导入对话框-->
    <el-dialog title="upload.title"  v-model="upload.open" width="450px" append-to-body>
      <el-upload ref="uploadRef" :limit="1" accept=".xlsx"
                 action=""
                 :multiple="false"
                 :http-request="diyUploadFile"
                 :file-list="fileList"
                 :on-change="onChangeFile"
                 :before-remove="beforeRemove"
                 :on-remove="onRemoveFile"
                 :on-progress="handleFileUploadProgress"
                 :on-success="fileUpSuccess"
                 :on-error="fileUpError"
                 :before-upload="onBeforeUpload"
                 :auto-upload="false"
                 drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <!--div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" />
            是否更新已经存在的检查任务数据
          </div-->
          <span>仅允许导入xlsx格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;"
                   @click="exportImportTemplate">下载模板
          </el-link>
        </div>
      </el-upload>
      <br>
      <el-form ref="importForm" :model="importForm"   label-width="0px" v-show="showImportError">
        <el-form-item label="" prop="remark"   >
          <el-input v-model="importForm.remark" type="textarea" :autosize="{maxRows : 4}"  :readonly=true placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">导 入</el-button>
        <el-button @click="importClose">关 闭</el-button>
      </div>
    </el-dialog>


  </div>
</template>

<script  setup name="BusinessModel">
import {
  listBusinessModel,
  listModelName,
  listModelTree,
  getBusinessModel,
  delBusinessModel,
  addBusinessModel,
  changeBusinessModelField,
  changeStatusBusinessModel,
  updateBusinessModel,
  exportBusinessModel,
  downloadTemplate,
  changeModelInfo,
  listTreeNavInfo,
  checkBusinessModelIfExist,
  checkBusinessModelIfUsed,
  checkMinioStatus, uploadModel
} from "@/api/report/BusinessModel";
import  Condition  from "@/api/search/condition";
import  searchParam  from "@/api/search/searchform";
// import uuidv1 from 'uuid/dist/esm-browser/v1';
import {v4 as uuidv4} from "uuid";
import { addFileShareCustom, getShareLink} from "@/api/report/FileShare";
import { getToken } from "@/utils/auth";
import saveAs from 'file-saver';

import { ref, reactive, onMounted} from 'vue';
import { useRouter, useRoute } from 'vue-router';
const { proxy } = getCurrentInstance();

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

//  分享是否成功
const shareIsSuccess = ref(false);

const shareOpen = ref(false);

const detailOpen = ref(false);

const showImportError = ref(false);

// 选中数组
const names = ref([]);

// 选中数组
const ids = ref([]);

// 状态
const statusOptions = ref([]);

// 【业务模型】表格数据
const BusinessModelList = ref([]);

//树形选择列表
const modelTreeList = ref([]);

//用于存放树形导航条当前节点和下级节点的id
const treeIdArrs = ref([]);

// 搜索参数
const searchParams = ref([]);

const searchParamTem = ref([]);

const modelNamelist = ref([]);

// 上传文件参数
//文件列表
const fileList = ref([]);

//文件File     上传参数
const upFile = ref([]);

//文件File列表 上传参数
const upFileList = ref([]);

const download_file_url = ref(import.meta.env.VITE_APP_BASE_API + '/api/infra/data/report/BusinessModel/downloadFile?filePath=');

const share_file_url = ref(import.meta.env.VITE_APP_BASE_API + '/api/infra/data/report/BusinessModel/share?shareFileId=');

const data = reactive({
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        modelName: null,
        modelShortName: null,
        modelParentId: null,
        storageFileId: null,
        storageFileName: null,
        storageFileExtendName: null,
        storageFileFullName: null,
        storageFilePath: null,
        storageFileSize: null,
        ifDir: null,
        kafkaTopice: null,
        columnNum: null,
        columnCnName: null,
        columnName: null,
        messageType: null
      },

      // 查询参数配置对象
      queryParamsConfig: {
        modelName: Condition.like(),
        modelShortName: Condition.like(),
        modelParentId: Condition.like(),
        storageFileId: Condition.like(),
        storageFileName: Condition.like(),
        storageFileExtendName: Condition.like(),
        storageFileFullName: Condition.like(),
        storageFilePath: Condition.like(),
        storageFileSize: Condition.like(),
        ifDir: Condition.like(),
        kafkaTopice: Condition.like(),
        columnNum: Condition.like(),
        columnCnName: Condition.like(),
        columnName: Condition.like(),
        messageType: Condition.eq()
      },

      // 表单参数
      form: {},

      // 表单校验
      rules: {
        modelName: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        modelShortName: [
          { required: false, message: "简称不能为空", trigger: "blur" }
        ],
        modelParentId: [
          { required: false, message: "父模型不能为空", trigger: "blur" }
        ],
        storageFileId: [
          { required: true, message: "文件id,minIO中的id不能为空", trigger: "blur" }
        ],
        storageFileName: [
          { required: true, message: "文件名称不能为空", trigger: "blur" }
        ],
        storageFileExtendName: [
          { required: true, message: "文件后缀不能为空", trigger: "blur" }
        ],
        storageFileFullName: [
          { required: true, message: "文件全名不能为空", trigger: "blur" }
        ],
        storageFilePath: [
          { required: true, message: "文件路径不能为空", trigger: "blur" }
        ],
        storageFileSize: [
          { required: true, message: "文件大小不能为空", trigger: "blur" }
        ],
        ifDir: [
          { required: true, message: "是否是目录;0-否,1-是不能为空", trigger: "blur" }
        ],
        kafkaTopice: [
          { required: true, message: "主题|数据表不能为空", trigger: "blur" },
          { min: 1, max: 50,message: '长度在 1 到 64 个字符', trigger: 'blur' },
        ],
        columnNum: [
          { required: true, message: "列数，用于判断上传文件列数是否一致不能为空", trigger: "blur" }
        ],
        columnCnName: [
          { required: true, message: "列中文名列表，用于判断上传文件列名不能为空", trigger: "blur" }
        ],
        columnName: [
          { required: true, message: "列英文名列表，用于判断上传文件列名不能为空", trigger: "blur" }
        ],
        remark: [
          { required: false, message: "备注不能为空", trigger: "blur" }
        ],
        messageType:[
          { required: true, message: "消息类型", trigger: "blur" }
        ]
      },

      Params: {
        pageNum : 1,
        pageSize: 1000,
        modelName: null,
        typeName:null
      },

      // ParamsConfig:{
      //   typeName:null,
      // },

      modelDscForm:{},

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
        ]
      },

      // 分享成功的数据
      shareData: {
        ifCode: false,
        shareBatchNum: '',
        extractionCode: ''
      },

      modelParmeter:{},

      //树形配置
      defaultProps: {
        parentId: "modelParentId",
        children: "children",
        label: "modelName",
        lazy: true
      },
      treeQueryParam: {
        pageNum : 1,
        pageSize: 5000
      },

      // 导入参数
      upload: {
        // 是否显示弹出层
        open: false,
        // 是否显示替换提示
        replace: false,
        // 弹出层标题
        title: "上传业务模型",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },

        url:  import.meta.env.VITE_APP_BASE_API + "/api/infra/data/report/BusinessModel/uploadfile"   // 文件上传地址
      },
      //导入错误显示框
      importForm:{},
});

const { queryParams, queryParamsConfig, form, rules, Params, modelDscForm, shareForm, shareRules, shareData, modelParmeter, upload, defaultProps, treeQueryParam, importForm } = toRefs(data);

const props = defineProps({
  parentId: {
    type: String,
    default: "modelParentId"
  },

  children: {
    type: String,
    default: "children"
  },

  label: {
    type: String,
    default: "modelName"
  },

  lazy: {
    type: Boolean,
    default: true,
  }
});


// 页面加载后触发
onMounted(() => {
  getModelNameList();
  getModelTreeList();
  getList();
})


function  getIfShowLink(columnNum){
  if(typeof columnNum == "undefined" || columnNum == null || columnNum == ""){
    return false;
  }else{
    return true;
  }
}

function  getDownloadFilePathlocal(row){
  return download_file_url.value + row.storageFilePath + "&fileName=" + row.storageFileName;
}

function  onBeforeUpload(file){
  var arrayList = file.name.split(".")
  if( arrayList.length >= 2 ) {
    modelParmeter.value.fileName = arrayList[0];
    modelParmeter.value.extendName = arrayList[1];
  }
}

//增加业务模型父类下拉框功能
function  getModelNameList() {
  // searchParamTem.value = searchParam(ParamsConfig.value, Params.value);
  listModelName(Params.value).then(response => {
    modelNamelist.value = response.rows;
  });
}

/**
* 文件上传成功时的钩子
*/
function  fileUpSuccess(res, file, fileList) {
  debugger
  console.log("上传模型成功!" + res.code) ;
  proxy.$refs.uploadRef.clearFiles(); //上传成功之后清除历史记录
  if( res.code == 200 ) {
    modelParmeter.value.storageFileId = file.uid;
    modelParmeter.value.storageFileFullName = file.name;
    modelParmeter.value.storageFilePath = res.data.storageFilePath;
    modelParmeter.value.storageFileSize = file.size;
    modelParmeter.value.columnNum = res.data.cellNum;
    modelParmeter.value.columnCnName = res.data.columnCnName;
    modelParmeter.value.columnName = res.data.columnName;
    changeModelInfo(modelParmeter.value).then(res =>{
      if( res.code == 200 ) {
        proxy.$modal.msgSuccess("文件上传成功并更新业务模型信息");
        modelParmeter.value = {};
        upload.value.open = false;
        getList();
      } else{
        proxy.$modal.msgError(res.msg);
      }
    })
  }else{
    proxy.$modal.notifyError({
      title: '文件上传失败！',
      message: res.msg
    })
  }

}

/**
* 文件上传失败时的钩子
*/
function  fileUpError(err, file, fileList) {
  proxy.$modal.notifyError({
    title: '错误',
    message: `文件上传失败`
  })
}

/**
* 点击上传按钮时操作
*/
function  fileUpload() {
  proxy.$refs.dataFormFile.submit()
}

/** 查询【业务模型】列表 */
function  getList() {
     debugger
    // searchParams.value = searchParam(queryParamsConfig.value, queryParams.value);
    loading.value = true;
    // listBusinessModel(searchParams.value).then(response => {
    listBusinessModel(queryParams.value).then(response => {
      debugger
      BusinessModelList.value = response.rows;
      total.value = response.total;
      loading.value = false;
      console.log("查询完成了!" + loading.value);
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
    modelName: null,
    modelShortName: null,
    modelParentId: null,
    storageFileId: null,
    storageFileName: null,
    storageFileExtendName: null,
    storageFileFullName: null,
    storageFilePath: null,
    storageFileSize: null,
    storageFileUrl: null,
    ifDir: null,
    kafkaTopice: null,
    columnNum: null,
    columnCnName: null,
    columnName: null,
    remark: null,
    messageType: null
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
  const {pageNum,pageSize} = queryParams.value;
  queryParams.value = {pageNum ,pageSize};
  proxy.resetForm("queryForm");
  handleQuery();
}

// 多选框选中数据
function  handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  names.value = selection.map(item => item.modelName);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function  handleAdd() {
  getModelNameList();
  reset();
  form.value.messageType = 0;
  open.value = true;
  title.value = "添加【业务模型】";
}

/** 修改按钮操作 */
function  handleUpdate(row) {
  getModelNameList();
  reset();
  const modelName = row.id || ids.value
  getBusinessModel(modelName).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改【业务模型】";
    console.log("目标库:" + form.value.messageType);
  });
}

/** 提交按钮 */
function  submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if ( valid ) {
      checkBusinessModelIfExist(form.value).then( res => {
        if ( res.code == 200 ) {
          if (form.value.id != null) {
            updateBusinessModel(form.value).then(response => {
              proxy.$modal.msgSuccess("修改成功");
              open.value = false;
              getList();
            });
          } else {
            addBusinessModel(form.value).then(response => {
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
    }
  });
}

/** 删除按钮操作 */
function  handleDelete(row) {
  const modelNames = row.id || ids.value;
  let modelNameList = row.modelName || names.value;
  //避免弹出窗数据太长，只显示前15条数据
  if ( modelNameList.length > 15 ) {
    modelNameList = modelNameList.slice(0,15);
  }

  checkBusinessModelIfUsed(modelNames).then(res => {
    if ( res.code == 200 && res.msg == "操作成功" ) {
      proxy.$confirm('是否确认删除【业务模型】名称为"' + modelNameList + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return delBusinessModel(modelNames);
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
  return changeStatusBusinessModel(row.id, row.status).then(response=>{
    if( response.code == 200 ){
      proxy.$modal.msgSuccess("操作成功");
    }
  });
}

/** 修改字段状态 **/
function  chanageFile(value , filed , id){
  return changeBusinessModelField(value , filed , id).then(response =>{
    if( response.code == 200 ){
      proxy.$modal.msgSuccess("操作成功");
    }
  }) ;
}

/** 详情按钮操作 */
function  handleDesc(row) {
  modelDscForm.value.modelName = row.modelName;
  modelDscForm.value.modelShortName = row.modelShortName;
  modelDscForm.value.messageTypeLabel =messageTypeLabel(row.messageType);
  modelDscForm.value.kafkaTopice = row.kafkaTopice;
  modelDscForm.value.storageFileFullName = row.storageFileFullName;
  modelDscForm.value.storageFileSize = row.storageFileSize;
  modelDscForm.value.columnNum = row.columnNum;
  modelDscForm.value.columnCnName = row.columnCnName;
  modelDscForm.value.columnName = row.columnName;
  modelDscForm.value.addTime = row.addTime;
  modelDscForm.value.remark = row.remark;
  detailOpen.value = true;
}

function  detailCancel()
{
  detailOpen.value = false ;
}

/** 导出按钮操作 */
function  handleExport() {
  const queryParams = queryParams.value ;
  proxy.$confirm('是否确认导出所有【业务模型】数据项?', "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }).then(function() {
      return exportBusinessModel(queryParams);
    }).then(response => {
      download(response.msg);
    })
}

function  handleShare(row) {
  resetshareForm();
  shareIsSuccess.value = false;
  shareOpen.value = true;
  shareForm.value.ifCode = 0;
  var uuid = uuidv4();
  shareForm.value.shareBatchNum = uuid.replace(/-/g,"")
  shareForm.value.shareFileType = 0 ;
  shareForm.value.shareFileId = row.id;
  shareForm.value.shareFileName = row.storageFileName;
  shareForm.value.shareFileExtendName = row.storageFileExtendName;
  shareForm.value.shareFileFullName = row.storageFileFullName;
  shareForm.value.shareFileSize = row.storageFileSize;
  shareForm.value.storageFileId = row.storageFileId;
  shareForm.value.storageFilePath = row.storageFilePath;
}

function  cancelShare(){
  shareOpen.value = false;
}

function  submitShareForm(){
  addFileShareCustom(shareForm.value).then(response => {
    if( response.code == 200 ){
      shareIsSuccess.value = true ; //  分享是否成功
      shareData.value.ifCode = response.data.ifCode;
      shareData.value.shareBatchNum = response.data.shareBatchNum;
      shareData.value.extractionCode = response.data.extractionCode;
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

function  handleUpload(row){
  checkMinioStatus().then(res =>{
    if ( res.code == 200 ){
      modelParmeter.value = {};
      modelParmeter.value.id = row.id;
      modelParmeter.value.messageType = row.messageType;
      modelParmeter.value.kafkaTopice = row.kafkaTopice;
      upload.value.isUploading = false;
      upload.value.replace = typeof(row.columnNum) == "undefined" ? false : true ;
      upload.value.open = true;
    }else{
      proxy.$modal.msgError(res.msg)
    }
  }).catch(error =>{
    proxy.$modal.msgError(res.msg)
  })

}

function  downloadTemplateF(){
  downloadTemplate().then((res) => {
    var   filename = "学生清单_业务模型示例.xlsx";
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    saveAs(blob, filename);
  })
}

// 文件上传中处理
function  handleFileUploadProgress(event, file, fileList) {
  upload.value.isUploading = true;
}

// 提交上传文件
function  submitFileForm() {
  proxy.$refs.uploadRef.submit();
}

//获取树形数据  pageSize pageNum
function  getModelTreeList() {
  loading.value = true;
  listModelTree(treeQueryParam.value).then(response => {
    console.log("树形返回结果了!" + response.rows) ;
    modelTreeList.value = proxy.handleTree(response.rows, "id", "modelParentId");
    loading.value = false;
  });
}

// 树形选择筛选节点
function  filterNode(value, data) {
  if ( !value ) return true;
  return data.label.indexOf(value) !== -1;
}

// 树形节点单击事件
function  handleNodeClick(node) {
  treeIdArrs.value = [] ;
  // 获取当前节点信息
  treeIdArrs.value.push(node.id)
  // 遍历当前节点的所有子节点
  if ( node.children ) {
    for (let i = 0; i < node.children.length; i++) {
      treeIdArrs.value.push(node.children[i].id)

    }

  }

  // searchParams.value = searchParam(queryParamsConfig.value, queryParams.value);
  loading.value = true;
  listTreeNavInfo(treeIdArrs.value, queryParams.value.pageNum, queryParams.value.pageSize, queryParams.value ).then(res => {
    BusinessModelList.value = res.rows;
    total.value = res.total;
    loading.value = false;
  })
}

function  messageTypeLabel(messageType) {
  switch(messageType) {
    case 0:
      return  "数据总线" ;
      break;
    case 1:
      return  "数据上报" ;
      break;
    default:
      return  "数据总线" ;
  }
}


// 选择上传文件
function   onChangeFile (file, fileList) {
  const isLt100M = file.size / 1024 / 1024 < 100
  if ( !isLt100M ) {
    proxy.$msgbox.alert('上传文件大小不能超过 100MB!')
    return false
  }

  upFileList.value = []
  for (let x of fileList) {
    if (x.raw) {
      upFileList.value.push(x.raw)
    }
  }
}

// 移除文件之前
function   beforeRemove(file, fileList) {
  return proxy.$msgbox.alert(`确定移除 ${file.name}？`)
}

// 移除文件
function   onRemoveFile (file, fileList) {
  upFileList.value = []
  for (let x of fileList) {
    if (x.raw) {
      upFileList.value.push(x.raw)
    }
  }
}

//vue界面将文件发送到后端
function   diyUploadFile() {
  debugger
  upFile.value = upFileList.value[0];
  let uploadForm = new FormData()
  uploadForm.append('file', upFile.value)
  modelParmeter.value.fileId = upFile.value.id;
  modelParmeter.value.fileName = upFile.value.name;
  // modelParmeter.value.extendName = upFile.value.extendName;
  modelParmeter.value.storageFileSize = upFile.value.size;
  uploadModel(modelParmeter.value, uploadForm).then(response => {
    if ( response.code == 200 ) {
      proxy.$modal.msgSuccess(response.msg)
      upload.value0.open = false;
      showImportError.value = false ;
    }else{
      proxy.$modal.msgError(response.msg)
      showImportError.value = true ;
      importForm.value.remark = response.msg;
    }
    return false

  }).catch(error => {
    showImportError.value = true ;
    importForm.value.remark = error.toString();
    return false
  })
}

function importClose(){
  upload.value.open=false;
  showImportError.value = false ;
  handleQuery();
  fileList.value = [] ;   //上传文件列表
  upFile.value = [] ;      //文件File 上传参数
  upFileList.value = [] ;   //文件File列表 上传参数
}



</script>
