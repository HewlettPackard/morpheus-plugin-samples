// © 2025 Hewlett Packard Enterprise Development LP

import { useCallback, useEffect, useMemo, useState } from "react";
import {
	Card,
	Button,
	Checkbox,
	Flex,
	FlexSpacer,
	Grid,
	Modal,
	ModalBody,
	ModalFooter,
	ModalHeader,
	Table,
	TextField,
	Toolbar
} from "focus-ui";

const defaultRows = [
	{
		selected: true,
		serial: "CZ20170FD4",
		model: "HPE ProLiant DL380 Gen10",
		cpuFamily: "Intel Xeon Gold 6248R",
		ipv4Address: "192.168.1.101",
		ipv6Address: "fe80:a236:9fff:fe1a:1f42",
		managementIp: "192.168.1.101",
		managementFqdn: "hpe-srv-001.domain.local",
		iloIp: "192.168.1.111"
	},
	{
		selected: true,
		serial: "CZ20170FD5",
		model: "HPE ProLiant DL380 Gen10",
		cpuFamily: "Intel Xeon Gold 6248R",
		ipv4Address: "192.168.1.102",
		ipv6Address: "fe80:a236:9fff:fe1a:1f43",
		managementIp: "192.168.1.102",
		managementFqdn: "hpe-srv-002.domain.local",
		iloIp: "192.168.1.112"
	}
];

const parseRows = (value, config) => {
	if (Array.isArray(value)) return value;
	if (typeof value === "string") {
		try {
			const parsed = JSON.parse(value);
			if (Array.isArray(parsed)) return parsed;
		} catch {
			// keep fallback
		}
	}
	if (Array.isArray(config?.rows)) return config.rows;
	if (typeof config?.rows === "string") {
		try {
			const parsedRows = JSON.parse(config.rows);
			if (Array.isArray(parsedRows)) return parsedRows;
		} catch {
			// keep fallback
		}
	}
	return defaultRows;
};

const updateRows = (rows, rowIndex, patch) =>
	rows.map((row, idx) => {
		if (idx !== rowIndex) return row;
		return { ...row, ...patch };
	});

const hostMetaStyle = {
	fontSize: "11px",
	color: "var(--hpe-color-text-secondary)",
	marginTop: "2px"
};

const editableInputStyle = {
	width: "100%",
	minWidth: "0"
};

const tableContainerStyle = {
	width: "100%",
	maxWidth: "100%",
	marginTop: "6px",
	overflowX: "auto",
	overflowY: "hidden"
};

const componentWrapperStyle = {
	width: "100%"
};

const requiredIndicatorStyle = {
	color: "var(--hpe-color-text-critical)",
	marginLeft: "2px",
	fontWeight: "700"
};

const formFieldLabelStyle = {
	marginBottom: "6px",
	fontWeight: "500",
	color: "var(--hpe-color-text-default)",
	fontSize: "14px"
};

const accordionToggleStyle = {
	appearance: "none",
	border: "none",
	background: "transparent",
	color: "var(--hpe-color-text-secondary)",
	cursor: "pointer",
	fontSize: "14px",
	lineHeight: 1,
	padding: 0,
	marginRight: "4px"
};

const toolbarTitleStyle = {
	fontWeight: 600,
	color: "var(--hpe-color-text-default)"
};

const accordionSummaryLabelStyle = {
	fontWeight: 600,
	color: "var(--hpe-color-text-default)"
};

const normalizeConfig = config => {
	return {
		allowManualEdit: config?.allowManualEdit !== false,
		enableAutoAssign: config?.enableAutoAssign !== false,
		allowPerRowToggle: config?.allowPerRowToggle !== false
	};
};

const parseIPv4 = value => {
	if (!value || typeof value !== "string") return null;
	const parts = value.split(".");
	if (parts.length !== 4) return null;
	const octets = parts.map(part => Number(part));
	if (octets.some(octet => !Number.isInteger(octet) || octet < 0 || octet > 255)) return null;
	return octets;
};

const formatIPv4 = octets => octets.join(".");

