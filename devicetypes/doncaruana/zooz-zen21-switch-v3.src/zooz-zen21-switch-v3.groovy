/**
 *  Zooz Zen21 Switch v3
 *
 *  2019-02-12 - Initial release
 *  2019-02-13 - Update for paddle control, added association capability
 *  2019-07-11 - Added all functions for firmware 3.01
 *  2019-07-13 - Fix logic for null preferences
 *  2019-09-07 - Fix typo in auto turn off timer parameter setting
 *  
 *  Supported Command Classes
 *  
 *         Association v2
 *         Association Group Information
 *         Basic
 *         Configuration
 *         Device Reset Local
 *         Manufacturer Specific
 *         Powerlevel
 *         Switch_binary
 *         Version v2
 *         ZWavePlus Info v2
 *         Multi-Channel Association
 *         Transport Service
 *         Security 2 (S2)
 *         Supervision
 *         Firmware Update Metadata
 *
 *   Parm Size Description                                   Value
 *      1    1 Paddle Control                                0 (Default)-Upper paddle turns light on, 1-Lower paddle turns light on, 2-either paddle toggles on/off
 *      2    1 LED Indicator                                 0 (Default)-LED is on when light is OFF, 1-LED is on when light is ON, 2-LED is always off, 3-LED is always on
 *      3    1 Auto Turn-Off                                 0 (Default)-Timer disabled, 1-Timer enabled; Set time in parameter 4
 *      4    4 Turn-off Timer                                60 (Default)-Time in minutes after turning on to automatically turn off (1-65535 minutes)
 *      5    1 Auto Turn-On                                  0 (Default)-Timer disabled, 1-Timer enabled; Set time in parameter 6
 *      6    4 Turn-on Timer                                 60 (Default)-Time in minutes after turning off to automatically turn on (1-65535 minutes)
 *      8    1 Power Restore                                 2 (Default)-Remember state from pre-power failure, 0-Off after power restored, 1-On after power restore
 *      9    1 Scene Control                                 0 (Default)-Scene control disabled, 1-Scene control enabled
 *     10    1 Disable Paddle                                1 (Default)-Paddle is used for local control, 0-Paddle single tap disabled
 */

