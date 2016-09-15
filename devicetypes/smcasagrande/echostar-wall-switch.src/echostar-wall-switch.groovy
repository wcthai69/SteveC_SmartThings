// S. Casagrade 2016
// Copied from Securefi key fob type

metadata {
    definition (name: "Echostar Wall Switch", namespace: "smcasagrande", author: "Steve Casagrande") {
     	capability "Configuration"
    	capability "Button"
   
    	fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0009", outClusters: "0003, 0006, 0008, 0019", manufacturer: "Echostar", model: "Switch", deviceJoinname: "Echostar Wall Switch"    
    }

    tiles {
	    standardTile("button", "device.button", width: 2, height: 2) {
		    state "default", label: "", icon: "st.Home.home30", backgroundColor: "#ffffff"
        }
    }
    main (["button"])
    details (["button"])
}

def parse(String description) {	       	            
    if (description?.startsWith('enroll request')) {        
        List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        def result = cmds?.collect { new physicalgraph.device.HubAction(it) }
        return result    
    } else if (description?.startsWith('catchall:')) {
        def msg = zigbee.parse(description)
        buttonPush(msg.command)
    } else {
        log.debug "parse description: $description"
    }    
}

def buttonPush(button){
    def name = null
    log.debug "button pushed: $button"
    if (button == 0) {
        //OFF Button
        name = "2"
        def currentST = device.currentState("2")?.value
        log.debug "OFF button Pushed"           
    } else if (button == 1) {
    	//ON Button
        name = "1"
        def currentST = device.currentState("1")?.value
        log.debug "ON button pushed"               
    } else {
    	log.debug "unexpected button, value: $button"
    }

    def result = createEvent(name: "button", value: "pushed", data: [buttonNumber: name], descriptionText: "$device.displayName button $name was pushed", isStateChange: true)
    log.debug "Parse returned ${result?.descriptionText}"
    return result
}


def enrollResponse() {
    log.debug "Sending enroll response"
    [            
    "raw 0x500 {01 23 00 00 00}", "delay 200",
    "send 0x${device.deviceNetworkId} ${endpointId} 1"        
    ]
}


def configure(){
    log.debug "Config Called"
    def configCmds = [
    "zcl global write 0x500 0x10 0xf0 {${device.zigbeeId}}", "delay 200",
    "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",
    "zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0501 {${device.zigbeeId}} {}", "delay 500",
    "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}"
    ]
    return configCmds
}