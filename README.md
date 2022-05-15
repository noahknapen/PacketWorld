
# General documentation for the PacketWorld Application

This project contains code for the PacketWorld Application, a simple Multi-agent framework to develop and evaluate pickup and delivery problems.

<img src="/res/packetworld.gif" alt="The Packet World" width="350">

[//]: # (![The Packet World]&#40;/res/packetworld.gif&#41;)

## Project structure

The project consists of two main parts: (1) the source code and (2) configuration files.


The source code is located under the `src` directory. In the source code you can find 6 main packages:

- agent: Implementations of different agent behaviors and behavior changes.
- environment: Code related to the environment as well as definitions for worlds in the applications.
- gui: Code related to the Graphical User Interface.
- support: Classes which support the functionality of the application.
- synchronizer: Classes used to realize synchronization in the application.
- util: General purpose classes and functions used within the project.


Configuration files can be found in the `configfiles` directory.
A distinction is made between behaviors and environments. 
A behavior configuration file describes the different behavior states an agent can have, and how an agent can transition between these states.

An environment configuration file describes an environment in which the selected implementation is evaluated. The environment contains information about agents, packets, energy stations, etc.

## Running the code

The project uses Apache Maven as a build framework, and is written in Java version 17 or above. To compile the code, simply run the following command:

`mvn compile`

To run the code we use the maven exec plugin. The plugin can be invoked as follows:

`mvn exec:java`

If desired, you can clear previously built source files with the following command:

`mvn clean`

Lastly, if you want to adhere to the coding style of the project, you can run the following command:

`mvn checkstyle:check`

# Assignemnt 1 A
The assignment_1_A solution implements a single behaviour. In this solution, the agent stores two lists: one with the discovered packets and one with the discovered destinations. Based on those lists, the agent creates and performs tasks. The movement of the agent is divided in two cases. If the goal position is in the perception, the agent moves straight to it, otherwise it will randomly move.

# Assignment 1 B
The assignment_1_B solution extends the assignment_1_A solution. The SimpleBehaviour has been divided in five different behaviours and corresponding BehaviourChange classes have been implemented. An overview scheme can be found in the assignment_1_B folder. Furthermore, a graph-based approach was implemented to optimize the movements of the agent.

# Assignment 2
In assignment_2 solution takes into account the agents with a battery and accordingly the charging stations. An agent now moves towards the charging station whenever it is not able to perform one of the available tasks and its battery level is too low. Furthermore, an emergency communication approach was implemented to avoid agents from running out of battery when different agents want to charge at the same time.

# Assignment 3 
In assignment_3 coloured agents are handled. The agents are now only able to deliver packets of their own colour. Besides, priority packets were implemented in order to handle blocked paths to destinations.


## Final notes

- To quickly get started, make sure to take a look at the example implementation provided in the configuration file `configfiles/behaviors/wander.txt` and the java source file `src/main/java/agent/behavior/wander/Wander.java`.
- It is not necessary to manually edit environment configuration files. If you would like to test different scenarios than the ones already provided, 
simply choose the _environment editor_ in the main menu of the application to edit or create new environments.
- After (or during) a normal run, you can export the monitored actions of agents in your run together with some metadata about the run. Navigate to the _Actions_ window and click on the _export_ button. For batch runs this is done automatically by specifying the output file before starting the runs.

