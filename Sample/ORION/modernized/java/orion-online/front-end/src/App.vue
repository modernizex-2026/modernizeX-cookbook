<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Entry from './components/Entry.vue'
import ScreenView from './components/ScreenView.vue'
import { listPrograms } from './api/client'

const params = new URLSearchParams(location.search)
// Empty program => CICS transaction-entry gateway (the default landing). A ?program=<id>
// deep-link still jumps straight to a screen.
const program = ref(params.get('program') || '')
const programs = ref<string[]>([])
onMounted(async () => { programs.value = await listPrograms() })
function open(p: string) { program.value = p }                 // entry resolved a TransID
function home() { program.value = '' }                          // brand click -> back to gateway
function pick(e: Event) { program.value = (e.target as HTMLSelectElement).value }
</script>

<template>
  <div class="app">
    <header class="bar">
      <span class="app-title" @click="home" title="Transaction entry">orion</span>
      <label>Program
        <select class="prog" :value="program" @change="pick">
          <option value="" disabled>&mdash; select &mdash;</option>
          <option v-for="p in programs" :key="p" :value="p">{{ p.toUpperCase() }}</option>
        </select>
      </label>
    </header>
    <Entry v-if="!program" @open="open" />
    <ScreenView v-else :key="program" :program="program" @exit="home" />
    <footer class="app-footer">orion &mdash; Mainframe Modernization</footer>
  </div>
</template>
