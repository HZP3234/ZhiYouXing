<template>
  <div class="placeholder">
    <h2>页面待实现</h2>
    <p class="path">当前路径：<code>{{ $route.fullPath }}</code></p>
    <p class="hint">
      这个页面在代码生成器的输出里存在，但还没有被移植进本仓库。<br />
      补页面的步骤：在 <code>src/router/index.js</code> 的兜底路由<strong>之前</strong>插入真实路由，
      再把对应的 <code>.vue</code> 放到 <code>src/pages/</code> 下。
    </p>
    <el-button type="primary" @click="$router.push('/index/home')">返回首页</el-button>
  </div>
</template>

<script>
export default {
  name: 'PlaceholderPage',
}
</script>

<style scoped>
.placeholder {
  max-width: 720px;
  margin: 60px auto;
  padding: 40px;
  text-align: center;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.placeholder h2 {
  margin: 0 0 16px;
  color: var(--brand);
}
.placeholder .path {
  color: #666;
}
.placeholder .hint {
  margin: 20px 0 28px;
  color: #909399;
  font-size: 14px;
  line-height: 1.8;
}
.placeholder code {
  padding: 2px 6px;
  background: #f5f7fa;
  border-radius: 4px;
  color: var(--brand);
}
</style>
