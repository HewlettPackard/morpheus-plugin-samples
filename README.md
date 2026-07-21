# Morpheus Plugin Samples

This repository contains functional source code examples designed to extend the HPE Morpheus Enterprise platform using the Java/Groovy Plugin SDK. 

Once compiled and uploaded via **Administration > Integrations > Plugins**, use this guide to locate where each specific plugin sample alters, extends, or manifests inside the Morpheus User Interface.

## UI Location Reference Table

| Plugin Directory Name | Extension Type / Target | Where to View in Morpheus UI |
| :--- | :--- | :--- |
| `morpheus-approvals-plugin` | Approval Provider | **Administration > Blueprints > Approvals** (Visible when adding an external Approval policy or tier). |
| `morpheus-cypher-plugin` | Cypher Backend / Module | **Tools > Cypher** (Affects custom secret-engine path routing or encryption keys). |
| `morpheus-generic-integration-plugin` | Generic Integration | **Administration > Integrations** (Lists as a custom option under the Integration type selection). |
| `morpheus-global-ui-plugin` | Global UI Component | **Every Authenticated Page Header/Footer** (Injects custom HTML script/styling elements globally). |
| `morpheus-guidance-plugin` | Guidance Recommendation | **Operations > Guidance** (Appears as custom optimization or resource recommendations). |
| `morpheus-reports-plugin` | Custom Report Provider | **Operations > Reports** (Adds a new exportable layout to the master list of system reports). |
| `morpheus-server-tab-plugin` | Infrastructure Detail Extension | **Infrastructure > Servers** (Click a specific server; displays as a custom sub-tab alongside "Overview"). |
| `morpheus-standard-catalog-layout-plugin` | Catalog Layout Customization | **Persona View > Service Catalog** (Alters render appearance of standard catalog order cards). |
| `morpheus-tab-plugin` | Instance Detail Extension | **Provisioning > Instances** (Click a specific running Instance; displays a brand-new sub-tab navigation path). |
| `morpheus-task-plugin` | Automation Task Engine | **Library > Automation > Tasks** (Visible in the task-type dropdown menu when building a task). |

---

## Detailed Directory Breakdown

### 1. Automation & Logic Extensions

#### `morpheus-task-plugin`
* **Morpheus UI Location:** `Library` ➔ `Automation` ➔ `Tasks`.
* **Execution View:** Click **Add**, click the **Type** selection drop-down list. The custom task type will appear here. It can then be included directly inside automation workflows or operational Playbooks.

#### `morpheus-approvals-plugin`
* **Morpheus UI Location:** `Administration` ➔ `Policies` (or `Blueprints` ➔ `Approvals`).
* **Execution View:** When selecting a rule configuration for structural approval steps on provisioning requests, this module integrates external engine tracking workflows into your default stack.

#### `morpheus-cypher-plugin`
* **Morpheus UI Location:** `Tools` ➔ `Cypher`.
* **Execution View:** Handles secure key-vault lookups. Allows operators to store encrypted variables, credentials, or text keys matching a specified custom URI string format.

---

### 2. User Interface (UI) Extensions

#### `morpheus-global-ui-plugin`
* **Morpheus UI Location:** Implements a global listener.
* **Execution View:** Viewable instantly on **any view dashboard** once compiled and enabled. It programmatically injects code blocks (such as persistent text alerts, support banners, tracking pixels, or chat widgets) directly across all app headers or footers.

#### `morpheus-tab-plugin` & `morpheus-server-tab-plugin`
* **Morpheus UI Location:** `Provisioning` ➔ `Instances` **OR** `Infrastructure` ➔ `Compute` ➔ `Servers`.
* **Execution View:** Drill down into any active virtual instance or compute host. Look past default telemetry tabs ("Overview", "Logs", "History"). You will see a newly appended, custom-named tab presenting isolated server metrics or parsed data tables.

#### `morpheus-standard-catalog-layout-plugin`
* **Morpheus UI Location:** `Tools` ➔ `Service Catalog` (when leveraging the **Catalog Persona** view mode).
* **Execution View:** Controls the frontend visual card aesthetics when users order items from self-service portal blueprints.

---

### 3. Monitoring & Operations Extensions

#### `morpheus-reports-plugin`
* **Morpheus UI Location:** `Operations` ➔ `Reports`.
* **Execution View:** Appends a custom analytical reporting schema. Click **Filter**, run the newly generated custom summary archetype, and export details directly to a CSV or JSON payload.

#### `morpheus-guidance-plugin`
* **Morpheus UI Location:** `Operations` ➔ `Guidance`.
* **Execution View:** Populates optimization lists. Displays new cards under the cost-savings, right-sizing, or orphan resource identification matrices.

#### `morpheus-generic-integration-plugin`
* **Morpheus UI Location:** `Administration` ➔ `Integrations`.
* **Execution View:** Hit **New Integration**. Your custom configured generic tracking provider block will render under the list of available external applications.

---

## How to Install these Samples
1. Follow the local environmental compilation rules using the provided gradle wrapper (`./gradlew shadowJar`).
2. Log into the Morpheus web UI as a System Administrator.
3. Navigate to **Administration > Integrations > Plugins**.
4. Click **Add Plugin**, drop the compiled `-all.jar` file artifact into the upload pane, and save.
