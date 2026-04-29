/**
 * Custom Rating Input Component for Morpheus
 *
 * A custom OptionType that provides a star rating interface.
 * Perfect for priority levels, quality ratings, or importance indicators.
 */

import React, { useCallback, useState } from "react";

const CustomRating = (props) => {
	const { value, setValue, config = {}, disabled, error, helpText, label } = props;

	const maxRating = config.maxRating || 5;
	const allowHalf = config.allowHalf || false;
	const showLabels = config.showLabels !== false;
	const labels = config.labels || ["Poor", "Fair", "Good", "Very Good", "Excellent"];
	const icon = config.icon || "★";
	const emptyIcon = config.emptyIcon || "☆";

	const [hoverValue, setHoverValue] = useState(null);

	const handleClick = useCallback(
		(rating) => {
			if (!disabled) {
				setValue?.(rating);
			}
		},
		[disabled, setValue],
	);

	const handleMouseEnter = useCallback(
		(rating) => {
			if (!disabled) {
				setHoverValue(rating);
			}
		},
		[disabled],
	);

	const handleMouseLeave = useCallback(() => {
		setHoverValue(null);
	}, []);

	const displayValue = hoverValue ?? value ?? 0;

	return (
		<div
			className={`custom-rating-container${error ? " has-error" : ""}`}
			style={{ marginBottom: "10px" }}
		>
			<div
				style={{
					display: "flex",
					alignItems: "center",
					gap: "5px",
					fontSize: "24px",
					cursor: disabled ? "not-allowed" : "pointer",
					opacity: disabled ? 0.5 : 1,
				}}
				onMouseLeave={handleMouseLeave}
			>
				{Array.from({ length: maxRating }, (_, i) => {
					const rating = i + 1;
					const isFilled = rating <= displayValue;

					return (
						<span
							key={rating}
							style={{
								color: isFilled ? "#f5a623" : "#ddd",
								transition: "color 0.2s",
							}}
							onMouseEnter={() => handleMouseEnter(rating)}
							onClick={() => handleClick(rating)}
						>
							{isFilled ? icon : emptyIcon}
						</span>
					);
				})}
				{showLabels && displayValue > 0 && displayValue <= labels.length && (
					<span
						style={{
							fontSize: "14px",
							marginLeft: "10px",
							color: "#666",
						}}
					>
						{labels[Math.ceil(displayValue) - 1]}
					</span>
				)}
			</div>
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
CustomRating.register = () => {
	window.Morpheus.components.registerNew("custom-rating", CustomRating, {
		type: "optionType",
		name: "Custom Rating",
		group: "custom",
		details: [
			{
				fieldLabel: "Maximum Rating",
				fieldName: "config.maxRating",
				type: "number",
				defaultValue: 5,
				helpBlock: "Maximum number of stars/icons (1-10)",
				displayOrder: 100,
			},
			{
				fieldLabel: "Show Labels",
				fieldName: "config.showLabels",
				type: "checkbox",
				defaultValue: true,
				helpBlock: "Display text labels for ratings",
				displayOrder: 101,
			},
			{
				fieldLabel: "Rating Labels",
				fieldName: "config.labels",
				type: "text",
				defaultValue: "Poor,Fair,Good,Very Good,Excellent",
				helpBlock: "Comma-separated labels for each rating level",
				displayOrder: 102,
			},
			{
				fieldLabel: "Filled Icon",
				fieldName: "config.icon",
				type: "text",
				defaultValue: "★",
				helpBlock: "Character/icon for filled rating",
				displayOrder: 103,
			},
			{
				fieldLabel: "Empty Icon",
				fieldName: "config.emptyIcon",
				type: "text",
				defaultValue: "☆",
				helpBlock: "Character/icon for empty rating",
				displayOrder: 104,
			},
			{
				fieldLabel: "Default Value",
				fieldName: "defaultValue",
				type: "number",
				defaultValue: 0,
				helpBlock: "Initial rating value",
				displayOrder: 70,
			},
		],
		help: `### Custom Rating Input

This input type provides an interactive star rating interface.

**Features:**
- Visual star-based rating system
- Hover preview before selection
- Customizable rating scale
- Optional text labels
- Configurable icons
- Theme integration

**Use Cases:**
- Priority levels
- Quality ratings
- Importance indicators
- User feedback collection
- Resource criticality

**Configuration:**
- Set maximum rating (e.g., 5 stars)
- Customize icons
- Configure label text
- Enable/disable label display

**Output:**
Returns an integer value from 1 to the maximum rating.`,
	});

	console.log("Custom Rating component registered");
};

export default CustomRating;
