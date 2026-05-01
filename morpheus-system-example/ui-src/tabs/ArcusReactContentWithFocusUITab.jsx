const { Flex, Text } = window.FocusUI || {};

const ArcusReactContentWithFocusUITab = props => {
	console.info("Rendering ArcusReactContentWithFocusUITab with props:", props);
	console.info("FocusUI components available in ArcusReactContentWithFocusUITab:", window.FocusUI);
	return (
		<>
			<Text size="xlarge" weight="bold">
				{props.name || "Arcus Summary"}
			</Text>
			<Flex direction="column" gap="small">
				<Text>ID: {props.id || "—"}</Text>
				<Text>Status: {props.status || "—"}</Text>
				<Text>Type: {props.type?.name || "—"}</Text>
				<Text>Layout: {props.layout?.name || "—"}</Text>
			</Flex>
		</>
	);
};

export default ArcusReactContentWithFocusUITab;
