<template>
  <header class="header">
    <div class="header-container">
      <div class="header-content">
        <img class="header-logo" src="/icons/logo.svg" alt="Logo" />
        <p v-if="storage" class="header-storage">ログイン者: {{ storage }}</p>
        <div v-if="sysDate || sysTime" class="scr-header-right">
          <span v-if="sysDate" class="scr-numeric">{{ sysDate }}</span>
          <span v-if="sysTime" class="scr-numeric">{{ sysTime }}</span>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  props: {
    storage: {
      type: String,
      required: false,
    },
    sysDate: {
      type: String,
      required: false,
    },
    sysTime: {
      type: String,
      required: false,
    },
  }
}
</script>

<style scoped>
.header {
    width: 100%;
    border-bottom: 1px solid #D9DDE4;
}

.header-container {
    max-width: 1280px;
    margin: 0 auto;
    padding: 12px 16px;
}

.header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.header-storage {
    font-size: 12px;
    color: #69707C;
}
</style>