# Todo
* Something strange with generator packets
	- you should hold the packet location if it is a generator packet
* Agent does not go to nearest packet
* Optimize communication behavior as now, multiple loops go over the perception to communicate graph, packets, destinations, charging stations, ...
* The behavior of TaskDefinitionPossible, TaskDefinitionNotPossible, AlternateTaskDefinitionPossible, AlternateTaskDefinitionNotPossible is correct, but it could be optimized.
* Combine as many memoryfragments as possible

# Part III TODO
* For energy environments, fix error with jackson on rgbcolor for charging station 
* Optimize code for performance

# Assignments
1) Fix bug in PickupPacketBehavior -> Noah (agent that does not pick up packet, stays in PickUpPacketBehavior as HasCarry is not satisfied, but its task changed to MoveToDestination)
	1) Communicate to agents who are standing at the same packet "I am going to pick this up"
	2) If agent receives this message from other agent, abandon packet and check TaskDefinitionPossible, otherwise
2) Explorer function -> Vince
3) Combine graphs of agents -> Charles
