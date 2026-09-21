<template>
  <!--
    富文本编辑器占位实现。
    代码生成器的 register 页（导游分支的「个人履历」）用了 <editor> 这个组件，
    但真正的 wangEditor 封装没有一起移植过来。这里先用 textarea 顶上，
    保证注册页能渲染、能提交；等需要富文本时替换成 wangEditor / Quill 即可，
    对外接口（v-model + action）保持不变。
  -->
  <div class="editor-stub">
    <el-input
      :model-value="modelValue"
      type="textarea"
      :rows="6"
      :placeholder="placeholder"
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <div class="tip">富文本编辑器尚未接入，当前为纯文本输入</div>
  </div>
</template>

<script>
export default {
  name: 'Editor',
  props: {
    modelValue: { type: String, default: '' },
    // 生成器页面会传 action="file/upload"，这里先接收但不使用，避免落到 DOM 属性上
    action: { type: String, default: '' },
    placeholder: { type: String, default: '请输入内容' },
  },
  emits: ['update:modelValue'],
}
</script>

<style scoped>
.editor-stub .tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}
</style>
