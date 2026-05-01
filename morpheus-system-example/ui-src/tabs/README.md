````md
# Morpheus React System Tabs

## Goal

Render React-based custom system tabs from a Morpheus plugin.

The plugin provides:

- Handlebars tab template
- React bundle asset
- Root DOM node with metadata
- Mount entry registered under `window.Morpheus.pluginTabs`

The Morpheus UI only:

- renders tab HTML
- loads the plugin bundle
- calls the plugin-declared entry

---

## Architecture

```text
SystemTabProvider
  -> renderTemplate(...)
  -> HBS emits root + script
  -> Morpheus UI injects HTML
  -> Morpheus UI loads script
  -> plugin registers window.Morpheus.pluginTabs[pluginKey]
  -> Morpheus UI calls pluginApi[entry](root)
````

---

## HBS Template

Example:

```hbs
<div
  id="arcus-react-content-tab-root-{{system.id}}"
  data-plugin-react-root
  data-plugin-key="arcus-system-example"
  data-plugin-entry="mountReactContentTab"
  data-react-props='{{{systemJson}}}'
></div>

<script
  src="{{asset '/arcus-system-react-tab.js'}}?v=@PLUGIN_UI_BUILD_VERSION@"
  nonce="{{nonce ''}}">
</script>
```

Important:

* Do **not** self-close script tags.
* Use `data-plugin-key` to identify the plugin.
* Use `data-plugin-entry` to identify the mount function.
* Keep inline mount scripts out of HBS.

---

## Morpheus UI Loader

`tabs.jsx`

```jsx
import { useEffect, useRef } from "react";
import { Tab, TabList, TabPanel, Tabs } from "focus-ui";

const loadedPluginBundles = new Map();

function loadPluginBundle(src, nonce) {
	if (loadedPluginBundles.has(src)) {
		return loadedPluginBundles.get(src);
	}

	const promise = new Promise((resolve, reject) => {
		const script = document.createElement("script");
		script.src = src;
		script.async = true;

		if (nonce) {
			script.setAttribute("nonce", nonce);
		}

		script.onload = resolve;
		script.onerror = () => reject(new Error(`Failed to load plugin bundle: ${src}`));

		document.body.appendChild(script);
	});

	loadedPluginBundles.set(src, promise);
	return promise;
}

function getPluginTabApi(pluginKey) {
	return window.Morpheus?.pluginTabs?.[pluginKey];
}

const PluginTabPanel = ({ tab }) => {
	const containerRef = useRef(null);

	useEffect(() => {
		const container = containerRef.current;
		if (!container) return undefined;

		container.innerHTML = tab.content || "";

		const root = container.querySelector("[data-plugin-react-root]");
		const script = container.querySelector("script[src]");
		const pluginKey = root?.dataset?.pluginKey;
		const entry = root?.dataset?.pluginEntry;

		if (!root || !script || !pluginKey || !entry) {
			return () => {
				container.innerHTML = "";
			};
		}

		let cancelled = false;

		loadPluginBundle(script.src, script.getAttribute("nonce"))
			.then(() => {
				if (cancelled) return;

				const pluginApi = getPluginTabApi(pluginKey);
				const mountFn = pluginApi?.[entry];

				if (typeof mountFn !== "function") {
					console.error(`Plugin tab entry "${entry}" was not found for plugin "${pluginKey}"`);
					return;
				}

				mountFn(root);
			})
			.catch(error => console.error(error));

		return () => {
			cancelled = true;

			const pluginApi = getPluginTabApi(pluginKey);

			setTimeout(() => {
				pluginApi?.unmount?.(root);
				container.innerHTML = "";
			}, 0);
		};
	}, [tab.code, tab.content]);

	return <div ref={containerRef} id={`system-custom-tab-content-${tab.code}`} />;
};

const SystemCustomTabs = ({ tabs = [] }) => {
	if (!tabs.length) return null;

	return (
		<Tabs>
			<TabList aria-label="System custom tabs">
				{tabs.map(tab => (
					<Tab key={tab.code} id={tab.code}>
						{tab.name}
					</Tab>
				))}
			</TabList>

			{tabs.map(tab => (
				<TabPanel key={tab.code} id={tab.code}>
					<PluginTabPanel tab={tab} />
				</TabPanel>
			))}
		</Tabs>
	);
};

export default SystemCustomTabs;
```

---

## Plugin React Entry

`ui-src/tabs/index.jsx`

```jsx
import { createRoot } from "react-dom/client";
import ArcusReactContentTab from "./ArcusReactContentTab";

const PLUGIN_CODE = "arcus-system-example";
const roots = new WeakMap();

function readProps(el) {
	try {
		return JSON.parse(el?.dataset?.reactProps || "{}");
	} catch {
		return {};
	}
}

function mount(el, Component) {
	if (!el) return;

	const existingRoot = roots.get(el);
	if (existingRoot) {
		setTimeout(() => {
			existingRoot.unmount();
		}, 0);
	}

	const root = createRoot(el);
	roots.set(el, root);
	root.render(<Component {...readProps(el)} />);
}