const incrementIPv4 = octets => {
	const next = [...octets];
	for (let idx = 3; idx >= 0; idx -= 1) {
		if (next[idx] < 255) {
			next[idx] += 1;
			return next;
		}
		next[idx] = 0;
	}
	return null;
};

const assignIncrementalIps = (rows, selectedIndexes, startIp, fieldName) => {
	const parsed = parseIPv4(startIp);
	if (!parsed) {
		return { rows, error: `${fieldName} start IP must be a valid IPv4 address.` };
	}

	let current = parsed;
	const nextRows = [...rows];
	for (let idx = 0; idx < selectedIndexes.length; idx += 1) {
		const rowIndex = selectedIndexes[idx];
		nextRows[rowIndex] = {
			...nextRows[rowIndex],
			[fieldName]: formatIPv4(current)
		};
		if (idx < selectedIndexes.length - 1) {
			const incremented = incrementIPv4(current);
			if (!incremented) {
				return { rows, error: `IP range overflow while assigning ${fieldName}.` };
			}
			current = incremented;
		}
	}

	return { rows: nextRows, error: null };
};

const hasRequiredValue = value => {
	if (value === null || value === undefined) return false;
	if (typeof value === "string") return value.trim().length > 0;
	return true;
};

const hasRequiredRowFields = rows => {
	if (!Array.isArray(rows) || rows.length === 0) return false;
	return rows.every(row => hasRequiredValue(row.managementIp) && hasRequiredValue(row.managementFqdn) && hasRequiredValue(row.iloIp));
};

