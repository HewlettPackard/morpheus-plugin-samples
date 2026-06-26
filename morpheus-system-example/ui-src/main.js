/**
 * Arcus Input Types Library
 *
 * Main entry point for custom Morpheus input type components.
 * This library registers multiple custom OptionType components with Morpheus.
 */

import CustomSlider from "./components/CustomSlider";
import CustomToggle from "./components/CustomToggle";
import CustomRating from "./components/CustomRating";
import CustomHostTable from "./components/CustomHostTable";

const components = [CustomSlider, CustomToggle, CustomRating, CustomHostTable];

const hasRegisterNew = () => typeof window.Morpheus?.components?.registerNew === "function";
const hasFocusUi = () => !!window.FocusUI;

let hasRegistered = false;
let waitAttempts = 0;
const MAX_WAIT_ATTEMPTS = 200;

const registerComponents = () => {
	if (hasRegistered) {
		return;
	}

	if (!hasRegisterNew()) {
		console.error("Morpheus React 19 component registry not available");
		throw new Error("Morpheus React 19 component registry not available");
	}

	if (!hasFocusUi()) {
		console.error("Focus-UI library not available");
		throw new Error("Focus-UI library not available");
	}

	components.forEach(component => {
		component?.register?.();
	});

	hasRegistered = true;

	console.log(`Arcus Input Types Library loaded: ${components.length} components registered`);
};

const waitForDependenciesAndRegister = () => {
	if (hasRegistered) {
		return;
	}

	if (hasRegisterNew() && hasFocusUi()) {
		registerComponents();
		return;
	}

	if (waitAttempts >= MAX_WAIT_ATTEMPTS) {
		console.error("Timed out waiting for Morpheus registerNew and Focus-UI");
		return;
	}

	waitAttempts += 1;
	window.setTimeout(waitForDependenciesAndRegister, 50);
};

if (hasRegisterNew() && hasFocusUi()) {
	registerComponents();
} else {
	window.addEventListener("morpheus:registry:ready", waitForDependenciesAndRegister, {
		once: true
	});
	waitForDependenciesAndRegister();
}
