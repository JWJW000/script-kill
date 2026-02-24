<template>
  <div class="orders-page">
    <div class="page-header fade-in-up">
      <h2 class="page-title">任务记录</h2>
    </div>

    <div class="tabs-bar fade-in-up stagger-1">
      <span v-for="tab in tabs" :key="tab.value" class="tab-item" :class="{ active: activeTab === tab.value }" @click="activeTab = tab.value; loadOrders()">
        {{ tab.label }}
      </span>
    </div>

    <div class="orders-list fade-in-up stagger-2">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-left">
          <div class="order-no"># {{ order.orderNo }}</div>
          <div class="order-script">{{ order.scriptName || '未知剧本' }}</div>
          <div class="order-meta">
            <span>{{ formatTime(order.sessionTime || order.createTime) }}</span>
            <span>{{ order.playerCount }}人</span>
          </div>
        </div>
        <div class="order-right">
          <div class="order-amount">¥{{ order.totalAmount }}</div>
          <div class="order-tags">
            <span class="neon-tag" :class="'neon-tag--' + statusColor(order.orderStatus)">{{ statusText(order.orderStatus) }}</span>
            <span v-if="order.reviewed" class="neon-tag neon-tag--cyan">已评价</span>
          </div>
          <div class="order-actions">
            <button v-if="order.orderStatus === 0" class="action-btn primary" @click="openPayDialog(order)">支付</button>
            <button v-if="order.orderStatus === 0" class="action-btn danger" @click="handleCancel(order)">取消</button>
            <button
              v-if="(order.orderStatus === 1 || order.orderStatus === 3) && !order.reviewed"
              class="action-btn ghost"
              @click="openReviewDrawer(order)"
            >
              评价
            </button>
            <span v-else-if="order.reviewed" class="reviewed-text">已评价</span>
          </div>
        </div>
      </div>
      <div v-if="orders.length === 0" class="empty-state">暂无订单记录</div>
    </div>

    <!-- 支付弹窗 -->
    <el-dialog v-model="showPayDialog" title="订单支付" width="400px">
      <div v-if="payingOrder" class="pay-content">
        <div class="pay-info">
          <span>订单号</span>
          <span>{{ payingOrder.orderNo }}</span>
        </div>
        <div class="pay-info">
          <span>金额</span>
          <span class="pay-amount">¥{{ payingOrder.totalAmount }}</span>
        </div>
        <div class="pay-methods">
          <label>支付方式</label>
          <div class="method-options">
            <div class="method-item" :class="{ active: payMethod === 'WECHAT' }" @click="payMethod = 'WECHAT'">
              <span class="method-icon" style="color: #07c160;">W</span>
              微信支付
            </div>
            <div class="method-item" :class="{ active: payMethod === 'ALIPAY' }" @click="payMethod = 'ALIPAY'">
              <span class="method-icon" style="color: #1677ff;">A</span>
              支付宝
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPayDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePay" :loading="paying">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 评价抽屉 -->
    <el-drawer v-model="showReviewDrawer" title="发表评价" size="420px">
      <div class="review-form" v-if="reviewingOrder">
        <el-form :model="reviewForm" label-position="top">
          <el-form-item label="剧本评分">
            <el-rate v-model="reviewForm.scriptRating" :max="5" size="large" />
          </el-form-item>
          <el-form-item label="剧本评价">
            <el-input v-model="reviewForm.scriptComment" type="textarea" :rows="3" placeholder="聊聊这个剧本的体验..." />
          </el-form-item>
          <el-form-item label="主持人评分">
            <el-rate v-model="reviewForm.hostRating" :max="5" size="large" />
          </el-form-item>
          <el-form-item label="主持人评价">
            <el-input v-model="reviewForm.hostComment" type="textarea" :rows="3" placeholder="对主持人的表现有什么看法..." />
          </el-form-item>
          <button type="button" class="submit-btn" @click="handleReview">提交评价</button>
        </el-form>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, payOrder, cancelOrder } from '@/api/order'
import { createReview } from '@/api/review'

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: '0' },
  { label: '已支付', value: '1' },
  { label: '已取消', value: '2' },
  { label: '已完成', value: '3' }
]

const activeTab = ref('all')
const orders = ref([])
const showPayDialog = ref(false)
const payingOrder = ref(null)
const payMethod = ref('WECHAT')
const paying = ref(false)
const showReviewDrawer = ref(false)
const reviewingOrder = ref(null)
const reviewForm = reactive({
  scriptRating: 5,
  scriptComment: '',
  hostRating: 5,
  hostComment: ''
})

const loadOrders = async () => {
  try {
    const orderStatus = activeTab.value === 'all' ? null : parseInt(activeTab.value)
    const res = await getOrderList(orderStatus)
    if (res.code === 200) orders.value = res.data || []
  } catch (e) { console.error(e) }
}

const openPayDialog = (order) => {
  payingOrder.value = order
  payMethod.value = 'WECHAT'
  showPayDialog.value = true
}

const handlePay = async () => {
  if (!payingOrder.value) return
  paying.value = true
  try {
    const res = await payOrder(payingOrder.value.orderNo, payMethod.value)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      showPayDialog.value = false
      loadOrders()
    }
  } catch (e) { ElMessage.error(e.message || '支付失败') }
  finally { paying.value = false }
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '取消订单', { type: 'warning' })
    const res = await cancelOrder(order.orderNo)
    if (res.code === 200) { ElMessage.success('已取消'); loadOrders() }
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '取消失败') }
}

