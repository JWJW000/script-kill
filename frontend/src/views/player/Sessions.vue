<template>
  <div class="sessions-page">
    <div class="page-header fade-in-up">
      <h2 class="page-title">场次预约</h2>
      <p class="page-desc">选择你的时间线，开启一段新的旅程</p>
    </div>

    <div class="sessions-timeline fade-in-up stagger-1">
      <div v-for="session in sessions" :key="session.id" class="timeline-card">
        <div class="card-left">
          <div class="time-badge">
            <span class="time-date">{{ formatDate(session.sessionTime) }}</span>
            <span class="time-hour">{{ formatHour(session.sessionTime) }}</span>
          </div>
        </div>
        <div class="card-center">
          <div class="session-title">{{ session.scriptName || '未知剧本' }}</div>
          <div class="session-meta">
            <span class="meta-item"><el-icon><Location /></el-icon>{{ session.location }}</span>
            <span class="meta-item"><el-icon><User /></el-icon>{{ session.currentPlayers }}/{{ session.maxPlayers }}人</span>
          </div>
          <div class="session-progress">
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: (session.currentPlayers / session.maxPlayers * 100) + '%' }" />
            </div>
          </div>
        </div>
        <div class="card-right">
          <div class="price">¥{{ session.price }}</div>
          <span v-if="session.userBooked" class="neon-tag neon-tag--cyan booked-tag">已预约</span>
          <span v-else class="neon-tag" :class="'neon-tag--' + statusColor(session.status)">{{ statusText(session.status) }}</span>
          <button v-if="session.userBooked" class="book-btn booked" disabled>已预约</button>
          <button v-else-if="session.status === 1" class="book-btn" @click="startBooking(session)">立即预约</button>
        </div>
      </div>
      <div v-if="sessions.length === 0" class="empty-state"><p>暂无可预约的场次</p></div>
    </div>

    <!-- 预约+支付一体化弹窗 -->
    <el-dialog v-model="showBookDialog" :title="stepTitles[currentStep]" width="520px" :close-on-click-modal="false" @closed="resetBooking">
      <!-- 步骤指示器 -->
      <div class="step-indicator">
        <div v-for="(s, i) in stepTitles" :key="i" class="step-dot" :class="{ active: currentStep === i, done: currentStep > i }">
          <span class="dot-inner">{{ currentStep > i ? '✓' : i + 1 }}</span>
          <span class="dot-label">{{ s }}</span>
        </div>
        <div class="step-line">
          <div class="step-line-fill" :style="{ width: (currentStep / (stepTitles.length - 1) * 100) + '%' }" />
        </div>
      </div>

      <!-- Step 1: 选择人数 -->
      <div v-if="currentStep === 0" class="step-content">
        <div class="booking-info">
          <div class="info-card">
            <div class="info-title">{{ bookingSession?.scriptName }}</div>
            <div class="info-meta">
              <span><el-icon><Clock /></el-icon>{{ formatDateTime(bookingSession?.sessionTime) }}</span>
              <span><el-icon><Location /></el-icon>{{ bookingSession?.location }}</span>
            </div>
          </div>
        </div>
        <div class="form-section">
          <label>预约人数</label>
          <div class="player-count-selector">
            <button class="count-btn" :disabled="bookPlayerCount <= 1" @click="bookPlayerCount--">−</button>
            <span class="count-value">{{ bookPlayerCount }}</span>
            <button class="count-btn" :disabled="bookPlayerCount >= remainingSlots" @click="bookPlayerCount++">+</button>
          </div>
          <span class="slots-hint">剩余 {{ remainingSlots }} 个名额</span>
        </div>
        <div class="price-summary">
          <div class="price-row">
            <span>单价</span>
            <span>¥{{ bookingSession?.price }}</span>
          </div>
          <div class="price-row">
            <span>人数</span>
            <span>× {{ bookPlayerCount }}</span>
          </div>
          <div class="price-row total">
            <span>合计</span>
            <span class="total-amount">¥{{ totalAmount }}</span>
          </div>
        </div>
      </div>

      <!-- Step 2: 选择支付方式 -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="order-summary">
          <div class="summary-row"><span>剧本</span><span>{{ bookingSession?.scriptName }}</span></div>
          <div class="summary-row"><span>时间</span><span>{{ formatDateTime(bookingSession?.sessionTime) }}</span></div>
          <div class="summary-row"><span>人数</span><span>{{ bookPlayerCount }}人</span></div>
          <div class="summary-row highlight"><span>应付金额</span><span class="amount">¥{{ totalAmount }}</span></div>
        </div>
        <div class="pay-methods">
          <label>选择支付方式</label>
          <div class="method-grid">
            <div class="method-card" :class="{ active: payMethod === 'WECHAT' }" @click="payMethod = 'WECHAT'">
              <div class="method-icon wechat">
                <svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28"><path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05a6.093 6.093 0 0 1-.273-1.82c0-3.571 3.307-6.468 7.382-6.468.342 0 .674.028 1.002.07C16.888 4.142 13.12 2.188 8.691 2.188zm-2.39 4.401a1.028 1.028 0 1 1 0 2.056 1.028 1.028 0 0 1 0-2.056zm4.798 0a1.028 1.028 0 1 1 0 2.056 1.028 1.028 0 0 1 0-2.056zm5.142 3.529c-3.57 0-6.467 2.554-6.467 5.7 0 3.15 2.897 5.7 6.467 5.7.715 0 1.398-.11 2.042-.29a.656.656 0 0 1 .544.073l1.36.815c.044.03.094.047.147.047.112 0 .206-.094.206-.206a.35.35 0 0 0-.034-.147l-.276-1.06a.455.455 0 0 1 .157-.508C22.07 19.393 23 17.696 23 15.818c0-3.146-2.896-5.7-6.467-5.7h-.292zm-1.859 2.973a.857.857 0 1 1 0 1.715.857.857 0 0 1 0-1.715zm3.722 0a.857.857 0 1 1 0 1.715.857.857 0 0 1 0-1.715z"/></svg>
              </div>
              <span>微信支付</span>
              <div class="check-mark" v-if="payMethod === 'WECHAT'">✓</div>
            </div>
            <div class="method-card" :class="{ active: payMethod === 'ALIPAY' }" @click="payMethod = 'ALIPAY'">
              <div class="method-icon alipay">
                <svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28"><path d="M21.422 15.358c-1.988-.762-3.234-1.252-4.14-1.63.286-.594.536-1.217.742-1.864h-3.853v-1.08h4.842V9.852h-4.842V8.078h-2.222v1.774H7.106v.932h4.843v1.08H7.766v.932h6.748a12.882 12.882 0 0 1-.594 1.378c-1.656-.624-3.468-.936-4.77-.168-1.224.726-1.614 2.142-.876 3.264.738 1.122 2.478 1.488 4.032.672.918-.486 1.62-1.32 2.172-2.226 1.476.666 4.512 1.95 4.512 1.95s.486.216.486-.174v-.354c0-.33-.054-.5-.054-.5zM10.14 17.478c-1.35.558-2.622.228-2.874-.39-.252-.618.402-1.476 1.698-1.836.948-.258 1.89-.012 2.808.402-.498.93-1.044 1.572-1.632 1.824z M21.6 3H2.4A2.4 2.4 0 0 0 0 5.4v13.2A2.4 2.4 0 0 0 2.4 21h19.2a2.4 2.4 0 0 0 2.4-2.4V5.4A2.4 2.4 0 0 0 21.6 3z"/></svg>
              </div>
              <span>支付宝</span>
              <div class="check-mark" v-if="payMethod === 'ALIPAY'">✓</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 3: 支付处理中 -->
      <div v-if="currentStep === 2" class="step-content processing">
        <div class="processing-animation">
          <div class="spinner">
            <div class="spinner-ring" />
            <div class="spinner-ring inner" />
          </div>
          <p class="processing-text">{{ processingText }}</p>
          <p class="processing-sub">请勿关闭页面...</p>
        </div>
      </div>

      <!-- Step 4: 支付成功 -->
      <div v-if="currentStep === 3" class="step-content success">
        <div class="success-animation">
          <div class="success-circle">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 12 10 16 18 8" /></svg>
          </div>
          <h3 class="success-title">支付成功</h3>
          <p class="success-desc">你的剧本杀之旅即将开始</p>
          <div class="success-detail">
            <div class="detail-row"><span>订单号</span><span class="mono">{{ createdOrderNo }}</span></div>
            <div class="detail-row"><span>剧本</span><span>{{ bookingSession?.scriptName }}</span></div>
            <div class="detail-row"><span>时间</span><span>{{ formatDateTime(bookingSession?.sessionTime) }}</span></div>
            <div class="detail-row"><span>支付金额</span><span class="amount">¥{{ totalAmount }}</span></div>
            <div class="detail-row"><span>支付方式</span><span>{{ payMethod === 'WECHAT' ? '微信支付' : '支付宝' }}</span></div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="currentStep === 0" @click="showBookDialog = false">取消</el-button>
          <el-button v-if="currentStep === 1" @click="currentStep = 0">上一步</el-button>
          <el-button v-if="currentStep === 0" type="primary" @click="goToPayStep">下一步：选择支付</el-button>
          <el-button v-if="currentStep === 1" type="primary" @click="executePayment">确认支付 ¥{{ totalAmount }}</el-button>
          <el-button v-if="currentStep === 3" type="primary" @click="finishBooking">完成</el-button>
          <el-button v-if="currentStep === 3" @click="goToOrders">查看订单</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSessionList } from '@/api/session'