const CustomHostTable = ({ value, setValue, config = {}, disabled, error, helpText }) => {
	const rows = useMemo(() => parseRows(value, config), [value, config]);
	const normalizedConfig = useMemo(() => normalizeConfig(config), [config]);
	const [expandedRows, setExpandedRows] = useState(() => new Set([0]));
	const [showAutoAssignModal, setShowAutoAssignModal] = useState(false);
	const [localError, setLocalError] = useState("");
	const [autoAssignForm, setAutoAssignForm] = useState(() => ({
		managementStartIp: rows.find(row => row.managementIp)?.managementIp || "",
		iloStartIp: rows.find(row => row.iloIp)?.iloIp || "",
		dataSubnet1StartIp: rows.find(row => row.dataSubnet1Ip)?.dataSubnet1Ip || "",
		dataSubnet2StartIp: rows.find(row => row.dataSubnet2Ip)?.dataSubnet2Ip || ""
	}));

	const pagedRows = useMemo(
		() =>
			rows.map((row, rowIndex) => ({
				...row,
				__rowIndex: rowIndex
			})),
		[rows]
	);

	const selectedRowIndexes = useMemo(
		() => rows.map((row, idx) => (row.selected ? idx : -1)).filter(idx => idx !== -1),
		[rows]
	);

	const isAutoAssignSaveEnabled = useMemo(() => {
		if (disabled || !normalizedConfig.enableAutoAssign) return false;
		if (selectedRowIndexes.length === 0) return false;
		if (!hasRequiredValue(autoAssignForm.managementStartIp) || !hasRequiredValue(autoAssignForm.iloStartIp)) return false;
		return Boolean(parseIPv4(autoAssignForm.managementStartIp) && parseIPv4(autoAssignForm.iloStartIp));
	}, [
		autoAssignForm.iloStartIp,
		autoAssignForm.managementStartIp,
		disabled,
		normalizedConfig.enableAutoAssign,
		selectedRowIndexes.length
	]);

	const onToggleSelected = useCallback(
		(rowIndex, selected) => {
			setLocalError("");
			setValue?.(updateRows(rows, rowIndex, { selected }));
		},
		[rows, setValue]
	);

	const onEditField = useCallback(
		(rowIndex, fieldName, fieldValue) => {
			setLocalError("");
			setValue?.(updateRows(rows, rowIndex, { [fieldName]: fieldValue }));
		},
		[rows, setValue]
	);

	const onToggleAllRows = useCallback(
		selected => {
			setLocalError("");
			setValue?.(rows.map(row => ({ ...row, selected })));
		},
		[rows, setValue]
	);

	const onSaveAutoAssign = useCallback(() => {
		const selectedIndexes = selectedRowIndexes;

		if (selectedIndexes.length === 0) {
			setLocalError("Select at least one row before auto assigning IP addresses.");
			return;
		}

		let nextRows = rows;

		if (autoAssignForm.managementStartIp) {
			const result = assignIncrementalIps(nextRows, selectedIndexes, autoAssignForm.managementStartIp, "managementIp");
			if (result.error) {
				setLocalError(result.error);
				return;
			}
			nextRows = result.rows;
		}

		if (autoAssignForm.iloStartIp) {
			const result = assignIncrementalIps(nextRows, selectedIndexes, autoAssignForm.iloStartIp, "iloIp");
			if (result.error) {
				setLocalError(result.error);
				return;
			}
			nextRows = result.rows;
		}

		if (autoAssignForm.dataSubnet1StartIp) {
			const result = assignIncrementalIps(nextRows, selectedIndexes, autoAssignForm.dataSubnet1StartIp, "dataSubnet1Ip");
			if (result.error) {
				setLocalError(result.error);
				return;
			}
			nextRows = result.rows;
		}

		if (autoAssignForm.dataSubnet2StartIp) {
			const result = assignIncrementalIps(nextRows, selectedIndexes, autoAssignForm.dataSubnet2StartIp, "dataSubnet2Ip");
			if (result.error) {
				setLocalError(result.error);
				return;
			}
			nextRows = result.rows;
		}

		setValue?.(nextRows);
		setShowAutoAssignModal(false);
		setLocalError("");
	}, [autoAssignForm, rows, selectedRowIndexes, setValue]);

	const closeAutoAssignModal = useCallback(() => {
		setShowAutoAssignModal(false);
	}, []);

	const toggleExpandedRow = useCallback(rowIndex => {
		setExpandedRows(current => {
			const next = new Set(current);
			if (next.has(rowIndex)) {
				next.delete(rowIndex);
			} else {
				next.add(rowIndex);
			}
			return next;
		});
	}, []);

	useEffect(() => {
		if ((value === undefined || value === null || value === "") && hasRequiredRowFields(rows)) {
			setValue?.(rows);
		}
	}, [rows, setValue, value]);

	const columns = useMemo(() => {
		return [
			{
				id: "serial",
				size: 330,
				header: () => (
					<Flex style={{ alignItems: "center", gap: "8px", fontWeight: 600, marginLeft: "4px" }}>
						<Checkbox
							isSelected={rows.length > 0 && rows.every(row => row.selected)}
							onChange={onToggleAllRows}
							isDisabled={disabled || !normalizedConfig.allowPerRowToggle}
							aria-label="Toggle all rows"
						/>
						<span>SERIAL AND DETAILS</span>
					</Flex>
				),
				cell: info => {
					const row = info.row.original;
					const rowIndex = row.__rowIndex;
					const isExpanded = expandedRows.has(rowIndex);
					return (
						<div>
							<Flex style={{ alignItems: "flex-start", gap: "8px" }}>
								<button
									type="button"
									onClick={() => toggleExpandedRow(rowIndex)}
									style={accordionToggleStyle}
									aria-label={`${isExpanded ? "Collapse" : "Expand"} host ${row.serial || rowIndex + 1}`}
								>
									{isExpanded ? "▾" : "▸"}
								</button>
								<Checkbox
									isSelected={!!row.selected}
									onChange={selected => onToggleSelected(rowIndex, selected)}
									isDisabled={disabled || !normalizedConfig.allowPerRowToggle}
									aria-label={`Select host ${row.serial || rowIndex + 1}`}
								/>
								<div>
									<div style={{ fontWeight: 600, color: "var(--hpe-color-text-default)" }}>
										{row.serial || `HOST-${rowIndex + 1}`}
									</div>
									<div style={hostMetaStyle}>{row.model || "Host"}</div>
									{isExpanded && (
										<Flex direction="column" gap="4px" style={{ marginTop: "6px" }}>
											<div style={accordionSummaryRowStyle}>
												<span style={accordionSummaryLabelStyle}>IPv4:</span>
												<span>{row.ipv4Address || row.managementIp || "-"}</span>
											</div>
											<div style={accordionSummaryRowStyle}>
												<span style={accordionSummaryLabelStyle}>IPv6:</span>
												<span>{row.ipv6Address || "-"}</span>
											</div>
										</Flex>
									)}
								</div>
							</Flex>
						</div>
					);
				}
			},
			{
				id: "cpuFamily",
				accessorKey: "cpuFamily",
				size: 220,
				header: "CPU FAMILY",
				cell: info => info.getValue?.() || "-"
			},
			{
				id: "managementIp",
				size: 280,
				header: () => (
					<Flex style={{ alignItems: "center", gap: "2px" }}>
						<span>MANAGEMENT IP ADDRESS</span>
						<span style={requiredIndicatorStyle}>*</span>
					</Flex>
				),
				cell: info => {
					const row = info.row.original;
					return (
						<Flex direction="column" style={{ justifyContent: "flex-start", height: "100%" }}>
							<TextField
								value={row.managementIp || ""}
								onChange={fieldValue => onEditField(row.__rowIndex, "managementIp", fieldValue)}
								isDisabled={disabled || !normalizedConfig.allowManualEdit}
								className="host-table-input"
								style={editableInputStyle}
								aria-label="Management IP Address"
							/>
						</Flex>
					);
				}
			},
			{
				id: "managementFqdn",
				size: 300,
				header: () => (
					<Flex style={{ alignItems: "center", gap: "2px" }}>
						<span>MANAGEMENT FQDN</span>
						<span style={requiredIndicatorStyle}>*</span>
					</Flex>
				),
				cell: info => {
					const row = info.row.original;
					return (
						<Flex direction="column" style={{ justifyContent: "flex-start", height: "100%" }}>
							<TextField
								value={row.managementFqdn || ""}
								onChange={fieldValue => onEditField(row.__rowIndex, "managementFqdn", fieldValue)}
								isDisabled={disabled || !normalizedConfig.allowManualEdit}
								className="host-table-input"
								style={editableInputStyle}
								aria-label="Management FQDN"
							/>
						</Flex>
					);
				}
			},
			{
				id: "iloIp",
				size: 240,
				header: () => (
					<Flex style={{ alignItems: "center", gap: "2px" }}>
						<span>ILO IP ADDRESS</span>
						<span style={requiredIndicatorStyle}>*</span>
					</Flex>
				),
				cell: info => {
					const row = info.row.original;
					return (
						<Flex direction="column" style={{ justifyContent: "flex-start", height: "100%" }}>
							<TextField
								value={row.iloIp || ""}
								onChange={fieldValue => onEditField(row.__rowIndex, "iloIp", fieldValue)}
								isDisabled={disabled || !normalizedConfig.allowManualEdit}
								className="host-table-input"
								style={editableInputStyle}
								aria-label="iLO IP Address"
							/>
						</Flex>
					);
				}
			}
		];
	}, [rows, onToggleAllRows, disabled, normalizedConfig.allowPerRowToggle, normalizedConfig.allowManualEdit, onToggleSelected, onEditField, expandedRows, toggleExpandedRow]);

	return (
		<Flex direction="column" className={`custom-host-table${error ? " has-error" : ""}`} style={componentWrapperStyle}>
			<Toolbar>
				<div style={toolbarTitleStyle}>Cluster Hosts</div>
				<FlexSpacer />
				<Button
					onClick={() => {
						setLocalError("");
						setShowAutoAssignModal(true);
					}}
					disabled={disabled || !normalizedConfig.enableAutoAssign}
					primary
				>
					Auto Assign IP Addresses
				</Button>
			</Toolbar>
			<div style={tableContainerStyle}>
				<Table
					columns={columns}
					data={pagedRows}
					withStripes
					displayEmptyPanel={true}
					stackOnMobile={true}
					enableColumnReordering
				/>
			</div>

			{showAutoAssignModal && (
				<Modal
					position="top"
					isOpen={showAutoAssignModal}
					size="medium"
					onOpenChange={closeAutoAssignModal}
					aria-label="Auto Assign IP Addresses"
				>
					<ModalHeader canClose title="Auto Assign IP Addresses" onClose={closeAutoAssignModal} />
					<ModalBody>
						<div style={{ color: "var(--hpe-color-text-secondary)", marginBottom: "16px", fontSize: "14px" }}>
							Enter start IP addresses to assign incrementally across selected rows.
						</div>
							<Grid gap="medium">
							<div>
								<label style={formFieldLabelStyle}>
									<span>Management Start IP Address</span>
								</label>
								<TextField
									value={autoAssignForm.managementStartIp}
									onChange={fieldValue => setAutoAssignForm(current => ({ ...current, managementStartIp: fieldValue }))}
									isDisabled={disabled}
									style={editableInputStyle}
									aria-label="Management Start IP Address"
								/>
							</div>
							<div>
								<label style={formFieldLabelStyle}>
									<span>iLO Start IP Address</span>
								</label>
								<TextField
									value={autoAssignForm.iloStartIp}
									onChange={fieldValue => setAutoAssignForm(current => ({ ...current, iloStartIp: fieldValue }))}
									isDisabled={disabled}
									style={editableInputStyle}
									aria-label="iLO Start IP Address"
								/>
							</div>
						</Grid>
					</ModalBody>
					<ModalFooter>
						<Button onClick={closeAutoAssignModal} secondary>
							Cancel
						</Button>
						<Button onClick={onSaveAutoAssign} disabled={!isAutoAssignSaveEnabled} primary>
							Save
						</Button>
					</ModalFooter>
				</Modal>
			)}

			{helpText && <small style={{ display: "block", marginTop: "8px", color: "var(--hpe-color-text-secondary)" }}>{helpText}</small>}
			{(error || localError) && (
				<small style={{ display: "block", marginTop: "8px", color: "var(--hpe-color-text-critical)" }}>{localError || error}</small>
			)}
		</Flex>
	);
};

