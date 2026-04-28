/**
 * Custom Toggle Input Component for Morpheus
 *
 * A custom OptionType that provides a styled toggle switch using Focus-UI components.
 * Perfect for boolean settings with enhanced visual feedback.
 */

import { useCallback } from "react";

const { Switch } = window.FocusUI;

const CustomToggle = (props) => {
	const { value, setValue, config = {}, disabled, error, helpText, label } = props;

	const onLabel = config.onLabel || "On";
	const offLabel = config.offLabel || "Off";
	const description = config.description || "";

	const handleChange = useCallback(
		(isSelected) => {
			setValue?.(isSelected);
		},
		[setValue],
	);

	return (
		<div
			className={`custom-toggle-container${error ? " has-error" : ""}`}
			style={{ marginBottom: "10px" }}
		>
			<div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
				<Switch
					isSelected={!!value}
					onChange={handleChange}
					isDisabled={disabled}
					aria-label={label || "Toggle switch"}
				>
					{value ? onLabel : offLabel}
				</Switch>
			</div>
			{description && (
				<small
					className="description-text"
					style={{ display: "block", marginTop: "5px", color: "#666" }}
				>
					{description}
				</small>
			)}
			{helpText && (
				<small className="help-block" style={{ display: "block", marginTop: "5px" }}>
					{helpText}
				</small>
			)}
			{error && (
				<small
					className="error-message"
					style={{ display: "block", color: "#d9534f", marginTop: "5px" }}
				>
					{error}
				</small>
			)}
		</div>
	);
};

// Registration function
CustomToggle.register = () => {
	window.Morpheus.components.registerNew("custom-toggle", CustomToggle, {
		type: "optionType",
		name: "Custom Toggle Switch",
		group: "custom",
		reactVersionOlderThan19: false,
		details: [
			{
				fieldLabel: "On Label",
				fieldName: "config.onLabel",
				type: "text",
				defaultValue: "On",
				helpBlock: "Label to display when toggle is on",
				displayOrder: 100,
			},
			{
				fieldLabel: "Off Label",
				fieldName: "config.offLabel",
				type: "text",
				defaultValue: "Off",
				helpBlock: "Label to display when toggle is off",
				displayOrder: 101,
			},
			{
				fieldLabel: "Description",
				fieldName: "config.description",
				type: "text",
				helpBlock: "Optional description text displayed below the toggle",
				displayOrder: 102,
			},
			{
				fieldLabel: "Default Value",
				fieldName: "defaultValue",
				type: "checkbox",
				defaultValue: false,
				helpBlock: "Initial state of the toggle (checked = on)",
				displayOrder: 70,
			},
		],
		help: `### Custom Toggle Switch

This input type provides an enhanced toggle switch control using Focus-UI components.

**Features:**
- Built with Focus-UI Switch component for consistent styling
- Accessible (WCAG 2.1 AA compliant)
- Theme-aware (supports light/dark mode)
- Customizable on/off labels
- Optional description text
- Clear visual feedback

**Use Cases:**
- Feature flags and settings toggles
- Enable/disable options
- Binary configuration choices
- Preference switches

**Configuration:**
- Customize the on/off labels
- Add descriptive text
- Set default state

**Output:**
Returns a boolean value (true/false).`,
	});

	console.log("Custom Toggle component registered");
};

export default CustomToggle;