metadata {
	definition (name: "Zooz Zen21 Switch v3", namespace: "doncaruana", author: "Don Caruana", ocfDeviceType: "oic.d.switch", mnmn: "SmartThings", vid: "generic-switch") {
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
		capability "Light"
		capability "Button"
			command "tapDown1"
			command "tapDown2"
			command "tapDown3"
			command "tapDown4"
			command "tapDown5"
			command "tapUp1"
			command "tapUp2"
			command "tapUp3"
			command "tapUp4"
			command "tapUp5"

//zw:L type:1001 mfr:027A prod:B111 model:1E1C ver:3.00 zwv:5.03 lib:03 cc:5E,25,85,8E,59,55,86,72,5A,73,70,6C,9F,7A role:05 ff:8700 ui:8704

	fingerprint mfr:"027A", prod:"B111", model:"1E1C", deviceJoinName: "Zooz Zen21 Switch v3"
	fingerprint deviceId:"0x1001", inClusters: "0x5E,0x59,0x85,0x70,0x5A,0x72,0x73,0x25,0x86,0x8E,0x55,0x6C,0x9F,0x7A"
	fingerprint cc: "0x5E,0x59,0x85,0x70,0x5A,0x72,0x73,0x25,0x86,0x8E,0x55,0x6C,0x9F,0x7A", mfr:"027A", prod:"B111", model:"1E1C", deviceJoinName: "Zooz Zen21 Switch v3"
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
	}

	preferences {
		input "ledIndicator", "enum", title: "LED Indicator", description: "When Off... ", options:["on": "When On", "off": "When Off", "never": "Never", "always": "Always"], defaultValue: "off"
		input "paddleControl", "enum", title: "Paddle Control", description: "Standard, Inverted, or Toggle", options:["std": "Standard", "invert": "Invert", "toggle": "Toggle"], defaultValue: "std"
		input "powerRestore", "enum", title: "After Power Restore", description: "State after power restore", options:["prremember": "Remember", "proff": "Off", "pron": "On"],defaultValue: "prremember",displayDuringSetup: false
		input "sceneCtrl", "bool", title: "Scene Control", description: "Enable scene control", required: false, defaultValue: false
		input "autoTurnoff", "bool", title: "Auto Off", description: "Light will automatically turn off after set time", required: false, defaultValue: false
		input "autoTurnon", "bool", title: "Auto On", description: "Light will automatically turn on after set time", required: false, defaultValue: false
		input "offTimer", "number", title: "Off Timer", description: "Time in minutes to automatically turn off", required: false, defaultValue: 60, range: "1..65535"
		input "onTimer", "number", title: "On Timer", description: "Time in minutes to automatically turn on", required: false, defaultValue: 60, range: "1..65535"
		input "localControl", "bool", title: "Local Control", description: "Local paddle control enabled", required: false, defaultValue: true
		input (
					type: "paragraph",
					element: "paragraph",
					title: "Configure Association Groups:",
					description: "Devices in association group 2 will receive Basic Set commands directly from the switch when it is turned on or off. Use this to control another device as if it was connected to this switch.\n\n" +"Devices are entered as a comma delimited list of IDs in hexadecimal format."
					)
		input (
					name: "requestedGroup2",
					title: "Association Group 2 Members (Max of 5):",
					type: "text",
					required: false
					)

	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
		standardTile("indicator", "device.indicatorStatus", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOff", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorNever", icon:"st.indicators.never-lit"
		}


		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","indicator","refresh"])
	}
}

private getCommandClassVersions() {
	[
		0x5E: 2,  // Z-Wave Plus Info
		0x59: 1,  // Association Group Information
		0x85: 2,  // Association
		0x70: 1,  // Configuration
		0x5A: 1,  // Device Reset Locally
		0x72: 2,  // Manufacturer Specific
		0x73: 1,  // Powerlevel
		0x25: 1,  // Binary Switch
		0x86: 1,  // Version
		0x8E: 1,  // Multi Channel Association
		0x55: 1,  // Transport Service
		0x6C: 1,  // Supervision
		0x9F: 1,  // S2 (Security 2)
		0x7A: 1,  // Firmware Update Meta Data
	]
}

def installed() {
	def cmds = []

	cmds << mfrGet()
	cmds << zwave.versionV1.versionGet().format()
	cmds << parmGet(1)
	cmds << parmGet(2)
	cmds << parmGet(3)
	cmds << parmGet(4)
	cmds << parmGet(5)
	cmds << parmGet(6)
	cmds << parmGet(8)
	cmds << parmGet(9)
	cmds << parmGet(10)
	cmds << zwave.basicV1.basicSet(value: 0xFF).format()
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	return response(delayBetween(cmds,200))
}

