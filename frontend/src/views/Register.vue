<template>
  <div class="register-scene">
    <div class="bg-particles">
      <div v-for="n in 20" :key="n" class="particle" :style="particleStyle(n)" />
    </div>

    <div class="register-box fade-in-up">
      <div class="logo-area">
        <h1 class="logo-title">CREATE IDENTITY</h1>
        <p class="logo-sub">注册你的剧本杀身份档案</p>
      </div>

      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="设置你的代号" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="registerForm.realName" placeholder="真实姓名" size="large" prefix-icon="UserFilled" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="registerForm.phone" placeholder="联系方式" size="large" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="设置通行密钥" size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认通行密钥" size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <button type="button" class="neon-btn" :class="{ loading }" @click="handleRegister" :disabled="loading">
            <span class="btn-text">{{ loading ? '档案生成中...' : '创建身份档案' }}</span>
          </button>
        </el-form-item>
        <div class="register-footer">
          <span>已有入场资格？</span>
          <a class="neon-link" @click="$router.push('/login')">立即登录</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  realName: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const particleStyle = (n) => ({
  left: `${Math.random() * 100}%`,
  top: `${Math.random() * 100}%`,
  width: `${2 + Math.random() * 3}px`,
  height: `${2 + Math.random() * 3}px`,
  animationDelay: `${Math.random() * 6}s`,
  animationDuration: `${4 + Math.random() * 6}s`
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次密钥输入不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请设置代号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请设置密钥', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密钥', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.register(registerForm)
        if (success) {
          ElMessage.success('身份档案创建成功')
          router.push('/login')
        }
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.register-scene {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(ellipse at 80% 50%, rgba(180, 74, 255, 0.06) 0%, transparent 50%),
              radial-gradient(ellipse at 20% 80%, rgba(0, 229, 255, 0.05) 0%, transparent 50%),
              var(--sk-bg-deepest);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.bg-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .particle {
    position: absolute;
    background: var(--sk-neon-purple);
    border-radius: 50%;
    opacity: 0;
    animation: particleDrift linear infinite;
  }
}

@keyframes particleDrift {
  0% { opacity: 0; transform: translateY(0) scale(0); }
  10% { opacity: 0.5; transform: scale(1); }
  90% { opacity: 0.2; }
  100% { opacity: 0; transform: translateY(-100vh) scale(0.5); }
}

.register-box {
  width: 100%;
  max-width: 420px;
  padding: 44px 36px;
  position: relative;
  z-index: 1;
  background: linear-gradient(135deg, rgba(15, 19, 40, 0.95), rgba(10, 14, 26, 0.9));
  border: 1px solid rgba(180, 74, 255, 0.15);
  border-radius: 20px;
  backdrop-filter: blur(20px);
  box-shadow: 0 0 40px rgba(180, 74, 255, 0.08),
              0 24px 48px rgba(0, 0, 0, 0.4);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 20%;
    right: 20%;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--sk-neon-purple), transparent);
  }
}

.logo-area {
  text-align: center;
  margin-bottom: 32px;

  .logo-title {
    font-family: var(--sk-font-display);
    font-size: 24px;
    font-weight: 900;
    color: var(--sk-neon-purple);
    letter-spacing: 3px;
    text-shadow: 0 0 20px rgba(180, 74, 255, 0.5);
    margin-bottom: 8px;
  }

  .logo-sub {
    font-size: 13px;
    color: var(--sk-text-muted);
    letter-spacing: 2px;
  }
}

.register-form {
  .el-form-item {
    margin-bottom: 18px;
  }
}

.neon-btn {
  width: 100%;
  height: 48px;
  border: 1px solid rgba(180, 74, 255, 0.4);
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(180, 74, 255, 0.12), rgba(180, 74, 255, 0.04));
  color: var(--sk-neon-purple);
  font-size: 16px;
  font-weight: 600;
  font-family: var(--sk-font-body);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 2px;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, rgba(180, 74, 255, 0.25), rgba(180, 74, 255, 0.1));
    box-shadow: 0 0 30px rgba(180, 74, 255, 0.3);
    transform: translateY(-2px);
  }

  &.loading {
    opacity: 0.7;
    cursor: wait;
  }
}

.register-footer {
  text-align: center;
  margin-top: 12px;
  color: var(--sk-text-muted);
  font-size: 14px;

  .neon-link {
    color: var(--sk-neon-purple);
    cursor: pointer;
    margin-left: 4px;
    transition: all 0.3s;
    text-decoration: none;

    &:hover {
      text-shadow: 0 0 8px rgba(180, 74, 255, 0.5);
    }
  }
}
</style>
