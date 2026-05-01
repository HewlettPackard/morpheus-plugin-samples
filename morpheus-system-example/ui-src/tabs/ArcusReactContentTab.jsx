const ArcusReactContentTab = props => {
	return (
		<div style={{ padding: 16 }}>
			Hello World of React tabs! This is the Arcus React Content Tab.
			<br />
			System: {props.name || props.id || "unknown"}
		</div>
	);
};

export default ArcusReactContentTab;
