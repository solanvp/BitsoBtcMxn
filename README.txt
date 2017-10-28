Oct 28, 2017

Bitso BTC_MXN Monitor and mock trading application


AUTOR

  Pedro P. Solano
  solanvp@gmail.com


SET UP

 Install Maven and the JDK 8 (at least)
 In a terminal, cd into the folder project and run the following commands:

	$ mvn package
	$ java -jar target/BtcMxnNetworkers-1.0-jar-with-dependencies.jar 



NOTES

This application follows only a ‘happy path’ approach, where the Bitso Public Rest API is available and accesible from the computer, as well as the Bitso websocket API.

Although the application includes a console-like section where the trades are displayed, also usable information is logged in the launching terminal, such as:
 - When requesting from the Bitso Rest API
 - When received a response from the Bitso Rest API
 - When connected to the websocket
 - When connected to a channel in the websocket
 - When closing the websocket
 - When runtime variables are changed

A chart displays the last 30 trades, both sell and buy trades. 
 - A sell trade is displayed as a small light-orange inverted triangle
 - A buy trade is displayed as a small light-blue triangle

Mock trades made with the contrarian trade strategy are displayed in both the console-like section just as any other trade but with the word ‘MOCK’ at the end. In the chart: 
 - A MOCK sell trade is displayed as a big red inverted triangle
 - A MOCK buy trade is displayed as a big green triangle

