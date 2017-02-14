/**
 *  Pushsafer
 *
 *  Author: Pushsafer.com
 *  Date: 2017-02-14
 *  Code: https://github.com/smartthings-users/smartapp.pushsafer
 */

preferences
{
    section("Devices...") {
        input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
        input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
        input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
        input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
        input "accelerationSensors", "capability.accelerationSensor", title: "Which Acceleration Sensors?", multiple: true, required: false
        input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
    }
    section("Application...") {
        input "push", "enum", title: "SmartThings App Notification?", required: true, multiple: false,
        metadata :[
           values: [ 'No', 'Yes' ]
        ]
     }
    section("Pushsafer...") {
        input "privatekey", "text", title: "Private or Alias Key", required: true
		input "title", "text", title: "Title", required: false
        input "device", "text", title: "Device or Device Group ID (blank for all)", required: false
		input "URL", "text", title: "URL or URL scheme", required: false
		input "URLtitle", "text", title: "Title of URL", required: false
		input "Time2Live", "text", title: "Time 2 Live", required: false
		input "icon", "text", title: "Icon", required: false
		input "sound", "text", title: "Sound", required: false
		input "vibration", "text", title: "Vibration", required: false
    }
}

def installed()
{
    log.debug "'Pushsafer' installed with settings: ${settings}"
    initialize()
}

def updated()
{
    log.debug "'Pushsafer' updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize()
{
    /**
     * You can customize each of these to only receive one type of notification
     * by subscribing only to the individual event for each type. Additional
     * logic would be required in the Preferences section and the device handler.
     */

    if (switches) {
        // switch.on or switch.off
        subscribe(switches, "switch", handler)
    }
    if (motionSensors) {
        // motion.active or motion.inactive
        subscribe(motionSensors, "motion", handler)
    }
    if (contactSensors) {
        // contact.open or contact.closed
        subscribe(contactSensors, "contact", handler)
    }
    if (presenceSensors) {
        // presence.present or 'presence.not present'  (Why the space? It is dumb.)
        subscribe(presenceSensors, "presence", handler)
    }
    if (accelerationSensors) {
        // acceleration.active or acceleration.inactive
        subscribe(accelerationSensors, "acceleration", handler)
    }
    if (locks) {
        // lock.locked or lock.unlocked
        subscribe(locks, "lock", handler)
    }
}

def handler(evt) {
    log.debug "$evt.displayName is $evt.value"

    if (push == "Yes")
    {
        sendPush("${evt.displayName} is ${evt.value} [Sent from 'Pushsafer']");
    }

    // Define the initial postBody keys and values for all messages
    def postBody = [
        k: "$privatekey",
        m: "${evt.displayName} is ${evt.value}"
    ]

    // We only have to define the device if we are sending to a single device
    if (device)
    {
        postBody['d'] = "$device"
    }
	
    if (icon)
    {
        postBody['i'] = "$icon"
    }
	
    if (sound)
    {
        postBody['s'] = "$sound"
    }
	
    if (vibration)
    {
        postBody['v'] = "$vibration"
    }

    if (URL)
    {
        postBody['u'] = "$URL"
    }
	
    if (URLtitle)
    {
        postBody['ut'] = "$URLtitle"
    }
	
    if (title)
    {
        postBody['t'] = "$title"
    }
	
    if (Time2Live)
    {
        postBody['l'] = "$Time2Live"
    }	
	
    // Prepare the package to be sent
    def params = [
        uri: "https://www.pushsafer.com/api",
        body: postBody
    ]

    log.debug postBody
    log.debug "Sending Pushsafer: Private/Alias key '${privatekey}'"
	
    httpPost(params){
        response ->
            if(response.status != 200)
            {
                sendPush("ERROR: 'Pushsafer' received HTTP error ${response.status}. Check your key!")
                log.error "Received HTTP error ${response.status}. Check your key!"
            }
            else
            {
                log.debug "HTTP response received [$response.status]"
            }
    }

}