import { createOrder, payOrder } from '@/api/order'

const router = useRouter()
const sessions = ref([])
const showBookDialog = ref(false)
const bookingSession = ref(null)
const bookPlayerCount = ref(1)
const payMethod = ref('WECHAT')
const currentStep = ref(0)
const createdOrderNo = ref('')
const processingText = ref('正在创建订单...')

const stepTitles = ['选择人数', '确认支付', '支付处理', '支付成功']

const remainingSlots = computed(() => {
  if (!bookingSession.value) return 0
  return bookingSession.value.maxPlayers - bookingSession.value.currentPlayers
})

const totalAmount = computed(() => {
  if (!bookingSession.value) return '0.00'
  return (bookingSession.value.price * bookPlayerCount.value).toFixed(2)
})

const loadSessions = async () => {
  try {
    const res = await getSessionList({})
    if (res.code === 200) sessions.value = res.data || []
  } catch (e) { console.error(e) }
}

const startBooking = (session) => {
  bookingSession.value = session
  bookPlayerCount.value = 1
  payMethod.value = 'WECHAT'
  currentStep.value = 0
  createdOrderNo.value = ''
  showBookDialog.value = true
}

const goToPayStep = () => {
  if (bookPlayerCount.value < 1) {
    ElMessage.warning('请选择预约人数')
    return
  }
  currentStep.value = 1
}