const openReviewDrawer = (order) => {
  reviewingOrder.value = order
  Object.assign(reviewForm, { scriptRating: 5, scriptComment: '', hostRating: 5, hostComment: '' })
  showReviewDrawer.value = true
}

const handleReview = async () => {
  if (!reviewingOrder.value) return
  try {
    const res = await createReview({
      orderId: reviewingOrder.value.id,
      scriptId: reviewingOrder.value.scriptId,
      hostId: reviewingOrder.value.hostId,
      ...reviewForm
    })
    if (res.code === 200) {
      ElMessage.success('评价已提交')
      showReviewDrawer.value = false
      loadOrders()
    }
  } catch (e) { ElMessage.error(e.message || '提交失败') }
}

const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''
const statusText = (s) => ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已完成' }[s] || '未知')
const statusColor = (s) => ({ 0: 'amber', 1: 'green', 2: 'red', 3: 'cyan' }[s] || 'cyan')

onMounted(() => { loadOrders() })
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 20px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); }
}

.tabs-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 24px;
  padding: 4px;
  background: rgba(15, 19, 40, 0.6);
  border-radius: 12px;
  border: 1px solid var(--sk-border);
  width: fit-content;

  .tab-item {
    padding: 8px 20px;
    border-radius: 8px;
    font-size: 13px;
    color: var(--sk-text-muted);
    cursor: pointer;
    transition: all 0.3s;

    &:hover { color: var(--sk-text-secondary); }
    &.active {
      background: rgba(0, 229, 255, 0.1);
      color: var(--sk-neon-cyan);
      font-weight: 600;
    }
  }
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .order-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    transition: all 0.3s;

    &:hover {
      border-color: var(--sk-border-glow);
      transform: translateX(4px);
    }

    .order-left {
      .order-no { font-size: 11px; color: var(--sk-text-muted); font-family: var(--sk-font-display); letter-spacing: 1px; margin-bottom: 6px; }
      .order-script { font-size: 16px; font-weight: 700; color: var(--sk-text-primary); margin-bottom: 6px; }
      .order-meta {
        font-size: 13px; color: var(--sk-text-muted);
        span { margin-right: 16px; }
      }
    }

    .order-right {
      text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 8px;
      .order-amount { font-size: 20px; font-weight: 900; color: var(--sk-neon-pink); font-family: var(--sk-font-display); }
      .order-tags {
        display: flex;
        align-items: center;
        gap: 6px;
        justify-content: flex-end;
      }
      .order-actions {
        display: flex; gap: 8px;
        .action-btn {
          padding: 5px 16px; border-radius: 6px; font-size: 12px; font-weight: 600;
          cursor: pointer; transition: all 0.3s; font-family: var(--sk-font-body);

          &.primary {
            background: rgba(0, 229, 255, 0.1); color: var(--sk-neon-cyan); border: 1px solid rgba(0, 229, 255, 0.3);
            &:hover { box-shadow: var(--sk-glow-cyan); }
          }
          &.danger {
            background: rgba(255, 23, 68, 0.1); color: var(--sk-neon-red); border: 1px solid rgba(255, 23, 68, 0.3);
            &:hover { box-shadow: var(--sk-glow-pink); }
          }
          &.ghost {
            background: transparent; color: var(--sk-text-secondary); border: 1px solid var(--sk-border);
            &:hover { border-color: var(--sk-neon-purple); color: var(--sk-neon-purple); }
          }
        }
        .reviewed-text {
          font-size: 12px;
          color: var(--sk-text-secondary);
          align-self: center;
        }
      }
    }
  }
}

.pay-content {
  .pay-info {
    display: flex; justify-content: space-between; padding: 12px 0;
    border-bottom: 1px solid var(--sk-border);
    color: var(--sk-text-secondary); font-size: 14px;
    .pay-amount { color: var(--sk-neon-pink); font-weight: 900; font-size: 20px; font-family: var(--sk-font-display); }
  }
  .pay-methods {
    margin-top: 20px;
    label { display: block; color: var(--sk-text-secondary); margin-bottom: 12px; }
    .method-options { display: flex; gap: 12px; }
    .method-item {
      flex: 1; padding: 14px; border-radius: 10px;
      border: 1px solid var(--sk-border); text-align: center;
      cursor: pointer; transition: all 0.3s;
      color: var(--sk-text-secondary); font-size: 13px;
      display: flex; align-items: center; justify-content: center; gap: 8px;

      .method-icon { font-weight: 900; font-size: 18px; }
      &:hover { border-color: var(--sk-border-glow); }
      &.active { border-color: var(--sk-neon-cyan); background: rgba(0, 229, 255, 0.06); color: var(--sk-neon-cyan); }
    }
  }
}

.review-form {
  .submit-btn {
    width: 100%; padding: 14px; border-radius: 12px; margin-top: 8px;
    background: linear-gradient(135deg, rgba(0, 229, 255, 0.15), rgba(0, 229, 255, 0.05));
    color: var(--sk-neon-cyan); border: 1px solid rgba(0, 229, 255, 0.35);
    font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.3s;
    font-family: var(--sk-font-body);
    &:hover { box-shadow: var(--sk-glow-cyan); }
  }
}

.empty-state { text-align: center; padding: 60px; color: var(--sk-text-muted); }
</style>
