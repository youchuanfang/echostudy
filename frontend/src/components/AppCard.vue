<template>
  <section class="es-card">
    <header v-if="$slots.title || title || $slots.extra" class="es-card__header">
      <div class="es-card__title">
        <slot name="title">{{ title }}</slot>
      </div>
      <div v-if="$slots.extra" class="es-card__extra">
        <slot name="extra" />
      </div>
    </header>
    <div class="es-card__body">
      <slot />
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>