def updated(){
	// These are needed as SmartThings is not honoring defaultValue in preferences. They are set to the device defaults
	def setLocalControl = 1
	if (localControl!=null) {setLocalControl = localControl == true ? 1 : 0}
	def setOffTimer = 60
	if (offTimer) {setOffTimer = offTimer}
	def setOnTimer = 60
	if (onTimer) {setOnTimer = onTimer}
	def nodes = []
	def commands = []
	if (getDataValue("MSR") == null) {
		def level = 99
		commands << mfrGet()
		commands << zwave.versionV1.versionGet().format()
		commands << zwave.basicV1.basicSet(value: level).format()
		commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	}
	def setScene = sceneCtrl == true ? 1 : 0
	def setPowerRestore = 2
	if (powerRestore != null) {setPowerRestore = powerRestore == "prremember" ? 2 : powerRestore == "proff" ? 0 : 1}
	def setAutoTurnon = autoTurnon == true ? 1 : 0
	def setAutoTurnoff = autoTurnoff == true ? 1 : 0
	def setPaddleControl = 0
	switch (paddleControl) {
		case "std":
			setPaddleControl = 0
			break
		case "invert":
			setPaddleControl = 1
			break
		case "toggle":
			setPaddleControl = 2
			break
		default:
			setPaddleControl = 0
			break
	}
	def setLedIndicator = 0
	switch (ledIndicator) {
		case "off":
			setLedIndicator = 0
			break
		case "on":
			setLedIndicator = 1
			break
		case "never":
			setLedIndicator = 2
			break
		case "always":
			setLedIndicator = 3
			break
		default:
			setLedIndicator = 0
			break
	}
	
	if (setScene) {
		sendEvent(name: "numberOfButtons", value: 10, displayed: false)
	} else {
		sendEvent(name: "numberOfButtons", value: 0, displayed: false)
	}
	
	if (settings.requestedGroup2 != state.currentGroup2) {
		nodes = parseAssocGroupList(settings.requestedGroup2, 2)
		commands << zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: []).format()
		commands << zwave.associationV2.associationSet(groupingIdentifier: 2, nodeId: nodes).format()
		commands << zwave.associationV2.associationGet(groupingIdentifier: 2).format()
		state.currentGroup2 = settings.requestedGroup2
	}
	
	//parmset takes the parameter number, it's size, and the value - in that order
	commands << parmSet(10, 1, setLocalControl)
	commands << parmSet(9, 1, setScene)
	commands << parmSet(8, 1, setPowerRestore)
	commands << parmSet(6, 4, setOnTimer)
	commands << parmSet(5, 1, setAutoTurnon)
	commands << parmSet(4, 4, setOffTimer)
	commands << parmSet(3, 1, setAutoTurnoff)
	commands << parmSet(2, 1, setLedIndicator)
	commands << parmSet(1, 1, setPaddleControl)

	commands << parmGet(10)
	commands << parmGet(9)
	commands << parmGet(8)
	commands << parmGet(6)
	commands << parmGet(5)
	commands << parmGet(4)
	commands << parmGet(3)
	commands << parmGet(2)
	commands << parmGet(1)
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
		return response(delayBetween(commands, 500))
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	log.debug "---ASSOCIATION REPORT V2--- ${device.displayName} sent groupingIdentifier: ${cmd.groupingIdentifier} maxNodesSupported: ${cmd.maxNodesSupported} nodeId: ${cmd.nodeId} reportsToFollow: ${cmd.reportsToFollow}"
	state.group3 = "1,2"
	if (cmd.groupingIdentifier == 3) {
		if (cmd.nodeId.contains(zwaveHubNodeId)) {
			createEvent(name: "numberOfButtons", value: 10, displayed: false)
		}
		else {
			sendEvent(name: "numberOfButtons", value: 0, displayed: false)
				sendHubCommand(new physicalgraph.device.HubAction(zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()))
				sendHubCommand(new physicalgraph.device.HubAction(zwave.associationV2.associationGet(groupingIdentifier: 3).format()))
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def name = ""
	def value = ""
	def reportValue = cmd.configurationValue[0]
	log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
	switch (cmd.parameterNumber) {
		case 1:
			name = "topoff"
			value = reportValue == 1 ? "true" : "false"
			break
		case 2:
			switch (reportValue) {
				case 0:
					value = "off"
					sendEvent(name: "indicatorStatus", value: "when off", display: false)
					break
				case 1:
					value = "on"
					sendEvent(name: "indicatorStatus", value: "when on", display: false)
					break
				case 2:
					value = "never"
					sendEvent(name: "indicatorStatus", value: "never", display: false)
					break
				case 3:
					value = "always"
					break
				default:
					value = "off"
					sendEvent(name: "indicatorStatus", value: "when off", display: false)
					break
			}
			name = "ledfollow"
			break
		case 3:
			name = "autooff"
			value = reportValue == 1 ? "true" : "false"
			break
		case 4:
			name = "autoofftimer"
			value = cmd.configurationValue[3] + (cmd.configurationValue[2] * 0x100) + (cmd.configurationValue[1] * 0x10000) + (cmd.configurationValue[0] * 0x1000000)
			break
		case 5:
			name = "autoon"
			value = reportValue == 1 ? "true" : "false"
			break
		case 6:
			name = "autoontimer"
			value = cmd.configurationValue[3] + (cmd.configurationValue[2] * 0x100) + (cmd.configurationValue[1] * 0x10000) + (cmd.configurationValue[0] * 0x1000000)
			break
		case 8:
			name = "afterfailure"
			switch (reportValue) {
				case 0:
					value = "off"
					break
				case 1:
					value = "on"
					break
				case 2:
					value = "remember"
					break
				default:
					break
			}
		case 9:
			name = "scene_control"
			value = reportValue == 1 ? "true" : "false"
			break
		case 10:
			name = "local_control"
			value = reportValue == 1 ? "true" : "false"
			break
		default:
			break
	}
	createEvent([name: name, value: value])
}


def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def manufacturerCode = String.format("%04X", cmd.manufacturerId)
	def productTypeCode = String.format("%04X", cmd.productTypeId)
	def productCode = String.format("%04X", cmd.productId)
	def msr = manufacturerCode + "-" + productTypeCode + "-" + productCode
	updateDataValue("MSR", msr)
	updateDataValue("Manufacturer", "Zooz")
	updateDataValue("Manufacturer ID", manufacturerCode)
	updateDataValue("Product Type", productTypeCode)
	updateDataValue("Product Code", productCode)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = commandClassVersions
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def poll() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
		if (getDataValue("MSR") == null) {
			commands << mfrGet()
			commands << zwave.versionV1.versionGet().format()
		}
		commands << zwave.switchBinaryV1.switchBinaryGet().format()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	def result = []
	switch (cmd.sceneNumber) {
		case 1:
			// Down
			switch (cmd.keyAttributes) {
				case 0:
					// Press Once
						result += createEvent(tapDown1Response("physical"))
						//result += createEvent([name: "switch", value: "off", type: "physical"])
						break
					case 3: 
						// 2 Times
						result +=createEvent(tapDown2Response("physical"))
						break
					case 4:
						// 3 times
						result=createEvent(tapDown3Response("physical"))
						break
					case 5:
						// 4 times
						result=createEvent(tapDown4Response("physical"))
						break
					case 6:
						// 5 times
						result=createEvent(tapDown5Response("physical"))
						break
					default:
						log.debug ("unexpected down press keyAttribute: $cmd.keyAttributes")
				}
				break
		case 2:
			// Up
			switch (cmd.keyAttributes) {
				case 0:
					// Press Once
					result += createEvent(tapUp1Response("physical"))
					//result += createEvent([name: "switch", value: "on", type: "physical"]) 
					break
				case 3: 
					// 2 Times
						result+=createEvent(tapUp2Response("physical"))
					break
				case 4:
					// 3 Times
					result=createEvent(tapUp3Response("physical"))
					break
				case 5:
					// 4 Times
					result=createEvent(tapUp4Response("physical"))
					break
				case 6:
					// 5 Times
					result=createEvent(tapUp5Response("physical"))
					break
				default:
					log.debug ("unexpected up press keyAttribute: $cmd.keyAttributes")
			}
			break
		default:
			// unexpected case
			log.debug ("unexpected scene: $cmd.sceneNumber")
			log.debug ("unexpected scene: $cmd")
	}
}

