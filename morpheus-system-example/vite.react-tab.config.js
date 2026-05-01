import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
	plugins: [
		react({
			jsxRuntime: "classic",
		}),
	],
	build: {
		lib: {
			entry: path.resolve(__dirname, "ui-src/tabs/index.jsx"),
			name: "ArcusSystemReactTab",
			fileName: () => "arcus-system-react-tab.js",
			formats: ["iife"],
		},
		rollupOptions: {
			external: ["react", "react-dom/client", "focus-ui"],
			output: {
				globals: {
					react: "React",
					"react-dom/client": "ReactDOM",
					"focus-ui": "FocusUI",
				},
			},
		},
		outDir: "src/assets/js",
		emptyOutDir: false,
		sourcemap: false,
	},
});