function unmount(el) {
	const root = roots.get(el);
	if (!root) return;

	setTimeout(() => {
		root.unmount();
		roots.delete(el);
	}, 0);
}

function registerTabs() {
	window.Morpheus = window.Morpheus || {};
	window.Morpheus.pluginTabs = window.Morpheus.pluginTabs || {};

	window.Morpheus.pluginTabs[PLUGIN_CODE] = {
		mountReactContentTab: el => mount(el, ArcusReactContentTab),
		unmount,
	};

	console.log(`[${PLUGIN_CODE}] React system tabs registered`);
}

if (window.Morpheus) {
	registerTabs();
} else {
	window.addEventListener("morpheus:registry:ready", registerTabs, { once: true });
}
```

---

## React Tab Component

Start simple:

```jsx
const ArcusReactContentTab = props => {
	return (
		<div style={{ padding: 16 }}>
			Hello World of React tabs.
			<br />
			System: {props.name || props.id || "unknown"}
		</div>
	);
};

export default ArcusReactContentTab;
```

Only use `focus-ui` inside the plugin after verifying:

```js
window.FocusUI
window.FocusUI?.Box
```

---

## Vite Config

```js
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
			external: ["react", "react-dom", "react-dom/client", "focus-ui"],
			output: {
				globals: {
					react: "React",
					"react-dom": "ReactDOM",
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
```

---

## Debugging Checklist

### 1. Confirm HBS rendered

Inspect API response or DOM:

```js
document.querySelector("[data-plugin-react-root]")
```

Expected: root element exists.

---

### 2. Confirm script exists in DOM

```js
document.querySelector('script[src*="arcus-system-react-tab"]')
```

Expected: script exists.

---

### 3. Confirm script loaded

Network tab should show:

```text
arcus-system-react-tab.js
```

Expected: `200 OK`.

---

### 4. Confirm plugin registered

```js
window.Morpheus?.pluginTabs?.["arcus-system-example"]
```

Expected:

```js
{
  mountReactContentTab: ƒ,
  unmount: ƒ
}
```

---

### 5. Confirm entry name matches

HBS:

```html
data-plugin-entry="mountReactContentTab"
```

Plugin entry:

```js
window.Morpheus.pluginTabs[PLUGIN_CODE] = {
	mountReactContentTab: ...
};
```

These names must match exactly.

---

### 6. Manual mount test

```js
window.Morpheus.pluginTabs["arcus-system-example"].mountReactContentTab(
	document.querySelector("[data-plugin-react-root]")
);
```

If this works, the bundle is good and the issue is in the loader flow.

---

## Common Errors

### Script not loaded

Symptom:

```js
window.Morpheus.pluginTabs["arcus-system-example"] === undefined
```

Fix:

* check script path
* check asset exists in JAR
* check HBS script tag is not self-closing
* check Network tab for 404

---

### Entry not found

Symptom:

```text
Plugin tab entry "X" was not found for plugin "arcus-system-example"
```

Fix:

Make sure HBS and plugin registration use the same name.

---

### React element type invalid

Symptom:

```text
Element type is invalid: expected a string or class/function but got undefined
```

Usually caused by:

* wrong default/named import
* `focus-ui` component is undefined
* `window.FocusUI.Box` is missing

Test with plain HTML first.

---

### Synchronous unmount warning

Symptom:

```text
Attempted to synchronously unmount a root while React was already rendering
```

Fix:

Defer unmount:

```js
setTimeout(() => {
	root.unmount();
	roots.delete(el);
}, 0);
```

---

### Template file not found

Symptom:

```text
Template file not found: hbs/tabs/reactContentTab
```

Fix:

Check JAR:

```bash
jar tf build/libs/*.jar | grep renderer/hbs/tabs
```

Check provider path:

```groovy
return getRenderer().renderTemplate("hbs/tabs/reactContentTab", model)
```

or if your renderer requires it:

```groovy
return getRenderer().renderTemplate("/hbs/tabs/reactContentTab", model)
```

---

### Stale plugin JAR

Fix:

```bash
rm -rf build
rm -f /path/to/plugins/morpheus-system-example*.jar
./gradlew clean shadowJar --rerun-tasks
```

Then restart backend.

---

## Final Contract

```text
HBS declares:
  data-plugin-key
  data-plugin-entry
  script src

Plugin registers:
  window.Morpheus.pluginTabs[pluginKey][entry]

Morpheus UI:
  loads script
  resolves plugin API
  calls entry(root)
```

```
```
## VITE CONFIG

### Why Keep individual Vite config files.

* IIFE builds do not support multiple entries in one build.
* Input types and React tabs have different externals.
* Debugging is clearer: vite.config.js for input types, vite.react-tab.config.js for tabs.
* Build output is easier to verify.
* Less mode-based hidden behavior.

### When is it recommended to have combined vite config:

* Use one combined config only if both bundles share the same externals, same output shape, and you want fewer files. 
* In this case, separate was cleaner, please add vite config files in your plugin based on your usecase.


