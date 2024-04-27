<template>
  <!--
    【数据分享】 功能列表

    @author paul
    @date 2022-11-28 10:28:04 class="app-container"
  -->
  <div class="app-container">
    <!-- 校验文件分享链接状态和是否需要提取码对话框 -->
    <el-dialog
      title="文件分享"
      :visible.sync="dialogShareFile.visible"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      width="500px"
    >
      <div class="end-time" v-if="shareStep === 1">此分享链接已过期</div>
      <el-form
        class="extraction-code-form"
        v-if="shareStep === 2"
        ref="codeForm"
        :model="dialogShareFile.codeForm"
        :rules="dialogShareFile.codeFormRules"
        label-width="80px"
      >
        <el-form-item label="提取码" prop="extractionCode">
          <el-input
            v-model="dialogShareFile.codeForm.extractionCode"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
				<el-button v-if="shareStep === 1" @click="handleCloseBtnClick()"
        >关 闭</el-button
        >
				<el-button
          v-else
          type="primary"
          @click="handleSubmitBtnClick(dialogShareFile.codeForm.extractionCode)"
        >提 交</el-button
        >
			</span>
    </el-dialog>

    <el-table v-loading="loading" :data="FileShareList" >
      <el-table-column label="文件名称" align="left" prop="shareFileFullName"  />
      <el-table-column label="文件大小" align="left" prop="shareFileSize"  />
      <el-table-column label="文件类型" align="left" prop="shareFileType"  >
        <template #default="scope">
          <span>{{ fileTypeTrans(scope.row.shareFileType)   }}</span>
        </template>
      </el-table-column>

      <el-table-column label="失效时间" align="left" prop="endTime"  >
        <template #default="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="添加时间" align="center" prop="addTime">
        <template #default="scope">
          <span>{{ parseDatetime(scope.row.addTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <a
            target="_blank"
            style="text-decoration:none; font-size:12px;color:#005CD4;margin-left: 15px"
            :href="getDownloadFilePathlocal(scope.row)"
            :download="scope.shareFileFullName"
            class="downloadDataMode"
          ><i class="el-icon-download"></i>下载
          </a>
        </template>
      </el-table-column>
    </el-table>

  </div>
</template>

<script  setup name="FileShare">
import { ref, reactive, onMounted, computed} from 'vue';
import { useRouter, useRoute } from 'vue-router';
const { proxy } = getCurrentInstance();

import {listFileShareNew,getShareInfo } from "@/api/report/FileShare";
import  Condition  from "@/api/search/condition";
import  searchParam  from "@/api/search/searchform";

const download_file_url = ref(import.meta.env.VITE_APP_BASE_API + '/api/infra/data/report/BusinessModel/downloadFile?&filePath=');

const shareStep = ref(0);

const loading = ref(false);


const fileList = ref([]);

// 【数据分享】表格数据
const FileShareList = ref([]);

// 搜索参数
const searchParams = ref([]);

const data = reactive({

      // 文件分享对话框数据
      dialogShareFile: {
        visible: false,
        codeForm: {
          oldExtractionCode: '',
          extractionCode: ''
        },
        codeFormRules: {
          extractionCode: [
            {
              required: true,
              message: '请输入提取码',
              trigger: 'blur'
            }
          ]
        }
      },

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
        shareBatchNum: Condition.eq(),
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
      }
});

const { dialogShareFile, queryParams, queryParamsConfig} = toRefs(data);

// 页面加载后触发
onMounted(() => {
  const params = proxy.$route.params;

  getShareInfoF(params.shareBatchNum);
})

let shareBatchNum = computed( () =>{
  const params = proxy.$route.params;
  return params.shareBatchNum
});



function  getShareInfoF(shareBatchNum){
    FileShareList.value = null ;
    if( shareBatchNum.value != null || shareBatchNum.value != "" ){
      getShareInfo(shareBatchNum.value).then(response => {
        if( response.code == 200 )
        {
          if(response.data.ifCode == 1)
          {
            dialogShareFile.value.visible = true;
            shareStep.value = 2 ;
            dialogShareFile.value.codeForm.oldExtractionCode = response.data.extractionCode;
          }else if(response.data.ifCode == 0){
            getList();
          }
        }else if( response.code == 501 ){
          dialogShareFile.value.visible =true;
          shareStep.value =1 ;
        }
        else{
          proxy.$modal.msgError(response.msg)
        }
      });
    }
}

function  handleSubmitBtnClick(extractionCode){
    if( extractionCode === dialogShareFile.value.codeForm.oldExtractionCode ){
      getList();
    }else{
      proxy.$modal.msgError("验证码无效!无法查看文件，请联系管理员!")
    }
}

function  getDownloadFilePathlocal(row){
    return download_file_url.value + row.storageFilePath + "&fileName=" + row.shareFileFullName;
}

function  getList() {
    const params = proxy.$route.params;
    queryParams.value.shareBatchNum = params.shareBatchNum;
    searchParams.value = searchParam(queryParamsConfig.value, queryParams.value);
    loading.value = true;
    listFileShareNew(searchParams.value).then(response => {
      FileShareList.value = response.rows;
      total.value = response.total;
      loading.value = false;
      dialogShareFile.value.visible = false;
    });
}




function  fileTypeTrans(value) {
  if (value === null || value === '') {
    return '';
  }
  switch (value) {
    case 0:
      return "业务模型";
    default:
      return "上报文件";
  }
}

</script>
