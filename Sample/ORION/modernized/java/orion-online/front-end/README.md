# orion — Frontend (SPA)

Vue 3 + Vite + TypeScript SPA. Layout comes from `public/manifest/<program>.json`;
data binds to the backend by field name over HTTP (`POST /api/terminal/execute`).

```bash
npm install
npm run dev      # http://localhost:5173  (proxies /api → :8080)
npm run build
```

The default landing is the **CICS transaction-entry gateway**: type a TransID (e.g. `CC00`)
to start its program, or pick any screen from the Program selector. Deep-link a screen with
`?program=<id>` (e.g. `?program=cosgn00c`).
