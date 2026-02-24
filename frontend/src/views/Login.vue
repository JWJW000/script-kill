<template>
  <div class="login-scene">
    <div class="bg-particles">
      <div v-for="n in 30" :key="n" class="particle" :style="particleStyle(n)" />
    </div>
    <div class="scan-overlay" />

    <div class="login-box fade-in-up">
      <div class="logo-area">
        <div class="logo-icon">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="24" cy="24" r="22" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
            <path d="M24 6 L30 18 L42 20 L33 29 L35 42 L24 36 L13 42 L15 29 L6 20 L18 18 Z" fill="currentColor" opacity="0.15" stroke="currentColor" stroke-width="1.2"/>
            <circle cx="24" cy="24" r="6" fill="currentColor" opacity="0.8"/>
          </svg>
        </div>
        <h1 class="logo-title">SCRIPT KILL</h1>
        <p class="logo-sub">沉浸式剧本杀预约平台</p>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="输入你的代号"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="输入通行密钥"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <button type="button" class="neon-btn" :class="{ loading }" @click="handleLogin" :disabled="loading">
            <span class="btn-text">{{ loading ? '身份验证中...' : '进入剧本世界' }}</span>
            <span class="btn-glow" />
          </button>
        </el-form-item>
        <div class="login-footer">
          <span>尚未获得入场资格？</span>
          <a class="neon-link" @click="$router.push('/register')">立即注册</a>
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
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const particleStyle = (n) => ({
  left: `${Math.random() * 100}%`,
  top: `${Math.random() * 100}%`,
  width: `${2 + Math.random() * 3}px`,
  height: `${2 + Math.random() * 3}px`,
  animationDelay: `${Math.random() * 6}s`,
  animationDuration: `${4 + Math.random() * 6}s`
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.login(loginForm)
        if (success) {
          ElMessage.success('身份验证通过，正在进入...')
          const role = userStore.role || userStore.userInfo?.role
          if (role === 'PLAYER') {
            router.push('/player')
          } else if (role === 'HOST') {
            router.push('/host')
          } else if (role === 'ADMIN') {
            router.push('/admin')
          } else {
            try {
              await userStore.getUserInfo()
              const retryRole = userStore.role || userStore.userInfo?.role
              if (retryRole === 'PLAYER') router.push('/player')
              else if (retryRole === 'HOST') router.push('/host')
              else if (retryRole === 'ADMIN') router.push('/admin')
              else ElMessage.warning('无法识别身份，请重试')
            } catch (error) {
              ElMessage.error('身份验证异常')
            }
          }
        }
      } catch (error) {
        ElMessage.error(error.message || '验证失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-scene {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(ellipse at 20% 50%, rgba(0, 229, 255, 0.06) 0%, transparent 50%),
              radial-gradient(ellipse at 80% 20%, rgba(180, 74, 255, 0.05) 0%, transparent 50%),
              radial-gradient(ellipse at 50% 80%, rgba(255, 45, 120, 0.04) 0%, transparent 50%),
              var(--sk-bg-deepest);
  position: relative;
  overflow: hidden;
}

.bg-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .particle {
    position: absolute;
    background: var(--sk-neon-cyan);
    border-radius: 50%;
    opacity: 0;
    animation: particleDrift linear infinite;
  }
}

@keyframes particleDrift {
  0% { opacity: 0; transform: translateY(0) scale(0); }
  10% { opacity: 0.6; transform: scale(1); }
  90% { opacity: 0.3; }
  100% { opacity: 0; transform: translateY(-100vh) scale(0.5); }
}

.scan-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 229, 255, 0.03) 50%, transparent 100%);
  height: 30%;
  animation: scan-line 6s linear infinite;
  pointer-events: none;
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 48px 36px;
  position: relative;
  z-index: 1;
  background: linear-gradient(135deg, rgba(15, 19, 40, 0.95), rgba(10, 14, 26, 0.9));
  border: 1px solid rgba(0, 229, 255, 0.15);
  border-radius: 20px;
  backdrop-filter: blur(20px);
  box-shadow: 0 0 40px rgba(0, 229, 255, 0.08),
              0 24px 48px rgba(0, 0, 0, 0.4);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 20%;
    right: 20%;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--sk-neon-cyan), transparent);
  }
}

.logo-area {
  text-align: center;
  margin-bottom: 36px;

  .logo-icon {
    width: 56px;
    height: 56px;
    margin: 0 auto 16px;
    color: var(--sk-neon-cyan);
    filter: drop-shadow(0 0 12px rgba(0, 229, 255, 0.5));
    animation: neon-flicker 4s ease-in-out infinite;

    svg {
      width: 100%;
      height: 100%;
    }
  }

  .logo-title {
    font-family: var(--sk-font-display);
    font-size: 28px;
    font-weight: 900;
    color: var(--sk-neon-cyan);
    letter-spacing: 4px;
    text-shadow: 0 0 20px rgba(0, 229, 255, 0.5),
                 0 0 40px rgba(0, 229, 255, 0.2);
    margin-bottom: 8px;
  }

  .logo-sub {
    font-size: 13px;
    color: var(--sk-text-muted);
    letter-spacing: 3px;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 22px;
  }
}

.neon-btn {
  width: 100%;
  height: 48px;
  border: 1px solid rgba(0, 229, 255, 0.4);
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(0, 229, 255, 0.12), rgba(0, 229, 255, 0.04));
  color: var(--sk-neon-cyan);
  font-size: 16px;
  font-weight: 600;
  font-family: var(--sk-font-body);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 2px;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, rgba(0, 229, 255, 0.25), rgba(0, 229, 255, 0.1));
    box-shadow: 0 0 30px rgba(0, 229, 255, 0.3), 0 0 60px rgba(0, 229, 255, 0.1);
    transform: translateY(-2px);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &.loading {
    opacity: 0.7;
    cursor: wait;
  }

  .btn-text {
    position: relative;
    z-index: 1;
  }

  .btn-glow {
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(0, 229, 255, 0.15) 0%, transparent 70%);
    opacity: 0;
    transition: opacity 0.4s;
  }

  &:hover .btn-glow {
    opacity: 1;
  }
}

.login-footer {
  text-align: center;
  margin-top: 16px;
  color: var(--sk-text-muted);
  font-size: 14px;

  .neon-link {
    color: var(--sk-neon-cyan);
    cursor: pointer;
    margin-left: 4px;
    transition: all 0.3s;
    text-decoration: none;

    &:hover {
      text-shadow: 0 0 8px rgba(0, 229, 255, 0.5);
    }
  }
}
</style>
