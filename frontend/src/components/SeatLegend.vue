<template>
  <div class="seat-legend">
    <span v-for="item in items" :key="item.value" class="seat-legend__item">
      <i class="seat-legend__dot" :class="item.value"></i>
      {{ item.text }}
    </span>
  </div>
</template>

<script setup>
const items = [
  { value: 'AVAILABLE', text: '可预约' },
  { value: 'RESERVED', text: '已预约' },
  { value: 'USING', text: '使用中' },
  { value: 'LEAVE', text: '暂离中' },
  { value: 'MINE', text: '我的预约' },
  { value: 'DISABLED', text: '不可用' },
  { value: 'FAULTY', text: '故障' }
]
</script>

<style scoped>
.seat-legend__dot.AVAILABLE { background: #2f8f83; }
.seat-legend__dot.RESERVED { background: #94a3b8; }
.seat-legend__dot.USING { background: #409eff; }
.seat-legend__dot.LEAVE { background: #f59e0b; }
.seat-legend__dot.MINE { background: #7c3aed; }
.seat-legend__dot.DISABLED { background: #64748b; }
.seat-legend__dot.FAULTY { background: #1f2937; }
</style>