const executePayment = async () => {
  currentStep.value = 2
  processingText.value = '正在创建订单...'

  try {
    // Step 1: 创建订单
    const orderRes = await createOrder(bookingSession.value.id, bookPlayerCount.value)
    if (orderRes.code !== 200) {
      throw new Error(orderRes.message || '创建订单失败')
    }

    const orderNo = orderRes.data?.orderNo || orderRes.data
    createdOrderNo.value = orderNo

    // 模拟短暂延迟，增加真实感
    processingText.value = '正在连接支付通道...'
    await sleep(800)

    processingText.value = payMethod.value === 'WECHAT' ? '微信支付处理中...' : '支付宝处理中...'
    await sleep(600)

    // Step 2: 模拟支付
    const payRes = await payOrder(orderNo, payMethod.value)
    if (payRes.code !== 200) {
      throw new Error(payRes.message || '支付失败')
    }

    processingText.value = '支付确认中...'
    await sleep(500)

    // Step 3: 完成
    currentStep.value = 3
    loadSessions()

  } catch (e) {
    ElMessage.error(e.message || '支付流程异常')
    currentStep.value = 1
  }
}

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

const finishBooking = () => {
  showBookDialog.value = false
}

const goToOrders = () => {
  showBookDialog.value = false
  router.push('/player/orders')
}