def buttonEvent(button, value) {
	createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

def tapDown1Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "2"], descriptionText: "$device.displayName Tap-Down-1 (button 2) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown2Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "4"], descriptionText: "$device.displayName Tap-Down-2 (button 4) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown3Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "6"], descriptionText: "$device.displayName Tap-Down-3 (button 6) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown4Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "8"], descriptionText: "$device.displayName Tap-Down-4 (button 8) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown5Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "10"], descriptionText: "$device.displayName Tap-Down-5 (button 10) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp1Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "1"], descriptionText: "$device.displayName Tap-Up-1 (button 1) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp2Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "3"], descriptionText: "$device.displayName Tap-Up-2 (button 3) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp3Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "5"], descriptionText: "$device.displayName Tap-Up-3 (button 5) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp4Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "7"], descriptionText: "$device.displayName Tap-Up-4 (button 7) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp5Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "9"], descriptionText: "$device.displayName Tap-Up-5 (button 9) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown1() {
	sendEvent(tapDown1Response("digital"))
}

def tapDown2() {
	sendEvent(tapDown2Response("digital"))
}

def tapDown3() {
	sendEvent(tapDown3Response("digital"))
}

def tapDown4() {
	sendEvent(tapDown4Response("digital"))
}

def tapDown5() {
	sendEvent(tapDown5Response("digital"))
}

