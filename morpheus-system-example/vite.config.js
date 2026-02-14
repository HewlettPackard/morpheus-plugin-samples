import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [
		react({
			jsxRuntime: "classic",
		}),
	],
	esbuild: {
		jsx: "transform",
		jsxFactory: "React.createElement",
		jsxFragment: "React.Fragment",
	},
	build: {
		lib: {
			entry: path.resolve(__dirname, "ui-src/main.js"),
			name: "ArcusInputTypes",
			fileName: () => "arcus-input-types.js",
			formats: ["iife"],
		},
		rollupOptions: {
			// Externalize React and Focus UI to use the global versions
			external: ["react", "focus-ui", /^@codemirror\/.*/, /^@lezer\/.*/, "codemirror"],
			output: {
				globals: {
					react: "React",
					"focus-ui": "FocusUI",
				},
				assetFileNames: (assetInfo) => {
					return "arcus-input-types.[ext]";
				},
			},
		},
		outDir: "src/assets/js",
		emptyOutDir: false,
		sourcemap: false,
	},
});
