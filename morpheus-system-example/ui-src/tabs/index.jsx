import { createRoot } from "react-dom/client";
import ArcusReactContentTab from "./ArcusReactContentTab";
import ArcusReactContentWithFocusUITab from "./ArcusReactContentWithFocusUITab";

const PLUGIN_CODE = "arcus-system-example";
const roots = new WeakMap();

export const readProps = el => {
	try {
		return JSON.parse(el?.dataset?.reactProps || "{}");
	} catch {
		return {};
	}
};

export const mount = (el, Component) => {
	if (!el) return;

	// avoid double mount crash
	const existing = roots.get(el);
	if (existing) {
		setTimeout(() => existing.unmount(), 0);
	}

	const root = createRoot(el);
	roots.set(el, root);

	root.render(<Component {...readProps(el)} />);
};

export const safeUnmount = el => {
	const root = roots.get(el);
	if (!root) return;

	setTimeout(() => {
		root.unmount();
		roots.delete(el);
	}, 0);
};

export const register = () => {
	window.Morpheus = window.Morpheus || {};
	window.Morpheus.pluginTabs = window.Morpheus.pluginTabs || {};

	window.Morpheus.pluginTabs[PLUGIN_CODE] = {
		mountReactContentTab: el => mount(el, ArcusReactContentTab),
		mountReactContentWithFocusUIComponentsTab: el => mount(el, ArcusReactContentWithFocusUITab),
		unmount: safeUnmount,
	};
};

if (window.Morpheus) {
	register();
} else {
	window.addEventListener("morpheus:registry:ready", register, { once: true });
}