const resetBooking = () => {
  currentStep.value = 0
  bookingSession.value = null
  createdOrderNo.value = ''
}

const formatDate = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
const formatHour = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
const formatDateTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''
const statusText = (s) => ({ 0: '已取消', 1: '可预约', 2: '已满员', 3: '已结束' }[s] || '未知')
const statusColor = (s) => ({ 0: 'red', 1: 'green', 2: 'amber', 3: 'cyan' }[s] || 'cyan')

onMounted(() => { loadSessions() })
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 28px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); margin-bottom: 6px; }
  .page-desc { color: var(--sk-text-muted); font-size: 14px; }
}

.sessions-timeline {
  display: flex; flex-direction: column; gap: 14px;

  .timeline-card {
    display: flex; align-items: center; gap: 20px;
    padding: 20px 24px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border); border-radius: var(--sk-radius-lg);
    transition: all 0.35s;

    &:hover {
      border-color: var(--sk-border-glow);
      transform: translateX(4px);
      box-shadow: -4px 0 20px rgba(0, 229, 255, 0.08);
    }

    .card-left .time-badge {
      display: flex; flex-direction: column; align-items: center;
      padding: 12px 16px; background: rgba(0, 229, 255, 0.06);
      border-radius: 10px; border: 1px solid rgba(0, 229, 255, 0.15); min-width: 80px;
      .time-date { font-size: 12px; color: var(--sk-text-muted); margin-bottom: 2px; }
      .time-hour { font-size: 20px; font-weight: 900; color: var(--sk-neon-cyan); font-family: var(--sk-font-display); }
    }

    .card-center {
      flex: 1;
      .session-title { font-size: 16px; font-weight: 700; color: var(--sk-text-primary); margin-bottom: 8px; }
      .session-meta {
        display: flex; gap: 20px; margin-bottom: 10px;
        .meta-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: var(--sk-text-muted); }
      }
      .progress-track {
        height: 3px; background: var(--sk-bg-elevated); border-radius: 2px; overflow: hidden;
        .progress-fill { height: 100%; background: linear-gradient(90deg, var(--sk-neon-cyan), var(--sk-neon-purple)); border-radius: 2px; transition: width 0.5s; }
      }
    }

    .card-right {
      display: flex; flex-direction: column; align-items: flex-end; gap: 8px;
      .price { font-size: 22px; font-weight: 900; color: var(--sk-neon-pink); font-family: var(--sk-font-display); }
      .book-btn {
        padding: 8px 24px; border-radius: 8px;
        background: linear-gradient(135deg, rgba(0, 229, 255, 0.2), rgba(0, 229, 255, 0.06));
        color: var(--sk-neon-cyan); border: 1px solid rgba(0, 229, 255, 0.35);
        font-weight: 600; cursor: pointer; transition: all 0.3s; font-family: var(--sk-font-body);
        &:hover { box-shadow: var(--sk-glow-cyan); transform: translateY(-1px); }
        &.booked {
          cursor: default; opacity: 1;
          background: rgba(0, 229, 255, 0.15); border-color: var(--sk-neon-cyan);
          color: var(--sk-neon-cyan);
        }
      }
      .booked-tag { font-weight: 600; }
    }
  }
}

