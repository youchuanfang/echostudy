<template>
  <div class="es-page-header">
    <div class="es-page-icon">
      <el-icon><component :is="config.icon" /></el-icon>
    </div>
    <div>
      <h1 class="es-page-title">{{ config.title }}</h1>
      <p class="es-page-subtitle">{{ config.subtitle }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getPageConfig } from '../config/themeConfig'

const props = defineProps({
  pageKey: {
    type: String,
    required: true
  }
})

const config = computed(() => getPageConfig(props.pageKey))
</script>
