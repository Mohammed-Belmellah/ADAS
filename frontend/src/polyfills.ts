// src/polyfills.ts
// Provide a Node-like "global" for libraries that expect it (e.g., sockjs-client)
// Some libs (like sockjs-client) expect `global` (Node) to exist.
// In the browser we can safely map it to window.
;(window as any).global = window

// Optional: add a minimal process.env so libs relying on it don't crash.
;(window as any).process = (window as any).process || {
  env: { NODE_ENV: import.meta.env.MODE }
}


// Optional: some libs read process.env.*
// If you see errors about "process", uncomment this:
// (globalThis as any).process = (globalThis as any).process || { env: {} };