// ====== Step Indicator ======
.step-indicator {
  display: flex; justify-content: space-between; align-items: flex-start;
  position: relative; margin-bottom: 32px; padding: 0 20px;

  .step-line {
    position: absolute; top: 14px; left: 50px; right: 50px; height: 2px;
    background: var(--sk-border); z-index: 0;
    .step-line-fill {
      height: 100%; background: var(--sk-neon-cyan);
      transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 0 8px rgba(0, 229, 255, 0.4);
    }
  }

  .step-dot {
    display: flex; flex-direction: column; align-items: center; gap: 6px;
    position: relative; z-index: 1;

    .dot-inner {
      width: 28px; height: 28px; border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 12px; font-weight: 700;
      background: var(--sk-bg-card); border: 2px solid var(--sk-border);
      color: var(--sk-text-muted); transition: all 0.3s;
    }
    .dot-label { font-size: 11px; color: var(--sk-text-muted); transition: color 0.3s; }

    &.active .dot-inner {
      border-color: var(--sk-neon-cyan); color: var(--sk-neon-cyan);
      box-shadow: 0 0 12px rgba(0, 229, 255, 0.4);
      animation: pulse-glow 2s infinite;
    }
    &.active .dot-label { color: var(--sk-neon-cyan); }

    &.done .dot-inner {
      border-color: var(--sk-neon-green); color: var(--sk-neon-green);
      background: rgba(0, 230, 118, 0.1);
    }
    &.done .dot-label { color: var(--sk-neon-green); }
  }
}

// ====== Step Content ======
.step-content { min-height: 200px; }

.booking-info .info-card {
  padding: 16px; background: rgba(0, 229, 255, 0.04);
  border: 1px solid rgba(0, 229, 255, 0.12); border-radius: 10px; margin-bottom: 24px;
  .info-title { font-size: 18px; font-weight: 700; color: var(--sk-text-primary); margin-bottom: 8px; }
  .info-meta {
    display: flex; gap: 20px; font-size: 13px; color: var(--sk-text-muted);
    span { display: flex; align-items: center; gap: 4px; }
  }
}

.form-section {
  margin-bottom: 24px;
  label { display: block; color: var(--sk-text-secondary); font-size: 14px; margin-bottom: 12px; }
  .player-count-selector {
    display: flex; align-items: center; gap: 16px;
    .count-btn {
      width: 40px; height: 40px; border-radius: 10px;
      border: 1px solid var(--sk-border); background: var(--sk-bg-input);
      color: var(--sk-text-primary); font-size: 20px; cursor: pointer;
      transition: all 0.2s; display: flex; align-items: center; justify-content: center;
      font-family: var(--sk-font-body);
      &:hover:not(:disabled) { border-color: var(--sk-neon-cyan); color: var(--sk-neon-cyan); }
      &:disabled { opacity: 0.3; cursor: not-allowed; }
    }
    .count-value {
      font-size: 32px; font-weight: 900; color: var(--sk-neon-cyan);
      font-family: var(--sk-font-display); min-width: 40px; text-align: center;
    }
  }
  .slots-hint { font-size: 12px; color: var(--sk-text-muted); margin-top: 8px; display: block; }
}

.price-summary {
  background: rgba(15, 19, 40, 0.6); border: 1px solid var(--sk-border);
  border-radius: 10px; padding: 16px;
  .price-row {
    display: flex; justify-content: space-between; padding: 6px 0;
    font-size: 14px; color: var(--sk-text-secondary);
    &.total {
      border-top: 1px solid var(--sk-border); margin-top: 8px; padding-top: 12px;
      font-weight: 700;
    }
    .total-amount { font-size: 24px; font-weight: 900; color: var(--sk-neon-pink); font-family: var(--sk-font-display); }
  }
}

.order-summary {
  background: rgba(15, 19, 40, 0.6); border: 1px solid var(--sk-border);
  border-radius: 10px; padding: 16px; margin-bottom: 24px;
  .summary-row {
    display: flex; justify-content: space-between; padding: 8px 0;
    font-size: 14px; color: var(--sk-text-secondary);
    border-bottom: 1px solid rgba(99, 110, 180, 0.08);
    &:last-child { border-bottom: none; }
    &.highlight { margin-top: 4px; padding-top: 12px; border-top: 1px solid var(--sk-border); border-bottom: none; }
    .amount { font-size: 22px; font-weight: 900; color: var(--sk-neon-pink); font-family: var(--sk-font-display); }
  }
}