CustomHostTable.validate = (value, config) => {
	if (!value) return false;
	if (!Array.isArray(value)) return false;
	if (value.length === 0) return false;
	return value.every(row => hasRequiredValue(row.managementIp) && hasRequiredValue(row.managementFqdn) && hasRequiredValue(row.iloIp));
};

CustomHostTable.register = () => {
	const registry = window.Morpheus?.components;
	if (typeof registry?.registerNew !== "function") {
		console.error("Morpheus registerNew API not available for custom-host-table");
		return;
	}

	registry.registerNew("custom-host-table", CustomHostTable, {
		type: "optionType",
		name: "Custom Host Table",
		group: "custom",
		isFullWidth: true,
		validate: CustomHostTable.validate,
		details: [
			{
				fieldLabel: "Rows JSON",
				fieldName: "config.rows",
				type: "textarea",
				helpBlock: "Optional JSON array of host rows to seed the table",
				displayOrder: 100
			},
			{
				fieldLabel: "Allow Manual Edit",
				fieldName: "config.allowManualEdit",
				type: "checkbox",
				defaultValue: true,
				helpBlock: "Enable manual editing for IP and FQDN table inputs",
				displayOrder: 101
			},
			{
				fieldLabel: "Enable Auto Assign",
				fieldName: "config.enableAutoAssign",
				type: "checkbox",
				defaultValue: true,
				helpBlock: "Show Auto Assign IP control and enable auto assignment",
				displayOrder: 102
			},
			{
				fieldLabel: "Allow Row Selection",
				fieldName: "config.allowPerRowToggle",
				type: "checkbox",
				defaultValue: true,
				helpBlock: "Allow selecting/deselecting rows for assignment",
				displayOrder: 103
			}
		],
		help: `### Custom Host Table\n\nEditable host table with selection, management IP, FQDN, and iLO columns. Supports manual edit and auto-assign IP ranges.\nReturns an array of host row objects.`
	});
};

export default CustomHostTable;
