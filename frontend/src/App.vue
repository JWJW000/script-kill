<template>
  <router-view v-slot="{ Component, route }">
    <transition :name="route.meta.transition || 'page-fade'" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background: var(--sk-bg-deepest);
}
</style>
