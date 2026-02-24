<template>
  <div class="hour-datetime-picker">
    <input
      v-model="innerDate"
      type="date"
      class="form-input"
    />
    <select
      v-model.number="innerHour"
      class="form-input hour-select"
    >
      <option :value="null">小时</option>
      <option
        v-for="h in 24"
        :key="h"
        :value="h - 1"
      >
        {{ String(h - 1).padStart(2, '0') }}:00
      </option>
    </select>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  /**
   * 统一与后端的时间格式：yyyy-MM-dd'T'HH:00:00（符合 LocalDateTime 默认解析）
   */
  modelValue: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:modelValue'])

const innerDate = ref('')
const innerHour = ref(null)

// 根据外部传入的时间字符串进行反向解析
watch(
  () => props.modelValue,
  (val) => {
    if (!val) {
      innerDate.value = ''
      innerHour.value = null
      return
    }
    // 兼容 "yyyy-MM-dd HH:mm:ss" 或 "yyyy-MM-ddTHH:mm:ss"
    let datePart = ''
    let timePart = ''
    if (val.includes('T')) {
      ;[datePart, timePart] = val.split('T')
    } else {
      ;[datePart, timePart] = val.split(' ')
    }
    innerDate.value = datePart || ''
    if (timePart) {
      const hour = Number(timePart.split(':')[0])
      innerHour.value = Number.isNaN(hour) ? null : hour
    } else {
      innerHour.value = null
    }
  },
  { immediate: true }
)

// 组合内部的 date + hour，向外输出统一格式
watch([innerDate, innerHour], ([d, h]) => {
  if (!d || h == null) {
    emit('update:modelValue', '')
    return
  }
  const hourStr = String(h).padStart(2, '0')
  emit('update:modelValue', `${d}T${hourStr}:00:00`)
})
</script>

<style scoped>
.hour-datetime-picker {
  display: flex;
  gap: 8px;
}

.form-input {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: #ffffff;
  flex: 1;
}

.hour-select {
  max-width: 110px;
}
</style>

