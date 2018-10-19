# Networking and Security

... signifies where your jar is located in the filesystem. if you are in the same director simply use testerQ.jar

SERVER COMMAND(Windows): java -cp "C:\...\testerQ.jar" testerq.server.Main 3000
CLIENT COMMAND(Windows): java -cp "C:\...\testerQ.jar" -D"file.encoding=UTF8" testerq.client.Main localhost 3000

SERVER COMMAND(unix/mac): java -cp /.../testerQ.jar testerq.server.Main 3000
CLIENT COMMAND(unix/mac): java -cp /.../testerQ.jar testerq.client.Main localhost 3000

run SERVER COMMAND on port of choice

run CLIENT COMMAND pointing to host and port of server

ON CLIENT:
Choose either 1 for login or 2 for new user

you will be prompted for a username and password either way

Choose avatar when prompted (If new user)


******COMMANDS*******
enter: exit
This will log you out and stop the client (progress is saved automatically)

when in game world issue commands such as "move north" OR "mv up 3" OR "mv east 5" this moves around the world
format is move <DIRECTION>

enter: say <What you want to say>
This is will broadcast your message to all connected clients and display your username and the message in the event log

enter: inspect <DIRECTION>
This will display an event log of your inspection

enter: interact <DIRECTION>
This will display an event log of your interaction

enter: chop <DIRECTION>
This will chop a tree and yield logs if there is a tree in the direction you chop

enter: list inventory OR list inv
This will display a event log with your current inventory

*******CONFIGURATION************
enter: config vwidth 35 OR config vheight 40
This will increase the rendered width or height of the map by the number set

*****NOTES********
Valid DIRECTIONs: north, up, n OR south, down, s OR east, right, e OR west, left, w

If you enter a space with an I, you should be moved to a new map zone

You should be able to observe an event log below the rendered map