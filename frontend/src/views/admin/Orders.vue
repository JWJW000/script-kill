<template>
  <div class="admin-orders">
    <div class="page-header fade-in-up">
      <h2 class="page-title">订单管理</h2>
    </div>

    <div class="table-card fade-in-up stagger-1">
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="200">
          <template #default="scope">
            <span style="font-family: var(--sk-font-display); font-size: 12px; letter-spacing: 0.5px;">{{ scope.row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" />
        <el-table-column prop="scriptName" label="剧本" />
        <el-table-column prop="playerCount" label="人数" width="80" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="scope"><span style="color: var(--sk-neon-pink); font-weight: 600;">¥{{ scope.row.totalAmount }}</span></template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="100">
          <template #default="scope">
            <span class="neon-tag" :class="'neon-tag--' + statusColor(scope.row.orderStatus)">{{ statusText(scope.row.orderStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button v-if="scope.row.orderStatus === 1" size="small" type="danger" @click="handleRefund(scope.row)">退款</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, refundOrder } from '@/api/order'

const orders = ref([])

const loadOrders = async () => {
  try {
    const res = await getOrderList()
    if (res.code === 200) orders.value = res.data || []
  } catch (e) { console.error(e) }
}

const handleRefund = async (order) => {
  try {
    await ElMessageBox.confirm('确定退款此订单？', '退款确认', { type: 'warning' })
    const res = await refundOrder(order.orderNo)
    if (res.code === 200) { ElMessage.success('退款成功'); loadOrders() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('退款失败') }
}

const statusText = (s) => ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已完成' }[s] || '未知')
const statusColor = (s) => ({ 0: 'amber', 1: 'green', 2: 'red', 3: 'cyan' }[s] || 'cyan')

onMounted(() => { loadOrders() })
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 24px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); }
}
.table-card {
  background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
  border: 1px solid var(--sk-border); border-radius: var(--sk-radius-lg); padding: 20px;
}
</style>
