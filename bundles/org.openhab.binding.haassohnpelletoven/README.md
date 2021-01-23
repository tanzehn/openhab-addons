# Haas Sohn Pellet Stove Binding

The binding for Haassohnpelletstove communicates with a Haas and Sohn Pelletstove through the optional
WIFI module. More information about the WIFI module can be found here: https://www.haassohn.com/de/ihr-plus/WLAN-Funktion

## Supported Things

| Things                    | Description                                                                  | Thing Type |
|---------------------------|------------------------------------------------------------------------------|------------|
| haassohnpelletoven        | Control of a Haas & Sohn Pellet Stove                                        | oven	    |


## Thing Configuration

In general two parameters are required. The IP-Address of the WIFI-Modul of the Stove in the local Network and the Access PIN of the Stove.
The PIN can be found directly at the stove under the Menue/Network/WLAN-PIN

Thing haassohnpelletoven:oven:myOven "Pelletstove" @"Roomname" [ hostIP="192.168.0.23", hostPIN="1234", refreshRate="30"]

## Channels

The following channels are yet supported:

| channel  		  		    | type               | description                                              					            |
|-----------------------|--------------------|--------------------------------------------------------------------------------|
| channelPrg      		  | Switch 	 	         | Turn the stove on/off		                              					              |
| channelIsTemp   		  | Number:Temperature | Receives the actual temperature of the stove	          						            |
| channelSpTemp   		  | Number:Temperature | Receives and sets the target temperature of the stove	  					            |
| channelMode     		  | String             | Receives the actual mode the stove is in like heating, cooling, error .... 	  |
| channelEcoMode  		  | Switch             | Turn the eco mode of the stove on/off	  									                    |
| channelIngitions		  | Number             | Amount of ignition's of the stove											                        |
| channelMaintenanceIn	| Number             | States the next maintenance in kg											                        |
| channelCleaningIn     | Number             | States the next cleaning window in hours								                		    |
| channelConsumption    | Number             | Total consumption of the stove												                          |
| channelOnTime     	  | Number             | Operation hours of the stove												                        	  |

## Full Example

demo.items:

```
Switch                  prg             { channel="haassohnpelletoven:oven:myOven:channelPrg" }
Switch                  ecoMode         { channel="haassohnpelletoven:oven:myOven:channelEcoMode" }
Number:Temperature      isTemp          { channel="haassohnpelletoven:oven:myOven:channelIsTemp" }
Number:Temperature      spTemp          { channel="haassohnpelletoven:oven:myOven:channelSpTemp" }
String                  mode            { channel="haassohnpelletoven:oven:myOven:channelMode" }
Number                  cleaningIn      { channel="haassohnpelletoven:oven:myOven:channelCleaningIn" }
Number                  maintainceIn    { channel="haassohnpelletoven:oven:myOven:channelMaintainceIn" }
Number                  ignitions       { channel="haassohnpelletoven:oven:myOven:channelIgnitions" }
Number                  consumption     { channel="haassohnpelletoven:oven:myOven:channelConsumption" }
Number                  onTime          { channel="haassohnpelletoven:oven:myOven:channelOnTime" }

```

## Tested Hardware

The binding was succesfully tested with the following ovens:

- HSP 7 DIANA
- HSP6 434.08
