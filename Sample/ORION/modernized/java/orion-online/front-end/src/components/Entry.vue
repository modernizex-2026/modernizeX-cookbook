<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listTransactions, listPrograms } from '../api/client'

const emit = defineEmits<{ (e: 'open', program: string): void }>()
const trans = ref<Record<string, string>>({})   // TransID -> programName (screen-backed only)
const tid = ref('')
const error = ref('')

onMounted(async () => {
  const [tx, progs] = await Promise.all([listTransactions(), listPrograms()])
  const screens = new Set(progs.map(p => p.toLowerCase()))
  const f: Record<string, string> = {}
  for (const t in tx) if (screens.has(tx[t].toLowerCase())) f[t] = tx[t]
  trans.value = f
})

function resolve(t: string): string | null {
  const p = trans.value[t.trim().toUpperCase()]
  return p ? p.toLowerCase() : null
}
function go() {
  const slug = resolve(tid.value)
  if (slug) { error.value = ''; emit('open', slug) }
  else error.value = `Transaction '${tid.value.trim().toUpperCase()}' not recognized`
}
function onKey(e: KeyboardEvent) { if (e.key === 'Enter') { e.preventDefault(); go() } }
</script>

<template>
  <div class="screen-wrap">
    <section class="entry-card">
      <div class="entry-title">CICS Transaction Gateway</div>
      <div class="entry-sub">Enter a transaction identifier to begin</div>
      <div class="entry-form">
        <label for="tid">Transaction ID:</label>
        <input id="tid" class="entry-input" v-model="tid" maxlength="8" autofocus
               @keydown="onKey" placeholder="" />
        <button class="entry-go" @click="go">ENTER</button>
      </div>
      <div class="entry-err">{{ error }}</div>
      <div class="entry-avail" v-if="Object.keys(trans).length">
        <span class="entry-avail-label">Available transactions</span>
        <div class="entry-chips">
          <button v-for="(p, t) in trans" :key="t" class="entry-chip" @click="emit('open', p.toLowerCase())">
            <b>{{ t }}</b><span>{{ p }}</span>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>
