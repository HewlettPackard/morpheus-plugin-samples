/**
 * Custom Slider Input Component for Morpheus
 *
 * This demonstrates how to create a custom OptionType input type
 * that integrates with the Morpheus UI component registry using Focus-UI components.
 *
 * Uses Focus-UI Slider component for consistent styling, accessibility,
 * and theme support (light/dark mode).
 */

import { useCallback } from "react";
import { Slider } from "focus-ui";

const CustomSlider = (props) => {
	const { value, setValue, config = {}, disabled, error, helpText, defaultValue } = props;

	const minValue = config.min ?? 0;
	const maxValue = config.max ?? 100;
	const step = config.step ?? 1;
	const showValue = config.showValue !== false;
	const unit = config.unit || "";

	const handleChange = useCallback(
		(newValue) => {
			setValue?.(newValue);
		},
		[setValue],
	);

	// Format the display value with unit if configured
	const formatDisplayValue = (val) => {
		return showValue && unit ? `${val} ${unit}` : `${val}`;
	};

	return (
		<div
			className={`custom-slider-container${error ? " has-error" : ""}`}
			style={{ marginBottom: "10px" }}
		>
			<Slider
				value={
					value !== undefined && value !== null
						? value
						: defaultValue !== undefined
							? defaultValue
							: config.defaultValue || 50
				}
				minValue={minValue}
				maxValue={maxValue}
				step={step}
				onChange={handleChange}
				isDisabled={disabled}
				formatDisplayValue={showValue ? formatDisplayValue : undefined}
				aria-label="Custom slider input"
			/>
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
CustomSlider.register = () => {
	window.Morpheus.components.registerNew("custom-slider", CustomSlider, {
		type: "optionType",
		name: "Custom Slider",
		group: "custom",
		reactVersionOlderThan19: false,
		details: [
			{
				fieldLabel: "Minimum Value",
				fieldName: "config.min",
				type: "number",
				defaultValue: 0,
				helpBlock: "Minimum slider value",
				displayOrder: 100,
			},
			{
				fieldLabel: "Maximum Value",
				fieldName: "config.max",
				type: "number",
				defaultValue: 100,
				helpBlock: "Maximum slider value",
				displayOrder: 101,
			},
			{
				fieldLabel: "Step",
				fieldName: "config.step",
				type: "number",
				defaultValue: 1,
				helpBlock: "Increment value for each step",
				displayOrder: 102,
			},
			{
				fieldLabel: "Default Value",
				fieldName: "config.defaultValue",
				type: "number",
				defaultValue: 50,
				helpBlock: "The initial value for this slider",
				displayOrder: 70,
			},
			{
				fieldLabel: "Show Value",
				fieldName: "config.showValue",
				type: "checkbox",
				defaultValue: true,
				helpBlock: "Display the current value next to the slider",
				displayOrder: 103,
			},
			{
				fieldLabel: "Unit Label",
				fieldName: "config.unit",
				type: "text",
				helpBlock: "Optional unit to display after the value (e.g., %, GB, etc.)",
				displayOrder: 104,
			},
		],
		help: `### Custom Slider Input

This input type provides an interactive slider control for numeric values using Focus-UI components.

**Features:**
- Built with Focus-UI Slider component for consistent styling
- Accessible (WCAG 2.1 AA compliant via React-Aria)
- Theme-aware (supports light/dark mode)
- Configurable min, max, and step values
- Optional value display with custom unit labels
- Smooth dragging interaction

**Use Cases:**
- Volume controls
- Percentage inputs
- Resource allocation (CPU, memory, storage)
- Quality settings

**Configuration:**
Configure the slider behavior using the field settings:
- Set min/max range for valid values
- Define step increment
- Choose whether to show the current value
- Add unit labels (%, GB, cores, etc.)

**Output:**
Returns an integer value within the configured range.`,
	});

	console.log("Custom Slider component registered");
};

export default CustomSlider;