def tapUp1() {
	sendEvent(tapUp1Response("digital"))
}

def tapUp2() {
	sendEvent(tapUp2Response("digital"))
}

def tapUp3() {
	sendEvent(tapUp3Response("digital"))
}

def tapUp4() {
	sendEvent(tapUp4Response("digital"))
}

def tapUp5() {
	sendEvent(tapUp5Response("digital"))
}

def parmSet(parmnum, parmsize, parmval) {
	return zwave.configurationV1.configurationSet(scaledConfigurationValue: parmval, parameterNumber: parmnum, size: parmsize).format()
}

def parmGet(parmnum) {
	return zwave.configurationV1.configurationGet(parameterNumber: parmnum).format()
}

def mfrGet() {
	return zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
	updateDataValue("applicationVersion", "${cmd.applicationVersion}")
	updateDataValue("applicationSubVersion", "${cmd.applicationSubVersion}")
	updateDataValue("zWaveLibraryType", "${cmd.zWaveLibraryType}")
	updateDataValue("zWaveProtocolVersion", "${cmd.zWaveProtocolVersion}")
	updateDataValue("zWaveProtocolSubVersion", "${cmd.zWaveProtocolSubVersion}")
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionCommandClassReport cmd) {
	log.debug "vccr"
	def rcc = ""
	log.debug "version: ${cmd.commandClassVersion}"
	log.debug "class: ${cmd.requestedCommandClass}"
	rcc = Integer.toHexString(cmd.requestedCommandClass.toInteger()).toString() 
	log.debug "${rcc}"
	if (cmd.commandClassVersion > 0) {log.debug "0x${rcc}_V${cmd.commandClassVersion}"}
}

private parseAssocGroupList(list, group) {
	def nodes = group == 2 ? [] : [zwaveHubNodeId]
 	if (list) {
		def nodeList = list.split(',')
		def max = group == 2 ? 5 : 4
		def count = 0

		nodeList.each { node ->
			node = node.trim()
			if ( count >= max) {
				log.warn "Association Group ${group}: Number of members is greater than ${max}! The following member was discarded: ${node}"
			}
			else if (node.matches("\\p{XDigit}+")) {
				def nodeId = Integer.parseInt(node,16)
				if (nodeId == zwaveHubNodeId) {
					log.warn "Association Group ${group}: Adding the hub as an association is not allowed (it would break double-tap)."
				}
				else if ( (nodeId > 0) & (nodeId < 256) ) {
					nodes << nodeId
					count++
				}
				else {
					log.warn "Association Group ${group}: Invalid member: ${node}"
				}
			}
			else {
				log.warn "Association Group ${group}: Invalid member: ${node}"
			}
		}
	}
 	return nodes
}