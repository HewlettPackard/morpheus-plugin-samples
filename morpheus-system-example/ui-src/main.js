/**
 * Arcus Input Types Library
 *
 * Main entry point for custom Morpheus input type components.
 * This library registers multiple custom OptionType components with Morpheus.
 */

import CustomSlider from "./components/CustomSlider";
import CustomToggle from "./components/CustomToggle";
import CustomRating from "./components/CustomRating";

const components = [CustomSlider, CustomToggle, CustomRating];

const registerComponents = () => {
	if (!window.Morpheus?.components?.registerNew) {
		console.error("Morpheus React 19 component registry not available");
		throw new Error("Morpheus React 19 component registry not available");
	}

	if (!window.FocusUI) {
		console.error("Focus-UI library not available");
		throw new Error("Focus-UI library not available");
	}

	components.forEach(component => {
		component?.register?.();
	});

	console.log(`Arcus Input Types Library loaded: ${components.length} components registered`);
};

if (window.Morpheus?.components?.registerNew) {
	registerComponents();
} else {
	window.addEventListener("morpheus:registry:ready", registerComponents, {
		once: true
	});
}
