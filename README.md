# Networking and Security

... signifies where your jar is located in the filesystem. if you are in the same directory simply use testerQ.jar

SERVER COMMAND(Windows): java -cp "C:\...\testerQ.jar" testerq.server.Main 3000
CLIENT COMMAND(Windows): java -cp "C:\...\testerQ.jar" -D"file.encoding=UTF8" testerq.client.Main localhost 3000

SERVER COMMAND(unix/mac): java -cp /.../testerQ.jar testerq.server.Main 3000
CLIENT COMMAND(unix/mac): java -cp /.../testerQ.jar testerq.client.Main localhost 3000

To run server from testerQ root:
java -Djavax.net.ssl.keyStore=/Users/jacobemo/keystore.jks -Djavax.net.ssl.keyStorePassword=tester -Djavax.net.debug=ssl -cp dist/testerQ.jar testerq.server.Main 3000

If jar is in current directory, run:
java -Djavax.net.ssl.trustStore=../keystore.jks -Djavax.net.ssl.trustStorePassword=tester -cp testerQ.jar testerq.client.Main localhost 3000

From testerQ file root run:
java -Djavax.net.ssl.trustStore=keystore.jks -Djavax.net.ssl.trustStorePassword=tester -cp dist/testerQ.jar testerq.client.Main localhost 3000

run SERVER COMMAND on port of choice

run CLIENT COMMAND pointing to host and port of server

ON CLIENT:
Choose either 1 for login or 2 for new user

you will be prompted for a username and password either way

Choose avatar when prompted (If new user)


******COMMANDS*******
enter: exit
This will log you out and stop the client (progress is saved automatically)

enter: help
This will list the available commands

enter: <command name>
This will give you usage instructions on any command

when in game world issue commands such as "move north" OR "mv up 3" OR "mv east 5" this moves around the world
format is move <DIRECTION>

enter: say <What you want to say>
This is will broadcast your message to all connected clients and display your username and the message in the event log

enter: inspect <DIRECTION>
This will display an event log of your inspection

enter: interact <DIRECTION>
This will display an event log of your interaction
**hint use "interact north" while standing in front of the king ($) within the castle.**

enter: chop <DIRECTION>
This will chop a tree (#) and yield logs if there is a tree in the direction you chop

enter: list inventory OR list inv
This will display a event log with your current inventory

enter: list stats OR list stats <member name>
This will display a event log with your current stats OR the stats of a fellow member

enter: list experience OR list exp
This will display a event log with your current skill experience

enter: trading <NUMBER> <ITEM NAME> for <NUMBER> <ITEM NAME>
This will propose a trade

enter: trade <PLAYER NAME>
This will accept and complete a trade

****QUEST GUIDE**********
interact with King Leroy "$" in the castle, he will tell you to find Macari
leave the castle and move north-east until you see an "M" interact with Macari
Macari will ask you to fetch him 20 treelogs
go find a tree "#" and enter the chop command until you have collected 20 or more treelogs (validate using the list inventory command)
return and interact with Macari with 20 treelogs in your inventory and the quest will be complete.
You should recieve a pickaxe as your reward.

*******TRADE GUIDE**********
With two players ("player1" and "player2") logged in:
player1 type: "trading 3 coins for 2 coins"
player2 type: "trade player1"
Your event log should confirm the trade
type: "list inventory" on both players to verify the trade.

*******CONFIGURATION************
enter: config vwidth 35 OR config vheight 40
This will increase the rendered width or height of the map by the number set

*****NOTES********
Valid DIRECTIONs: north, up, n OR south, down, s OR east, right, e OR west, left, w

If you enter a space with an I, you should be moved to a new map zone

You should be able to observe an event log below the rendered map