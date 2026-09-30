<template>
  <header class="app-nav">
    <nav class="app-nav__top">
      <span class="app-nav__brand">Console</span>
    </nav>
  </header>
</template>

<style scoped>
.app-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
}

.app-nav__top {
  height: 64px;
  background-color: var(--scr-primary);
  display: flex;
  align-items: center;
  padding: 0 16px;
  box-sizing: border-box;
}

.app-nav__brand {
  color: var(--color-white);
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
  line-height: 1;
}
</style>
