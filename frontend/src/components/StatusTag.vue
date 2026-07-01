<template>
  <span class="status-tag" :class="meta.type">{{ meta.text }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { getStatusMeta } from '../config/statusMap'

const props = defineProps({
  value: {
    type: String,
    default: ''
  }
})

const meta = computed(() => getStatusMeta(props.value))
</script>
