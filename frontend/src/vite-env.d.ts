/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_BOOK_API_URL?: string;
  readonly VITE_MEMBER_API_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