.pay-methods {
  label { display: block; color: var(--sk-text-secondary); font-size: 14px; margin-bottom: 12px; }
  .method-grid { display: flex; gap: 14px; }
  .method-card {
    flex: 1; padding: 20px 16px; border-radius: 12px;
    border: 2px solid var(--sk-border); background: var(--sk-bg-input);
    display: flex; flex-direction: column; align-items: center; gap: 10px;
    cursor: pointer; transition: all 0.3s; position: relative;

    .method-icon {
      width: 48px; height: 48px; border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
      &.wechat { background: rgba(7, 193, 96, 0.12); color: #07c160; }
      &.alipay { background: rgba(22, 119, 255, 0.12); color: #1677ff; }
    }
    span { font-size: 14px; color: var(--sk-text-secondary); font-weight: 500; }
    .check-mark {
      position: absolute; top: 8px; right: 10px;
      color: var(--sk-neon-cyan); font-size: 14px; font-weight: 700;
    }

    &:hover { border-color: var(--sk-border-glow); }
    &.active {
      border-color: var(--sk-neon-cyan);
      background: rgba(0, 229, 255, 0.04);
      box-shadow: 0 0 16px rgba(0, 229, 255, 0.12);
      span { color: var(--sk-neon-cyan); }
    }
  }
}

// ====== Processing ======
.processing {
  display: flex; align-items: center; justify-content: center;

  .processing-animation { text-align: center; }

  .spinner {
    width: 80px; height: 80px; margin: 0 auto 24px; position: relative;
    .spinner-ring {
      position: absolute; inset: 0; border: 3px solid transparent;
      border-top-color: var(--sk-neon-cyan); border-radius: 50%;
      animation: spin 1.2s linear infinite;
      &.inner {
        inset: 12px; border-top-color: var(--sk-neon-purple);
        animation-duration: 0.8s; animation-direction: reverse;
      }
    }
  }

  .processing-text { font-size: 16px; color: var(--sk-text-primary); font-weight: 600; margin-bottom: 6px; }
  .processing-sub { font-size: 13px; color: var(--sk-text-muted); }
}

@keyframes spin { from { transform: rotate(0); } to { transform: rotate(360deg); } }

// ====== Success ======
.success {
  display: flex; align-items: center; justify-content: center;

  .success-animation { text-align: center; width: 100%; }

  .success-circle {
    width: 72px; height: 72px; border-radius: 50%;
    border: 3px solid var(--sk-neon-green); margin: 0 auto 20px;
    display: flex; align-items: center; justify-content: center;
    color: var(--sk-neon-green);
    animation: scaleIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    box-shadow: 0 0 24px rgba(0, 230, 118, 0.3);
    svg { width: 36px; height: 36px; }
  }

  .success-title {
    font-size: 22px; font-weight: 800; color: var(--sk-neon-green); margin-bottom: 6px;
    letter-spacing: 1px;
  }
  .success-desc { font-size: 14px; color: var(--sk-text-muted); margin-bottom: 24px; }

  .success-detail {
    background: rgba(0, 230, 118, 0.04); border: 1px solid rgba(0, 230, 118, 0.15);
    border-radius: 10px; padding: 16px; text-align: left;

    .detail-row {
      display: flex; justify-content: space-between; padding: 8px 0;
      font-size: 13px; color: var(--sk-text-secondary);
      border-bottom: 1px solid rgba(99, 110, 180, 0.06);
      &:last-child { border-bottom: none; }
      .mono { font-family: var(--sk-font-display); font-size: 11px; letter-spacing: 0.5px; color: var(--sk-text-muted); }
      .amount { color: var(--sk-neon-pink); font-weight: 700; font-family: var(--sk-font-display); }
    }
  }
}

@keyframes scaleIn {
  from { transform: scale(0); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.dialog-footer { display: flex; justify-content: flex-end; gap: 8px; }
.empty-state { text-align: center; padding: 60px; color: var(--sk-text-muted); }
</style>
