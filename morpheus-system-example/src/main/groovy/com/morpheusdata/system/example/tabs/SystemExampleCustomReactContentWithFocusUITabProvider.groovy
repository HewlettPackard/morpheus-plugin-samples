package com.morpheusdata.system.example.tabs
import com.morpheusdata.core.providers.AbstractSystemTabProvider
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Account
import com.morpheusdata.model.User
import com.morpheusdata.views.HTMLResponse
import com.morpheusdata.views.ViewModel
import com.morpheusdata.model.TaskConfig
import com.morpheusdata.model.ContentSecurityPolicy
import groovy.json.JsonOutput
import com.morpheusdata.model.system.System

class SystemExampleCustomReactContentWithFocusUITabProvider extends AbstractSystemTabProvider {

	Plugin plugin
	MorpheusContext morpheus

	String code = "arcus-system-react-content-with-focus-ui-tab"
	String name = "React Content with Focus UI Tab"

	@Override
	String getCode() { code }

	@Override
	String getName() { name }

	@Override
	Integer getOrder() { 1 }

	SystemExampleCustomReactContentWithFocusUITabProvider(Plugin plugin, MorpheusContext morpheus) {
		this.plugin = plugin
		this.morpheus = morpheus
	}

	@Override
	Boolean show(System system, User user, Account account) {
		return system?.type?.code == "arcus-system" &&
			system?.layout?.code == "arcus-standard-layout"
	}

	@Override
	HTMLResponse renderTemplate(System system) {
		ViewModel<System> model = new ViewModel<>()
		model.object = [
			system    : system,
			systemJson: JsonOutput.toJson([
				id    : system.id,
				name  : system.name,
				status: system.status,
				type  : [
					id  : system.type?.id,
					code: system.type?.code,
					name: system.type?.name
				],
				layout: [
					id  : system.layout?.id,
					code: system.layout?.code,
					name: system.layout?.name
				]
			])
		]
		return getRenderer().renderTemplate("/hbs/tabs/reactContentWithFocusUITab", model)
	}

	/**
	 * Allows various sources used in the template to be loaded
	 * @return
	 */
	@Override
	ContentSecurityPolicy getContentSecurityPolicy() {
		return null // bundle is same-origin; no extra CSP directives needed
	}
}