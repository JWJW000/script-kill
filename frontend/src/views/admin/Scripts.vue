<template>
  <div class="admin-scripts">
    <div class="page-header fade-in-up">
      <h2 class="page-title">剧本管理</h2>
      <el-button type="primary" @click="handleAdd">新增剧本</el-button>
    </div>

    <div class="table-card fade-in-up stagger-1">
      <el-table :data="scripts" style="width: 100%">
        <el-table-column label="封面" width="80">
          <template #default="scope">
            <el-image v-if="scope.row.cover" :src="scope.row.cover" fit="cover" class="cover-thumb" :preview-src-list="[scope.row.cover]" />
            <span v-else class="no-cover">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="剧本名称" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <span class="neon-tag neon-tag--cyan">{{ scope.row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <span class="neon-tag" :class="'neon-tag--' + difficultyColor(scope.row.difficulty)">{{ difficultyText(scope.row.difficulty) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="80">
          <template #default="scope">{{ scope.row.duration }}min</template>
        </el-table-column>
        <el-table-column label="人数" width="80">
          <template #default="scope">{{ scope.row.minPlayers }}-{{ scope.row.maxPlayers }}</template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="90">
          <template #default="scope"><span style="color: var(--sk-neon-pink); font-weight: 600;">¥{{ scope.row.price }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <span class="neon-tag" :class="scope.row.status === 1 ? 'neon-tag--green' : 'neon-tag--red'">{{ scope.row.status === 1 ? '上架' : '下架' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">{{ scope.row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑剧本' : '新增剧本'" width="640px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="剧本名称" prop="name"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" style="width: 100%">
                <el-option v-for="t in ['恐怖', '情感', '硬核', '欢乐', '推理']" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" style="width: 100%">
                <el-option label="新手" value="BEGINNER" /><el-option label="进阶" value="INTERMEDIATE" /><el-option label="烧脑" value="EXPERT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时长(分钟)" prop="duration"><el-input-number v-model="form.duration" :min="60" :max="600" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="最少人数" prop="minPlayers"><el-input-number v-model="form.minPlayers" :min="2" :max="20" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最多人数" prop="maxPlayers"><el-input-number v-model="form.maxPlayers" :min="2" :max="20" style="width: 100%" /></el-form-item></el-col>
        </el-row>

        <!-- 封面图片：新增/编辑均支持上传 -->
        <el-form-item label="封面图片">
          <div class="cover-upload-area">
            <div v-if="form.cover" class="cover-preview">
              <el-image :src="form.cover" fit="cover" class="preview-img" />
              <div class="preview-actions">
                <el-button size="small" type="danger" @click="form.cover = ''">移除</el-button>
              </div>
            </div>
            <el-upload
              v-if="!form.cover"
              class="cover-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="handleCoverUpload"
              accept="image/*"
            >
              <div class="upload-trigger">
                <el-icon :size="24"><Plus /></el-icon>
                <span>{{ isEdit ? '更换封面' : '上传封面' }}</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="简介" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScripts, getScriptById, createScript, updateScript, deleteScript, updateScriptStatus, uploadScriptCover } from '@/api/script'
import { uploadFile } from '@/api/oss'

const scripts = ref([])
const showDialog = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const form = reactive({ id: null, name: '', type: '', difficulty: 'BEGINNER', duration: 180, minPlayers: 4, maxPlayers: 8, price: 0, cover: '', description: '', status: 1 })

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
  minPlayers: [{ required: true, message: '请输入最少人数', trigger: 'blur' }],
  maxPlayers: [{ required: true, message: '请输入最多人数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const loadScripts = async () => {
  try {
    const res = await getScripts({})
    if (res.code === 200) scripts.value = res.data || []
  } catch (e) { console.error(e) }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', type: '', difficulty: 'BEGINNER', duration: 180, minPlayers: 4, maxPlayers: 8, price: 0, cover: '', description: '', status: 1 })
  showDialog.value = true
}

const handleEdit = async (script) => {
  isEdit.value = true
  try {
    const res = await getScriptById(script.id)
    if (res.code === 200 && res.data) { Object.assign(form, res.data); showDialog.value = true }
  } catch (e) { ElMessage.error('加载失败') }
}

const handleCoverUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) { ElMessage.error('只能上传图片'); return false }
  if (file.size > 10 * 1024 * 1024) { ElMessage.error('图片不能超过10MB'); return false }

  try {
    if (isEdit.value && form.id) {
      const res = await uploadScriptCover(form.id, file)
      if (res.code === 200) {
        form.cover = res.data
        ElMessage.success('封面上传成功')
        loadScripts()
      }
    } else {
      const res = await uploadFile(file, 'cover')
      if (res.code === 200 && res.data?.url) {
        form.cover = res.data.url
        ElMessage.success('封面上传成功，保存剧本后将使用该封面')
      }
    }
  } catch (e) {
    ElMessage.error('封面上传失败')
  }
  return false
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = isEdit.value ? await updateScript(form) : await createScript(form)
        if (res.code === 200) { ElMessage.success(isEdit.value ? '更新成功' : '创建成功'); showDialog.value = false; loadScripts() }
      } catch (e) { ElMessage.error('操作失败') }
    }
  })
}

const toggleStatus = async (script) => {
  try {
    const res = await updateScriptStatus(script.id, script.status === 1 ? 0 : 1)
    if (res.code === 200) { ElMessage.success(script.status === 1 ? '已下架' : '已上架'); loadScripts() }
  } catch (e) { ElMessage.error('操作失败') }
}

const handleDelete = async (script) => {
  try {
    await ElMessageBox.confirm('确定删除此剧本？', '删除确认', { type: 'warning' })
    const res = await deleteScript(script.id)
    if (res.code === 200) { ElMessage.success('已删除'); loadScripts() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const difficultyText = (d) => ({ BEGINNER: '新手', INTERMEDIATE: '进阶', EXPERT: '烧脑' }[d] || d)
const difficultyColor = (d) => ({ BEGINNER: 'green', INTERMEDIATE: 'amber', EXPERT: 'red' }[d] || 'cyan')

onMounted(() => { loadScripts() })
</script>

<style scoped lang="scss">
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); }
}
.table-card {
  background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
  border: 1px solid var(--sk-border); border-radius: var(--sk-radius-lg); padding: 20px;
}

.cover-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  border: 1px solid var(--sk-border);
}
.no-cover {
  font-size: 12px;
  color: var(--sk-text-muted);
}

.cover-upload-area {
  display: flex;
  align-items: flex-start;
  gap: 16px;

  .cover-preview {
    position: relative;
    .preview-img {
      width: 120px;
      height: 80px;
      border-radius: 8px;
      border: 1px solid var(--sk-border);
    }
    .preview-actions {
      margin-top: 6px;
      text-align: center;
    }
  }

  .cover-uploader {
    :deep(.el-upload) {
      border: 1px dashed var(--sk-border) !important;
      border-radius: 8px;
      transition: all 0.3s;
      &:hover {
        border-color: var(--sk-neon-cyan) !important;
      }
    }

    .upload-trigger {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 120px;
      height: 80px;
      color: var(--sk-text-muted);
      gap: 4px;
      cursor: pointer;
      transition: color 0.3s;

      span { font-size: 12px; }

      &:hover { color: var(--sk-neon-cyan); }
    }
  }
}
</style>
