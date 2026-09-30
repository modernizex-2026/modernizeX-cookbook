/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_WS_BASE_URL?: string;
  readonly VITE_STATION_NAME?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
