/**
 * Arcus Input Types Library
 *
 * Main entry point for custom Morpheus input type components.
 * This library registers multiple custom OptionType components with Morpheus.
 */

import CustomSlider from "./components/CustomSlider";
import CustomToggle from "./components/CustomToggle";
import CustomRating from "./components/CustomRating";

// Ensure Morpheus and Focus-UI are available
if (
	typeof window.Morpheus === "undefined" ||
	!window.Morpheus.components ||
	!window.Morpheus.components.registry
) {
	console.error("Morpheus component registry not available");
	throw new Error("Morpheus component registry not available");
}

if (!window.FocusUI) {
	console.error("Focus-UI library not available");
	throw new Error("Focus-UI library not available");
}

// Register all custom components
const components = [CustomSlider, CustomToggle, CustomRating];

components.forEach((component) => {
	if (component && component.register) {
		component.register();
	}
});

console.log(`Arcus Input Types Library loaded: ${components.length} components registered`);
